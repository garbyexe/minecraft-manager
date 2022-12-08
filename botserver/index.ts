// Require the necessary discord.js classes
import * as fs from "node:fs";
import * as path from "node:path";
import {
  Client,
  Events,
  GatewayIntentBits,
  SlashCommandBuilder,
  Collection,
  BaseInteraction,
  Interaction,
  CacheType,
} from "discord.js";
import config from "./config.json";

export class nClient extends Client {
  commands?: Collection<unknown, any>;
}

// Create a new client instance
const client: nClient = new Client({ intents: [GatewayIntentBits.Guilds], allowedMentions: {repliedUser: true, roles: [config.adminRole]} });

client.commands = new Collection();

const commandsPath = path.join(__dirname, "commands");
const commandFiles = fs
  .readdirSync(commandsPath)
  .filter((file) => file.endsWith(".ts"));

for (const file of commandFiles) {
  const filePath = path.join(commandsPath, file);
  const command = require(filePath);
  // Set a new item in the Collection with the key as the command name and the value as the exported module
  if ("data" in command && "execute" in command) {
    client.commands.set(command.data.name, command);
  } else {
    console.log(
      `[WARNING] The command at ${filePath} is missing a required "data" or "execute" property.`
    );
  }
}
client.on(
  Events.InteractionCreate,
  async (interaction: Interaction<CacheType> & { client: nClient }) => {
    const autocomplete = interaction.isAutocomplete();

    if (autocomplete || interaction.isChatInputCommand()) {
      const command = interaction.client.commands!.get(interaction.commandName);

      if (!command) {
        console.error(
          `No command matching ${interaction.commandName} was found.`
        );
        return;
      }
      try {
        autocomplete
          ? await command.autocomplete(interaction)
          : await command.execute(interaction);
      } catch (error: any) {
        console.error(error);
        if (!autocomplete)
          await interaction.reply({
            content: `There was an error while executing this command! Error message: \`${error.message}\``,
            ephemeral: true,
          });
      }
    }
  }
);
// When the client is ready, run this code (only once)
// We use 'c' for the event parameter to keep it separate from the already defined 'client'
client.once(Events.ClientReady, (c) => {
  // TODO: get metadata
  // TODO:
  console.log(`Ready! Logged in as ${c.user.tag}`);
});

// Log in to Discord with your client's token
client.login(config.token);
