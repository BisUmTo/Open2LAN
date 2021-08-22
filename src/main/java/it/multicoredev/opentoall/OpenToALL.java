package it.multicoredev.opentoall;

import it.multicoredev.opentoall.playit.PlayIt;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static it.multicoredev.opentoall.Resources.MOD_NAME;

public class OpenToALL implements ClientModInitializer {
    public static Logger LOGGER = LogManager.getLogger();
    public static boolean DEBUG = true;
    public static PlayIt PLAYIT;

    public static void log(Level level, String message) {
        LOGGER.log(level, "[" + MOD_NAME + "] " + message);
    }

    public static void shutDown() {
        if (PLAYIT != null) PLAYIT.stop();
    }

    @Override
    public void onInitializeClient() {
        Runtime.getRuntime().addShutdownHook(new Thread(OpenToALL::shutDown));
    }
}