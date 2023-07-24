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
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.exception.ConflictException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.exception.NotModifiedException;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PullResponseItem;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;


public class Container {
    private static final Logger logger = LoggerFactory.getLogger(Container.class);
    private final DockerClient client;
    private String containerId;

    public String getContainerId() {
        return containerId;
    }

    private boolean pullAttempted = false;


    Container(DockerClient client) {
        this.client = client;

    }

    Container(DockerClient client, String containerId) {
        this.client = client;
        this.containerId = containerId;

    }

    /**
     * Attach to container with stdin
     *
     * @param adapter callback when new frame is received
     * @return stream to send stdin
     */

    PipedOutputStream attachWithStdin(ResultCallback.Adapter<Frame> adapter) {
        logger.debug("Attaching to container {}", containerId);
        PipedOutputStream outputStream = new PipedOutputStream();
        try {
            InputStream stdin = new PipedInputStream(outputStream);

            client.attachContainerCmd(containerId)
                    .withStdOut(true)
                    .withStdErr(true)
                    .withFollowStream(true)
                    .withStdIn(stdin)
                    .exec(adapter);
        } catch (IOException e) {
            throw new RuntimeException("IO Error Occurred while attaching to container", e);

        }
        logger.debug("Attached to container {}", containerId);
        return outputStream;

    }

    /**
     * Attach to container read only
     *
     * @param adapter callback when new frame is received
     */
    ResultCallback.Adapter<Frame> attach(ResultCallback.Adapter<Frame> adapter) {
        return client.attachContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .withFollowStream(true)
                .exec(adapter);
    }

    /**
     * Create and start container
     *
     * @param image image to use, must include tag
     * @param name  name of container
     * @param ram   ram limit in bytes
     * @param binds binds to use
     * @throws IllegalArgumentException if ram is negative or zero
     */
    void createAndStart(@NotNull String image, @Nullable String name, @Nullable Long ram, @Nullable Bind... binds) {
        logger.debug("Creating container with image {}", image);

        if (ram != null && ram <= 0) throw new IllegalArgumentException("ram cannot be negative or zero");


        // set up config, ignore nulls

        HostConfig hostConfig = new HostConfig();
        CreateContainerCmd cmd = client.createContainerCmd(image);

        if (ram != null) hostConfig.withMemory(ram);
        if (name != null) cmd.withName(name);
        if (binds != null) hostConfig.withBinds(binds);

        cmd.withHostConfig(hostConfig
                        .withAutoRemove(true))
                .withStdinOpen(true);


        create(cmd);
        start();

    }

    /**
     * Create container
     *
     * @param createContainerCmd config to use to create container
     */
    private void create(@NotNull CreateContainerCmd createContainerCmd) {
        try {

            this.containerId = createContainerCmd.exec().getId();
            logger.debug("Created container {}", containerId);


        } catch (ConflictException e) {
            logger.warn("Container already exists, using existing container");
            this.containerId = createContainerCmd.getName();
        } catch (NotFoundException e) {
            if (pullAttempted) {
                logger.error("Image {} not found, but pull was already attempted.", createContainerCmd.getImage());
                throw e;
            } else {
                logger.warn("Image {} not found, attempting to pull", createContainerCmd.getImage());
                pullImage(createContainerCmd.getImage());
            }


        }
    }


    /**
     * Start container
     */
    private void start() {
        try {

            client.startContainerCmd(containerId).exec();
            logger.info("Started container {}", containerId);

        } catch (NotFoundException e) {
            logger.error("Container not found: {}", containerId);
            containerId = null;
            throw e;
        } catch (NotModifiedException e) {
            logger.warn("Container already started: {}", containerId);
        }
    }

    /**
     * Pull image
     *
     * @param image image id to pull, must include tag
     */
    private void pullImage(String image) {
        pullAttempted = true;
        try {
            client.pullImageCmd(image).exec(new ResultCallback.Adapter<PullResponseItem>() {
                @Override
                public void onStart(Closeable closeable) {
                    logger.info("Pulling image: {}...", image);
                }

                @Override
                public void onNext(PullResponseItem item) {
                    logger.info("{}: {}", item.getStatus(), item.getId());
                }

                @Override
                public void onComplete() {
                    logger.info("Successfully Pulled image: {}", image);
                }

                @Override
                public void onError(Throwable throwable) {
                    logger.error("Failed to pull image: {}", image);
                    throw new RuntimeException("Failed to pull image", throwable);
                }

            }).awaitCompletion();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Stop container
     */
    void stop() {
        try {

            client.stopContainerCmd(containerId).exec();
            logger.info("Stopped container {}", containerId);
            containerId = null;

        } catch (NotFoundException e) {
            logger.error("Container not found: {}", containerId);
            throw e;

        }
    }


}
