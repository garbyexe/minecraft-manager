
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


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.AttachContainerCmd;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

public class Docker {
    private static final Logger logger = LoggerFactory.getLogger(Docker.class);

    DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder().dockerHost(new URI("unix:///var/run/docker.sock")).build();
    DockerClient dockerClient = DockerClientImpl.getInstance(DefaultDockerClientConfig.createDefaultConfigBuilder().build(), httpClient);
    CountDownLatch latch = new CountDownLatch(1);

    public Docker() throws URISyntaxException {
    }


    public void test() throws InterruptedException {
        //start a container
        // docker run --name=dev --rm -idv /home/michael/rebuild/test_survival/:/opt/minecraft dev
        //limit ram

        try {
            String containerId = dockerClient.createContainerCmd("eclipse-temurin:17-jre-alpine")
                    .withHostConfig(new HostConfig().
                            withAutoRemove(true)
                            .withBinds(new Bind("/home/michael/rebuild/test_survival/", new Volume("/opt/minecraft")))
                    )
                    .withStdinOpen(true)
                    .exec().getId();
            dockerClient.startContainerCmd(containerId).exec();
        } catch (NotFoundException e) {
            logger.info("Image not found, pulling");
            dockerClient.pullImageCmd("thisdoesnexistagdfghadhgfkjkajdfkjhg").exec(new ResultCallback<PullResponseItem>() {
                @Override
                public void onStart(Closeable closeable) {
                    logger.info("Pulling image - start");

                }

                @Override
                public void onNext(PullResponseItem object) {
                    logger.info(object.toString());
                }


                @Override
                public void onError(Throwable throwable) {
                    logger.error("Pulling image - error", throwable);

                }

                @Override
                public void onComplete() {
                    logger.info("Pulling image - complete");

                }

                @Override
                public void close() {

                }
            })
            ;
        }




        latch.await();
    }

    public void attach() throws InterruptedException, IOException {

        PipedOutputStream outputStream = new PipedOutputStream();
        InputStream inputStream = new PipedInputStream(outputStream);

        AttachContainerCmd attachCmd = dockerClient.attachContainerCmd("dev");

        attachCmd.withStdOut(true).withStdErr(true).withFollowStream(true).withLogs(false).withStdIn(inputStream);

        attachCmd.exec(new ResultCallback.Adapter<>() {
            @Override
            public void onNext(Frame frame) {

                logger.info(new String(frame.getPayload(), StandardCharsets.UTF_8).trim());
//                logger.debug("stdout",new Exception());
//                logger.debug(Hex.encodeHexString(frame.getPayload()));

                if (frame.toString().equals("DONE")) {
                    latch.countDown();
                }

            }


        });
        Thread.sleep(5000);
        logger.debug("sending stop");
        outputStream.write("stop\n".getBytes());
        logger.debug("stop sent");
        latch.await();

    }


}
