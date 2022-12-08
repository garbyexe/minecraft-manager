// TODO: get metadata
// setup api
// get metadata

// import compute = require('@google-cloud/compute')

import compute from "@google-cloud/compute";

const projectId = "garbysites";
const zone = "us-central1-a";
const instanceName = "test-server";

export async function getMetadata() {
  console.log("getMetadata");
  return { status: "TERMINATED" };
}
//   export async function getMetadata() {
//   const instancesClient = new compute.InstancesClient();

//   const [instance] = await instancesClient.get({
//     project: projectId,
//     zone,
//     instance: instanceName,
//   });
//   return instance;
// }

// console.log((await getMetadata()))
export async function startInstance() {
  console.log("startInstance");
}

// export async function startInstance() {
//   const instancesClient = new compute.InstancesClient();

//   const [response] = await instancesClient.start({
//     project: projectId,
//     zone,
//     instance: instanceName,
//   });
//   let operation = response.latestResponse;
//   const operationsClient = new compute.ZoneOperationsClient();

//   // Wait for the operation to complete.
//   while (operation.done !== true) {
//     await operationsClient.wait({
//       operation: operation.name,
//       project: projectId,
//       zone: zone,
//     });
//   }

//   console.log("Instance started.");
// }

// startInstance().then((data) => console.log(data))

export async function stopInstance() {
  console.log("stopInstance");
}
// export async function stopInstance() {
//   const instancesClient = new compute.InstancesClient();

//   const [response] = await instancesClient.stop({
//     project: projectId,
//     zone,
//     instance: instanceName,
//   });
//   let operation = response.latestResponse;
//   const operationsClient = new compute.ZoneOperationsClient();

//   while (operation.done !== true) {
//     await operationsClient.wait({
//       operation: operation.name,
//       project: projectId,
//       zone: zone,
//     });
//   }
// }

// stopInstance().then((data) => console.log(data));
