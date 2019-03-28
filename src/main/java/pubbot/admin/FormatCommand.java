package pubbot.admin;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageHistory;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import pubbot.utils.Messages;
import pubbot.utils.Permissions;

import java.awt.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class FormatCommand extends Command {
    private EventWaiter waiter;
    private ArrayList<String[]> fieldLists = new ArrayList<>();

    public FormatCommand(EventWaiter waiter) {
        this.name = "format";
        this.aliases = new String[]{"textformat"};
        this.category = new Category("Admin");
        this.waiter = waiter;
    }

    protected void execute(CommandEvent event) {
        if(!Permissions.isAdmin(event, event.getAuthor())){
            return;
        }
        event.getMessage().delete().queue();
        Messages.replyTimed(event, "Creating an embed.. `edit` or `add`?", 10, TimeUnit.SECONDS);
        waitForType(event);
    }

    private void waitForType(CommandEvent event) {
        waiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                e -> {
                    if(e.getMessage().getContentRaw().trim().equalsIgnoreCase("cancel"))
                    {
                        e.getMessage().delete().queue();
                        Messages.replyTimed(event, "Embed has been cancelled.", 10, TimeUnit.SECONDS);
                        return;
                    }
                    else {
                        String type = e.getMessage().getContentRaw().trim();
                        e.getMessage().delete().queue();
                        if(type.equalsIgnoreCase("add")) {
                            Messages.replyTimed(event, "Mention the channel where you want the embed.", 10, TimeUnit.SECONDS);
                            waitForChannel(event, false);
                        }
                        else if(type.equalsIgnoreCase("edit")){
                            Messages.replyTimed(event, "Mention the channel where the embed is.", 10, TimeUnit.SECONDS);
                            waitForChannel(event, true);
                        }
                        else {
                            Messages.errorTimed(event, "Invalid input. Type `add`, `edit`, or `cancel`.", 10, TimeUnit.SECONDS);
                            waitForType(event);
                        }
                    }
                },
                2, TimeUnit.MINUTES, () -> Messages.errorTimed(event, "Smh slowpoke, "+event.getAuthor().getAsMention()+". Embed has been cancelled, you took too long.", 10, TimeUnit.SECONDS));
    }

    private void waitForChannel(CommandEvent event, boolean isEdit){
        waiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                e -> {
                    if(e.getMessage().getContentRaw().trim().equalsIgnoreCase("cancel"))
                    {
                        e.getMessage().delete().queue();
                        Messages.replyTimed(event, "Embed has been cancelled.", 10, TimeUnit.SECONDS);
                        return;
                    }
                    else {
                        TextChannel channel = e.getMessage().getMentionedChannels().isEmpty() ? null : e.getMessage().getMentionedChannels().get(0);
                        e.getMessage().delete().queue();
                        if(channel == null){
                            Messages.errorTimed(event, "Invalid channel. Mention a valid channel or `cancel`.", 10, TimeUnit.SECONDS);
                            waitForChannel(event, isEdit);
                        }
                        else{
                            if(isEdit){
                                Messages.replyTimed(event, "Enter the title of the embed or `none`.", 10, TimeUnit.SECONDS);
                                waitForMessageID(event, channel);
                            }
                            else {
                                Messages.replyTimed(event, "Enter the title of the embed or `none`.", 10, TimeUnit.SECONDS);
                                waitForTitle(event, channel, null, false);
                            }
                        }
                    }
                },
                2, TimeUnit.MINUTES, () -> Messages.errorTimed(event, "Smh slowpoke, "+event.getAuthor().getAsMention()+". Embed has been cancelled, you took too long.", 10, TimeUnit.SECONDS));
    }

    private void waitForMessageID(CommandEvent event, TextChannel channel){
        waiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                e -> {
                    if(e.getMessage().getContentRaw().trim().equalsIgnoreCase("cancel"))
                    {
                        e.getMessage().delete().queue();
                        Messages.replyTimed(event, "Embed has been cancelled.", 10, TimeUnit.SECONDS);
                        return;
                    }
                    else {
                        MessageHistory mh = new MessageHistory(channel);
                        Message message = mh.getMessageById(e.getMessage().getContentRaw().trim());
                        e.getMessage().delete().queue();

                        if(message == null){
                            Messages.errorTimed(event, "Invalid message id. Provide a valid message id or `cancel`.", 10, TimeUnit.SECONDS);
                            waitForMessageID(event, channel);
                        }
                        else{
                            Messages.replyTimed(event, "Enter the title of the embed or `none`.", 10, TimeUnit.SECONDS);
                            waitForTitle(event, channel, message,true);
                        }
                    }
                },
                2, TimeUnit.MINUTES, () -> Messages.errorTimed(event, "Smh slowpoke, "+event.getAuthor().getAsMention()+". Embed has been cancelled, you took too long.", 10, TimeUnit.SECONDS));
    }

    private void waitForTitle(CommandEvent event, TextChannel channel, Message message, boolean isEdit){
        waiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                e -> {
                    if(e.getMessage().getContentRaw().trim().equalsIgnoreCase("cancel"))
                    {
                        e.getMessage().delete().queue();
                        Messages.replyTimed(event, "Embed has been cancelled.", 10, TimeUnit.SECONDS);
                        return;
                    }
                    else {
                        String title = e.getMessage().getContentRaw().trim();
                        e.getMessage().delete().queue();
                        if(title.equalsIgnoreCase("none")){
                            Messages.replyTimed(event, "Enter the description you want for the embed or type `none`.", 10, TimeUnit.SECONDS);
                            waitForDescription(event, channel, message, isEdit, "");
                        }
                        else{
                            if(title.length() >= MessageEmbed.TITLE_MAX_LENGTH){
                                Messages.errorTimed(event, "Too long of a title, my dude. Try again.", 10, TimeUnit.SECONDS);
                                waitForTitle(event, channel, message, isEdit);
                            }
                            else{
                                Messages.replyTimed(event, "Enter the description you want for the embed or type `none`.", 10, TimeUnit.SECONDS);
                                waitForDescription(event, channel, message, isEdit, title);
                            }
                        }
                    }
                },
                2, TimeUnit.MINUTES, () -> Messages.errorTimed(event, "Smh slowpoke, "+event.getAuthor().getAsMention()+". Embed has been cancelled, you took too long.", 10, TimeUnit.SECONDS));
    }

    private void waitForDescription(CommandEvent event, TextChannel channel, Message message, boolean isEdit, String title){
        waiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                e -> {
                    if(e.getMessage().getContentRaw().trim().equalsIgnoreCase("cancel"))
                    {
                        e.getMessage().delete().queue();
                        Messages.replyTimed(event, "Embed has been cancelled.", 10, TimeUnit.SECONDS);
                        return;
                    }
                    else {
                        String description = e.getMessage().getContentRaw().trim();
                        e.getMessage().delete().queue();
                        if(description.equalsIgnoreCase("none")){
                            Messages.replyTimed(event, "Enter field name and field description separated by a `|` or type `none`.", 10, TimeUnit.SECONDS);
                            waitForFields(event, channel, message, isEdit, title, "");
                        }
                        else{
                            if(description.length() >= MessageEmbed.TEXT_MAX_LENGTH){
                                Messages.errorTimed(event, "Too long of a description, my dude. Try again.", 10, TimeUnit.SECONDS);
                                waitForDescription(event, channel, message, isEdit, title);
                            }
                            else{
                                Messages.replyTimed(event, "Enter field name and field description separated by a `|`.", 10, TimeUnit.SECONDS);
                                waitForFields(event, channel, message, isEdit, title, description);
                            }
                        }
                    }
                },
                2, TimeUnit.MINUTES, () -> Messages.errorTimed(event, "Smh slowpoke, "+event.getAuthor().getAsMention()+". Embed has been cancelled, you took too long.", 10, TimeUnit.SECONDS));
    }

    private void waitForFields(CommandEvent event, TextChannel channel, Message message, boolean isEdit, String title, String description){
        waiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                e -> {
                    if(e.getMessage().getContentRaw().trim().equalsIgnoreCase("cancel"))
                    {
                        e.getMessage().delete().queue();
                        Messages.replyTimed(event, "Embed has been cancelled.", 10, TimeUnit.SECONDS);
                        return;
                    }
                    else {

                        String[] fieldParts = Pattern.compile("\\|").split(e.getMessage().getContentRaw().trim(), 2);
                        if(e.getMessage().getContentRaw().trim().equalsIgnoreCase("none")){
                            Messages.replyTimed(event, "Provide a color in hex for the embed, make sure to include #.", 10, TimeUnit.SECONDS);
                            waitForColor(event, channel, message, isEdit, title, description);
                            e.getMessage().delete().queue();
                        }
                        else{
                            if(fieldParts.length != 2){
                                Messages.errorTimed(event, "Need two things separated by a `|`. Try again.", 10, TimeUnit.SECONDS);
                                waitForFields(event, channel, message, isEdit, title, description);
                            }
                            if(fieldParts[0].length() >= MessageEmbed.TITLE_MAX_LENGTH){
                                Messages.errorTimed(event, "Too long of a field name, my dude. Try again.", 10, TimeUnit.SECONDS);
                                waitForFields(event, channel, message, isEdit, title, description);
                            }
                            else if(fieldParts[1].length() >= MessageEmbed.VALUE_MAX_LENGTH){
                                Messages.errorTimed(event, "Too long of a field description, my dude. Try again.", 10, TimeUnit.SECONDS);
                                waitForFields(event, channel, message, isEdit, title, description);
                            }
                            else{
                                fieldLists.add(new String[] {fieldParts[0], fieldParts[1]});
                                Messages.replyTimed(event, "Added field. Want more fields? Type `yes`, `no`, or `cancel`.", 10, TimeUnit.SECONDS);
                                waitForMore(event, channel, message, isEdit, title, description);
                            }
                        }
                    }
                },
                2, TimeUnit.MINUTES, () -> Messages.errorTimed(event, "Smh slowpoke, "+event.getAuthor().getAsMention()+". Embed has been cancelled, you took too long.", 10, TimeUnit.SECONDS));
    }

    private void waitForMore(CommandEvent event, TextChannel channel, Message message, boolean isEdit, String title, String description){
        waiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                e -> {
                    if(e.getMessage().getContentRaw().trim().equalsIgnoreCase("cancel"))
                    {
                        e.getMessage().delete().queue();
                        Messages.replyTimed(event, "Embed has been cancelled.", 10, TimeUnit.SECONDS);
                        return;
                    }
                    else {
                        String answer = e.getMessage().getContentRaw().trim();
                        e.getMessage().delete().queue();
                        if(answer.equalsIgnoreCase("yes")){
                            Messages.replyTimed(event, "Enter field name and field description separated by a `|`.", 10, TimeUnit.SECONDS);
                            waitForFields(event, channel, message, isEdit, title, description);
                        }
                        else if(answer.equalsIgnoreCase("no")){
                            Messages.replyTimed(event, "Provide a color in hex for the embed, make sure to include #.", 10, TimeUnit.SECONDS);
                            waitForColor(event, channel, message, isEdit, title, description);
                        }
                        else{
                            Messages.errorTimed(event, "Not a right answer. Get good and type either `yes`, `no`, or `cancel`.", 10, TimeUnit.SECONDS);
                            waitForMore(event, channel, message, isEdit, title, description);
                        }
                    }
                },
                2, TimeUnit.MINUTES, () -> Messages.errorTimed(event, "Smh slowpoke, "+event.getAuthor().getAsMention()+". Embed has been cancelled, you took too long.", 10, TimeUnit.SECONDS));
    }

    private void waitForColor(CommandEvent event, TextChannel channel, Message message, boolean isEdit, String title, String description){
        waiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                e -> {
                    if(e.getMessage().getContentRaw().trim().equalsIgnoreCase("cancel"))
                    {
                        e.getMessage().delete().queue();
                        Messages.replyTimed(event, "Embed has been cancelled.", 10, TimeUnit.SECONDS);
                        return;
                    }
                    else {
                        String color = e.getMessage().getContentRaw().trim();
                        e.getMessage().delete().queue();
                        if(color.isEmpty() || color.length() != 7){
                            Messages.errorTimed(event, "Smh, need role color in hex as an argument with #. Eg: #ABCDEF. Try again.", 10, TimeUnit.SECONDS);
                            waitForColor(event, channel, message, isEdit, title, description);
                        }
                        else {
                            Color c = color.replace("#", "").length() < 6 ? null : hex2Rgb(color);
                            if(c == null){
                                Messages.errorTimed(event, "Invalid color, use hex with #.", 10, TimeUnit.SECONDS);
                                waitForColor(event, channel, message, isEdit, title, description);
                            }
                            else{
                                Messages.replyTimed(event, "If you want a thumbnail, please prove a link to the image or type `none`.", 10, TimeUnit.SECONDS);
                                waitForThumbnail(event, channel, message, isEdit, title, description, c);
                            }
                        }
                    }
                },
                2, TimeUnit.MINUTES, () -> Messages.errorTimed(event, "Smh slowpoke, "+event.getAuthor().getAsMention()+". Embed has been cancelled, you took too long.", 10, TimeUnit.SECONDS));
    }

    private void waitForThumbnail(CommandEvent event, TextChannel channel, Message message, boolean isEdit, String title, String description, Color c){
        waiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                e -> {
                    if(e.getMessage().getContentRaw().trim().equalsIgnoreCase("cancel"))
                    {
                        e.getMessage().delete().queue();
                        Messages.replyTimed(event, "Embed has been cancelled.", 10, TimeUnit.SECONDS);
                        return;
                    }
                    else {
                        String thumbnailURL = e.getMessage().getContentRaw().trim();
                        e.getMessage().delete().queue();
                        if(thumbnailURL.length() >= MessageEmbed.URL_MAX_LENGTH){
                            Messages.errorTimed(event, "Too long of a URL, my dude. Try again.", 10, TimeUnit.SECONDS);
                            waitForThumbnail(event, channel, message, isEdit, title, description, c);
                        }
                        else{
                            if(thumbnailURL.equalsIgnoreCase("none")){
                                Messages.replyTimed(event, "Do you want to add an image then? If so, provide link or type `none`.", 10, TimeUnit.SECONDS);
                                waitForImage(event, channel, message, isEdit, title, description, c, "");
                            }
                            else{
                                EmbedBuilder em = new EmbedBuilder();
                                try{
                                    em.setThumbnail(thumbnailURL);
                                }catch (IllegalArgumentException ex){
                                    Messages.errorTimed(event, "Invalid url provided. S M H. Try again or type `none`.", 10, TimeUnit.SECONDS);
                                    waitForThumbnail(event, channel, message, isEdit, title, description, c);
                                }
                                Messages.replyTimed(event, "Do you want to add an image? If so, provide link or type 'none'.", 10, TimeUnit.SECONDS);
                                waitForImage(event, channel, message, isEdit, title, description, c, thumbnailURL);
                            }
                        }
                    }
                },
                2, TimeUnit.MINUTES, () -> Messages.errorTimed(event, "Smh slowpoke, "+event.getAuthor().getAsMention()+". Embed has been cancelled, you took too long.", 10, TimeUnit.SECONDS));
    }

    private void waitForImage(CommandEvent event, TextChannel channel, Message message, boolean isEdit, String title, String description, Color c, String thumbnailURL){
        waiter.waitForEvent(GuildMessageReceivedEvent.class,
                e -> e.getAuthor().equals(event.getAuthor()) && e.getChannel().equals(event.getChannel()) && !e.getAuthor().isBot(),
                e -> {
                    if(e.getMessage().getContentRaw().trim().equalsIgnoreCase("cancel"))
                    {
                        e.getMessage().delete().queue();
                        Messages.replyTimed(event, "Embed has been cancelled.", 10, TimeUnit.SECONDS);
                        return;
                    }
                    else {
                        String imageURL = e.getMessage().getContentRaw().trim();
                        e.getMessage().delete().queue();
                        if(imageURL.length() >= MessageEmbed.URL_MAX_LENGTH){
                            Messages.errorTimed(event, "Too long of a URL, my dude. Try again.", 10, TimeUnit.SECONDS);
                            waitForImage(event, channel, message, isEdit, title, description, c, thumbnailURL);
                        }
                        else{
                            if(imageURL.equalsIgnoreCase("none")){
                                Messages.replyTimed(event, "Ok cool, you're done. I'll create the embed now then.", 10, TimeUnit.SECONDS);
                                createEmbed(event, channel, message, isEdit, title, description, c, thumbnailURL, "");
                            }
                            else{
                                EmbedBuilder em = new EmbedBuilder();
                                try{
                                    em.setImage(imageURL);
                                }catch (IllegalArgumentException ex){
                                    Messages.errorTimed(event, "Invalid url provided. S M H. Try again or type `none`.", 10, TimeUnit.SECONDS);
                                    waitForImage(event, channel, message, isEdit, title, description, c, thumbnailURL);
                                }
                                Messages.replyTimed(event, "Ok cool, you're done. I'll create the embed now then.", 10, TimeUnit.SECONDS);
                                createEmbed(event, channel, message, isEdit, title, description, c, thumbnailURL, imageURL);
                            }
                        }
                    }
                },
                2, TimeUnit.MINUTES, () -> Messages.errorTimed(event, "Smh slowpoke, "+event.getAuthor().getAsMention()+". Embed has been cancelled, you took too long.", 10, TimeUnit.SECONDS));
    }

    private void createEmbed(CommandEvent event, TextChannel channel, Message message, boolean isEdit, String title, String description, Color c, String thumbnailURL, String imageURL) {
        EmbedBuilder em = new EmbedBuilder();
        if(!title.isEmpty()) em.setTitle(title);
        if(!description.isEmpty()) em.setDescription(description);
        if(!fieldLists.isEmpty()){
            for(String[] field : fieldLists){
                em.addField(field[0], field[1], false);
            }
            fieldLists.clear();
        }
        em.setColor(c);
        if(!thumbnailURL.isEmpty()) em.setThumbnail(thumbnailURL);
        if(!imageURL.isEmpty()) em.setImage(imageURL);
        if(isEdit){
            message.editMessage(em.build()).queue(v -> Messages.replyTimed(event, "Embed successfully edited.", 5, TimeUnit.SECONDS));
        }
        else{
            channel.sendMessage(em.build()).queue(v -> Messages.replyTimed(event, "Embed successfully created.", 5, TimeUnit.SECONDS));
        }
    }

    private static Color hex2Rgb(String colorStr) {
        return new Color(
                Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
    }
}