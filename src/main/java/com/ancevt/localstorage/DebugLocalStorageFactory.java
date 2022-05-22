package com.ancevt.localstorage;

public class DebugLocalStorageFactory {

    private static LocalStorage localStorage;

    public static LocalStorage debugLocalStorage() {
        if (localStorage == null) {
            localStorage = new LocalStorageBuilder("debug-local-storage.ls", FileLocalStorage.class)
                    .saveOnWrite(true)
                    .storageId("debug-local-storage")
                    .directoryPath("./")
                    .build();
        }
        return localStorage;
    }
}
