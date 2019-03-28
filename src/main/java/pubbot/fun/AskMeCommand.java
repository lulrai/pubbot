package pubbot.fun;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import pubbot.utils.Messages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

public class AskMeCommand extends Command {
    private static ArrayList<String> sentences = new ArrayList<>();

    public AskMeCommand() {
        this.name = "ama";
        this.aliases = new String[]{"askme"};
        this.category = new Category("Fun");
    }

    public static void generateAMA() {
        Path workingDir = Paths.get(System.getProperty("user.dir"));
        File extraDir = new File(workingDir.resolve("extra").toUri());
        if(!extraDir.exists()){
            System.out.println("No directory called " + extraDir.getName() + " is present.");
            return;
        }
        File amaFile = new File(extraDir, "ama.txt");
        if(!amaFile.exists()){
            System.out.println("No file called " + amaFile.getName() + " is present.");
        }
        try{
            BufferedReader br = new BufferedReader(new FileReader(amaFile));
            String currentLine;
            while((currentLine = br.readLine()) != null){
                sentences.add(currentLine.trim());
            }
            br.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    protected void execute(CommandEvent event) {
        Random num = new Random();
        Messages.reply(event, sentences.get(num.nextInt(sentences.size())));
    }
}