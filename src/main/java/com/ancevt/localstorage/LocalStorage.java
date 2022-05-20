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


import com.ancevt.util.args.Args;
import com.ancevt.util.texttable.TextTable;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Byte.parseByte;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.Short.parseShort;

public class LocalStorage {

    private static final String DEFAULT_FASTCACHE_FILENAME = "localstorage";
    private static final String EXTENSION = ".ls";
    private static final String DELIMITER = "=";
    private static final Map<String, LocalStorage> storages = new ConcurrentHashMap<>();
    private static boolean defaultSaveOnWrite = true;

    private final String name;
    private final Map<String, String> data;
    private final boolean saveOnWrite;

    private LocalStorage(@NotNull String name, boolean saveOnWrite) {
        this.name = name;
        this.saveOnWrite = saveOnWrite;
        data = new ConcurrentHashMap<>();

        if (Files.exists(Path.of(name + EXTENSION))) {
            load();
        }
    }

    public String getString(String key) {
        return data.get(key);
    }

    public int getInt(String key, int defaultValue) {
        try {
            return parseInt(getString(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public long getLong(String key, long defaultValue) {
        try {
            return parseLong(getString(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        try {
            return getString(key).equalsIgnoreCase("true");
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public byte getByte(String key, byte defaultValue) {
        try {
            return parseByte(getString(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public short getShort(String key, short defaultValue) {
        try {
            return parseShort(getString(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public char getChar(String key, char defaultValue) {
        try {
            return getString(key).charAt(0);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public float getFloat(String key, float defaultValue) {
        try {
            return parseFloat(getString(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public double getDouble(String key, double defaultValue) {
        try {
            return parseDouble(getString(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public LocalStorage put(String key, Object value) {
        data.put(key, String.valueOf(value));
        if (saveOnWrite) save();
        return this;
    }

    public LocalStorage putAll(Map<String, String> map) {
        data.putAll(map);
        if (saveOnWrite) save();
        return this;
    }

    public LocalStorage remove(String key) {
        data.remove(key);
        return this;
    }

    public LocalStorage addMap(Map<String, String> map) {
        data.putAll(map);
        if (saveOnWrite) save();
        return this;
    }

    public LocalStorage clear() {
        data.clear();
        if (saveOnWrite) save();
        return this;
    }

    public LocalStorage storeTo(@NotNull Map<String, String> map) {
        map.putAll(toMap());
        return this;
    }

    @SneakyThrows
    public LocalStorage load() {
        Files.readAllLines(Path.of(getName() + EXTENSION)).forEach(line -> {
            StringTokenizer stringTokenizer = new StringTokenizer(line, DELIMITER);
            String key = stringTokenizer.nextToken();
            String value = stringTokenizer.nextToken();
            data.put(key, value);
        });
        return this;
    }

    @SneakyThrows
    public void save() {
        Files.writeString(
                Path.of(getName() + EXTENSION),
                stringify(),
                StandardCharsets.UTF_8,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    public String getName() {
        return name;
    }

    public String stringify() {
        StringBuilder stringBuilder = new StringBuilder();
        data.forEach((key, value) -> stringBuilder
                .append(key)
                .append(DELIMITER)
                .append(value)
                .append('\n')
        );
        return stringBuilder.toString();
    }

    public int getItemCount() {
        return data.size();
    }

    public Map<String, String> toMap() {
        return Map.copyOf(data);
    }

    public String toFormattedString() {
        TextTable textTable = new TextTable(true, "Key", "Value");
        data.forEach(textTable::addRow);
        return textTable.render();
    }

    public String toFormattedString(boolean decorated) {
        TextTable textTable = new TextTable(decorated, "Key", "Value");
        data.forEach(textTable::addRow);
        return textTable.render();
    }

    public static void setDefaultSaveOnWrite(boolean defaultSaveOnWrite) {
        LocalStorage.defaultSaveOnWrite = defaultSaveOnWrite;
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull LocalStorage lookup() {
        return lookup(DEFAULT_FASTCACHE_FILENAME, defaultSaveOnWrite);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NotNull LocalStorage lookup(String name) {
        return storages.computeIfAbsent(name, localStorageName -> new LocalStorage(localStorageName, defaultSaveOnWrite));
    }

    public static @NotNull LocalStorage lookup(boolean saveOnWrite) {
        return lookup(DEFAULT_FASTCACHE_FILENAME, saveOnWrite);
    }

    public static @NotNull LocalStorage lookup(String name, boolean saveOnWrite) {
        return storages.computeIfAbsent(name, localStorageName -> new LocalStorage(localStorageName, saveOnWrite));
    }

    public static void main(String[] args) {
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
                        LocalStorage.lookup().put(key, value);
                    }
                    case "get" -> {
                        String key = tokens.next();
                        System.out.println(key + "=" + LocalStorage.lookup().getString(key));
                    }
                    case "ls" -> {
                        System.out.println(LocalStorage.lookup().toFormattedString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}









































