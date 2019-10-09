/*
 * Copyright (c) 2009-2019 digi.me Limited. All rights reserved.
 */

package me.digi.sdk.core.internal;

import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SuppressWarnings("SameParameterValue")
public final class Util {

    final private static char[] hexChars = "0123456789ABCDEF".toCharArray();
    public static String byteArrayToHexString(byte[] bytes) {
        char[] hexBytes = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexBytes[j * 2] = hexChars[v >>> 4];
            hexBytes[j * 2 + 1] = hexChars[v & 0x0F];
        }
        return new String(hexBytes);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for(int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    public static String digestStringWithLimit(String inString, int limit) {
        if (inString.length() <= limit) { return inString; }
        MessageDigest msgd;
        byte[] digestBytes;
        try {
            msgd = MessageDigest.getInstance("SHA-256");
            msgd.reset();
            digestBytes = msgd.digest(inString.getBytes());
        } catch (Exception ex) {
            if (ex instanceof NoSuchAlgorithmException) {
                Log.d("Utils", "SHA256 provider doesn't exist");
            } else {
                Log.d("Utils", "Failed to compute SHA-256");
            }
            return inString;
        }
        String outString = byteArrayToHexString(digestBytes);

        int trueLimit = limit >= outString.length() ? outString.length() : limit;
        return outString.substring(0, trueLimit);
    }

    public static boolean validateContractId(String contractId) {
        return contractId.matches("^[a-zA-Z0-9_]+$") && contractId.length() > 5 && contractId.length() < 64;
    }

    public static String removeNewLinesAndSpaces(String input) {
        return input.replaceAll("[\n\r ]", "");
    }

}
