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

    LocalStorage delete(String key);

    LocalStorage addMap(Map<String, String> map);

    LocalStorage clear();

    LocalStorage exportTo(@NotNull Map<String, String> map);

    LocalStorage load();

    LocalStorage delete();

    void save();

    String stringify();

    int getItemCount();

    Map<String, String> toMap();

    String toFormattedString();

    String toFormattedString(boolean decorated);

    String getDirectoryPath();

    String getFilename();

    String getApplicationId();
}
