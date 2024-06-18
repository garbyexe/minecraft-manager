package dev.garby.discordGameManager.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Scanner;

import static dev.garby.discordGameManager.server.Constants.*;

public class McServer {
    private static final Logger logger = LoggerFactory.getLogger(McServer.class);

    String name;
    File path;
    Process process;

    ServerState currentState = ServerState.UNKNOWN;

    public String getName() {
        return name;
    }

    public File getPath() {
        return path;
    }

    public Process getProcess() {
        return process;
    }

    public ServerState getCurrentState() {
        return currentState;
    }

    public void setCurrentState(ServerState currentState) {
        this.currentState = currentState;
    }


    public McServer(String name, File path) {
        this.name = name;
        this.path = path;
    }

    void createProcess() throws IOException {
        ProcessBuilder builder = new ProcessBuilder(EXECUTABLE_RUNNER, "./" + EXECUTABLE);

        builder.directory(path);
        process = builder.start();
    }

    Status awaitStart() throws IOException {
        if (!this.isDead() || this.currentState == ServerState.STARTED || this.currentState == ServerState.BOOTING) {
            logger.error("Trying to start Server {}, but is already started or starting", name);
            return Status.FAILED;
        }

        try (InputStream stdout = process.getInputStream()) {

            ServerState StartupEndState = ServerState.UNKNOWN;

            String line;

            try (Scanner scanner = new Scanner(stdout)) {
                while (scanner.hasNextLine()) {
                    line = scanner.nextLine();
                    logger.info("Server Console: {}", line);

                    ServerState lineAction = checkLineAction(line);

                    if (lineAction == ServerState.BOOTING) continue;

                    // server must have started
                    StartupEndState = lineAction;
                    break;
                }
            }
            // the server must have started or crashed to get here

            if (StartupEndState == ServerState.STARTED) {
                logger.info("Server {} started!", name);
                this.currentState = ServerState.STARTED;
                return Status.SUCCESS;
            }
            this.currentState = ServerState.CRASHED;

            if (StartupEndState == ServerState.UNKNOWN) {
                // the Scanner didn't find any lines
                logger.error("Unable to get lines from minecraft server {}. Does the startup script work?", name);
                return Status.FAILED;
            } else if (StartupEndState == ServerState.BOOTING) {
                logger.error("Server {} crashed!, The logs might be useful", name);
                return Status.FAILED;
            } else {
                logger.error("Server {} crashed!, The logs might be useful. This could be a problem with the minecraft server or the server manager", name);
                return Status.FAILED;
            }


        }

    }

    public Status stop() throws IOException, InterruptedException {
        if (this.isDead()) return Status.FAILED;
        try (InputStream stdout = process.getInputStream()) {
            process.destroy();
            logger.info("Server {} stopping", name);
            String line;
            try (Scanner scanner = new Scanner(stdout)) {
                while (scanner.hasNextLine()) {
                    line = scanner.nextLine();
                    logger.info("Server Console: {}", line);
                }
                if (process.waitFor(30, java.util.concurrent.TimeUnit.SECONDS)) {
                    logger.info("Server {} stopped", name);
                    this.currentState = ServerState.STOPPED;
                    return Status.SUCCESS;
                }
                logger.error("Server {} failed to stop", name);
                this.currentState = ServerState.STOPPED;
                return Status.FAILED;

            }


        }
    }

    public Status kill() throws InterruptedException {
        process.destroyForcibly();
        process.waitFor();
        this.currentState = ServerState.STOPPED;
        return Status.SUCCESS;
    }

    public Status runCommand(String command) throws IOException {
        if (this.isDead()) return Status.FAILED;

        try (
                OutputStream stdin = process.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin))
        ) {
            logger.info("Running command '{}' on server {}", name, command);
            writer.write(command);
            writer.flush();
            return Status.SUCCESS;

        }
    }

    /**
     * Checks if the server is no longer alive
     *
     * @return true if the server is dead
     */
    public boolean isDead() {
        boolean isDead = process == null || !process.isAlive();
        if (isDead) {
            logger.warn("Server {}'s process is dead! Server must have stopped", name);
            logger.debug("Removing server {} from list", name);
            this.currentState = ServerState.STOPPED;
            process = null;
        } else {
            logger.debug("Server {} is alive", name);
        }
        return isDead;
    }

    /**
     * Checks if a line shows that the server has started or crashed or is still starting
     *
     * @param line Line to check
     * @return LineAction, action to take
     */
    private ServerState checkLineAction(String line) {
        if (line.contains(STARTUP_COMPLETE_CHECK)) {
            logger.info("Server {} started!", name);
            return ServerState.STARTED;
        } else {
            return ServerState.BOOTING;
        }
    }


    /**
     * Possible states of a server
     */
    public enum ServerState {
        UNKNOWN,
        BOOTING,
        STARTED,
        CRASHED,
        STOPPED
    }

    /**
     * The Returned Status of an action
     */
    public enum Status {
        UNKNOWN,
        SUCCESS,
        FAILED
    }


}
