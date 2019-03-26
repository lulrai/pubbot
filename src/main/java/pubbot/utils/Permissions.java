package pubbot.utils;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Permissions {
    public static boolean isBotOwner(User u){
        for(String id : Constants.BOT_OWNER_IDS){
            if(u.getId().equals(id)) return true;
        }
        return false;
    }

    public static boolean isServerOwner(CommandEvent event, User u){
        Member m = event.getGuild().getMember(u);
        return m.isOwner();
    }

    public static boolean isAdmin(CommandEvent event, User u){
        List<String> admins = new ArrayList<>(Arrays.asList("bot admin", "admin", "administrator"));
        Member m = event.getGuild().getMember(u);
        boolean isAdmin = false;
        for(Role role : m.getRoles()) {
            if(admins.contains(role.getName().toLowerCase())) {
                isAdmin = true;
            }
        }
        return m.hasPermission(Permission.ADMINISTRATOR) || isAdmin || m.isOwner();
    }

    public static boolean isMod(CommandEvent event, User u){
        List<String> mods = new ArrayList<>(Arrays.asList("bot mod", "mod", "moderator", "discord moderator"));
        Member m = event.getGuild().getMember(u);
        boolean isMod = false;
        for(Role role : m.getRoles()) {
            if(mods.contains(role.getName().toLowerCase())) {
                isMod = true;
            }
        }
        return m.hasPermission(Permission.MANAGE_PERMISSIONS) || isMod || isAdmin(event, u) || m.isOwner();
    }

    public static boolean isBot(CommandEvent event){
        return event.getMessage().getMentionedUsers().get(0).isBot() || event.getAuthor().isBot();
    }
}