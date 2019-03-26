package pubbot;

/*
 * Main Class
 */

//Logs
import java.util.logging.Level;
import java.util.logging.Logger;

//JDA Utilities

//JDA
import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;

//Class imports
import net.dv8tion.jda.core.entities.Game;
import pubbot.utils.Constants;

public class Bot {
    private static JDA jda;

    public static void main(String[] args) throws Exception {
        Logger.getLogger("org.slf4j.impl.StaticLoggerBinder").setLevel(Level.OFF);
        Logger.getLogger("org.apache.http.client.protocol.ResponseProcessCookies").setLevel(Level.OFF);
        EventWaiter waiter = new EventWaiter();
        jda = new JDABuilder(AccountType.BOT)
                .setToken(Constants.CLIENT_SECRET_CODE)
                .addEventListener(commandClient(waiter))
                .addEventListener(waiter)
                //.addEventListener(new CustomCheck())
                .setStatus(OnlineStatus.ONLINE)
                .setGame(Game.playing("Connecting.."))
                .build();
        jda.setAutoReconnect(true);
    }

    private static CommandClient commandClient(EventWaiter waiter) {
        return new CommandClientBuilder()
                .setPrefix(Constants.PREFIX)
                .setOwnerId(Constants.BOT_OWNER_IDS[0])
                .setEmojis(Constants.SUCCESS, Constants.WARNING, Constants.ERROR)
                //.setGame(Game.of(Game.GameType.WATCHING,"Homework Hub", Constants.SERVER_INVITE))
                .setStatus(OnlineStatus.ONLINE)
                .addCommands(
                    //Commands go here
                )
                .build();
    }
}
