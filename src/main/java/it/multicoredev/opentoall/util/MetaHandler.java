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

package it.multicoredev.opentoall.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MetaHandler extends CompletableHandler<List<MetaHandler.GameVersion>> {
    private final String metaUrl;
    private List<GameVersion> versions;

    public MetaHandler(String url) {
        this.metaUrl = url;
    }

    public MetaHandler load() throws IOException {
        URL url = new URL(metaUrl);
        JsonArray jsonArray = new JsonParser().parse(InstallerUtil.readTextFile(url)).getAsJsonArray();
        List<JsonObject> jsonObjectList = new ArrayList<>();
        for (JsonElement jsonElement : jsonArray) jsonObjectList.add((JsonObject) jsonElement);
        this.versions = jsonObjectList.stream()
                .map(GameVersion::new)
                .collect(Collectors.toList());

        complete(versions);
        return this;
    }

    public List<GameVersion> getVersions() {
        return Collections.unmodifiableList(versions);
    }

    public GameVersion getLatestVersion(boolean snapshot) {
        if (snapshot) {
            return versions.get(0);
        } else {
            return versions.stream()
                    .filter(GameVersion::isStable).findFirst().orElse(null);
        }
    }

    public static class GameVersion {
        String version;
        boolean stable;

        public GameVersion(JsonObject json) {
            version = json.get("version").getAsString();
            stable = json.get("stable").getAsBoolean();
        }

        public String getVersion() {
            return version;
        }

        public boolean isStable() {
            return stable;
        }
    }
}
