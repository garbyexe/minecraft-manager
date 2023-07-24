/*
 * Copyright (c) 2023 GarbyEXE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package dev.garby.mcserver;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.event.EventHandler;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Mcserver extends Plugin implements Listener {
    private static final long STOP_MINUTES = 5;
    private final long STARTUP_STOP_DELAY = 5;
    //on server start, add these 2 to get time up before stop.
    //on player leave, use STOP_MINUTES time before stop
    final static String[] SHUTDOWN_COMMAND = new String[]{"shutdown", "+1"};
    static Api api = new Api();


    /**
     * Log a message to the bungee-cord console
     *
     * @param message Message to send
     */
    public static void log(String message) {
        ProxyServer.getInstance().getPluginManager().getPlugin("GarbyServer").getLogger().info(message);
    }


    @Override
    public void onEnable() {
        if (Pattern.compile(Pattern.quote("windows"), Pattern.CASE_INSENSITIVE).matcher(System.getProperty("os.name")).find()) {

            throw new RuntimeException("This plugin doesnt work on windows");
        }
        //register leave event
        getProxy().getPluginManager().registerListener(this, this);

        try {
            // load bungee cord config to get server list
            Configuration configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File("./config.yml"));
            String[] servers = configuration.getSection("servers").getKeys().toArray(new String[0]);

            //start the Api on a new thread
            getProxy().getScheduler().runAsync(this, () -> {

                api.start(servers);
                getLogger().info("API Loaded");

            });

        } catch (IOException e) {
            getLogger().severe("Could not start API");
            throw new RuntimeException(e);

        }
        getProxy().getScheduler().schedule(this, () -> startDelay(), STARTUP_STOP_DELAY, TimeUnit.MINUTES);

    }


    @Override
    public void onDisable() {
        //stop all active minecraft servers
        if (!api.processes.isEmpty()) {
            try {
                api.softStop("all");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        getLogger().info("Stopped");
    }

    @EventHandler
    public void onPlayerDisconnectEvent(PlayerDisconnectEvent event) {

        if (ProxyServer.getInstance().getPlayers().size() - 1 == 0) {
            getLogger().info("No players online, starting stop timer");
            startDelay();
        }

    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        stopDelay();
    }

    private static int delayId = 0;

    protected static void startDelay() {
        if (delayId != 0) return;
        if (ProxyServer.getInstance().getPlayers().size() != 0) return;
        delayId = ProxyServer.getInstance().getScheduler().schedule(ProxyServer.getInstance().getPluginManager().getPlugin("GarbyServer"), () -> {
            try {
                shutdown();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, STOP_MINUTES, TimeUnit.MINUTES).getId();
        log("Delay Started. Delay id:" + delayId);
    }

    protected static void stopDelay() {

        if (delayId == 0) return;
        ProxyServer.getInstance().getScheduler().cancel(delayId);
        delayId = 0;
        log("Delay Stopped");
    }

    protected static void shutdown() throws IOException, InterruptedException {
        if (ProxyServer.getInstance().getPlayers().size() == 0) {
            try {
                new ProcessBuilder(SHUTDOWN_COMMAND).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            api.softStop("all");
            ProxyServer.getInstance().stop("[GarbyServer] Closing server due to lack of player");
        }


    }


}
