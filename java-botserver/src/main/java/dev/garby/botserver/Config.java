package dev.garby.botserver;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

class Config {
    private static final Logger logger
            = LoggerFactory.getLogger(Config.class);


    protected static CommandData getCommands() {
        return
            net.dv8tion.jda.api.interactions.commands.build.Commands.slash("server", "Manage the server! (spend money)")
            .addSubcommands(new SubcommandData("start", "Start minecraft server and/or virtual machine.")
                    .addOption(OptionType.STRING,"server","Server Name",true,true)
            )
            .addSubcommands(new SubcommandData("stop","stop a server safely (no argument for all)")
//                    .addOption(OptionType.STRING,"server","Server Name",true,true)
            )
            .addSubcommands(new SubcommandData("list","List all available servers and update saved server list"))


            .addSubcommandGroups(new SubcommandGroupData("manage","Other Management commands for the servers")
                    //TODO: add backup command
//                    .addSubcommands(new SubcommandData("kill","Force Stop The Server (Be Careful)"))
                    .addSubcommands(new SubcommandData("keep","Keep the server online with no players for 15 minutes."))
                    //TODO: add status command
            );

    }
    protected static HashMap<String,String[]> autocomplete() {
        HashMap<String,String[]> args = new HashMap<>();
        args.put("server start",Main.SERVER_LIST);
        args.put("server stop",Main.SERVER_LIST);

        return args;

    }
    protected static void commandSplitter(SlashCommandInteractionEvent event) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        final String command = event.getFullCommandName();
        CommandUsage runner = new CommandUsage(event);
        logger.debug("Handling command: " + command);

        if (!event.getUser().getId().equals(Main.OWNER_ID)) {
            event.reply("I dont have time to polish this, so its lockdowned to owner only").setEphemeral(true).queue();
            return;
        }



        switch (command) {
            case "server start" -> runner.start();
            case "server stop" -> runner.stop();
            case "server list" -> runner.list();
            case "server manage kill" -> runner.kill();
            case "server manage keep" -> runner.keep();
            default -> event.reply("Command not found").setEphemeral(true).queue();

        }
    }


}
