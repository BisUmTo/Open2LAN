package it.multicoredev.opentoall.installer;

import it.multicoredev.opentoall.OpenToALL;
import it.multicoredev.opentoall.util.ArgumentParser;
import it.multicoredev.opentoall.util.CrashDialog;
import it.multicoredev.opentoall.util.InstallerUtil;
import it.multicoredev.opentoall.util.MetaHandler;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static it.multicoredev.opentoall.installer.InstallerProgress.CONSOLE;

public class Installer {
    private static ArgumentParser ARGS;
    public static MetaHandler GAME_VERSION_META;
    public static MetaHandler LOADER_META;


    public static void main(String... args) throws IOException, URISyntaxException {
        System.out.println(Installer.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath());

        System.out.println("Loading \"Open To ALL\" Installer: " + OpenToALL.MOD_VERSION);
        Installer.ARGS = ArgumentParser.create(args);
        String command = Installer.ARGS.getCommand().orElse(null);

        GAME_VERSION_META = new MetaHandler("https://meta.fabricmc.net/v2/versions/game").load();
        LOADER_META = new MetaHandler("https://meta.fabricmc.net/v2/versions/loader").load();

        if (GraphicsEnvironment.isHeadless() && command == null) {
            command = "help";
        }

        if (command == null) {
            try {
                InstallerGui.start();
            } catch (Exception e) {
                e.printStackTrace();
                new CrashDialog(e);
            }
        } else if (command.equals("help")) {
            System.out.println("help - Opens this menu");
            System.out.println("install -dir <install dir> -mcversion <minecraft version, default latest> -loader <loader version, default latest>");

            LOADER_META.load();
            GAME_VERSION_META.load();

            System.out.printf("\nLatest Version: %s\nLatest Loader: %s\n", GAME_VERSION_META.getLatestVersion(Installer.ARGS.has("snapshot")).getVersion(), Installer.LOADER_META.getLatestVersion(false).getVersion());
        } else if (command.equals("install")){
            try {
                installCli();
            } catch (Exception e) {
                throw new RuntimeException("Failed to install");
            }
        } else {
            System.out.println("Command not found, see help");
        }
    }

    public static void installCli() throws Exception {
        Path path = Paths.get(ARGS.getOrDefault("dir", () -> InstallerUtil.getWorkingDirectory().toString()));

        if (!Files.exists(path)) {
            throw new FileNotFoundException("Launcher directory not found at " + path);
        }

        String loaderVersion = InstallerUtil.getLoaderVersion(ARGS);

        FabricInstaller.install(path, OpenToALL.MINECRAFT_VERSION, loaderVersion, CONSOLE);

        if (ARGS.has("noprofile")) {
            return;
        }

        String profileName = String.format("fabric-loader-%s-%s", loaderVersion, OpenToALL.MINECRAFT_VERSION );
        ProfileInstaller.setupProfile(path, profileName, OpenToALL.MINECRAFT_VERSION, CONSOLE);
    }



}
