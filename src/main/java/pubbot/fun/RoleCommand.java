package pubbot.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.exceptions.HierarchyException;
import pubbot.utils.Messages;

import java.awt.*;

public class RoleCommand  extends Command {
    public RoleCommand() {
        this.name = "role";
        this.aliases = new String[]{"rolecolor"};
        this.category = new Category("Fun");
    }

    protected void execute(CommandEvent event) {
        if(event.getArgs().trim().isEmpty() || event.getArgs().trim().length() != 7){
            Messages.error(event, "Smh, need role color in hex as an argument with #. Eg: #ABCDEF");
            return;
        }
        Role checkRole = event.getMember().getRoles().parallelStream().filter(r -> r.getName().startsWith("#")).findFirst().orElse(null);
        if(checkRole != null){
            if(checkRole.getName().equalsIgnoreCase(event.getArgs().trim())){
                Messages.reply(event, "You already have this color.");
                return;
            }
            event.getGuild().getController().removeSingleRoleFromMember(event.getMember(), checkRole).complete();
            if(event.getGuild().getMembersWithRoles(checkRole).size() <= 1){
                checkRole.delete().queue();
            }
        }
        Color c;
        try {
            c = event.getArgs().trim().replace("#", "").length() < 6 ? null : hex2Rgb(event.getArgs().trim());
        }catch (NumberFormatException ex){
            Messages.error(event, "Not a hex value, color reset to default. Use hex value.");
            return;
        }
        if(c == null){
            Messages.error(event, "Invalid color, use hex with #.");
        }
        try {
            Role role = event.getGuild().getRoles().parallelStream().filter(r -> r.getName().equalsIgnoreCase(event.getArgs().trim())).findFirst().orElse(null);
            if(role == null){
                int index = event.getGuild().getRoles().parallelStream().filter(r -> r.getName().equalsIgnoreCase("New Bitches")).findFirst().get().getPositionRaw();
                role = event.getGuild().getController().createRole().setName(event.getArgs().trim()).setColor(c).setHoisted(false).setMentionable(false).complete();
                event.getGuild().getController().modifyRolePositions().selectPosition(role).moveTo(index+1).queue();
            }
            EmbedBuilder em = new EmbedBuilder();
            em.setColor(c);
            role.getManager().setPermissions(Permission.EMPTY_PERMISSIONS).queue();
            event.getGuild().getController().addSingleRoleToMember(event.getMember(), role).queue(s -> {
                em.setDescription("Color changed successfully to " + event.getArgs().trim() + ".");
                event.getTextChannel().sendMessage(em.build()).queue();
            }, f -> {
                Messages.error(event, "Color change failed.");
            });
        } catch (HierarchyException h){
            Messages.error(event, h.getMessage());
        }
    }

    private static Color hex2Rgb(String colorStr) {
        return new Color(
                Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
                Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
                Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
    }
}