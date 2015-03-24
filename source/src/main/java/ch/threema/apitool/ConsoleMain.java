/*
 * $Id$
 *
 * The MIT License (MIT)
 * Copyright (c) 2015 Threema GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE
 */
package ch.threema.apitool;

import ch.threema.apitool.exceptions.InvalidKeyException;
import ch.threema.apitool.exceptions.MessageParseException;
import com.neilalexander.jnacl.NaCl;

import java.io.*;

/**
 * Command line interface for {@link CryptTool} and {@link APIConnector} operations
 * for testing purposes and simple invocation from other programming languages.
 */
public class ConsoleMain {

	public static void main(String[] args) throws Exception {

		if (args.length == 3 && (args[0].equals("-e"))) {
			handleEncryptTextMessage(args[1], args[2]);
		} else if (args.length == 4 && (args[0].equals("-d"))) {
			handleDecryptMessage(args[1], args[2], args[3]);
		} else if (args.length == 3 && args[0].equals("-g")) {
			handleGenerateKeyPair(args[1], args[2]);
		} else if (args.length == 3 && args[0].equals("-h")) {
			if (args[1].equals("-e"))
				handleHashEmail(args[2]);
			else if (args[1].equals("-p"))
				handleHashPhone(args[2]);
			else
				usage();
		} else if (args.length == 2 && (args[0].equals("-d"))) {
			handleDerivePublicKey(args[1]);
		} else if (args.length == 4 && args[0].equals("-s")) {
			handleSendSimple(args[1], args[2], args[3]);
		} else if (args.length == 6 && args[0].equals("-S")) {
			handleSendEndToEnd(args[1], args[2], args[3], args[4], args[5]);
		} else if (args.length == 5 && args[0].equals("-l")) {
			if (args[1].equals("-e"))
				handleLookupEmail(args[2], args[3], args[4]);
			else if (args[1].equals("-p"))
				handleLookupPhone(args[2], args[3], args[4]);
			else if (args[1].equals("-k"))
				handleLookupKey(args[2], args[3], args[4]);
			else
				usage();
		} else {
			usage();
		}
	}

	private static void handleEncryptTextMessage(String privateKeyStr, String publicKeyStr) throws IOException, InvalidKeyException {
		byte[] privateKey = readKey(privateKeyStr, Key.KeyType.PRIVATE);
		byte[] publicKey = readKey(publicKeyStr, Key.KeyType.PUBLIC);
		byte[] nonce = CryptTool.randomNonce();

		/* read text from stdin */
		String text = readStream(System.in, "UTF-8").trim();

		byte[] box = CryptTool.encryptTextMessage(text, privateKey, publicKey, nonce);

		System.out.println(DataUtils.byteArrayToHexString(nonce));
		System.out.println(DataUtils.byteArrayToHexString(box));
	}

	private static void handleDecryptMessage(String privateKeyStr, String publicKeyStr, String nonceStr) throws IOException, MessageParseException, InvalidKeyException {
		byte[] privateKey = readKey(privateKeyStr, Key.KeyType.PRIVATE);
		byte[] publicKey = readKey(publicKeyStr, Key.KeyType.PUBLIC);
		byte[] nonce = DataUtils.hexStringToByteArray(nonceStr);

		/* read box from stdin */
		byte[] box = DataUtils.hexStringToByteArray(readStream(System.in, "UTF-8"));

		ThreemaMessage message = CryptTool.decryptMessage(box, privateKey, publicKey, nonce);

		System.out.println(message);
	}

	private static void handleGenerateKeyPair(String privateKeyFile, String publicKeyFile) throws IOException {
		byte[] privateKey = new byte[NaCl.SECRETKEYBYTES];
		byte[] publicKey = new byte[NaCl.PUBLICKEYBYTES];

		CryptTool.generateKeyPair(privateKey, publicKey);

		// Write both keys to file
		DataUtils.writeKeyFile(new File(privateKeyFile), new Key(Key.KeyType.PRIVATE, privateKey));
		DataUtils.writeKeyFile(new File(publicKeyFile), new Key(Key.KeyType.PUBLIC, publicKey));
	}

	private static void handleHashEmail(String email) {
		byte[] emailHash = CryptTool.hashEmail(email);
		System.out.println(DataUtils.byteArrayToHexString(emailHash));
	}

	private static void handleHashPhone(String phone) {
		byte[] phoneHash = CryptTool.hashPhoneNo(phone);
		System.out.println(DataUtils.byteArrayToHexString(phoneHash));
	}

	private static void handleDerivePublicKey(String privateKeyStr) throws IOException, InvalidKeyException {
		byte[] privateKey = readKey(privateKeyStr, Key.KeyType.PRIVATE);
		byte[] publicKey = CryptTool.derivePublicKey(privateKey);

		System.out.println(new Key(Key.KeyType.PUBLIC, publicKey).encode());
	}

	private static void handleSendSimple(String to, String from, String secret) throws IOException {
		/* read text from stdin */
		String text = readStream(System.in, "UTF-8").trim();

		APIConnector apiConnector = new APIConnector(from, secret);
		String messageId = apiConnector.sendTextMessageSimple(to, text);
		System.out.println(messageId);
	}

	private static void handleSendEndToEnd(String to, String from, String secret, String privateKeyStr, String publicKeyStr) throws IOException, InvalidKeyException {
		/* read text from stdin */
		String text = readStream(System.in, "UTF-8").trim();

		byte[] privateKey = readKey(privateKeyStr, Key.KeyType.PRIVATE);
		byte[] publicKey = readKey(publicKeyStr, Key.KeyType.PUBLIC);

		byte[] nonce = CryptTool.randomNonce();
		byte[] box = CryptTool.encryptTextMessage(text, privateKey, publicKey, nonce);

		APIConnector apiConnector = new APIConnector(from, secret);
		String messageId = apiConnector.sendTextMessageEndToEnd(to, nonce, box);
		System.out.println(messageId);
	}

	private static void handleLookupEmail(String email, String from, String secret) throws IOException {
		APIConnector apiConnector = new APIConnector(from, secret);
		String id = apiConnector.lookupEmail(email);
		if (id != null)
			System.out.println(id);
	}

	private static void handleLookupPhone(String phone, String from, String secret) throws IOException {
		APIConnector apiConnector = new APIConnector(from, secret);
		String id = apiConnector.lookupPhone(phone);
		if (id != null)
			System.out.println(id);
	}

	private static void handleLookupKey(String id, String from, String secret) throws IOException {
		APIConnector apiConnector = new APIConnector(from, secret);
		byte[] publicKey = apiConnector.lookupKey(id);
		if (publicKey != null) {
			System.out.println(new Key(Key.KeyType.PUBLIC, publicKey).encode());
		}
	}

	private static void usage() {
		System.out.println("version:" + ConsoleMain.class.getPackage().getImplementationVersion());

		System.out.println("usage:\n");

		System.out.println("General information");
		System.out.println("-------------------\n");

		System.out.println("Where a key needs to be specified, it can either be given directly as");
		System.out.println("a command line parameter (in hex with a prefix indicating the type;");
		System.out.println("not recommended on shared machines as other users may be able to see");
		System.out.println("the arguments), or as the path to a file that it should be read from");
		System.out.println("(file contents also in hex with the prefix).\n");


		System.out.println("\nLocal operations (no network communication)");
		System.out.println("-------------------------------------------\n");

		System.out.println("ThreemaAPITool -e <privateKey> <publicKey>");
		System.out.println("	Encrypt standard input using the given sender private key and recipient public key.");
		System.out.println("	Prints two lines to standard output: first the nonce (hex), and then the");
		System.out.println("	encrypted box (hex).\n");

		System.out.println("ThreemaAPITool -d <privateKey> <publicKey> <nonce>");
		System.out.println("	Decrypt standard input using the given recipient private key and sender public key.");
		System.out.println("	The nonce must be given on the command line, and the box (hex) on standard input.");
		System.out.println("	Prints the decrypted message to standard output.\n");

		System.out.println("ThreemaAPITool -g <privateKeyFile> <publicKeyFile>");
		System.out.println("	Generate a new key pair and write the private and public keys to");
		System.out.println("	the respective files.\n");

		System.out.println("ThreemaAPITool -h -e <email>");
		System.out.println("	Hash an email address for identity lookup. Prints the hash in hex.\n");

		System.out.println("ThreemaAPITool -h -p <phoneNo>");
		System.out.println("	Hash a phone number for identity lookup. Phone number must be specified as E.164.");
		System.out.println("	Prints the hash in hex.\n");

		System.out.println("ThreemaAPITool -d <privateKey>");
		System.out.println("	Derive the public key that corresponds with the given private key.\n");


		System.out.println("\nNetwork operations");
		System.out.println("------------------\n");

		System.out.println("ThreemaAPITool -s <to> <from> <secret>");
		System.out.println("	Send a message from standard input with server-side encryption to the given ID.");
		System.out.println("	'from' is the API identity and 'secret' is the API secret.");
		System.out.println("	Prints the message ID on success.\n");

		System.out.println("ThreemaAPITool -S <to> <from> <secret> <privateKey> <publicKey>");
		System.out.println("	Encrypt standard input and send the message to the given ID.");
		System.out.println("	'from' is the API identity and 'secret' is the API secret.");
		System.out.println("	Prints the message ID on success.\n");

		System.out.println("ThreemaAPITool -l -e <email> <from> <secret>");
		System.out.println("	Lookup the ID linked to the given email address (will be hashed locally).\n");

		System.out.println("ThreemaAPITool -l -p <phoneNo> <from> <secret>");
		System.out.println("	Lookup the ID linked to the given phone number (will be hashed locally).\n");

		System.out.println("ThreemaAPITool -l -k <id> <from> <secret>");
		System.out.println("	Lookup the public key for the given ID.\n");
	}

	private static byte[] readKey(String argument, String expectedKeyType) throws IOException, InvalidKeyException {
		Key key;

		// Try to open a file with that name
		File keyFile = new File(argument);
		if (keyFile.isFile()) {
			key = DataUtils.readKeyFile(keyFile, expectedKeyType);
		} else {
			key = Key.decodeKey(argument, expectedKeyType);
		}

		return key.key;
	}

	private static String readStream(InputStream stream, String charset) throws IOException {
		try {
			Reader reader = new BufferedReader(new InputStreamReader(stream, charset));
			StringBuilder builder = new StringBuilder();
			char[] buffer = new char[8192];
			int read;
			while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
				builder.append(buffer, 0, read);
			}
			return builder.toString();
		} finally {
			stream.close();
		}
	}
}
