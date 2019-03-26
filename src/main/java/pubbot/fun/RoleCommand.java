package pubbot.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import net.dv8tion.jda.core.exceptions.PermissionException;
import pubbot.utils.Message;
import pubbot.utils.Permissions;

import java.awt.*;

public class RoleCommand  extends Command {
    public RoleCommand() {
        this.name = "role";
        this.aliases = new String[]{"rolecolor"};
        this.category = new Category("Fun");
    }

    protected void execute(CommandEvent event) {
        String args[] = event.getArgs().split("\\s+(?=\\S*+$)");
        if(args.length < 2){
            Message.error(event, "Smh, need 2 args, role name and role color in hex.");
            return;
        }
        Role toChange = event.getMember().getRoles().parallelStream().filter(r -> r.getName().equalsIgnoreCase(args[0])).findFirst().orElse(null);
        if(toChange == null){
            Message.error(event, "You don't have the role you gave me, dumb bitch.");
            return;
        }
        Color c = args[1].replace("#", "").length() < 6 ? null : hex2Rgb(args[1]);
        if(c == null){
            Message.error(event, "Invalid color, use hex with #.");
        }
        try {
            toChange.getManager().setColor(c).queue(s -> {
                Message.reply(event, "Color changed successfully of " + toChange.getName() + " to " + args[1] + ".");
            }, f -> {
                Message.error(event, "Color change failed.");
            });
        } catch (HierarchyException h){
            Message.error(event, h.getMessage());
        }
    }

    private static Color hex2Rgb(String colorStr) {
        return new Color(
                Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
    }
}