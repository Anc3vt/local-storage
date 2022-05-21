/*
 *   Ancevt LocalStorage
 *   Copyright (C) 2022 Ancevt (me@ancevt.com)
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
