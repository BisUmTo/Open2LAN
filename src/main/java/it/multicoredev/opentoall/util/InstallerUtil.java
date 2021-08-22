package it.multicoredev.opentoall.util;

import it.multicoredev.opentoall.installer.Installer;
import it.multicoredev.opentoall.playit.PlayIt;

import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

public class InstallerUtil {

    public static File getWorkingDirectory() {
        return getWorkingDirectory("minecraft");
    }

    public static File getWorkingDirectory(String applicationName) {
        String userHome = System.getProperty("user.home", ".");
        File workingDirectory = null;
        PlayIt.OS os = PlayIt.OS.getOs();
        if (os == null) workingDirectory = new File(userHome, applicationName + '/');
        else switch (os) {
            case LINUX -> workingDirectory = new File(userHome, '.' + applicationName + '/');
            case WINDOWS -> {
                String applicationData = System.getenv("APPDATA");
                if (applicationData != null) {
                    workingDirectory = new File(applicationData, "." + applicationName + '/');
                } else {
                    workingDirectory = new File(userHome, '.' + applicationName + '/');
                }
            }
            case MAC -> workingDirectory = new File(userHome, "Library/Application Support/" + applicationName);
        }

        if (!workingDirectory.exists() && !workingDirectory.mkdirs()) {
            throw new RuntimeException("The working directory could not be created: " + workingDirectory);
        } else {
            return workingDirectory;
        }
    }

    public static String getExceptionStackTrace(Throwable e) {
        StringWriter swr = new StringWriter();
        PrintWriter pwr = new PrintWriter(swr);
        e.printStackTrace(pwr);
        pwr.close();

        try {
            swr.close();
        } catch (IOException var4) {
        }

        return swr.getBuffer().toString();
    }

    public static String getLoaderVersion(ArgumentParser args) {
        return args.getOrDefault("loader", () -> {
            System.out.println("Using latest loader version");

            try {
                Installer.LOADER_META.load();
            } catch (IOException e) {
                throw new RuntimeException("Failed to load latest versions", e);
            }

            return Installer.LOADER_META.getLatestVersion(false).getVersion();
        });
    }

    public static String getProfileIcon() {
        try (InputStream is = InstallerUtil.class.getClassLoader().getResourceAsStream("profile_icon.png")) {
            byte[] ret = new byte[4096];
            int offset = 0;
            int len;

            while ((len = is.read(ret, offset, ret.length - offset)) != -1) {
                offset += len;
                if (offset == ret.length) ret = Arrays.copyOf(ret, ret.length * 2);
            }

            return "data:image/png;base64," + Base64.getEncoder().encodeToString(Arrays.copyOf(ret, offset));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "TNT";
    }

    public static Reader urlReader(URL url) throws IOException {
        return new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
    }

    public static String readTextFile(URL url) throws IOException {
        try (BufferedReader reader = new BufferedReader(urlReader(url))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    public static void centerWindow(Component c, Component par) {
        if (c != null) {
            Rectangle rect = c.getBounds();
            Rectangle parRect;
            if (par != null && par.isVisible()) {
                parRect = par.getBounds();
            } else {
                Dimension scrDim = Toolkit.getDefaultToolkit().getScreenSize();
                parRect = new Rectangle(0, 0, scrDim.width, scrDim.height);
            }

            int newX = parRect.x + (parRect.width - rect.width) / 2;
            int newY = parRect.y + (parRect.height - rect.height) / 2;
            if (newX < 0) {
                newX = 0;
            }

            if (newY < 0) {
                newY = 0;
            }

            c.setBounds(newX, newY, rect.width, rect.height);
        }
    }

}
