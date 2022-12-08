package dev.garby.botserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Api {
    private static final Logger logger
            = LoggerFactory.getLogger(Api.class);
    protected boolean ping() {
        try {
            ApiResponse response = request("/ping", Method.GET);
            if (response.getStatus() == 200) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }
    protected String start(String server) throws IOException {
        ApiResponse response = request("/start/" + server,Method.POST);

        if (response.getStatus() == 200) {
            return "DONE";
        } else {
            return response.getBody();
        }

    }
    protected String stop(String server) throws IOException {
        ApiResponse response = request("/stop/" + server,Method.DELETE);

        if (response.getStatus() == 200) {
            return "DONE";
        } else {
            return response.getBody();
        }

    }

    protected ApiResponse request(String path, Method method) throws IOException {
        URL url = new URL("http://"+Main.BACKEND_HOSTNAME+"/api"+path);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method.toString());
        con.connect();

        Reader streamReader;

        if (con.getResponseCode() > 299) {
            streamReader = new InputStreamReader(con.getErrorStream());
        } else {
            streamReader = new InputStreamReader(con.getInputStream());
        }
        BufferedReader in = new BufferedReader(streamReader);
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();


        ApiResponse response = new ApiResponse(con.getResponseCode(), content.toString());
        con.disconnect();
        return response;
    }



    public static class ApiResponse {
        private final int status;
        private final String body;

        public ApiResponse(int status, String body) {
            this.status = status;
            this.body = body;
        }

        public int getStatus() {
            return status;
        }

        public String getBody() {
            return body;
        }
    }
    protected enum Method {
        GET,
        POST,
        PUT,
        DELETE
    }
}
