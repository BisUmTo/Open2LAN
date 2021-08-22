package it.multicoredev.opentoall.ngrok;

import it.multicoredev.opentoall.OpenToALL;
import it.multicoredev.opentoall.util.ZipUtil;
import net.minecraft.client.MinecraftClient;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import static it.multicoredev.opentoall.OpenToALL.DEBUG;
import static it.multicoredev.opentoall.OpenToALL.NGROK_THREAD;
import static it.multicoredev.opentoall.gui.screen.OpenToWanScreen.DONLOADED_TEXT;
import static it.multicoredev.opentoall.gui.screen.OpenToWanScreen.DONLOADING_TEXT;

public class NgrokThread extends Thread {
    public static final String NGROK_FOLDER = "mods";
    public static final String NGROK_CONFIG = ".ngrok2/ngrok.yml";

    private File ngrokFile;
    private OS os;
    private Process process;
    private String authtoken;

    public static boolean needAuthentication() {
        File configFile = OS.getConfig();
        if (!configFile.exists() || !configFile.isFile()) return true;
        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            String str;
            while ((str = br.readLine()) != null)
                if (str.contains("authtoken")) return false;
        } catch (IOException e) {
            if (OpenToALL.DEBUG) e.printStackTrace();
        }
        return true;
    }

    public static String getAuthtoken() {
        File configFile = OS.getConfig();
        if (!configFile.exists() || !configFile.isFile()) return null;
        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            String str;
            while ((str = br.readLine()) != null)
                if (str.contains("authtoken")) {
                    String[] split = str.split(" ");
                    return split[split.length - 1];
                }
        } catch (IOException e) {
            if (OpenToALL.DEBUG) e.printStackTrace();
        }
        return null;
    }

    public void setAuthtoken(String authtoken) throws Exception {
        String[] split = authtoken.split(" ");
        String token = split[split.length - 1].replaceAll("[^a-zA-Z0-9_]", "");
        if (token.length() == 42 || token.length() == 48)
            this.authtoken = token;
        else
            throw new Exception();
    }

    public static boolean fileExist() {
        File tmp = new File(NGROK_FOLDER, Objects.requireNonNull(OS.getOs()).getExe());
        return !tmp.exists() || !tmp.isFile();
    }

    public static boolean isRunning(int attempts) {
        for (int i = 0; i < attempts; i++) {
            try {
                HttpURLConnection con = (HttpURLConnection) new URL("http://localhost:4040/api/").openConnection();
                con.setRequestMethod("GET");
                con.setConnectTimeout(5000);
                int r = con.getResponseCode();
                if (r == 200) return true;
            } catch (Exception ignored) {
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                if (DEBUG) e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void run() {
        this.os = OS.getOs();

        if (os == null) {
            OpenToALL.log(Level.ERROR, "Unknown OS: Impossible to start Ngrok app.");
            return;
        }
        ngrokFile = new File(NGROK_FOLDER, os.exe);
        boolean success = true;
        if (!ngrokFile.exists() || !ngrokFile.isFile()) success = download();
        if (needAuthentication()) authenticate();
        if (success) startNgrok();

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            if (DEBUG) e.printStackTrace();
        }

        NGROK_THREAD = null;
    }

    private void authenticate() {
        try {
            if (process != null) process.destroy();
            process = new ProcessBuilder(ngrokFile.getAbsolutePath(), "authtoken", authtoken).start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            OpenToALL.log(Level.ERROR, "Failed to authenticate on Ngrok app!");
            if (DEBUG) e.printStackTrace();
        }
    }

    public void close() throws InterruptedException {
        if (process != null) {
            process.destroy();
            process.waitFor();
        }
    }

    private void startNgrok() {
        try {
            if (process != null) process.destroy();
            process = new ProcessBuilder(ngrokFile.getAbsolutePath(), "start", "--none").start();
        } catch (IOException e) {
            OpenToALL.log(Level.ERROR, "Failed to start Ngrok app!");
            if (DEBUG) e.printStackTrace();
        }
    }

    public boolean download() {
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(DONLOADING_TEXT);
        OpenToALL.log(Level.INFO, DONLOADING_TEXT.getString());
        File zip = new File(NGROK_FOLDER, "ngrok.zip");
        try {
            FileUtils.copyURLToFile(new URL(os.download), zip);
            ZipUtil.unzip(zip, new File(NGROK_FOLDER));
            zip.delete();
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(DONLOADED_TEXT);
            OpenToALL.log(Level.INFO, DONLOADED_TEXT.getString());
            return true;
        } catch (IOException e) {
            OpenToALL.log(Level.ERROR, "Failed to download Ngrok app!");
            if (DEBUG) e.printStackTrace();
            return false;
        }
    }

    public enum OS {
        WINDOWS("ngrok.exe", "https://bin.equinox.io/c/4VmDzA7iaHb/ngrok-stable-windows-amd64.zip"),
        MAC("ngrok", "https://bin.equinox.io/c/4VmDzA7iaHb/ngrok-stable-linux-amd64.zip"),
        LINUX("ngrok", "https://bin.equinox.io/c/4VmDzA7iaHb/ngrok-stable-darwin-amd64.zip");

        private final String exe;
        private final String download;

        OS(String exe, String download) {
            this.exe = exe;
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

        public static File getConfig() {
            return new File(System.getProperty("user.home"), NGROK_CONFIG);
        }

        public String getExe() {
            return exe;
        }

        public String getDownload() {
            return download;
        }
    }
}
