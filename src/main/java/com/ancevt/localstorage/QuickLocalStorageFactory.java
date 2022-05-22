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
