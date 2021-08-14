package it.multicoredev.opentoall.ngrok;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class NgrokTunnel {
    private final String ngrokAddr;
    private final String url;
    private final String name;
    private final int port;


    public NgrokTunnel(String url, int port) throws Exception {
        this.name = UUID.randomUUID().toString();
        this.ngrokAddr = url;
        this.port = port;

        JsonObject json = new JsonObject();
        json.addProperty("addr", port);
        json.addProperty("name", this.name);
        json.addProperty("proto", "tcp");
        String jsonInputString = json.toString();

        HttpURLConnection con = (HttpURLConnection) new URL(ngrokAddr + "/api/tunnels").openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Content-Type", "application/json; utf-8");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setDoOutput(true);
        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        if (con.getResponseCode() != 201) throw new NgrokTunnelException();

        try (InputStreamReader reader = new InputStreamReader(con.getInputStream())) {
            Response response = new Gson().fromJson(reader, Response.class);
            this.url = response.publicURL;
        }
        if (this.url == null) throw new Exception();
    }

    public NgrokTunnel(int port) throws Exception {
        this("http://127.0.0.1:4040", port);
    }

    public String url() {
        return this.url;
    }

    public String ip() {
        if (url != null && url.startsWith("tcp://")) return url.substring(6);
        return url;
    }

    public void close() throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(ngrokAddr + "/api/tunnels/" + name).openConnection();
        con.setRequestMethod("DELETE");
        con.connect();
    }

    public int port() {
        return port;
    }

    public static class Response {
        @SerializedName("public_url")
        public String publicURL;
    }

    private static class NgrokTunnelException extends Exception {
    }
}
