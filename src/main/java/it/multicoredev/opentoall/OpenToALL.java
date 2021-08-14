package it.multicoredev.opentoall;

import it.multicoredev.opentoall.ngrok.NgrokThread;
import it.multicoredev.opentoall.ngrok.NgrokTunnel;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class OpenToALL implements ModInitializer {
    public static final String MOD_ID = "opentoall";
    public static final String MOD_NAME = "Open to ALL";
    public static Logger LOGGER = LogManager.getLogger();
    public static boolean DEBUG = true;

    public static NgrokTunnel NGROK_TUNNEL;
    public static NgrokThread NGROK_THREAD;


    public static void log(Level level, String message) {
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }

    @Override
    public void onInitialize() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (NGROK_TUNNEL != null) NGROK_TUNNEL.close();
            } catch (IOException ignored) {
            }
            if (NGROK_THREAD != null && NGROK_THREAD.isAlive()) NGROK_THREAD.interrupt();
        }));

    }

}