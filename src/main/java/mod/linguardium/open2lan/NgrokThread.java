package mod.linguardium.open2lan;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;

import java.io.*;
import java.net.URL;

public class NgrokThread extends Thread {
    public static final String NGROK_FOLDER = "mods";
    public static final String NGROK_CONFIG = ".ngrok2/ngrok.yml";

    private File ngrokFile;
    private OS os;
    private Process process;
    private String authtoken;

    @Override
    public void run() {
        this.os = OS.getOs();

        if (os == null) {
            Open2Lan.log(Level.ERROR,"Unknown OS: Impossible to start Ngrok app.");
            return;
        }
        ngrokFile = new File(NGROK_FOLDER, os.exe);
        boolean success = true;
        if (!ngrokFile.exists() || !ngrokFile.isFile()) success = download();
        if (needAuthentication()) authenticate();
        if (success) startNgrok();
    }

    private void authenticate() {
        try {
            if(process != null) process.destroy();
            process = new ProcessBuilder(ngrokFile.getAbsolutePath(), "authtoken ", authtoken).start();
        } catch (IOException e) {
            Open2Lan.log(Level.ERROR,"Failed to authenticate on Ngrok app!");
            Open2Lan.LOGGER.error(e);
        }
    }

    private boolean needAuthentication() {
        File configFile = OS.getConfig();
        if (!configFile.exists() || !configFile.isFile()) return true;
        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            String str;
            while ((str = br.readLine()) != null)
                if(str.contains("authtoken")) return false;
        } catch (IOException ignored) {
        }
        return true;
    }

    private void startNgrok() {
        try {
            if(process != null) process.destroy();
            process = new ProcessBuilder(ngrokFile.getAbsolutePath(), "start", "--none").start();
        } catch (IOException e) {
            Open2Lan.log(Level.ERROR,"Failed to start Ngrok app!");
            Open2Lan.LOGGER.error(e);
        }
    }

    public boolean download() {
        Open2Lan.LOGGER.info("Downloading Ngrok app...");
        File zip = new File(NGROK_FOLDER, "ngrok.zip");
        try {
            FileUtils.copyURLToFile(new URL(os.download), zip);
            ZipUtil.unzip(zip, new File(NGROK_FOLDER));
            zip.delete();
            return true;
        } catch (IOException e) {
            Open2Lan.log(Level.ERROR,"Failed to download Ngrok app!");
            Open2Lan.LOGGER.error(e);
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

    private static class NgrokThreadExcption extends Exception {
    }
}
