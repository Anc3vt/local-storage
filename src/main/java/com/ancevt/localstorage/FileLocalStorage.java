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
                    case "clear" -> {
                        localStorage.clear();
                    }
                    case "load" -> {
                        localStorage.load();
                    }
                    case "delete" -> {
                        localStorage.deleteResources();
                    }
                    case "cls" -> {
                        for (int i = 0; i < 100; i++) {
                            System.out.println();
                        }
                    }
                    case "filename" -> {
                        System.out.println(localStorage.getFilename());
                    }
                    case "id" -> {
                        System.out.println(localStorage.getStorageId());
                    }
                    case "dir" -> {
                        System.out.println(localStorage.getDirectoryPath());
                    }
                    case "count" -> {
                        System.out.println(localStorage.getItemCount());
                    }
                    case "export" -> {
                        localStorage.exportTo(Path.of(tokens.next()));
                    }
                    case "import" -> {
                        localStorage.importFrom(Path.of(tokens.next()));
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
