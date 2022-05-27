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

public class QuickLocalStorageFactory {

    private static LocalStorage localStorage;
    private static LocalStorage encryptedLocalStorage;

    public static LocalStorage quickLocalStorage() {
        if (localStorage == null) {
            localStorage = new LocalStorageBuilder("qls.ls", FileLocalStorage.class)
                    .saveOnWrite(true)
                    .storageId("quick-storage")
                    .directoryPath("./")
                    .build();
        }
        return localStorage;
    }

    public static LocalStorage quickEncryptedLocalStorage() {
        if (encryptedLocalStorage == null) {
            encryptedLocalStorage = new LocalStorageBuilder("qls.ls", FileLocalStorage.class)
                    .saveOnWrite(true)
                    .storageId("quick-storage")
                    .directoryPath("./")
                    .build();
        }
        return encryptedLocalStorage;
    }
}
