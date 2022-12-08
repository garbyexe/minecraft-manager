package dev.garby.botserver;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private static final Logger logger
            = LoggerFactory.getLogger(MainTest.class);

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Owner ID")
    void getConfig() {
        Main.PATH = "C:\\Users\\garby\\Desktop\\config.properties";
        Main.getConfig();
        assertEquals("458020695952326677", Main.OWNER_ID);
    }
}