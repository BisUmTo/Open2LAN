package it.multicoredev.opentoall.playit;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import it.multicoredev.opentoall.OpenToALL;
import it.multicoredev.opentoall.gui.screen.OpenToWanScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static it.multicoredev.opentoall.OpenToALL.*;
import static it.multicoredev.opentoall.Resources.MOD_ID;

public class PlayIt {
    public static final String PLAYIT_API = "https://api.playit.gg";
    public static final String PLAYIT_FOLDER = "mods/" + MOD_ID;
    public static final String PLAYIT_CONFIG = "playit/config.json";
    public static final String PLAYIT_VERSION = "0.4.4";

    public static final Text DOWNLOADING = new TranslatableText("wanServer.downloading");
    public static final Text DOWNLOADED = new TranslatableText("wanServer.downloaded");
    public static final Text DOWNLOAD_FAILED = new TranslatableText("wanServer.downloadFailed").styled(style -> style.withColor(Formatting.RED));
    public static final String STARTED = "wanServer.started";
    public static final Text START_FAILED = new TranslatableText("wanServer.startFailed").styled(style -> style.withColor(Formatting.RED));
    public static final Text LAUNCHING = new TranslatableText("wanServer.launching");
    public static final Text LAUNCHED = new TranslatableText("wanServer.launched");
    public static final Text TUNNELELING = new TranslatableText("wanServer.tunneling");
    public static final Text TUNNELED = new TranslatableText("wanServer.tunneled");
    private static final Text STOPPED =  new TranslatableText("wanServer.stopped");

    private static final ClientPlayerEntity player = MinecraftClient.getInstance().player;

    private final int port;
    private final Gson GSON = new Gson();
    private String url = PLAYIT_API;
    private OS os;
    private Config config;
    private Process process;
    private Tunnel tunnel;
    private Thread thread;

    public PlayIt(int port) {
        this.port = port;
    }

    public static boolean download() {
        OS os = OS.getOs();
        if (os == null) return false;
        sendInfo(DOWNLOADING);
        File binary = new File(PLAYIT_FOLDER, os.binary);
        try {
            Files.createDirectories(binary.toPath().getParent());
            FileUtils.copyURLToFile(new URL(os.download), binary);
            binary.setExecutable(true);
            sendInfo(DOWNLOADED);
            return true;
        } catch (IOException e) {
            sendError(DOWNLOAD_FAILED);
            if (DEBUG) e.printStackTrace();
            return false;
        }
    }

    public static void createEnvFile() {
        try {
            File env = new File(PLAYIT_FOLDER, ".env");
            if (!env.exists() && env.createNewFile()) {
                try (FileWriter bw = new FileWriter(env)) {
                    bw.write("NO_BROWSER=true");
                } catch (IOException e) {
                    if (DEBUG) e.printStackTrace();
                }
            }
        } catch (IOException e) {
            if (DEBUG) e.printStackTrace();
        }
    }

    private static void sendInfo(Text text) {
        player.sendMessage(text, true);
        OpenToALL.log(Level.INFO, text.asString());
    }

    private static void sendError(Text text) {
        player.sendMessage(text, true);
        OpenToALL.log(Level.ERROR, text.asString());
    }

    public boolean start() {
        if (thread != null && thread.isAlive()) return false;
        thread = new Thread(this::run);
        thread.start();
        return true;
    }

    public void run() {
        try {
            // Initialize os
            this.os = OS.getOs();
            if (os == null) throw new PlayItException("Unknown OS: Impossible to start PlayIT app.");
            getConfig().delete();

            // Initialize process
            File playItFile = new File(PLAYIT_FOLDER, os.binary);
            if (!playItFile.exists() && !download())
                throw new PlayItException("No file: Impossible to download or find PlayIT app.");
            createEnvFile();
            startProcess(playItFile);

            // Initialize agent
            File config = getConfig();
            if (config == null || !config.exists())
                throw new PlayItException("Unknown config file: Impossible to locate PlayIT config.");
            try {
                JsonReader reader = new JsonReader(new FileReader(config));
                this.config = GSON.fromJson(reader, Config.class);
            } catch (FileNotFoundException e) {
                if (DEBUG) e.printStackTrace();
                throw new PlayItException("Unknown agent: Impossible parse PlayIT config.");
            }

            // Start PlayIT
            fetch();
            createTunnel();

            // Feedback
            OpenToWanScreen.sendWanMessage(STARTED);
            log(Level.INFO, new TranslatableText(STARTED, PLAYIT.getAddress()).asString());

            try {
                process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread = null;
        } catch (PlayItException e) {
            if (DEBUG) e.printStackTrace();
            log(Level.ERROR, START_FAILED.asString());
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(START_FAILED);
        }
    }

    public String getAddress() {
        if (tunnel == null) return null;
        return tunnel.connectAddress;
    }

    public int getPort() {
        return port;
    }

    public void stop() {
        if (process != null && process.isAlive()) {
            process.destroy();
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                if (DEBUG) e.printStackTrace();
            }
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(STOPPED);
        }
    }

    public boolean isRunning() {
        return process.isAlive();
    }

    private void createTunnel() throws PlayItException {
        sendInfo(TUNNELELING);
        Tunnel tunnel = fetch("/account/tunnels", "POST", GSON.toJson(new Tunnel()), Tunnel.class);
        if (tunnel == null) throw new PlayItException("Unknown tunnel: Unable to start a PlayIT tunnel.");

        int attempts = 0;
        while (this.tunnel == null || this.tunnel.connectAddress == null || this.tunnel.domainId == null) {
            Tunnels tunnels = fetch("/account/tunnels", "GET", null, Tunnels.class);
            if (tunnels == null) throw new PlayItException("Unknown tunnel: No tunnel are running in PlayIT.");
            for (Tunnel t : tunnels.tunnels) {
                if (tunnel.id.equals(t.id)) {
                    this.tunnel = t;
                    break;
                }
            }
            if (attempts++ > 60) throw new PlayItException("Unknown tunnel: Unable to find running tunnel in PlayIT.");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                if (DEBUG) e.printStackTrace();
            }
        }
        sendInfo(TUNNELED);
    }

    private <T> T fetch(String url, String method, String jsonData, Type type) throws PlayItException {
        if (!url.startsWith("https://") && !url.startsWith("http://")) {
            if (url.startsWith("/")) url = PLAYIT_API + url;
            else url = PLAYIT_API + '/' + url;
        }
        try {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod(method);
            if (type == null) con.setRequestProperty("Accept", "application/json");
            con.setRequestProperty("authorization", "agent " + config.agentKey);
            if (jsonData != null && !jsonData.equals("")) {
                con.setRequestProperty("Content-Type", "application/json; utf-8");
                con.setDoOutput(true);
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
            }
            if (con.getResponseCode() == 200 && type != null) {
                try (InputStreamReader reader = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8)) {
                    return GSON.fromJson(reader, type);
                }
            }
            return null;
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
            throw new PlayItException("Request failed: Impossible comunicate with PlayIT API.");
        }
    }

    private void fetch() throws PlayItException {
        fetch(url, "GET", "", null);
    }

    private void startProcess(File playItFile) throws PlayItException {
        try {
            sendInfo(LAUNCHING);
            this.process = new ProcessBuilder(playItFile.getAbsolutePath()).directory(playItFile.getParentFile()).start();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    Matcher matcher = Pattern.compile("https://[0-9a-z./]*").matcher(line);
                    if (matcher.find()) {
                        this.url = matcher.group();
                        OpenToALL.log(Level.INFO, "Browser interface: " + this.url);
                        break;
                    }
                }
            } catch (IOException e) {
                if (DEBUG) e.printStackTrace();
                throw new PlayItException("Unable to start: Can't connect to the PlayIT API.");
            }
        } catch (IOException e) {
            if (DEBUG) e.printStackTrace();
            throw new PlayItException("Unable to start: Can't run PlayIT app.");
        }
        sendInfo(LAUNCHED);
    }

    public File getConfig() {
        if (os == null) return null;
        if (os == OS.WINDOWS)
            return Paths.get(System.getenv("APPDATA"), PLAYIT_CONFIG).toFile();
        else
            return Paths.get(System.getProperty("user.home"), ".config", PLAYIT_CONFIG).toFile();
    }

    public void disableTunnel() throws PlayItException {
        fetch("/account/tunnels/" + tunnel.id + "/disable", "GET", "", null);
    }

    public void enableTunnel() throws PlayItException {
        fetch("/account/tunnels/" + tunnel.id + "/enable", "GET", "", null);
    }

    public enum OS {
        WINDOWS("https://playit.gg/downloads/playit-win_64-" + PLAYIT_VERSION + ".exe"),
        MAC("https://playit.gg/downloads/playit-darwin_64-" + PLAYIT_VERSION),
        LINUX("`https://playit.gg/downloads/playit-linux_64-" + PLAYIT_VERSION);

        public final String binary;
        public final String download;

        OS(String download) {
            this.binary = download.substring(download.lastIndexOf('/') + 1);
            this.download = download;
        }

        public static OS getOs() {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win"))
                return OS.WINDOWS;
            else if (os.contains("nix") || os.contains("nux") || os.contains("aix"))
                return OS.LINUX;
            else if (os.contains("mac"))
                return OS.MAC;
            return null;
        }
    }

    private static class Config {
        @SerializedName("agent_key")
        public String agentKey;
        @SerializedName("preferred_tunnel")
        public String preferredTunnel;
    }

    private class Tunnels {
        public Tunnel[] tunnels;
    }

    public class Tunnel {
        public Integer id;
        public String game = "minecraft";
        @SerializedName("local_port")
        public int localPort;
        @SerializedName("local_ip")
        public String localIp = "127.0.0.1";
        @SerializedName("local_proto")
        public String localProto = "Tcp";
        @SerializedName("agent_id")
        public int agentId;
        @SerializedName("domain_id")
        public Integer domainId;
        public String status;
        @SerializedName("connect_address")
        public String connectAddress;
        @SerializedName("is_custom_domain")
        public boolean isCustomDomain;
        @SerializedName("tunnel_version")
        public int tunnelVersion;

        public Tunnel() throws PlayItException {
            this.localPort = port;
            Agents agents = fetch("/account/agents", "GET", "", Agents.class);
            if (agents == null)
                throw new PlayItException("Unknown agent: Impossible get agent ID from PlayIT API.");
            for (Agent agent : agents.agents) {
                if (agent.key.equals(PlayIt.this.config.agentKey)) {
                    this.agentId = agent.id;
                    break;
                }
            }
        }
    }

    private class Agents {
        public Agent[] agents;
    }

    private class Agent {
        public int id;
        public String key;
        @SerializedName("is_connected")
        public boolean isConnected;
        @SerializedName("host_ip")
        public boolean hostIp;
        @SerializedName("tunnel_server_name")
        public boolean tunnelServerName;
    }

    private class PlayItException extends Exception {
        public PlayItException(String message) {
            super(message);
            OpenToALL.log(Level.ERROR, message);
        }
    }
}
