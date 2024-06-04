package dev.garby;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;


public class Main {
    static Logger logger = LoggerFactory.getLogger(Main.class);
    protected static String CONFIG_URL;
    protected static String[] SERVER_LIST;
    protected static File WORKING_DIRECTORY = new File(System.getProperty("user.dir"));

    public static void main(String... args) {

        Main main = new Main();
        logger.debug(Arrays.toString(args));
        if (Arrays.toString(args).contains("--extract")) {
            logger.info("Extracting resources");
            main.extractResources();
        }

        logger.info("Loading config");

        main.getConfigPath();
        main.readConfig();


        logger.debug("Startup");
        try (Api api = new Api(new File(System.getProperty("user.dir")), SERVER_LIST)) {
            api.init();
        } catch (IOException e) {
            logger.error("Failed to start API", e);
        }

    }

    @SuppressWarnings("ConstantConditions")
    private void extractResources() {
        try {

            for (String resource : Path.of(Main.class.getResource("/resources").toURI()).toFile().list()) {
                extractResource(resource);
            }

            extractResource("");
        } catch (IOException | URISyntaxException e) {
            logger.error("Failed to extract", e);
            System.exit(1);
        }
        System.exit(0);
    }

    private void getConfigPath() {
        logger.debug("Getting config file");
        CONFIG_URL = System.getProperty("mcManager.configFile");
        if (CONFIG_URL == null) {
            CONFIG_URL = System.getenv("MCMANAGER_CONFIG_FILE");
        }
        if (CONFIG_URL == null) {
            CONFIG_URL = "McManager.properties";
        }
        logger.debug("Using config file {}", CONFIG_URL);
    }

    private void readConfig() {
        Path configFile = Path.of(CONFIG_URL);

        try (InputStream input = Files.newInputStream(configFile)) {
            Properties prop = new Properties();
            // load a properties file
            prop.load(input);
            // get the property value
            SERVER_LIST = prop.get("servers").toString().split(",");

            logger.debug("Loaded servers {}", Arrays.toString(SERVER_LIST));


        } catch (NoSuchFileException | NullPointerException e) {
            if (e instanceof NullPointerException) {
                logger.error("Config file is missing required properties");
                logger.info("making backup of old config");
                backupConfig();
            } else {
                logger.error("Config file not found");
            }
            logger.info("Creating default config");
            createDefaultConfig();
            System.exit(1);

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void backupConfig() {
        try {
            Files.move(Path.of(CONFIG_URL), Path.of(CONFIG_URL + ".old"));
        } catch (IOException e) {
            logger.error("Failed to make backup of old config");
            throw new UncheckedIOException(e);
        }
    }

    private void createDefaultConfig() {
        try {
            extractResource("McManager.properties");
        } catch (IOException e) {
            logger.error("Failed to create default config");
            throw new UncheckedIOException(e);
        } catch (URISyntaxException | NullPointerException e) {
            logger.error("Failed to create default config");
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void extractResource(String resource) throws IOException, URISyntaxException, NullPointerException {
        Files.copy(Main.class.getResourceAsStream("/resources/" + resource), Path.of(WORKING_DIRECTORY + "/" + resource));
    }


}