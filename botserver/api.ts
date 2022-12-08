import * as http from "node:http";

import config from "./config.json";

// async function start(server: String) {
//     let options: http.RequestOptions = {
//         host: HOST,
//         path: "/api/start/" + server,
//         port: 4567,
//     };
//     let callback = function (response:any) {};
//     let req = http.request(options, callback);
//     req.write("");
//     req.end();
// }
export function ping() {
  let options: http.RequestOptions = {
    host: config.host,
    path: "/api/ping",
    port: 4567,
    method: "GET",
  };
  let req = http.request(options);
  req.write("");
  req.end();

  return new Promise((resolve, reject) => {
    req.on("response", (res) => {
      let data = "";
      res.on("data", (chunk) => {
        data += chunk;
      });
      res.on("end", () => {
        resolve(data);
      });
    });
    req.on("error", () => {
      reject("Connection Refused");
    });
  });
}

export function start(server: String) {
  console.log("start");
  let options: http.RequestOptions = {
    host: config.host,
    path: "/api/start/" + server,
    port: 4567,
    method: "POST",
  };
  let req = http.request(options);
  req.write("");
  req.end();
  return new Promise((resolve, reject) => {
    req.on("response", (res) => {
      if (res.statusCode !=200 ) Promise.reject()
      let data = "";
      res.on("data", (chunk) => {
        data += chunk;
      });
      res.on("end", () => {
        resolve(res.statusCode + ":_:" + data);
      });
    });
  });
}
export function stop(server: String) {
  console.log("stop");
  let options: http.RequestOptions = {
    host: config.host,
    path: "/api/stop/" + server,
    port: 4567,
    method: "DELETE",
  };
  let req = http.request(options);
  req.write("");
  req.end();
  return new Promise((resolve, reject) => {
    req.on("response", (res) => {
      let data = "";
      res.on("data", (chunk) => {
        data += chunk;
      });
      res.on("end", () => {
        resolve(data);
      });
    });
  });
}
export function keep() {
  console.log("keep");

  let options: http.RequestOptions = {
    host: config.host,
    path: "/api/keep/",
    port: 4567,
    method: "POST",
  };
  let req = http.request(options);
  req.write("");
  req.end();
  return new Promise((resolve, reject) => {
    req.on("response", (res) => {
      let data = "";
      res.on("data", (chunk) => {
        data += chunk;
      });
      res.on("end", () => {
        resolve(data);
      });
    });
  });
}
//TODO: write functions for other api calls
//TODO: write quick responses after request sent
//TODO: edit message? followup? on reply
//TODO: change <br> in api to \n
//TODO: start and BACKUP with Gcloud api
//TODO: start gcloud-> ping-> start
