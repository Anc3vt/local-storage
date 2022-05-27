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
