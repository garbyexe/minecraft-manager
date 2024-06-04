package dev.garby;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    static Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static Path configFile;
    private static ConfigManager instance = null;
    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }
    public static void setConfigFile(Path configFile) {
        ConfigManager.configFile = configFile;
    }
    private ConfigManager() {
        LoadSettings settings = LoadSettings.builder().setSchema(Server).build();
        Load load = new Load(settings);
        try (InputStream inputStream = Files.newInputStream(configFile)) {
            Config config = (Config) load.loadFromInputStream(inputStream);
            logger.info(config.Server_List.toString());
        } catch (Exception e) {
            logger.error("Failed to load config", e);
            throw new RuntimeException(e);
        }

    }
    private static class Config {
        private List<Server> Server_List;

    }
    private static class Server {
        private String id;
        private String game;
        private String start_string;

    }
    public static void main(String[] args) {
        ConfigManager.setConfigFile(Path.of("/home/michael/Documents/github/mcmanager-dirty/src/main/resources/resources/McManager.yaml"));
        ConfigManager.getInstance();
    }
}
