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
import com.ancevt.util.texttable.TextTable;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.lang.Byte.parseByte;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.Short.parseShort;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

public class FileLocalStorage implements LocalStorage {

    private static final String DELIMITER = "=";

    private final String filename;
    private final Map<String, String> data;
    private final boolean saveOnWrite;
    private final String storageId;
    private final String directoryPath;

    FileLocalStorage(@NotNull String filename,
                     boolean saveOnWrite,
                     String storageId,
                     String directoryPath) {

        this.filename = filename;
        this.saveOnWrite = saveOnWrite;
        this.storageId = storageId;
        this.directoryPath = directoryPath;
        data = new ConcurrentHashMap<>();

        Path dir = DirectoryHelper.createOrGetDirectory(this);

        if (Files.exists(Path.of(dir.toString() + File.separatorChar + filename))) {
            load();
        }
    }

    @Override
    public boolean contains(String key) {
        return data.containsKey(key);
    }

    @Override
    public String computeIfAbsent(String key, Function<String, String> mappingFunction) {
        return data.computeIfAbsent(key, mappingFunction);
    }

    @Override
    public String getString(String key) {
        return data.get(key);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return data.getOrDefault(key, defaultValue);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        try {
            return parseInt(getString(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public long getLong(String key, long defaultValue) {
        try {
            return parseLong(getString(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        try {
            return getString(key).equalsIgnoreCase("true");
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public byte getByte(String key, byte defaultValue) {
        try {
            return parseByte(getString(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public short getShort(String key, short defaultValue) {
        try {
            return parseShort(getString(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public char getChar(String key, char defaultValue) {
        try {
            return getString(key).charAt(0);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        try {
            return parseFloat(getString(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        try {
            return parseDouble(getString(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public LocalStorage put(String key, Object value) {
        data.put(key, String.valueOf(value));
        if (saveOnWrite) save();
        return this;
    }

    @Override
    public LocalStorage putAll(Map<String, String> map) {
        data.putAll(map);
        if (saveOnWrite) save();
        return this;
    }

    @Override
    public LocalStorage addMap(Map<String, String> map) {
        data.putAll(map);
        if (saveOnWrite) save();
        return this;
    }

    @Override
    public LocalStorage clear() {
        data.clear();
        if (saveOnWrite) save();
        return this;
    }

    @Override
    public LocalStorage exportTo(@NotNull Map<String, String> exportTo) {
        exportTo.putAll(toMap());
        return this;
    }

    @SneakyThrows
    @Override
    public LocalStorage exportTo(Path filePath) {
        Files.writeString(filePath, stringify(), StandardCharsets.UTF_8, CREATE, WRITE, TRUNCATE_EXISTING);
        return this;
    }

    @Override
    public LocalStorage exportGroupTo(@NotNull Map<String, String> exportTo, String keyStartsWith) {
        exportTo.putAll(toSortedMapGroup(keyStartsWith));
        return this;
    }

    @SneakyThrows
    @Override
    public LocalStorage exportGroupTo(Path filePath, String keyStartsWith) {
        Files.writeString(filePath, stringifyGroup(keyStartsWith), StandardCharsets.UTF_8, CREATE, WRITE, TRUNCATE_EXISTING);
        return this;
    }

    @Override
    public LocalStorage importFrom(@NotNull Map<String, String> importFrom) {
        data.putAll(importFrom);
        return this;
    }

    @SneakyThrows
    @Override
    public LocalStorage importFrom(Path filePath) {
        Files.readAllLines(filePath).forEach(this::parseLineAndPut);
        return this;
    }

    @Override
    public LocalStorage importGroupFrom(@NotNull Map<String, String> importFrom, String keyStartsWith) {
        importFrom.forEach((k, v) -> {
            if (k.startsWith(keyStartsWith)) data.put(k, v);
        });
        return this;
    }

    @SneakyThrows
    @Override
    public LocalStorage importGroupFrom(Path filePath, String keyStartsWith) {
        Files.readAllLines(filePath).forEach(line -> {
            if (line.startsWith(keyStartsWith)) parseLineAndPut(line);
        });
        return this;
    }

    @SneakyThrows
    @Override
    public LocalStorage deleteResources() {
        clear();
        Path dir = DirectoryHelper.createOrGetDirectory(this);
        Files.deleteIfExists(Path.of(dir.toString() + File.separatorChar + filename));
        DirectoryHelper.deleteDirectoryIfEmpty(this);
        return this;
    }

    @Override
    public LocalStorage remove(String key) {
        data.remove(key);
        return this;
    }

    @Override
    public LocalStorage removeGroup(String keyStartsWith) {
        toSortedMapGroup(keyStartsWith).forEach((k, v) -> remove(k));
        return this;
    }

    @SneakyThrows
    @Override
    public LocalStorage load() {
        Path dir = DirectoryHelper.createOrGetDirectory(this);

        System.out.println(dir);
        try {
            Files.readAllLines(
                    Path.of(dir.toString() + File.separatorChar + getFilename())
            ).forEach(this::parseLineAndPut);
        } catch (Exception e) {
            // TODO: log error
            e.printStackTrace();
        }
        return this;
    }

    private void parseLineAndPut(String line) {
        StringTokenizer stringTokenizer = new StringTokenizer(line, DELIMITER);
        String key = stringTokenizer.nextToken();
        String value = stringTokenizer.nextToken();
        data.put(key, value);
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

    @Override
    public String stringify() {
        StringBuilder stringBuilder = new StringBuilder();
        toSortedMap().forEach((key, value) -> stringBuilder
                .append(key)
                .append(DELIMITER)
                .append(value)
                .append('\n')
        );
        return stringBuilder.toString();
    }

    @Override
    public String stringifyGroup(String keyStartsWith) {
        StringBuilder stringBuilder = new StringBuilder();
        toSortedMapGroup(keyStartsWith).forEach((key, value) -> stringBuilder
                .append(key)
                .append(DELIMITER)
                .append(value)
                .append('\n')
        );
        return stringBuilder.toString();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public Map<String, String> toMap() {
        return Map.copyOf(data);
    }

    @Override
    public Map<String, String> toSortedMap() {
        return new TreeMap<>(toMap());
    }

    @Override
    public Map<String, String> toSortedMapGroup(String startsWith) {
        Map<String, String> map = new TreeMap<>();
        toMap().forEach((k, v) -> {
            if (k.startsWith(startsWith)) map.put(k, v);
        });
        return map;
    }

    @Override
    public String toFormattedString() {
        return toFormattedString(true);
    }

    @Override
    public String toFormattedString(boolean decorated) {
        TextTable textTable = new TextTable(decorated, "Key", "Value");
        toSortedMap().forEach(textTable::addRow);
        return textTable.render();
    }

    @Override
    public String toFormattedStringGroup(String keyStartsWith) {
        return toFormattedStringGroup(keyStartsWith, true);
    }

    @Override
    public String toFormattedStringGroup(String keyStartsWith, boolean decorated) {
        TextTable textTable = new TextTable(decorated, "Key", "Value");
        toSortedMapGroup(keyStartsWith).forEach(textTable::addRow);
        return textTable.render();
    }

    @Override
    public String getDirectoryPath() {
        return directoryPath;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public String getStorageId() {
        return storageId;
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
