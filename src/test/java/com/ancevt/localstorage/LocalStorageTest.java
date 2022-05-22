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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

abstract public class LocalStorageTest {

    private static final String STORAGE_FILENAME = "teststorage";

    private static final Class<? extends LocalStorage> TYPE = FileLocalStorage.class;

    private final Class<? extends LocalStorage> localStorageType;

    public LocalStorageTest(Class<? extends LocalStorage> localStorageType) {
        this.localStorageType = localStorageType;
    }

    @Test
    void testQuotes() {
        LocalStorage localStorage = createLocalStorage("""
            key1="
            key2=""
            key3=\"""
            key4=\"""\"
            """);

        assertThat(localStorage.getString("key1"), is("\""));
        assertThat(localStorage.getString("key2"), is(""));
        assertThat(localStorage.getString("key3"), is("\""));
        assertThat(localStorage.getString("key4"), is("\"\""));
    }

    @Test
    void testNullValue() {
        LocalStorage localStorage = createLocalStorage("""
            key1=null
            key2="null"
            """);

        assertNull(localStorage.getString("key1"));
        assertThat(localStorage.getString("key2"), is("null"));
    }

    @Test
    void testEmptyValue() {
        assertThat(createLocalStorage("key=").getString("key"), is(""));
    }

    @Test
    void twoEqualsChars() {
        assertThat(createLocalStorage("two=equals=chars").getString("two"), is("equals=chars"));
    }

    @Test
    void testOneQuoteInEdge() {
        LocalStorage localStorage = createLocalStorage("""
            key1=" space edged value
            key2=space edged value \"""");
        assertThat(localStorage.getString("key1"), is("\" space edged value"));
        assertThat(localStorage.getString("key2"), is("space edged value \""));
    }

    @Test
    void testSpaceEdgedValue() {
        LocalStorage localStorage = createLocalStorage("key=\" space edged value \"");
        assertThat(localStorage.getString("key"), is(" space edged value "));
    }

    @Test
    void testEmptyKey() {
        assertThrows(LocalStorageException.class, () -> {
            createLocalStorage("=value");
        });
    }

    @Test
    void testPlain() {
        assertThat(createLocalStorage("key=value").getString("key"), is("value"));
    }

    @Test
    void testStringWithoutEqualsChar() {
        assertThrows(LocalStorageException.class, () -> {
            createLocalStorage("string without equals char");
        });
    }

    @Test
    void testEmptyString() {
        createLocalStorage("");
    }

    @Test
    void testBlankString() {
        createLocalStorage("  \t");
    }

    @AfterEach
    void dispose() {
        createLocalStorage(null).deleteResources();
    }

    private @NotNull LocalStorage createLocalStorage(String source) {
        LocalStorage localStorage = new LocalStorageBuilder(STORAGE_FILENAME, localStorageType)
                .storageId("test.localstorage")
                .saveOnWrite(true)
                .build();

        if (source != null) localStorage.parse(source);

        return localStorage;
    }

}
