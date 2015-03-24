/*
 * $Id$
 *
 * Copyright (c) 2014 Threema GmbH. All rights reserved.
 *
 * This software is intended for use by Threema Message API customers only. Distribution prohibited.
 */

package ch.threema.apitool;

import ch.threema.apitool.exceptions.InvalidKeyException;

public class Key {
    public static final String separator = ":";

    public static class KeyType {
        public static final String PRIVATE = "private";
        public static final String PUBLIC = "public";
    }

    /* Attributes */
    public byte[] key;
    public String type;

    public Key(String type, byte[] key) {
        this.key = key;
        this.type = type;
    }

    /**
     * Decodes and validates an encoded key.
     * Encoded key format: type:hex_key
     *
     * @param encodedKey an encoded key
     * @throws ch.threema.apitool.exceptions.InvalidKeyException
     */
    public static Key decodeKey(String encodedKey) throws InvalidKeyException {
        // Split key and check length
        String[] keyArray = encodedKey.split(Key.separator);
        if (keyArray.length != 2) {
            throw new InvalidKeyException("Does not contain a valid key format");
        }

        // Unpack key
        String keyType = keyArray[0];
        String keyContent = keyArray[1];

        // Is this a valid hex key?
        if (!keyContent.matches("[0-9a-fA-F]{64}")) {
            throw new InvalidKeyException("Does not contain a valid key");
        }

        return new Key(keyType, DataUtils.hexStringToByteArray(keyContent));
    }

    /**
     * Decodes and validates an encoded key.
     * Encoded key format: type:hex_key
     *
     * @param encodedKey an encoded key
     * @param expectedKeyType the expected type of the key
     * @throws InvalidKeyException
     */
    public static Key decodeKey(String encodedKey, String expectedKeyType) throws InvalidKeyException {
        Key key = decodeKey(encodedKey);

        // Check key type
        if (!key.type.equals(expectedKeyType)) {
            throw new InvalidKeyException("Expected key type: " + expectedKeyType + ", got: " + key.type);
        }

        return key;
    }

    /**
     * Encodes a key.
     *
     * @return an encoded key
     */
    public String encode() {
        return this.type + Key.separator + DataUtils.byteArrayToHexString(this.key);
    }
}
