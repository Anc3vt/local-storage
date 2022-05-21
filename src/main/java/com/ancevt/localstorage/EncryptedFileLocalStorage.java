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

public class EncryptedFileLocalStorage extends LocalStorage {

    private static final String EXTENSION = ".ls";
    private static final String DELIMITER = "=";

    private final String name;
    private final Map<String, String> data;
    private final boolean saveOnWrite;

    EncryptedFileLocalStorage(@NotNull String name, boolean saveOnWrite) {
        this.name = name;
        this.saveOnWrite = saveOnWrite;
        data = new ConcurrentHashMap<>();

        if (Files.exists(Path.of(name + EXTENSION))) {
            if (EncryptionUtils.keyPairExists()) {
                try {
                    load();
                } catch (LocalStorageException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public String getString(String key) {
        return data.get(key);
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
    public LocalStorage delete(String key) {
        data.remove(key);
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
    public LocalStorage exportTo(@NotNull Map<String, String> map) {
        map.putAll(toMap());
        return this;
    }

    @SneakyThrows
    @Override
    public LocalStorage delete() {
        clear();
        Files.deleteIfExists(Path.of(name + EXTENSION));
        return this;
    }

    @SneakyThrows
    @Override
    public LocalStorage load() {
        String decrypt = EncryptionUtils.decrypt(Files.readAllBytes(Path.of(getName() + EXTENSION)));
        decrypt.lines().forEach(line -> {
            StringTokenizer stringTokenizer = new StringTokenizer(line, DELIMITER);
            String key = stringTokenizer.nextToken();
            String value = stringTokenizer.nextToken();
            data.put(key, value);
        });
        return this;
    }

    @SneakyThrows
    @Override
    public void save() {
        Files.write(
                Path.of(getName() + EXTENSION),
                EncryptionUtils.encrypt(stringify()),
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
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

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public Map<String, String> toMap() {
        return Map.copyOf(data);
    }

    @Override
    public String toFormattedString() {
        TextTable textTable = new TextTable(true, "Key", "Value");
        data.forEach(textTable::addRow);
        return textTable.render();
    }

    @Override
    public String toFormattedString(boolean decorated) {
        TextTable textTable = new TextTable(decorated, "Key", "Value");
        data.forEach(textTable::addRow);
        return textTable.render();
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        LocalStorage.getSettings().setLocalStorageClass(EncryptedFileLocalStorage.class);
        LocalStorage.getSettings().setDefaultLocalStorageName("local-storage");

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
                    case "save" -> {
                        LocalStorage.lookup().save();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
