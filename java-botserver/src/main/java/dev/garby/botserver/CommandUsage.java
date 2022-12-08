package dev.garby.botserver;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;


class CommandUsage {
    SlashCommandInteractionEvent event;
    private static final Logger logger
            = LoggerFactory.getLogger(CommandUsage.class);

    public CommandUsage(SlashCommandInteractionEvent event) {
        this.event = event;

    }

    protected void start() throws IOException {

        final String server = event.getOption("server").getAsString();
        if (!serverValidate(server)) {
            event.reply("Server not found").setEphemeral(true).queue();
            return;
        }
        Api API = new Api();

        event.replyEmbeds(startupTemplate("Starting Virtual Machine").build()).queue();
        try {
            GCloud.startInstance();
        } catch (IOException | ExecutionException | TimeoutException | InterruptedException e) {
            logger.error("Failed to start instance", e);
        }

        // check if proxy has started
        event.getHook().editOriginalEmbeds(startupTemplate("Connecting To Server").build()).queue();
        for (int i = 0; i < 32; i++) {
            if (API.ping()) {
                break;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (i == 31) throw new RuntimeException("Proxy failed to start - ping timeout");
        }
        event.getHook().editOriginalEmbeds(startupTemplate("Starting Minecraft Server", 0x226a98).build()).queue();

        String response = API.start(server);
        if (response.equals("DONE")) {
            event.getHook().editOriginalEmbeds(startupTemplate("connect with ``" + Main.MC_IP+"``",
                    0x16c60c,
                    "https://cdn.discordapp.com/emojis/1061328167844974692.webp?size=96&quality=lossless",
                    "Started Successfully", new MessageEmbed.Field("Server", server, false))
                    .build()).queue();
        } else {
            if (response.codePoints().count() > 4096) {
                response = "(too long to display, see console)";
            }
            event.getHook().editOriginalEmbeds(startupTemplate("```\n"+response+"\n```",
                    0xfe2c2d,
                    "https://cdn.discordapp.com/emojis/1061331357386678382.webp?size=96&quality=lossless", "Startup Crash")
                    .build()).queue();
        }

    }


    protected void stop() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        //TODO: dis broken
        event.deferReply().queue();
        GCloud.stopInstance();
        event.getHook().editOriginal("Stopped Successfully").queue();


//        final String server = event.getOption("server").getAsString();
//        if (!serverValidate(server)) {
//            event.reply("Server not found").setEphemeral(true).queue();
//            return;
//        }
//        event.deferReply().queue();
//        Api API = new Api();
//        String response = API.stop(server);
//        if (response.equals("DONE")) {
//            event.getHook().editOriginal("Stopped Successfully").queue();
//        } else {
//            logger.error("Error stopping server: " + response);
//            event.getHook().editOriginal("Error Stopping: "+ response).queue();
//        }

    }

    protected void kill() throws InterruptedException {
        event.deferReply().queue();
        Thread.sleep(2000);
        //TODO: Api call to kill server
        event.getHook().editOriginal("Killing Server (but not)").queue();
    }

    protected void keep() {
        if (event.getUser().getId().equals(Main.OWNER_ID)) {
            //TODO: Api call to keep server online
            event.reply("Keeping Server").queue();
        } else {
            event.reply("This command is dangerous and has the potential to spend more money then necessary so it has been restricted to "
                    + event.getJDA().getUserById(Main.OWNER_ID).getAsMention() + " .").setEphemeral(true).queue();
        }
    }

    protected void list() {
        MessageEmbed embed = new EmbedBuilder()
                .setTitle("Server List")
                .setDescription(String.join("\n", Main.SERVER_LIST))
                .setColor(0x226a98)
                .setFooter("This must be updated manually (see your config file)")
                .build();
        event.reply("").addEmbeds(embed).queue();
    }

    private EmbedBuilder startupTemplate(String description) {
        return new EmbedBuilder().setDescription(description).setFooter("Server Manager by garby.dev");

    }

    private EmbedBuilder startupTemplate(String description, int color) {
        return startupTemplate(description).setColor(color);
    }

    private EmbedBuilder startupTemplate(String description, int color, String image) {
        return startupTemplate(description, color).setImage(image);
    }

    private EmbedBuilder startupTemplate(String description, int color, String image, String title) {
        return startupTemplate(description, color, image).setTitle(title);
    }

    private EmbedBuilder startupTemplate(String description, int color, String image, String title, MessageEmbed.Field field) {
        return startupTemplate(description, color, image, title).addField(field);
    }
    private boolean serverValidate(String server) {
        return  Arrays.asList(Main.SERVER_LIST).contains(server);
    }

}
