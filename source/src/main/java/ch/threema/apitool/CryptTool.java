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

import ch.threema.apitool.exceptions.*;
import com.neilalexander.jnacl.NaCl;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains static methods to do various Threema cryptography related tasks.
 */
public class CryptTool {

	/* HMAC-SHA256 keys for email/mobile phone hashing */
	private static final byte[] EMAIL_HMAC_KEY = new byte[] {(byte)0x30,(byte)0xa5,(byte)0x50,(byte)0x0f,(byte)0xed,(byte)0x97,(byte)0x01,(byte)0xfa,(byte)0x6d,(byte)0xef,(byte)0xdb,(byte)0x61,(byte)0x08,(byte)0x41,(byte)0x90,(byte)0x0f,(byte)0xeb,(byte)0xb8,(byte)0xe4,(byte)0x30,(byte)0x88,(byte)0x1f,(byte)0x7a,(byte)0xd8,(byte)0x16,(byte)0x82,(byte)0x62,(byte)0x64,(byte)0xec,(byte)0x09,(byte)0xba,(byte)0xd7};
	private static final byte[] PHONENO_HMAC_KEY = new byte[] {(byte)0x85,(byte)0xad,(byte)0xf8,(byte)0x22,(byte)0x69,(byte)0x53,(byte)0xf3,(byte)0xd9,(byte)0x6c,(byte)0xfd,(byte)0x5d,(byte)0x09,(byte)0xbf,(byte)0x29,(byte)0x55,(byte)0x5e,(byte)0xb9,(byte)0x55,(byte)0xfc,(byte)0xd8,(byte)0xaa,(byte)0x5e,(byte)0xc4,(byte)0xf9,(byte)0xfc,(byte)0xd8,(byte)0x69,(byte)0xe2,(byte)0x58,(byte)0x37,(byte)0x07,(byte)0x23};

	private static final SecureRandom random = new SecureRandom();

	/**
	 * Encrypt a text message.
	 *
	 * @param text the text to be encrypted (max. 3500 bytes)
	 * @param senderPrivateKey the private key of the sending ID
	 * @param recipientPublicKey the public key of the receiving ID
	 * @param nonce the nonce to be used for the encryption (usually 24 random bytes)
	 * @return encrypted box
	 */
	public static byte[] encryptTextMessage(String text, byte[] senderPrivateKey, byte[] recipientPublicKey, byte[] nonce) {

		/* prepend type byte (0x01) to message data */
		byte[] textBytes;
		try {
			textBytes = text.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			/* should never happen, UTF-8 is always supported */
			throw new RuntimeException(e);
		}

		/* determine random amount of PKCS7 padding */
		int padbytes = random.nextInt(254) + 1;

		byte[] data = new byte[textBytes.length + 1 + padbytes];
		data[0] = 1;
		System.arraycopy(textBytes, 0, data, 1, textBytes.length);

		/* append padding */
		for (int i = 0; i < padbytes; i++) {
			data[i + textBytes.length + 1] = (byte)padbytes;
		}

		NaCl nacl = new NaCl(senderPrivateKey, recipientPublicKey);
		return nacl.encrypt(data, nonce);
	}

	/**
	 * Decrypt a message.
	 *
	 * @param box the box to be decrypted
	 * @param recipientPrivateKey the private key of the receiving ID
	 * @param senderPublicKey the public key of the sending ID
	 * @param nonce the nonce that was used for the encryption
	 * @return decrypted message (text or delivery receipt)
	 */
	public static ThreemaMessage decryptMessage(byte[] box,  byte[] recipientPrivateKey, byte[] senderPublicKey, byte[] nonce) throws MessageParseException {

		NaCl nacl = new NaCl(recipientPrivateKey, senderPublicKey);

		byte[] data = nacl.decrypt(box, nonce);
		if (data == null)
			throw new DecryptionFailedException();

		/* remove padding */
		int padbytes = data[data.length-1] & 0xFF;
		int realDataLength = data.length - padbytes;
		if (realDataLength < 1)
			throw new BadMessageException();     /* Bad message padding */

		/* first byte of data is type */
		int type = data[0] & 0xFF;

		switch (type) {
			case TextMessage.TYPE_CODE:
				/* Text message */
				if (realDataLength < 2)
					throw new BadMessageException();

				try {
					return new TextMessage(new String(data, 1, realDataLength - 1, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					/* should never happen, UTF-8 is always supported */
					throw new RuntimeException(e);
				}
			case DeliveryReceipt.TYPE_CODE:
				/* Delivery receipt */
				if (realDataLength < MessageId.MESSAGE_ID_LEN + 2 || ((realDataLength - 2) % MessageId.MESSAGE_ID_LEN) != 0)
					throw new BadMessageException();

				DeliveryReceipt.Type receiptType = DeliveryReceipt.Type.get(data[1] & 0xFF);
				if (receiptType == null)
					throw new BadMessageException();

				List<MessageId> messageIds = new LinkedList<MessageId>();

				int numMsgIds = ((realDataLength - 2) / MessageId.MESSAGE_ID_LEN);
				for (int i = 0; i < numMsgIds; i++) {
					messageIds.add(new MessageId(data, 2 + i*MessageId.MESSAGE_ID_LEN));
				}

				return new DeliveryReceipt(receiptType, messageIds);
			default:
				throw new UnsupportedMessageTypeException();
		}
	}

	/**
	 * Generate a new key pair.
	 *
	 * @param privateKey is used to return the generated private key (length must be NaCl.PRIVATEKEYBYTES)
	 * @param publicKey is used to return the generated public key (length must be NaCl.PUBLICKEYBYTES)
	 */
	public static void generateKeyPair(byte[] privateKey, byte[] publicKey) {
		if (publicKey.length != NaCl.PUBLICKEYBYTES || privateKey.length != NaCl.SECRETKEYBYTES)
			throw new IllegalArgumentException("Wrong key length");

		NaCl.genkeypair(publicKey, privateKey);
	}

	/**
	 * Hashes an email address for identity lookup.
	 *
	 * @param email the email address
	 * @return the raw hash
	 */
	public static byte[] hashEmail(String email) {
		try {
			Mac emailMac = Mac.getInstance("HmacSHA256");
			emailMac.init(new SecretKeySpec(EMAIL_HMAC_KEY, "HmacSHA256"));
			String normalizedEmail = email.toLowerCase().trim();
			return emailMac.doFinal(normalizedEmail.getBytes("US-ASCII"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Hashes a phone number for identity lookup.
	 *
	 * @param phoneNo the phone number
	 * @return the raw hash
	 */
	public static byte[] hashPhoneNo(String phoneNo) {
		try {
			Mac phoneMac = Mac.getInstance("HmacSHA256");
			phoneMac.init(new SecretKeySpec(PHONENO_HMAC_KEY, "HmacSHA256"));
			String normalizedPhoneNo = phoneNo.replaceAll("[^0-9]", "");
			return phoneMac.doFinal(normalizedPhoneNo.getBytes("US-ASCII"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Generate a random nonce.
	 *
	 * @return random nonce
	 */
	public static byte[] randomNonce() {
		byte[] nonce = new byte[NaCl.NONCEBYTES];
		random.nextBytes(nonce);
		return nonce;
	}

	/**
	 * Return the public key of a private key
	 * @param privateKey
	 * @return
	 */
	public static byte[] derivePublicKey(byte[] privateKey) {
		return NaCl.derivePublicKey(privateKey);
	}
}
