package pubbot.utils;

/*
 * Useful methods for quick use of embeds for messages
 */

//JDA Utilities
import com.jagrosh.jdautilities.command.CommandEvent;

//JDA
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

//Color import
import java.awt.*;

//Concurrency
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Message {
    public static void reply(MessageReceivedEvent event, String message){
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.CYAN);
        em.setDescription(message);
        event.getTextChannel().sendMessage(em.build()).queue();
    }

    public static void reply(CommandEvent event, String message) {
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.CYAN);
        em.setDescription(message);
        event.getTextChannel().sendMessage(em.build()).queue();
    }

    public static void error(MessageReceivedEvent event, String message){
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.RED);
        em.setDescription(message);
        event.getTextChannel().sendMessage(em.build()).queue();
    }

    public static void error(CommandEvent event, String message){
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.RED);
        em.setDescription(message);
        event.getTextChannel().sendMessage(em.build()).queue();
    }

    public static void replyTimed(CommandEvent event, String message, int timeTillDeletion, TimeUnit timeType) {
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.CYAN);
        em.setDescription(message);
        embedTimedMessage(event, em, timeTillDeletion, timeType);
    }

    public static void errorTimed(CommandEvent event, String message, int timeTillDeletion, TimeUnit timeType) {
        EmbedBuilder em = new EmbedBuilder();
        em.setColor(Color.RED);
        em.setDescription(message);
        embedTimedMessage(event, em, timeTillDeletion, timeType);
    }

    public static void send(CommandEvent event, String message) {
        event.getTextChannel().sendMessage(message).queue();
    }

    public static void normalTimedMessage(CommandEvent event, String message, int timeTillDeletion, TimeUnit timeType) {
        event.getTextChannel().sendMessage(message).queue(m -> {
            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(20);
            exec.schedule(() -> m.delete().queue(), timeTillDeletion, timeType);
        });
    }

    private static void embedTimedMessage(CommandEvent event, EmbedBuilder embed, int timeTillDeletion, TimeUnit timeType) {
        event.getTextChannel().sendMessage(embed.build()).queue(m -> {
            ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(20);
            exec.schedule(() -> m.delete().queue(), timeTillDeletion, timeType);
        });
    }
}