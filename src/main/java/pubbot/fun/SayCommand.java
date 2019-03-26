package pubbot.fun;

/*
 * Say Command - a command that repeats what the user provides as an arg
 */

//JDA Utilities
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

//JDA
import net.dv8tion.jda.core.exceptions.PermissionException;

//Utils
import pubbot.utils.Message;
import pubbot.utils.Permissions;

public class SayCommand extends Command {
    public SayCommand() {
        this.name = "say";
        this.aliases = new String[]{"repeat"};
        this.category = new Category("Fun");
    }

    protected void execute(CommandEvent event) {
        if(!event.getArgs().trim().isEmpty()){
            if(Permissions.isAdmin(event, event.getAuthor())){
                try{
                    event.getMessage().delete().queue();
                }catch (PermissionException e){
                    Message.error(event,"Missing permissions: "+e.getPermission());
                }
                Message.send(event, event.getArgs());
            }
            else{
                Message.error(event,"You don't have permission to perform this command.");
            }
        }
    }
}
