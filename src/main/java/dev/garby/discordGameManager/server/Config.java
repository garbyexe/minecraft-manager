package dev.garby.discordGameManager.server;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Config {
    public List<Server> serverList;

    @Getter
    @Setter
    public static class Server {
        private String id;
        private String game;
        private String start_string;

    }
}
