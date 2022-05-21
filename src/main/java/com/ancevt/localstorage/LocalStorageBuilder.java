package com.ancevt.localstorage;

import java.lang.reflect.InvocationTargetException;

public class LocalStorageBuilder {

    private final Class<? extends LocalStorage> type;
    private final String filename;
    private boolean saveOnWrite = false;
    private String applicationId = LocalStorage.class.getName();
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

    public LocalStorageBuilder applicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    public LocalStorage build() {
        try {
            return (LocalStorage) type.getDeclaredConstructors()[0].newInstance(
                    filename,
                    saveOnWrite,
                    applicationId,
                    directoryPath
            );
        } catch (IllegalAccessException |
                InstantiationException |
                InvocationTargetException e) {
            throw new LocalStorageException(e);
        }
    }

}
