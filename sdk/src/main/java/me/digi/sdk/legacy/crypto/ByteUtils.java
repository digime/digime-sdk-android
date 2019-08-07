/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.legacy.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@SuppressWarnings("WeakerAccess")
public class ByteUtils {

    private static final Charset UTF_8 = StandardCharsets.UTF_8;

    public static String bytesToHex(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static byte[] hexToBytes(String in) {
        int len = in.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(in.charAt(i), 16) << 4) + Character.digit(in.charAt(i + 1), 16));
        }

        return data;
    }

    public static String hexToUTF(String in) {
        return bytesToString(hexToBytes(in));
    }

    public static String bytesToString(byte[] in) {
        return new String(in, UTF_8);
    }

    public static String hexToBinary(String hex, int size) {
        String bin = new BigInteger(hex, size).toString(2);
        int inb = Integer.parseInt(bin);
        bin = String.format(Locale.ENGLISH, "%08d", inb);
        return bin;
    }

    public static String bytesToBinary(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for (int i = 0; i < Byte.SIZE * bytes.length; i++)
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }

    public static byte[] binaryToBytes(String binary) {
        short a = Short.parseShort(binary, 2);
        ByteBuffer bytes = ByteBuffer.allocate(2).putShort(a);
        return bytes.array();
    }

    public static byte[] intToBytes(int input) {
        return new byte[]{
                (byte) (input >>> 24),
                (byte) (input >>> 16),
                (byte) (input >>> 8),
                (byte) input};
    }

    public static int bytesToInt(byte[] b) {
        int value = 0;
        for (byte aB : b) value = (value << 8) | aB;
        return value;
    }

    public static byte[] readBytesFromStream(InputStream stream) throws IOException {
        int nRead;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[16];

        while ((nRead = stream.read(data)) != -1) {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }

    public static byte[] getDataAndHashBytes(byte[] data) {
        byte[] hash = CryptoUtils.hashSha512(data);
        return concatenateByteArrays(hash, data);
    }

    public static byte[] concatenateByteArrays(byte[] b1, byte[] b2) {
        byte[] concatenated = new byte[b2.length + b1.length];
        System.arraycopy(b1, 0, concatenated, 0, b1.length);
        System.arraycopy(b2, 0, concatenated, b1.length, b2.length);
        return concatenated;
    }
}
