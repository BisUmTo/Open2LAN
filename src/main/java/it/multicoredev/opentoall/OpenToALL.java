package it.multicoredev.opentoall;

import it.multicoredev.opentoall.ngrok.NgrokThread;
import it.multicoredev.opentoall.ngrok.NgrokTunnel;
import net.fabricmc.api.ModInitializer;
import net.minecraft.MinecraftVersion;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static it.multicoredev.opentoall.Resources.MOD_NAME;
import static it.multicoredev.opentoall.ngrok.NgrokThread.NGROK_FOLDER;

public class OpenToALL implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();
    public static boolean DEBUG = true;
    public static NgrokTunnel NGROK_TUNNEL;
    public static NgrokThread NGROK_THREAD;

    public static void log(Level level, String message) {
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }

    public static void shutDown() {
        try {
            if (NGROK_TUNNEL != null) NGROK_TUNNEL.close();
            if (NGROK_THREAD != null) NGROK_THREAD.close();
        } catch (IOException | InterruptedException ignored) {
        }
    }

    public static void main(String... args) {
        try {
            File ngrokFile = new File(NGROK_FOLDER, NgrokThread.OS.WINDOWS.getExe());
            Process p = new ProcessBuilder(ngrokFile.getAbsolutePath(), "start", "--none").start();

            for (int i = 0; i < 10; i++) {
                try {
                    HttpURLConnection con = (HttpURLConnection) new URL("http://localhost:4040/api/").openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(5000);
                    int r = con.getResponseCode();
                    System.out.println(r);
                    if (r == 200) break;
                } catch (Exception ignored) {
                    System.out.println("wait");
                    Thread.sleep(100);
                }
            }

            p.destroy();
            p.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInitialize() {
        Runtime.getRuntime().addShutdownHook(new Thread(OpenToALL::shutDown));
    }


}