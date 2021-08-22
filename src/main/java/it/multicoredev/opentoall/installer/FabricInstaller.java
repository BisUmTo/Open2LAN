/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.multicoredev.opentoall.installer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import it.multicoredev.opentoall.OpenToALL;
import it.multicoredev.opentoall.Resources;

public class FabricInstaller {
    public static void install(Path mcDir, String profileName, String loaderVersion, InstallerProgress progress) throws IOException {
        System.out.println("Installing " + Resources.MINECRAFT_VERSION + " with Open to ALL");

        Path versionsDir = mcDir.resolve("versions");
        Path profileDir = versionsDir.resolve(profileName);
        Path profileJson = profileDir.resolve(profileName + ".json");

        if (!Files.exists(profileDir)) {
            Files.createDirectories(profileDir);
        }

        Path dummyJar = profileDir.resolve(profileName + ".jar");
        Files.deleteIfExists(dummyJar);
        Files.createFile(dummyJar);

        URL profileUrl = new URL(String.format("https://meta.fabricmc.net/v2/versions/loader/%s/%s/profile/json", Resources.MINECRAFT_VERSION, loaderVersion));
        downloadFile(profileUrl, profileJson);

        progress.updateProgress("Done");
    }

    public static void downloadFile(URL url, Path path) throws IOException {
        Files.createDirectories(path.getParent());

        try (InputStream in = url.openStream()) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
