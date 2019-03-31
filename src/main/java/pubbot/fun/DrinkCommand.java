package pubbot.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import org.json.JSONArray;
import org.json.JSONObject;
import pubbot.utils.Messages;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

public class DrinkCommand extends Command {
    private EventWaiter waiter;
    public DrinkCommand(EventWaiter waiter) {
        this.name = "drink";
        this.aliases = new String[]{"recipe"};
        this.category = new Category("Fun");
        this.waiter = waiter;
        this.cooldown = 5;
        this.cooldownScope = CooldownScope.USER;
    }

    protected void execute(CommandEvent event) {
        if(event.getArgs().trim().length() < 1){
            Messages.error(event, "If you want a drink, then tell me what drink you want smfh.");
            return;
        }
        String drink = event.getArgs().trim();
        try {
            URL url = new URL("https://www.thecocktaildb.com/api/json/v2/8673533/search.php?s="+ URLEncoder.encode(drink, "UTF-8"));

            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("GET");
            urlConn.setDoOutput(true);
            urlConn.connect();
            InputStreamReader read = new InputStreamReader(urlConn.getInputStream());
            BufferedReader each = new BufferedReader(read);
            String line;
            while ((line = each.readLine()) != null) {
                JSONObject jsonObject = new JSONObject(line);
                if(jsonObject.isNull("drinks")){
                    Messages.error(event, "No drinks found with the name **"+drink+"**.");
                    return;
                }
                else{
                    JSONArray drinks = jsonObject.getJSONArray("drinks");
                    if(drinks.length() > 1){
                        StringBuilder choices = new StringBuilder();
                        for(int i = 0; i < drinks.length(); i++){
                            JSONObject indvDrinks = drinks.getJSONObject(i);
                            choices.append(i+1).append(". ").append(indvDrinks.get("strDrink")).append("\n");
                        }
                        EmbedBuilder em = new EmbedBuilder();
                        em.setTitle("Pick which type of **"+drink.toLowerCase()+"** you want.");
                        em.setColor(Color.getHSBColor(294, 71,89));
                        em.setDescription(choices.toString());
                        em.setFooter("Reply with the number by the drink or `cancel`.",null);
                        Message m = event.getTextChannel().sendMessage(em.build()).complete();
                        waitForChoice(event, drinks, m);
                    }
                    else if(drinks.length() == 1){
                        makeDrink(event, drinks, 0);
                    }
                    else{
                        Messages.error(event, "Not sure what happened but things didn't workout. Try again.");
                        return;
                    }
                }
            }
            read.close();
            each.close();
            urlConn.disconnect();
        }catch (Exception ex){
            Messages.error(event, "Not sure what happened but things didn't workout. Try again and notify the bitch who made me.");
            ex.printStackTrace();
        }
    }

    private void waitForChoice(CommandEvent event, JSONArray drinks, Message m) {
        waiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                e -> {
                    if(m != null){
                        m.delete().queue();
                    }
                    if(e.getMessage().getContentRaw().trim().equalsIgnoreCase("cancel"))
                    {
                        Messages.reply(event, "Smh, pathetic, stop being a wuss.");
                        return;
                    }
                    else {
                        String choice = e.getMessage().getContentRaw().trim();
                        int choiceNum = 0;
                        try{
                            choiceNum = Integer.parseInt(choice);
                        }catch (NumberFormatException ex){
                            Messages.error(event, "Wtf, pick a number not anything else, drink cancelled.");
                            return;
                        }
                        if(choiceNum < 1 || choiceNum >  drinks.length()) {Messages.reply(event, "God damn it, can't you see that's not in the range."); return;}
                        makeDrink(event, drinks, choiceNum-1);
                    }
                },
                2, TimeUnit.MINUTES, () -> Messages.errorTimed(event, "Smh slowpoke, "+event.getAuthor().getAsMention()+". I'm not serving you a drink, you took too long.", 10, TimeUnit.SECONDS));
    }

    private void makeDrink(CommandEvent event, JSONArray drinks, int choiceNum) {
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.getHSBColor(294, 71,89));
        JSONObject drink = drinks.getJSONObject(choiceNum);
        em.setTitle(drink.getString("strDrink"));
        em.setDescription(drink.getString("strCategory"));

        // Drink ID
        em.addField("Drink ID", drink.getString("idDrink"), true);

        // Alcoholic or not
        em.addField("Alcoholic?", drink.getString("strAlcoholic"), true);

        // Alternate Names
        if(!drink.isNull("strDrinkAlternate")) em.addField("Alternate Names", drink.getString("strDrinkAlternate"), true);

        // Different languages
        StringBuilder diffLang = new StringBuilder();
        if(!drink.isNull("strDrinkES")) diffLang.append("Spanish: ").append(drink.get("strDrinkES")).append("\n");
        if(!drink.isNull("strDrinkDE")) diffLang.append("German: ").append(drink.get("strDrinkDE")).append("\n");
        if(!drink.isNull("strDrinkFR")) diffLang.append("French: ").append(drink.get("strDrinkFR")).append("\n");
        if(!drink.isNull("strDrinkZH-HANS")) diffLang.append("Chinese Simplified: ").append(drink.get("strDrinkZH-HANS")).append("\n");
        if(!drink.isNull("strDrinkZH-HANT")) diffLang.append("Chinese Traditional: ").append(drink.get("strDrinkZH-HANT")).append("\n");
        if(!diffLang.toString().trim().isEmpty()) em.addField("Other Languages", diffLang.toString(), true);

        // Glass Type
        if(!drink.isNull("strGlass")) em.addField("Glass", drink.getString("strGlass"), true);

        // Measures
        StringBuilder measures = new StringBuilder();
        for(int i = 1; i <= 15; i++){
            String givenMeasure = drink.isNull("strMeasure"+i) ? "" : drink.getString("strMeasure"+i);
            if(!givenMeasure.trim().isEmpty())  measures.append(i).append(". ").append(givenMeasure).append("\n");
        }
        if(!measures.toString().isEmpty()) em.addField("Measures", measures.toString(), true);

        // Ingredients
        StringBuilder ingredients = new StringBuilder();
        for(int i = 1; i <= 15; i++){
            String givenIngredient =  drink.isNull("strIngredient"+i) ? "" : drink.getString("strIngredient"+i);
            if(!givenIngredient.trim().isEmpty())  ingredients.append(i).append(". ").append(givenIngredient).append("\n");
        }
        if(!ingredients.toString().isEmpty()) em.addField("Ingredients", ingredients.toString(), true);

        // Instructions
        if(!drink.isNull("strInstructions")) em.addField("Instruction", drink.getString("strInstructions"), false);

        // Thumbnail
        if(!drink.isNull("strDrinkThumb")) em.setThumbnail(drink.getString("strDrinkThumb"));

        // Modified
        if(!drink.isNull("dateModified")) em.setFooter("Date Modified: " + drink.getString("dateModified"), null);
        else em.setFooter("Date Modified: -", null);

        event.getTextChannel().sendMessage(em.build()).queue();
    }
}