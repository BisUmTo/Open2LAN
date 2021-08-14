package mod.linguardium.open2lan;

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class Open2Lan implements ModInitializer {
    public static final String MOD_ID = "open2lan";
    public static final String MOD_NAME = "Open To Lan";
    public static Logger LOGGER = LogManager.getLogger();

    public static NgrokTunnel NGROK_TUNNEL;
    public static NgrokThread NGROK_THREAD;


    public static void log(Level level, String message) {
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }

    @Override
    public void onInitialize() {
        // todo muovere thread

        NGROK_THREAD = new NgrokThread();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                NGROK_TUNNEL.close();
            } catch (IOException ignored) {
            }
            NGROK_THREAD.interrupt();
        }));
        NGROK_THREAD.start();
    }

}