package com.twoeightnine.root.xvii.utils.crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES256Cipher {

    private static final int KEY_LENGTH = 32;
    private static final int IV_LENGTH = 16;
    private static final String CIPHER = "AES/CBC/PKCS5Padding";
    private static final String AES = "AES";
    private static final String KEY_LENGTH_EXCEPTION = "AES-256 requires " + KEY_LENGTH + " byte of key";
    private static final String IV_LENGTH_EXCEPTION = "AES-256 requires " + IV_LENGTH + " byte of IV";

    public static byte[] encrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
            throws java.io.UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {

        if (keyBytes.length != KEY_LENGTH) {
            throw new InvalidKeyException(KEY_LENGTH_EXCEPTION);
        }
        if (ivBytes.length != IV_LENGTH) {
            throw new InvalidKeyException(IV_LENGTH_EXCEPTION);
        }
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, AES);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(textBytes);
    }

    public static byte[] decrypt(byte[] ivBytes, byte[] keyBytes, byte[] textBytes)
            throws java.io.UnsupportedEncodingException,
            NoSuchAlgorithmException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            IllegalBlockSizeException,
            BadPaddingException {

        if (keyBytes.length != KEY_LENGTH) {
            throw new InvalidKeyException(KEY_LENGTH_EXCEPTION);
        }
        if (ivBytes.length != IV_LENGTH) {
            throw new InvalidKeyException(IV_LENGTH_EXCEPTION);
        }
        AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, AES);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
        return cipher.doFinal(textBytes);
    }
}
