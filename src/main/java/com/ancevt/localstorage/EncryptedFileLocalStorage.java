/*
 *   Ancevt LocalStorage
 *   Copyright (C) 2022 Ancevt (me@ancevt.com)
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General @Override public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General @Override public License for more details.
 *
 *   You should have received a copy of the GNU General @Override public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.ancevt.localstorage;


import com.ancevt.util.args.Args;
import com.ancevt.util.texttable.TextTable;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.Byte.parseByte;
import static java.lang.Double.parseDouble;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.Short.parseShort;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

public class EncryptedFileLocalStorage implements LocalStorage {

    private static final String DELIMITER = "=";

    private final String filename;
    private final Map<String, String> data;
    private final boolean saveOnWrite;
    private final String storageId;
    private final String directoryPath;
    private final EncryptionHelper encryptionHelper;

    EncryptedFileLocalStorage(@NotNull String filename,
                              boolean saveOnWrite,
                              String storageId,
                              String directoryPath) {

        this.filename = filename;
        this.saveOnWrite = saveOnWrite;
        this.storageId = storageId;
        this.directoryPath = directoryPath;
        data = new ConcurrentHashMap<>();
        encryptionHelper = new EncryptionHelper(this);

        Path dir = DirectoryHelper.createOrGetDirectory(this);

        if (Files.exists(Path.of(dir.toString() + File.separatorChar + filename))) {
            if (encryptionHelper.keyPairExists()) {
                try {
                    load();
                } catch (LocalStorageException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public String getString(String key) {
        return data.get(key);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        try {
            return parseInt(getString(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public long getLong(String key, long defaultValue) {
        try {
            return parseLong(getString(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        try {
            return getString(key).equalsIgnoreCase("true");
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public byte getByte(String key, byte defaultValue) {
        try {
            return parseByte(getString(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public short getShort(String key, short defaultValue) {
        try {
            return parseShort(getString(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public char getChar(String key, char defaultValue) {
        try {
            return getString(key).charAt(0);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        try {
            return parseFloat(getString(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        try {
            return parseDouble(getString(key));
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public LocalStorage put(String key, Object value) {
        data.put(key, String.valueOf(value));
        if (saveOnWrite) save();
        return this;
    }

    @Override
    public LocalStorage putAll(Map<String, String> map) {
        data.putAll(map);
        if (saveOnWrite) save();
        return this;
    }

    @Override
    public LocalStorage addMap(Map<String, String> map) {
        data.putAll(map);
        if (saveOnWrite) save();
        return this;
    }

    @Override
    public LocalStorage clear() {
        data.clear();
        if (saveOnWrite) save();
        return this;
    }

    @Override
    public LocalStorage exportTo(@NotNull Map<String, String> exportTo) {
        exportTo.putAll(toMap());
        return this;
    }

    @Override
    public LocalStorage exportGroupTo(@NotNull Map<String, String> exportTo, String keyStartsWith) {
        exportTo.putAll(toSortedMapGroup(keyStartsWith));
        return this;
    }

    @Override
    public LocalStorage importFrom(@NotNull Map<String, String> importFrom) {
        data.putAll(importFrom);
        return this;
    }

    @Override
    public LocalStorage importGroupFrom(@NotNull Map<String, String> importFrom, String keyStartsWith) {
        importFrom.forEach((k, v) -> {
            if (k.startsWith(keyStartsWith)) data.put(k, v);
        });
        return this;
    }

    @SneakyThrows
    @Override
    public LocalStorage deleteResources() {
        clear();
        Path dir = DirectoryHelper.createOrGetDirectory(this);
        encryptionHelper.deleteKeys();
        Files.deleteIfExists(Path.of(dir.toString() + File.separatorChar + filename));
        DirectoryHelper.deleteDirectoryIfEmpty(this);
        return this;
    }

    @SneakyThrows
    @Override
    public LocalStorage load() {
        Path dir = DirectoryHelper.createOrGetDirectory(this);

        String decrypt = encryptionHelper.decrypt(Files.readAllBytes(Path.of(dir.toString() + File.separatorChar + getFilename())));
        decrypt.lines().forEach(line -> {
            StringTokenizer stringTokenizer = new StringTokenizer(line, DELIMITER);
            String key = stringTokenizer.nextToken();
            String value = stringTokenizer.nextToken();
            data.put(key, value);
        });
        return this;
    }

    @SneakyThrows
    @Override
    public void save() {
        Path dir = DirectoryHelper.createOrGetDirectory(this);

        Files.write(
                Path.of(dir.toString() + File.separatorChar + getFilename()),
                encryptionHelper.encrypt(stringify()),
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    @Override
    public String stringify() {
        StringBuilder stringBuilder = new StringBuilder();
        toSortedMap().forEach((key, value) -> stringBuilder
                .append(key)
                .append(DELIMITER)
                .append(value)
                .append('\n')
        );
        return stringBuilder.toString();
    }

    @Override
    public String stringifyGroup(String keyStartsWith) {
        StringBuilder stringBuilder = new StringBuilder();
        toSortedMapGroup(keyStartsWith).forEach((key, value) -> stringBuilder
                .append(key)
                .append(DELIMITER)
                .append(value)
                .append('\n')
        );
        return stringBuilder.toString();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public Map<String, String> toMap() {
        return Map.copyOf(data);
    }

    @Override
    public Map<String, String> toSortedMap() {
        return new TreeMap<>(toMap());
    }

    @Override
    public Map<String, String> toSortedMapGroup(String startsWith) {
        Map<String, String> map = new TreeMap<>();
        toMap().forEach((k, v) -> {
            if (k.startsWith(startsWith)) map.put(k, v);
        });
        return map;
    }

    @Override
    public LocalStorage remove(String key) {
        data.remove(key);
        return this;
    }

    @Override
    public LocalStorage removeGroup(String keyStartsWith) {
        toSortedMapGroup(keyStartsWith).forEach((k, v) -> remove(k));
        return this;
    }

    @Override
    public String toFormattedString() {
        TextTable textTable = new TextTable(true, "Key", "Value");
        data.forEach(textTable::addRow);
        return textTable.render();
    }

    @Override
    public String toFormattedString(boolean decorated) {
        TextTable textTable = new TextTable(decorated, "Key", "Value");
        data.forEach(textTable::addRow);
        return textTable.render();
    }

    @Override
    public String toFormattedStringGroup(String keyStartsWith) {
        return toFormattedStringGroup(keyStartsWith, true);
    }

    @Override
    public String toFormattedStringGroup(String keyStartsWith, boolean decorated) {
        TextTable textTable = new TextTable(decorated, "Key", "Value");
        toSortedMapGroup(keyStartsWith).forEach(textTable::addRow);
        return textTable.render();
    }

    @Override
    public String getDirectoryPath() {
        return directoryPath;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public String getStorageId() {
        return storageId;
    }

    private static class EncryptionHelper {

        private static final int KEY_SIZE = 2048;
        private static final String ALGORITHM = "RSA";
        private static final int MAX_STACK_OVERFLOW_SAFE_ATTEMPTS = 2;

        private static int stackOverflowSafeAttempts = MAX_STACK_OVERFLOW_SAFE_ATTEMPTS;

        private Path publicKeyPath;
        private Path privateKeyPath;

        private PrivateKey privateKey;
        private PublicKey publicKey;

        private final EncryptedFileLocalStorage encryptedFileLocalStorage;

        public EncryptionHelper(EncryptedFileLocalStorage encryptedFileLocalStorage) {

            this.encryptedFileLocalStorage = encryptedFileLocalStorage;
        }

        @SneakyThrows
        private void generateKeyPairIfNotExists() {
            Path dir = DirectoryHelper.createOrGetDirectory(encryptedFileLocalStorage);

            privateKeyPath = Path.of(dir.toString() + File.separatorChar + encryptedFileLocalStorage.filename + ".rsa");
            publicKeyPath = Path.of(dir.toString() + File.separatorChar + encryptedFileLocalStorage.filename + ".rsa.pub");

            if (!Files.exists(privateKeyPath) || !Files.exists(publicKeyPath)) {
                KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
                generator.initialize(KEY_SIZE);
                KeyPair pair = generator.generateKeyPair();

                privateKey = pair.getPrivate();
                publicKey = pair.getPublic();

                Files.write(privateKeyPath, privateKey.getEncoded(), CREATE, WRITE, TRUNCATE_EXISTING);
                Files.write(publicKeyPath, publicKey.getEncoded(), CREATE, WRITE, TRUNCATE_EXISTING);
            }
        }

        @SneakyThrows
        public void deleteKeys() {
            Files.deleteIfExists(publicKeyPath);
            Files.deleteIfExists(privateKeyPath);
        }

        @SneakyThrows
        public boolean keyPairExists() {
            Path dir = DirectoryHelper.createOrGetDirectory(encryptedFileLocalStorage);

            privateKeyPath = Path.of(dir.toString() + File.separatorChar + encryptedFileLocalStorage.filename + ".rsa");
            publicKeyPath = Path.of(dir.toString() + File.separatorChar + encryptedFileLocalStorage.filename + ".rsa.pub");

            return Files.exists(privateKeyPath) && Files.exists(publicKeyPath);
        }

        @SneakyThrows
        public byte[] encrypt(String string) {
            generateKeyPairIfNotExists();
            if (publicKey == null) {
                KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
                byte[] publicKeyBytes = Files.readAllBytes(publicKeyPath);
                EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
                publicKey = keyFactory.generatePublic(publicKeySpec);
            }

            Cipher encryptCipher = Cipher.getInstance(ALGORITHM);
            try {
                encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
                System.err.println("Public key is invalid. Attempting to regenerate key pair");
                repairKeyPair();
                return encrypt(string);
            }

            byte[] stringBytes = string.getBytes(StandardCharsets.UTF_8);
            byte[] result;
            try {
                result = encryptCipher.doFinal(stringBytes);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
                System.err.println("Public key is invalid. Attempting to regenerate key pair");
                repairKeyPair();
                return encrypt(string);
            }

            stackOverflowSafeAttempts = MAX_STACK_OVERFLOW_SAFE_ATTEMPTS;

            return result;
        }

        @SneakyThrows
        public @NotNull String decrypt(byte[] bytes) {
            generateKeyPairIfNotExists();
            if (privateKey == null) {
                KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
                byte[] privateKeyBytes = Files.readAllBytes(privateKeyPath);
                EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
                privateKey = keyFactory.generatePrivate(privateKeySpec);
            }

            Cipher decryptCipher = Cipher.getInstance(ALGORITHM);
            try {
                decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
                System.err.println("Private key is invalid. Attempting to regenerate key pair");
                repairKeyPair();
                return decrypt(bytes);
            }

            byte[] decryptedMessageBytes;
            try {
                decryptedMessageBytes = decryptCipher.doFinal(bytes);
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                e.printStackTrace();
                System.err.println("Private key is invalid. Attempting to regenerate key pair");
                repairKeyPair();
                return decrypt(bytes);
            }

            stackOverflowSafeAttempts = MAX_STACK_OVERFLOW_SAFE_ATTEMPTS;

            return new String(decryptedMessageBytes, StandardCharsets.UTF_8);
        }

        @SneakyThrows
        private void repairKeyPair() {
            Files.deleteIfExists(publicKeyPath);
            Files.deleteIfExists(privateKeyPath);

            generateKeyPairIfNotExists();

            stackOverflowSafeAttempts--;
            if (stackOverflowSafeAttempts <= 0) {
                stackOverflowSafeAttempts = MAX_STACK_OVERFLOW_SAFE_ATTEMPTS;
                throw new LocalStorageException("Attempts to regenerate keys have been exhausted");
            }
        }
    }

    public static void main(String[] args) {

        LocalStorage localStorage = new LocalStorageBuilder("localstorage", EncryptedFileLocalStorage.class)
                .saveOnWrite(false)
                .directoryPath("./_storage/")
                .storageId(EncryptedFileLocalStorage.class.getName())
                .build();

        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            try {
                Args tokens = new Args(scanner.nextLine());
                String command = tokens.next();

                switch (command) {
                    case "set" -> {
                        String keyValue = tokens.next();
                        StringTokenizer stringTokenizer = new StringTokenizer(keyValue, "=");
                        String key = stringTokenizer.nextToken();
                        String value = stringTokenizer.nextToken();
                        localStorage.put(key, value);
                    }
                    case "get" -> {
                        String key = tokens.next();
                        System.out.println(localStorage.getString(key));
                    }
                    case "ls" -> {
                        System.out.println(localStorage.toFormattedString());
                    }
                    case "lsg" -> {
                        String keyStartsWith = tokens.next();
                        System.out.println(localStorage.toFormattedStringGroup(keyStartsWith));
                    }
                    case "save" -> {
                        localStorage.save();
                    }
                    case "rm" -> {
                        String key = tokens.next();
                        localStorage.remove(key);
                    }
                    case "rmg" -> {
                        String keyStartsWith = tokens.next();
                        localStorage.removeGroup(keyStartsWith);
                    }
                    case "delete" -> {
                        localStorage.deleteResources();
                    }
                    case "cls" -> {
                        for (int i = 0; i < 100; i++) {
                            System.out.println();
                        }
                    }

                    default -> {
                        System.err.println("Unknown command: " + command);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
