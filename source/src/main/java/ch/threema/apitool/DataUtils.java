/*
 * $Id$
 *
 * Copyright (c) 2014 Threema GmbH. All rights reserved.
 *
 * This software is intended for use by Threema Message API customers only. Distribution prohibited.
 */

package ch.threema.apitool;

import ch.threema.apitool.exceptions.InvalidKeyException;

import java.io.*;

public class DataUtils {

	/**
	 * Convert a string in hexadecimal representation to a byte array.
	 *
	 * @param s hex string
	 * @return decoded byte array
	 */
	public static byte[] hexStringToByteArray(String s) {
		String sc = s.replaceAll("[^0-9a-fA-F]", "");
		int len = sc.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(sc.charAt(i), 16) << 4)
					+ Character.digit(sc.charAt(i+1), 16));
		}
		return data;
	}

	/**
	 * Convert a byte array into a hexadecimal string (lowercase).
	 *
	 * @param bytes the bytes to encode
	 * @return hex encoded string
	 */
	public static String byteArrayToHexString(byte[] bytes) {
		final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
		char[] hexChars = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	/**
	 * Read hexadecimal data from a file and return it as a byte array.
	 *
	 * @param inFile input file
	 * @return the decoded data
	 * @throws java.io.IOException
	 */
	public static byte[] readHexFile(File inFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		byte[] data = hexStringToByteArray(br.readLine().trim());
		br.close();
		return data;
	}

	/**
	 * Write a byte array into a file in hexadecimal format.
	 *
	 * @param outFile output file
	 * @param data the data to be written
	 */
	public static void writeHexFile(File outFile, byte[] data) throws IOException {
		FileWriter fw = new FileWriter(outFile);
		fw.write(byteArrayToHexString(data));
		fw.write('\n');
		fw.close();
	}

	/**
	 * Read an encoded key from a file and return it as a key instance.
	 *
	 * @param inFile input file
	 * @return the decoded key
	 * @throws java.io.IOException
	 */
	public static Key readKeyFile(File inFile) throws IOException, InvalidKeyException {
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		String encodedKey = br.readLine().trim();
		br.close();
		return Key.decodeKey(encodedKey);
	}

	/**
	 * Read an encoded key from a file and return it as a key instance.
	 *
	 * @param inFile input file
	 * @param expectedKeyType validates the key type (private or public)
	 * @return the decoded key
	 * @throws java.io.IOException
	 */
	public static Key readKeyFile(File inFile, String expectedKeyType) throws IOException, InvalidKeyException {
		BufferedReader br = new BufferedReader(new FileReader(inFile));
		String encodedKey = br.readLine().trim();
		br.close();
		return Key.decodeKey(encodedKey, expectedKeyType);
	}

	/**
	 * Write an encoded key to a file
	 * Encoded key format: type:hex_key.
	 *
	 * @param outFile output file
	 * @param key a key that will be encoded and written to a file
	 */
	public static void writeKeyFile(File outFile, Key key) throws IOException {
		FileWriter fw = new FileWriter(outFile);
		fw.write(key.encode());
		fw.write('\n');
		fw.close();
	}
}
