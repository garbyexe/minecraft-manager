package dev.garby.discordGameManager.server;


import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.constructor.ConstructorException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public enum ConfigManager {
    INSTANCE;
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    @Getter
    @Setter
    private Path configFile;
    @Getter
    private static Config config;

    public void locateConfigFile() {
        logger.debug("Locating config file");
        String configUrl = System.getProperty("mcManager.configFile");
        if (configUrl == null) {
            configUrl = System.getenv("MCMANAGER_CONFIG_FILE");
        }
        if (configUrl == null) {
            configUrl = "config.yaml";
        }
        logger.debug("Using config file {}", configUrl);
        setConfigFile(Path.of(configUrl));

    }

    public void readOrCreate() {
        if (configFile == null) {
            locateConfigFile();
        }
        if (!Files.exists(configFile)) {
            logger.debug("file does not exist");
            if (Files.isDirectory(configFile)) {
                logger.error("Config file at '{}' is a folder. What are you Doing?", configFile.toAbsolutePath());
                System.exit(1);
            }
            createDefaultConfig();
        }
        readConfig();
    }

    public void createDefaultConfig() {
        try {
            Files.copy(
                    Objects.requireNonNull(ConfigManager.class.getResourceAsStream("/extracted/config.yaml")),
                    getConfigFile()
            );

        } catch (Exception e) {
            logger.error("Failed to extract default config");
            System.exit(1);
        }

    }

    private void readConfig() {
        Yaml yaml = new Yaml(new Constructor(Config.class, new LoaderOptions()));
        try (InputStream inputStream = Files.newInputStream(configFile)) {
            Config rawConfig = yaml.load(inputStream);
            if (isConfigValid(rawConfig)) {
                ConfigManager.config = rawConfig;
            } else {
                logger.info("Exiting due to configuration errors");
                System.exit(1);
            }
        } catch (ConstructorException e) {
            logger.warn("Config syntax Error: extra fields found");
            backupConfig();
            createDefaultConfig();

        } catch (Exception e) {
            logger.error("Failed to load config", e);
        }
    }

    private void backupConfig() {
        try {
            logger.info("Backing up old config...");
            Files.move(configFile, configFile.resolveSibling(configFile.getFileName() + ".old"));
        } catch (IOException e) {
            logger.error("Failed to make backup of old config");
            System.exit(1);
        }
    }

    public boolean isConfigValid(Config config) {
        boolean validConfig = true;
        if (config == null) {
            logger.error("Config syntax Error: Config must contain a server list");
            validConfig = false;
        }
        for (Config.Server server : config.getServerList()) {
            if (server.getId() == null) {
                logger.error("Config syntax Error: Every game must have a valid id");
                validConfig = false;
            }
            if (server.getGame() == null && server.getStart_string() == null) {
                logger.error("Config syntax Error: Game Entry '{}' must have either a game or a start string", server.getId());
                validConfig = false;
            }
        }
        return validConfig;
    }

    public static void main(String[] args) {
        ConfigManager.INSTANCE.readOrCreate();
        System.exit(0);
    }


}
