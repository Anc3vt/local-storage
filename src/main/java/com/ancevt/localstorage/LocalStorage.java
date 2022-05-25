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

import com.ancevt.util.texttable.TextTable;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import static java.lang.Byte.parseByte;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.Short.parseShort;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

abstract public class LocalStorage {

    protected static final String DELIMITER = "=";

    protected final Map<String, String> data;

    private final String filename;
    private final boolean saveOnWrite;
    private final String storageId;
    private final String directoryPath;

    public LocalStorage(@NotNull String filename,
                        boolean saveOnWrite,
                        String storageId,
                        String directoryPath) {
        this.filename = filename;
        this.saveOnWrite = saveOnWrite;
        this.storageId = storageId;
        this.directoryPath = directoryPath;
        data = new ConcurrentHashMap<>();
    }
    
    public boolean contains(String key) {
        return data.containsKey(key);
    }

    
    public String computeIfAbsent(String key, Function<String, String> mappingFunction) {
        return data.computeIfAbsent(key, mappingFunction);
    }

    
    public String getString(String key) {
        return data.get(key);
    }

    
    public String getString(String key, String defaultValue) {
        return data.getOrDefault(key, defaultValue);
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

    
    public LocalStorage parse(@NotNull String source) {
        source.lines().forEach(this::parseLine);
        return this;
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

    
    public LocalStorage exportTo(@NotNull Map<String, String> exportTo) {
        exportTo.putAll(toMap());
        return this;
    }

    @SneakyThrows
    
    public LocalStorage exportTo(Path filePath) {
        Files.writeString(filePath, stringify(), StandardCharsets.UTF_8, CREATE, WRITE, TRUNCATE_EXISTING);
        return this;
    }

    
    public LocalStorage exportGroupTo(@NotNull Map<String, String> exportTo, String keyStartsWith) {
        exportTo.putAll(toSortedMapGroup(keyStartsWith));
        return this;
    }

    @SneakyThrows
    
    public LocalStorage exportGroupTo(Path filePath, String keyStartsWith) {
        Files.writeString(filePath, stringifyGroup(keyStartsWith), StandardCharsets.UTF_8, CREATE, WRITE, TRUNCATE_EXISTING);
        return this;
    }

    
    public LocalStorage importFrom(@NotNull Map<String, String> importFrom) {
        data.putAll(importFrom);
        return this;
    }

    @SneakyThrows
    
    public LocalStorage importFrom(Path filePath) {
        Files.readAllLines(filePath).forEach(this::parseLine);
        return this;
    }

    
    public LocalStorage importGroupFrom(@NotNull Map<String, String> importFrom, String keyStartsWith) {
        importFrom.forEach((k, v) -> {
            if (k.startsWith(keyStartsWith)) data.put(k, v);
        });
        return this;
    }

    @SneakyThrows
    
    public LocalStorage importGroupFrom(Path filePath, String keyStartsWith) {
        Files.readAllLines(filePath).forEach(line -> {
            if (line.startsWith(keyStartsWith)) parseLine(line);
        });
        return this;
    }

    
    public LocalStorage remove(String key) {
        data.remove(key);
        return this;
    }

    
    public LocalStorage removeGroup(String keyStartsWith) {
        toSortedMapGroup(keyStartsWith).forEach((k, v) -> remove(k));
        return this;
    }

    
    public LocalStorage parseLine(@NotNull String line) {
        if (line.trim().equals("")) return this;

        if (line.trim().startsWith(DELIMITER))
            throw new LocalStorageException("local store line starts with \"%s\": %s".formatted(DELIMITER, line));

        if (!line.contains(DELIMITER))
            throw new LocalStorageException("No \"%s\" in local storage line: %s".formatted(DELIMITER, line));

        String[] split = line.split(DELIMITER, 2);
        String key = split[0].trim();
        String value = split[1].trim();

        if (value.equals("null")) {
            data.remove(key);
            return this;
        } else if (value.equals("\"\"")) {
            value = "";
        } else if (value.startsWith("\"") && value.endsWith("\"") && !value.equals("\"")) {
            value = value.substring(1, value.length() - 1);
        }

        data.put(key, value);
        return this;
    }

    
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

    
    public int getItemCount() {
        return data.size();
    }

    
    public Map<String, String> toMap() {
        return Map.copyOf(data);
    }

    
    public Map<String, String> toSortedMap() {
        return new TreeMap<>(toMap());
    }

    
    public Map<String, String> toSortedMapGroup(String startsWith) {
        Map<String, String> map = new TreeMap<>();
        toMap().forEach((k, v) -> {
            if (k.startsWith(startsWith)) map.put(k, v);
        });
        return map;
    }

    
    public String toFormattedString() {
        return toFormattedString(true);
    }

    
    public String toFormattedString(boolean decorated) {
        TextTable textTable = new TextTable(decorated, "Key", "Value");
        toSortedMap().forEach(textTable::addRow);
        return textTable.render();
    }

    
    public String toFormattedStringGroup(String keyStartsWith) {
        return toFormattedStringGroup(keyStartsWith, true);
    }

    
    public String toFormattedStringGroup(String keyStartsWith, boolean decorated) {
        TextTable textTable = new TextTable(decorated, "Key", "Value");
        toSortedMapGroup(keyStartsWith).forEach(textTable::addRow);
        return textTable.render();
    }

    
    public String getDirectoryPath() {
        return directoryPath;
    }

    
    public String getFilename() {
        return filename;
    }

    
    public String getStorageId() {
        return storageId;
    }

    abstract public void save();
    abstract public LocalStorage load();
    abstract public LocalStorage deleteResources();
}
