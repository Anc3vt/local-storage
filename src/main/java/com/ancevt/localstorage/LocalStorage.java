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

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

abstract public class LocalStorage {

    private static final LocalStorageSettings settings = new LocalStorageSettings();
    private static final Map<String, LocalStorage> storages = new ConcurrentHashMap<>();

    public static LocalStorageSettings getSettings() {
        return settings;
    }

    abstract public String getString(String key);

    abstract public int getInt(String key, int defaultValue);

    abstract public long getLong(String key, long defaultValue);

    abstract public boolean getBoolean(String key, boolean defaultValue);

    abstract public byte getByte(String key, byte defaultValue);

    abstract public short getShort(String key, short defaultValue);

    abstract public char getChar(String key, char defaultValue);

    abstract public float getFloat(String key, float defaultValue);

    abstract public double getDouble(String key, double defaultValue);

    abstract public LocalStorage put(String key, Object value);

    abstract public LocalStorage putAll(Map<String, String> map);

    abstract public LocalStorage delete(String key);

    abstract public LocalStorage addMap(Map<String, String> map);

    abstract public LocalStorage clear();

    abstract public LocalStorage exportTo(@NotNull Map<String, String> map);

    abstract public LocalStorage load();

    abstract public LocalStorage delete();

    abstract public void save();

    abstract public String getName();

    abstract public String stringify();

    abstract public int getItemCount();

    abstract public Map<String, String> toMap();

    abstract public String toFormattedString();

    abstract public String toFormattedString(boolean decorated);

    public static @NotNull LocalStorage lookup() {
        return lookup(settings.getDefaultLocalStorageName(), settings.isDefaultSaveOnWrite());
    }

    public static @NotNull LocalStorage lookup(boolean saveOnWrite) {
        return lookup(settings.getDefaultLocalStorageName(), saveOnWrite);
    }

    public static @NotNull LocalStorage lookup(String name) {
        return lookup(name, settings.isDefaultSaveOnWrite());
    }

    public static @NotNull LocalStorage lookup(String name, boolean saveOnWrite) {
        return storages.computeIfAbsent(name, localStorageName -> {
            Class<? extends LocalStorage> clazz = settings.getLocalStorageClass();
            try {
                return clazz.getDeclaredConstructor(String.class, boolean.class).newInstance(localStorageName, saveOnWrite);
            } catch (NoSuchMethodException |
                    IllegalAccessException |
                    InstantiationException |
                    InvocationTargetException e) {
                throw new LocalStorageException(e);
            }
        });
    }
}
