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

import lombok.SneakyThrows;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static com.ancevt.commons.platformdepend.OsDetector.isWindows;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

public class EncryptionUtils {

    private static final int KEY_SIZE = 2048;
    private static final String ALGORITHM = "RSA";
    private static final int MAX_STACK_OVERFLOW_SAFE_ATTEMPTS = 2;

    private static Path publicKeyPath;
    private static Path privateKeyPath;

    private static PrivateKey privateKey;
    private static PublicKey publicKey;

    private static int stackOverflowSafeAttempts = MAX_STACK_OVERFLOW_SAFE_ATTEMPTS;

    @SneakyThrows
    private static void generateKeyPairIfNotExists() {
        String homeDir = System.getProperty("user.home");

        Path dir;
        if (isWindows()) {
            dir = Path.of(homeDir + "\\AppData\\Roaming\\" + LocalStorage.getSettings().getLocalStorageId());
        } else {
            dir = Path.of(homeDir + "/.local/share/" + LocalStorage.getSettings().getLocalStorageId());
        }

        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        privateKeyPath = Path.of(dir.toString() + File.separatorChar + "rsa");
        publicKeyPath = Path.of(dir.toString() + File.separatorChar + "rsa.pub");

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
    public static boolean keyPairExists() {
        String homeDir = System.getProperty("user.home");

        Path dir;
        if (isWindows()) {
            dir = Path.of(homeDir + "\\AppData\\Roaming\\" + LocalStorage.getSettings().getLocalStorageId());
        } else {
            dir = Path.of(homeDir + "/.local/share/" + LocalStorage.getSettings().getLocalStorageId());
        }

        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        privateKeyPath = Path.of(dir.toString() + File.separatorChar + "rsa");
        publicKeyPath = Path.of(dir.toString() + File.separatorChar + "rsa.pub");

        return Files.exists(privateKeyPath) && Files.exists(publicKeyPath);
    }

    @SneakyThrows
    public static byte[] encrypt(String string) {
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
    @Contract("_ -> new")
    public static @NotNull String decrypt(byte[] bytes) {
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
    private static void repairKeyPair() {
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
