package dev.garby.botserver;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.compute.v1.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.Error;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class GCloud {
    private static final Logger logger
            = LoggerFactory.getLogger(GCloud.class);
    public static void startInstance()
            throws IOException, ExecutionException, InterruptedException, TimeoutException {
        if (Main.local) {
            logger.info("local mode - emulating instance START");
            return;
        }

    /* Initialize client that will be used to send requests. This client only needs to be created
       once, and can be reused for multiple requests. After completing all of your requests, call
       the `instancesClient.close()` method on the client to safely
       clean up any remaining background resources. */
        try (InstancesClient instancesClient = InstancesClient.create()) {

            // Get the latest information about the instance.
            GetInstanceRequest getInstanceRequest = com.google.cloud.compute.v1.GetInstanceRequest.newBuilder()
                    .setProject(Main.GCLOUD_PROJECT)
                    .setZone(Main.GCLOUD_ZONE)
                    .setInstance(Main.GCLOUD_INSTANCE)
                    .build();
            Instance instance = instancesClient.get(getInstanceRequest);
            String status = instance.getStatus();
            logger.info("Instance status: " + status);

            if (status.equals("RUNNING")) {
                return;
            } else if (status.equals("TERMINATED")) {
                // Create the request.
                StartInstanceRequest startInstanceRequest = StartInstanceRequest.newBuilder()
                        .setProject(Main.GCLOUD_PROJECT)
                        .setZone(Main.GCLOUD_ZONE)
                        .setInstance(Main.GCLOUD_INSTANCE)
                        .build();

                OperationFuture<Operation, Operation> operation = instancesClient.startAsync(
                        startInstanceRequest);
                logger.info("Starting instance...");
                Operation response = operation.get(3, TimeUnit.MINUTES);

                if (response.getStatus() == Operation.Status.DONE) {
                    logger.info("Instance started successfully!");
                    return;
                } else
                    logger.error("Instance failed to start!");
            }




        }
        throw new Error("Problem starting instance. wrong status?");
    }
    public static void stopInstance()
            throws IOException, ExecutionException, InterruptedException, TimeoutException {
        if (Main.local) {
            logger.info("local mode - emulating instance STOP");
            return;
        }
    /* Initialize client that will be used to send requests. This client only needs to be created
       once, and can be reused for multiple requests. After completing all of your requests, call
       the `instancesClient.close()` method on the client to safely
       clean up any remaining background resources. */
        try (InstancesClient instancesClient = InstancesClient.create()) {

            StopInstanceRequest stopInstanceRequest = StopInstanceRequest.newBuilder()
                    .setProject(Main.GCLOUD_PROJECT)
                    .setZone(Main.GCLOUD_ZONE)
                    .setInstance(Main.GCLOUD_INSTANCE)
                    .build();

            OperationFuture<Operation, Operation> operation = instancesClient.stopAsync(
                    stopInstanceRequest);
            Operation response = operation.get(3, TimeUnit.MINUTES);

            if (response.getStatus() == Operation.Status.DONE) {
                logger.info("Instance stopped successfully! ");
            } else
                logger.error("Instance failed to stop!");
        }
    }


    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException, TimeoutException {
        Main.GCLOUD_INSTANCE = "test-server";
        Main.GCLOUD_ZONE = "us-central1-a";
        Main.GCLOUD_PROJECT = "garbysites";
        Main.local = false;

    }

}
