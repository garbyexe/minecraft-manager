import {
  CacheType,
  ChatInputCommandInteraction,
  SlashCommandBuilder,
  EmbedBuilder,
  AutocompleteInteraction,
  userMention,
  ColorResolvable,
  APIEmbedField,
  inlineCode,
  RoleMention,
  UserMention,
  roleMention,
  codeBlock,
} from "discord.js";
import { nClient } from "../index";
import config from "./../config.json";
import { start, stop, ping, keep } from "../api";
import { startInstance, stopInstance, getMetadata } from "../gcloud";

const rawData: SlashCommandBuilder = new SlashCommandBuilder();
module.exports = {
  data: new SlashCommandBuilder()
    .setName("server")
    .setDescription("Manage the server! (spend money)")
    .addSubcommand((subcommand) =>
      subcommand
        .setName("start")
        .setDescription("Start minecraft server and/or virtual machine")
        .addStringOption((option) =>
          option
            .setName("server")
            .setDescription("The Server to Start")
            .setRequired(true)
            .setAutocomplete(true)
        )
    )
    .addSubcommand((subcommand) =>
      subcommand
        .setName("stop")
        .setDescription("stop a server safely (no argument for all)")
        .addStringOption((option) =>
          option
            .setName("server")
            .setDescription("name of the server to stop")
            .setAutocomplete(true)
            .setRequired(false)
        )
    )
    .addSubcommand((subcommand) =>
      subcommand
        .setName("list")
        .setDescription(
          "List all available servers and update saved server list"
        )
    )
    .addSubcommandGroup((group) =>
      group
        .setName("manage")
        .setDescription("Other Management commands for the servers")
        .addSubcommand((subcommand) =>
          subcommand
            .setName("restore")
            .setDescription(
              "restore to a previous backup (effects ALL minecraft servers)"
            )
            .addStringOption((option) =>
              option
                .setName("backup")
                .setDescription(
                  "Name of previous backup (use /server backup list)"
                )
                .setRequired(true)
            )
        )
        .addSubcommand((subcommand) =>
          subcommand
            .setName("kill")
            .setDescription("Force Stop The Server (Be Careful)")
        )
        .addSubcommand((subcommand) =>
          subcommand
            .setName("keep")
            .setDescription(
              "keep the server online with no players for 15 minutes"
            )
        )
        .addSubcommand((subcommand) =>
          subcommand
            .setName("status")
            .setDescription("check status of Virtual Machine")
        )
    ),

  async execute(interaction: ChatInputCommandInteraction<CacheType>) {
    // interaction.user is the object representing the User who ran the command
    // interaction.member is the GuildMember object, which represents the user in the specific guild

    // let channel = await (await interaction.client.guilds.fetch("820062761409183794")).channels.fetch("1051201026910330943")
    // channel.setName('no')
    const cmd =
      interaction.options.getSubcommandGroup() +
      "." +
      interaction.options.getSubcommand();
    switch (cmd) {
      case "null.list": {
        await interaction.reply({
          embeds: [
            new EmbedBuilder()
              .setColor(0x226a98)
              .setTitle("Server List")
              .setDescription(config.serverList.join("\n"))
              .setFooter({
                text: "This must be updated manually (see config.json)",
              }),
          ],
        });
        break;
      }
      case "null.start": {
        console.log("start");

        const server = interaction.options.getString("server");
        if (server == null) throw new Error("no server found");
        if (!config.serverList.includes(server))
          throw new Error("server not found");

        await interaction.reply({
          embeds: [getEmbed("Checking Machine Status")],
        });
        const status = (await getMetadata()).status;
        if (!/^(TERMINATED|RUNNING)$/.test(status ? status : "")) {
          await interaction.editReply(
            "**ERROR:** virtual machine status code " +
              status +
              " Is not acceptable at this time."
          );
        }
        if (status === "TERMINATED") {
          await interaction.editReply({
            embeds: [getEmbed("Starting Virtual Machine")],
          });
          let i = 60;
          await startInstance();
          while (i > 0) {
            try {
              console.log("p-ing");

              await ping();
              break;
            } catch (error: any) {
              await wait(2000);
              i--;
            }
          }
          if (i <= 0) {
            interaction.editReply("ERROR Timeout");
          }
        }
        start(server).then((response) => {
          const value = response as string;
          const valueArray = value.split(":_:");
          if (valueArray[0] == "500") {
            interaction.editReply({
              embeds: [
                getEmbed(
                  codeBlock(valueArray[1]),
                  0xfe2c2d,
                  "https://cdn.discordapp.com/emojis/1061331357386678382.webp?size=96&quality=lossless",
                  "Startup Crash"
                ),
              ],
            });
          } else {
            interaction.editReply({
              embeds: [
                getEmbed(
                  "connect with " + inlineCode(config.domain),
                  0x16c60c,
                  "https://cdn.discordapp.com/emojis/1061328167844974692.webp?size=96&quality=lossless",
                  "Started Successfully",
                  { name: "Server", value: server }
                ),
              ],
            });
            interaction.followUp(roleMention(config.adminRole));
          }
        });
        interaction.editReply({
          embeds: [getEmbed("Starting Minecraft Server", 0x226a98)],
        });

        break;
      }
      case "null.stop": {
        //TODO: validate that server is online
        const server = interaction.options.getString("server");
        if (server == null) throw new Error("no server found");
        if (!config.serverList.includes(server) && !(server === "all"))
          throw new Error("server not found");
          await interaction.reply("Stopping...");
        stop(server).then((value) => {
          if (value == "200") {
            interaction.editReply("Stopped");
          } else {
            interaction.editReply("error (check vm console)");
          }
        });
        
        break;
      }
      case "manage.restore": {
        await interaction.reply(
          "Sorry, this command is not part of this minimum viable product release, contact " +
            userMention(config.ownerID) +
            " for manual backups and restores."
        );
        break;
      }
      case "manage.keep": {
        if (interaction.user.id == config.ownerID) {
          await keep();
          interaction.reply("keeping the server online");
        } else {
          interaction.reply(
            "This command is dangerous and has the potential to spend more money then necessary so it has been restricted to " +
              userMention(config.ownerID) +
              " ."
          );
        }
        break;
      }
      case "manage.kill": {
        interaction.reply("Forcefully closing the server ");
      }
      case "manage.status": {
        await interaction.reply("loading");
        const metadata = await getMetadata();
        if (metadata.status == null) throw new Error("error getting metadata");
        await interaction.editReply(metadata.status);
      }
      default:
        throw new Error(`command "${cmd}" not found`);
    }
  },
  async autocomplete(
    interaction: AutocompleteInteraction<CacheType> & { client: nClient }
  ) {
    const focusedValue = interaction.options.getFocused();

    const choices = config.serverList;
    if (interaction.options.getSubcommand() == "stop") {
      choices.push("all");
    }
    const filtered = choices.filter((choice) =>
      choice.startsWith(focusedValue)
    );
    await interaction.respond(
      filtered.map((choice) => ({ name: choice, value: choice }))
    );
  },
};

function getEmbed(
  description: string,
  color?: ColorResolvable,
  image?: string,
  title: string = "Loading...",
  fields?: APIEmbedField,
  footer: string = "Server Manager by garby.dev"
) {
  let embed = new EmbedBuilder();
  if (typeof color != "undefined") embed.setColor(color);
  if (typeof image != "undefined") embed.setThumbnail(image);
  if (typeof fields != "undefined") embed.addFields(fields);
  embed.setFooter({ text: footer }).setDescription(description).setTitle(title);
  return embed;
}
const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));
