/*
 *   Ancevt LocalStorage
 *   Copyright (C) 2022 Ancevt (me@ancevt.com)
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General @Override public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General @Override public License for more details.
 *
 *   You should have received a copy of the GNU General @Override public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.ancevt.localstorage;


import com.ancevt.util.args.Args;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;
import java.util.StringTokenizer;

import static java.nio.file.StandardOpenOption.CREATE;

public class FileLocalStorage extends LocalStorage {

    FileLocalStorage(@NotNull String filename,
                     boolean saveOnWrite,
                     String storageId,
                     String directoryPath) {
        super(filename, saveOnWrite, storageId, directoryPath);

        Path dir = DirectoryHelper.createOrGetDirectory(this);

        if (Files.exists(Path.of(dir.toString() + File.separatorChar + filename))) {
            load();
        }
    }

    @SneakyThrows
    @Override
    public void save() {
        Path dir = DirectoryHelper.createOrGetDirectory(this);
        Files.writeString(
                Path.of(dir.toString() + File.separatorChar + getFilename()),
                stringify(),
                StandardCharsets.UTF_8,
                StandardOpenOption.WRITE,
                CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    @SneakyThrows
    @Override
    public LocalStorage load() {
        Path dir = DirectoryHelper.createOrGetDirectory(this);

        System.out.println(dir);
        try {
            Files.readAllLines(
                    Path.of(dir.toString() + File.separatorChar + getFilename())
            ).forEach(this::parseLine);
        } catch (Exception e) {
            // TODO: log error
            e.printStackTrace();
        }
        return this;
    }

    @SneakyThrows
    @Override
    public LocalStorage deleteResources() {
        clear();
        Path dir = DirectoryHelper.createOrGetDirectory(this);
        Files.deleteIfExists(Path.of(dir.toString() + File.separatorChar + getFilename()));
        DirectoryHelper.deleteDirectoryIfEmpty(this);
        return this;
    }

    public static void main(String[] args) {

        LocalStorage localStorage = new LocalStorageBuilder("localstorage", FileLocalStorage.class)
                .saveOnWrite(false)
                .storageId(FileLocalStorage.class.getName())
                .build();

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            try {
                Args tokens = new Args(scanner.nextLine());
                String command = tokens.next();

                switch (command) {
                    case "set" -> {
                        String keyValue = tokens.next();
                        StringTokenizer stringTokenizer = new StringTokenizer(keyValue, "=");
                        String key = stringTokenizer.nextToken();
                        String value = stringTokenizer.nextToken();
                        localStorage.put(key, value);
                    }
                    case "get" -> {
                        String key = tokens.next();
                        System.out.println(localStorage.getString(key));
                    }
                    case "ls" -> {
                        System.out.println(localStorage.toFormattedString());
                    }
                    case "lsg" -> {
                        String keyStartsWith = tokens.next();
                        System.out.println(localStorage.toFormattedStringGroup(keyStartsWith));
                    }
                    case "save" -> {
                        localStorage.save();
                    }
                    case "rm" -> {
                        String key = tokens.next();
                        localStorage.remove(key);
                    }
                    case "rmg" -> {
                        String keyStartsWith = tokens.next();
                        localStorage.removeGroup(keyStartsWith);
                    }
                    case "delete" -> {
                        localStorage.deleteResources();
                    }
                    case "cls" -> {
                        for (int i = 0; i < 100; i++) {
                            System.out.println();
                        }
                    }

                    default -> {
                        System.err.println("Unknown command: " + command);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
