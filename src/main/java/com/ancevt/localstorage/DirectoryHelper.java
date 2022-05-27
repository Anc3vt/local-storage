/**
 * Copyright (C) 2022 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ancevt.localstorage;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.ancevt.commons.platformdepend.OsDetector.isWindows;

class DirectoryHelper {

    @SneakyThrows
    static @NotNull Path createOrGetDirectory(@NotNull LocalStorage localStorage) {
        if (localStorage.getDirectoryPath() == null && localStorage.getStorageId() != null) {

            String homeDir = System.getProperty("user.home");

            Path dir;
            if (isWindows()) {
                dir = Path.of(homeDir + "\\AppData\\Roaming\\" + localStorage.getStorageId());
            } else {
                dir = Path.of(homeDir + "/.local/share/" + localStorage.getStorageId());
            }

            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            return dir;
        } else {
            Path dir = Path.of(localStorage.getDirectoryPath() + File.separatorChar + localStorage.getStorageId());
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            return dir;
        }
    }

    @SneakyThrows
    static void deleteDirectoryIfEmpty(@NotNull LocalStorage localStorage) {
        if (localStorage.getDirectoryPath() == null && localStorage.getStorageId() != null) {
            String homeDir = System.getProperty("user.home");

            Path dir;
            if (isWindows()) {
                dir = Path.of(homeDir + "\\AppData\\Roaming\\" + localStorage.getStorageId());
            } else {
                dir = Path.of(homeDir + "/.local/share/" + localStorage.getStorageId());
            }

            if (!Files.exists(dir) && isDirectoryEmpty(dir)) {
                Files.deleteIfExists(dir);
            }
        } else {
            Path dir = Path.of(localStorage.getDirectoryPath() + File.separatorChar + localStorage.getStorageId());
            if (Files.exists(dir) && isDirectoryEmpty(dir)) Files.deleteIfExists(dir);
            dir = Path.of(localStorage.getDirectoryPath());
            if (Files.exists(dir) && isDirectoryEmpty(dir)) Files.deleteIfExists(dir);
        }
    }

    @SneakyThrows
    static boolean isDirectoryEmpty(Path path) {
        if (Files.isDirectory(path)) {
            try (DirectoryStream<Path> directory = Files.newDirectoryStream(path)) {
                return !directory.iterator().hasNext();
            }
        }

        return false;
    }
}
