package dev.garby.discordGameManager.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.io.File;

public class test {
    @Test
    public void test() {
        File file = new File(new File(System.getProperty("user.dir")).getAbsolutePath()+ "/extracted/config.properties");
        File file2 = new File(new File(System.getProperty("user.dir"))+ "/extracted/config.properties");

        Assertions.assertEquals(new File("extracted/config.properties").getAbsolutePath(), file2.getAbsolutePath());
    }
}
