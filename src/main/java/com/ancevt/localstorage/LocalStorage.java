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

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;

public interface LocalStorage {

    String getString(String key);

    int getInt(String key, int defaultValue);

    long getLong(String key, long defaultValue);

    boolean getBoolean(String key, boolean defaultValue);

    byte getByte(String key, byte defaultValue);

    short getShort(String key, short defaultValue);

    char getChar(String key, char defaultValue);

    float getFloat(String key, float defaultValue);

    double getDouble(String key, double defaultValue);

    LocalStorage put(String key, Object value);

    LocalStorage putAll(Map<String, String> map);

    LocalStorage addMap(Map<String, String> map);

    LocalStorage clear();

    LocalStorage exportTo(@NotNull Map<String, String> exportTo);

    LocalStorage exportTo(Path filePath);

    LocalStorage exportGroupTo(@NotNull Map<String, String> exportTo, String keyStartsWith);

    LocalStorage exportGroupTo(Path filePath, String keyStartsWith);

    LocalStorage importFrom(@NotNull Map<String, String> importFrom);

    LocalStorage importFrom(Path filePath);

    LocalStorage importGroupFrom(@NotNull Map<String, String> importFrom, String keyStartsWith);

    LocalStorage importGroupFrom(Path filePath, String keyStartsWith);

    LocalStorage load();

    LocalStorage deleteResources();

    LocalStorage remove(String key);

    LocalStorage removeGroup(String keyStartsWith);

    void save();

    String stringify();

    String stringifyGroup(String keyStartsWith);

    int getItemCount();

    Map<String, String> toMap();

    Map<String, String> toSortedMap();

    Map<String, String> toSortedMapGroup(String startsWith);

    String toFormattedString();

    String toFormattedString(boolean decorated);

    String toFormattedStringGroup(String keyStartsWith);

    String toFormattedStringGroup(String keyStartsWith, boolean decorated);

    String getDirectoryPath();

    String getFilename();

    String getStorageId();
}
