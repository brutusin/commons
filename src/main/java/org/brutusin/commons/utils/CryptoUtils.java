/*
 * Copyright 2015 Ignacio del Valle Alles idelvall@brutusin.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.brutusin.commons.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Ignacio del Valle Alles idelvall@brutusin.org
 */
public final class CryptoUtils {

    private CryptoUtils() {
    }

    /**
     * Calls {@code getHash(str, "SHA-512")}
     *
     * @param str
     * @return
     */
    public static String getHash512(String str) {
        try {
            return getHash(str, "SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Calls {@code getHash(str, "MD5")}
     *
     * @param str
     * @return
     */
    public static String getHashMD5(String str) {
        try {
            return getHash(str, "MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the hezadecimal representation of the string digest given the
     * specified algorithm.
     *
     * @param str
     * @param algorithm
     * @return
     * @throws NoSuchAlgorithmException
     */
    private static String getHash(String str, String algorithm) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.update(str.getBytes());

        byte byteData[] = md.digest();

        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < byteData.length; i++) {
            String hex = Integer.toHexString(0xff & byteData[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }
}
