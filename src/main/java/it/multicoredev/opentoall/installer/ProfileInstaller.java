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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.multicoredev.opentoall.OpenToALL;
import it.multicoredev.opentoall.util.InstallerUtil;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ProfileInstaller {
    public static void setupProfile(Path path, String name, String gameVersion, InstallerProgress progress) throws IOException {
        Path launcherProfiles = path.resolve("launcher_profiles.json");

        if (!Files.exists(launcherProfiles)) {
            progress.updateProgress("Could not find launcher_profiles");
            return;
        }

        progress.updateProgress("Creating profile...");

        JsonObject jsonObject = new JsonParser().parse(Files.readString(launcherProfiles)).getAsJsonObject();
        JsonObject profiles = (JsonObject) jsonObject.get("profiles");
        String profileName = OpenToALL.MOD_NAME + " - " + gameVersion;

        JsonObject profile;

        if (profiles.has(profileName)) {
            profile = profiles.getAsJsonObject(profileName);
        } else {
            profile = createProfile(profileName);
        }

        profile.addProperty("lastVersionId", name);
        profiles.add(profileName, profile);

        Files.writeString(launcherProfiles, jsonObject.toString());
        progress.updateProgress("Profile created");
    }

    private static JsonObject createProfile(String name) {
        DateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", name);
        jsonObject.addProperty("type", "custom");
        jsonObject.addProperty("created", ISO_8601.format(new Date()));
        jsonObject.addProperty("lastUsed", ISO_8601.format(new Date()));
        jsonObject.addProperty("icon", InstallerUtil.getProfileIcon());
        return jsonObject;
    }
}
