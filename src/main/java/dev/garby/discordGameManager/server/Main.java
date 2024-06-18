package dev.garby.discordGameManager.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;


public class Main {
    static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) {
        logger.debug(Arrays.toString(args));

        logger.info("Loading config...");
        ConfigManager.INSTANCE.readOrCreate();

        logger.debug("Startup");
        try (Api api = new Api(new File(System.getProperty("user.dir")))) {
            api.init();
        } catch (IOException e) {
            logger.error("Failed to start API", e);
            System.exit(1);
        }

    }



}