package dev.garby.botserver;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main extends ListenerAdapter {
    protected static String PATH;
    protected static String TOKEN;
    protected static String OWNER_ID;
    protected static String[] SERVER_LIST;
    protected static String PING_ROLE;
    protected static String BACKEND_HOSTNAME;
    protected static String MC_IP;
    protected static String GCLOUD_PROJECT;
    protected static String GCLOUD_ZONE;
    protected static String GCLOUD_INSTANCE;
    protected static Boolean local = false;
    private static final Logger logger
            = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) throws InterruptedException  {
        if ( args.length == 0 ) {
            logger.error("No path provided");
            PATH = "config.properties";
        } else {
            PATH = args[0];
        }
        getConfig();


        // create JDA instance
        JDA jda;
        try {
            jda = JDABuilder.createDefault(TOKEN).build();
            jda.addEventListener(new Main());
        } catch (Exception e) {
            logger.error("Error", e);
            return;
        }
        jda.awaitReady();
        if (Arrays.asList(args).contains("--refresh")) {
            jda.updateCommands().addCommands(Config.getCommands()).queue();
            logger.info("Refreshed Slash Config");

        }
        if (Arrays.asList(args).contains("--local")) {
            logger.info("Running in local mode");
            local = true;

        }

        logger.info("Ready - logged in as " + jda.getSelfUser().getAsTag());
    }
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        try {


            Config.commandSplitter(event);
            if (!event.isAcknowledged())
                event.reply("Command '"+event.getFullCommandName()+"' is not implemented yet").queue();

        } catch (Exception e ) {
            e.printStackTrace();
            event.reply("**Error** Executing command! \n ``"+e.getMessage()+"``").queue();
        }


    }
    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {

        List<Command.Choice> options = Stream.of(Config.autocomplete().get(event.getFullCommandName()))
                .filter(word -> word.startsWith(event.getFocusedOption().getValue())) // only display words that start with the user's current input
                .map(word -> new Command.Choice(word, word)) // map the words to choices
                .collect(Collectors.toList());
        event.replyChoices(options).queue();


    }
    protected static void getConfig() {
        try (InputStream input = Files.newInputStream(Paths.get(PATH))) {
            Properties prop = new Properties();
            // load a properties file
            prop.load(input);
            // get the property value
            TOKEN = prop.getProperty("token");
            OWNER_ID = prop.getProperty("owner-id");
            SERVER_LIST = prop.getProperty("server-list").split(",");
            PING_ROLE = prop.getProperty("ping-role");
            BACKEND_HOSTNAME = prop.getProperty("backend-hostname");
            MC_IP = prop.getProperty("mc-ip");
            GCLOUD_PROJECT = prop.getProperty("gcloud-project");
            GCLOUD_ZONE = prop.getProperty("gcloud-zone");
            GCLOUD_INSTANCE = prop.getProperty("gcloud-instance");

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
}