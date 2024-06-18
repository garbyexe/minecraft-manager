package dev.garby.discordGameManager.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Api implements Closeable, AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(Api.class);

    Map<String, McServer> servers = new HashMap<String, McServer>();
    String[] serverNames;

    File serversDirectory;

    public Api(File serversDirectory, String... serverNames) {
        this.serversDirectory = serversDirectory;
        this.serverNames = serverNames;
    }

    /**
     * Starts the API
     */
    public void init() {
        logger.debug("Starting API");
        Spark.before((req, res) -> {
            String server = req.params("server");
            logger.debug("{} {}", req.requestMethod(), req.pathInfo());
            if (!serverIsValid(server)) {
                Spark.halt(404, "Server Not Found");
            }

            if (!req.pathInfo().contains("start")) {
                if (!serverProcessExists(server)) {
                    Spark.halt(400, "Server is not started");
                }
            }


        });

        Spark.post("/api/start/:server", (req, res) -> {
            String server = req.params("server");

            McServer mcServer = new McServer(server, new File(serversDirectory + "/" + server));
            mcServer.createProcess();


            McServer.Status resultStatus = mcServer.awaitStart();
            if (resultStatus == McServer.Status.SUCCESS) {
                servers.put(server, mcServer);
                return "Server started";
            } else {
                return Spark.halt(500, "Server failed to start");
            }

        });
        Spark.delete("/api/stop/:server", (req, res) -> {
            String server = req.params("server");
            McServer mcServer = servers.get(server);
            if (mcServer.stop().equals(McServer.Status.SUCCESS)) {
                servers.remove(server);
                return "Server stopped";
            } else {
                return Spark.halt(500, "Server failed to stop");
            }

        });
        Spark.post("/api/command:server", (req, res) -> {
            String server = req.params("server");
            McServer mcServer = servers.get(server);
            String command = req.queryParams("command");
            if(mcServer.runCommand(command).equals(McServer.Status.SUCCESS)) {
                return "Command sent";
            } else {
                return Spark.halt(500, "Command failed");
            }
        });
        Spark.get("/api/ping", (req, res) -> "pong");

    }
    public boolean serverIsValid(String server) {
        return Arrays.asList(serverNames).contains(server);
    }
    public boolean serverProcessExists(String server) {
        return servers.containsKey(server);
    }

    @Override
    public void close() throws IOException {
        Spark.stop();

    }
}
