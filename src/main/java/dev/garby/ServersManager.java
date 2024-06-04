package dev.garby;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class ServersManager {
    private static final Logger logger = LoggerFactory.getLogger(ServersManager.class);

    Map<String, McServer> servers = new HashMap<String, McServer>();
    String[] serverNames;

}
