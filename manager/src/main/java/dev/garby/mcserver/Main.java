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

import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    //init
    //pull container
    // create network
    //start container

    public static void main(String[] args) throws URISyntaxException, InterruptedException, IOException {
        logger.info("Starting...");
        logger.debug("Debugging Enabled");
        Docker docker = new Docker();
        docker.test();
//        InputStream i = System.in;
//
//        while (true) {
//            logger.debug(String.valueOf(i.read()));
//
//        }



        // get server listx
        // start api & start waterfall ( network)
        // send ready request - include serverlist

        // returns command & server
        // start dockerManager container
        //listen for bungee user

        //auth with sign

    }

}
