/*
 *    Copyright 2023 Garby
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package dev.garby.mcserver;

import net.md_5.bungee.api.ProxyServer;
import spark.Spark;

import java.io.*;
import java.util.*;

public class Api {

    public Map<String, Process> processes = new HashMap<String, Process>();


    /**
     * Starts the API
     *
     * @param servers List of available servers
     */
    public void start(String[] servers) {


        Spark.post("/api/start/:server", (req, res) -> {

            String server = req.params("server");

            if (!Arrays.asList(servers).contains(server)) {
                return Spark.halt(400, "Server Not Found: " + Arrays.toString(servers) + " NOT " + server);

            }
            if (processes.containsKey(server)) {
                return Spark.halt(400, "Server is already started or starting");
            }
            String dataFolder = ProxyServer.getInstance().getPluginManager().getPlugin("GarbyServer").getDataFolder().getAbsolutePath();

            // path to folder with instance to start
            String startPath = dataFolder + "/" + server;
            // command to run
            String[] startCMD = new String[]{"bash", startPath + "/start.sh"};

            // start the server
            Mcserver.log(String.format("______Starting %s______", server));
            ProcessBuilder builder = new ProcessBuilder(startCMD);
            Process process = builder.start();



            // get output of command
            InputStream stdout = process.getInputStream();
            Scanner scanner = new Scanner(stdout);

            List<String> lines = new ArrayList<>();
            // check for completion / error, log lines
            while (scanner.hasNextLine()) {

                String line = scanner.nextLine();
                Mcserver.log(line);
                lines.add(line);
                // success
                if (line.contains("Timings Reset")) {
                    Mcserver.log(String.format("______DONE: Server %s Started______", server));
                    scanner.close();

                    processes.put(server, process);

                    Mcserver.log("Server started:"+ (processes.get(server) == null));
                    return Spark.halt(200, String.join("<br>", lines));
                    // error
                } else if (line.contains("ERROR") || line.contains("Failed")) {
                    process.destroyForcibly();
                    break;
                }

            }
            process.destroyForcibly();
            scanner.close();
            return Spark.halt(500, String.join("<br>", lines));
        });


        Spark.delete("/api/stop/:server", (req, res) -> {
            //TODO: make this work
            String server = req.params("server");
            if (server.equalsIgnoreCase("all")) {
                if (processes.isEmpty()) {
                    Mcserver.shutdown();
                    return Spark.halt(200,"Server Closed");
                }

                processes.forEach((k, v) -> {
                    try {
                        int status = softStop(k);
                        if (status != 200) Spark.halt(status);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });

            } else if (!processes.containsKey(server)) {
                Spark.halt(400, "Server Not Started");
            } else {
                if(softStop(server)!=200 ) Spark.halt(500,"Soft Stop Failed"); else return Spark.halt(200,"Server Stopped");
            }
            if (processes.isEmpty()) {
                Mcserver.shutdown();

            }
            Mcserver.log("Error stopping servers");

            return Spark.halt(500);


        });
        Spark.post("/api/keep", (req, res) -> {
            Mcserver.log("-------Keeping server online-------");
            Mcserver.stopDelay();
            Mcserver.startDelay();
            return 200;

        });

        Spark.get("/api/ping", (req, res) -> "pong");

//        Spark.get("/api/status",(req,res) -> status() );


    }

    /**
     * stop a server
     *
     * @param server The server to stop
     * @return A Made up a http status code
     * @throws InterruptedException when waiting for server to close is interrupted
     */
    protected int softStop(String server) throws InterruptedException {
        //TODO: make this work
        Mcserver.log(String.format("Stopping Server %s", server));

        Process process = processes.get(server);
        if (process == null) {
            Mcserver.log(String.format("ERROR: Server %s Is Null", server));
            return 400;
        }
        if (!process.isAlive()) {
            Mcserver.log(String.format("ERROR: Server %s Is Dead", server));
            return 400;
        }
        process.destroy();
        process.waitFor();
        if (!process.isAlive()) {
            Mcserver.log(String.format("DONE: Server %s Stopped", server));
            processes.remove(server);
            return 200;
        } else {
            //This should never happen
            Mcserver.log(String.format("ERROR: Server %s Failed to Stop, killing", server));
            process.destroyForcibly();
            processes.remove(server);
            return 500;
        }
        //sadly no logs any more from the shutdown sequence
    }
//
//    /**
//     * Removes the process if its no longer alive
//     *
//     * @param server  server to remove
//     * @param process server to remove
//     * @param kill    if the process is alive, kill it first or return 0
//     * @return 200 on completion, else 0
//     */
//    private int removeIfDead(String server, Process process, Boolean kill) {
//
//        if (process != null && process.isAlive()) {
//            if (kill) {
//                Mcserver.log("Force closing process");
//                process.destroy();
//            } else {
//                return 0;
//            }
//        }
//        Mcserver.log(String.format("______DONE: Stopped %s______", server));
//
//        processes.remove(server);
//        return 200;
//    }
}
