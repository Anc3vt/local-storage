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

import java.lang.reflect.InvocationTargetException;

public class LocalStorageBuilder {

    private final Class<? extends LocalStorage> type;
    private final String filename;
    private boolean saveOnWrite = false;
    private String storageId = LocalStorage.class.getName();
    private String directoryPath = null;

    /**
     * @param type values: {@link EncryptedFileLocalStorage}.class or {@link FileLocalStorage}.class
     * @param filename
     */
    public LocalStorageBuilder(String filename, Class<? extends LocalStorage> type) {
        this.type = type;
        this.filename = filename;
    }

    public LocalStorageBuilder directoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
        return this;
    }

    public LocalStorageBuilder saveOnWrite(boolean saveOnWrite) {
        this.saveOnWrite = saveOnWrite;
        return this;
    }

    public LocalStorageBuilder storageId(String storageId) {
        this.storageId = storageId;
        return this;
    }

    public LocalStorage build() {
        try {
            return (LocalStorage) type.getDeclaredConstructors()[0].newInstance(
                    filename,
                    saveOnWrite,
                    storageId,
                    directoryPath
            );
        } catch (IllegalAccessException |
                InstantiationException |
                InvocationTargetException e) {
            throw new LocalStorageException(e);
        }
    }

}
