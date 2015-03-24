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

import com.neilalexander.jnacl.NaCl;
import org.junit.Test;

public class CryptToolTest {

	@Test
	public void testRandomNonce() throws Exception {
		byte[] randomNonce = CryptTool.randomNonce();

		//random nonce sould be a byte array
		Assert.assertNotNull("random nonce", randomNonce);

		//with a length of 24
		Assert.assertSame("nonce length", randomNonce.length, NaCl.NONCEBYTES);
	}

	@Test
	public void testCreateKeyPair() {
		byte[] privateKey = new byte[NaCl.SECRETKEYBYTES];
		byte[] publicKey = new byte[NaCl.PUBLICKEYBYTES];

		CryptTool.generateKeyPair(privateKey, publicKey);

		Assert.assertFalse("empty private key", Common.isEmpty(privateKey));
		Assert.assertFalse("empty public key", Common.isEmpty(publicKey));
	}

	@Test
	public void testDecrypt() throws Exception {
		String nonce = "0a1ec5b67b4d61a1ef91f55e8ce0471fee96ea5d8596dfd0";
		String box = "45181c7aed95a1c100b1b559116c61b43ce15d04014a805288b7d14bf3a993393264fe554794ce7d6007233e8ef5a0f1ccdd704f34e7c7b77c72c239182caf1d061d6fff6ffbbfe8d3b8f3475c2fe352e563aa60290c666b2e627761e32155e62f048b52ef2f39c13ac229f393c67811749467396ecd09f42d32a4eb419117d0451056ac18fac957c52b0cca67568e2d97e5a3fd829a77f914a1ad403c5909fd510a313033422ea5db71eaf43d483238612a54cb1ecfe55259b1de5579e67c6505df7d674d34a737edf721ea69d15b567bc2195ec67e172f3cb8d6842ca88c29138cc33e9351dbc1e4973a82e1cf428c1c763bb8f3eb57770f914a";

		Key privateKey = Key.decodeKey(Common.otherPrivateKey);
		Key publicKey = Key.decodeKey(Common.myPublicKey);

		ThreemaMessage message = CryptTool.decryptMessage(
				DataUtils.hexStringToByteArray(box),
				privateKey.key,
				publicKey.key,
				DataUtils.hexStringToByteArray(nonce)
		);

		Assert.assertNotNull(message);
		Assert.assertTrue("message is not a instance of text message", message instanceof TextMessage);
		Assert.assertEquals(((TextMessage) message).getText(), "Dies ist eine Testnachricht. äöü");
	}

	@Test
	public void testEncrypt() throws Exception {
		String text = "Dies ist eine Testnachricht. äöü";
		String nonce = "0a1ec5b67b4d61a1ef91f55e8ce0471fee96ea5d8596dfd0";

		Key privateKey = Key.decodeKey(Common.myPrivateKey);
		Key publicKey = Key.decodeKey(Common.otherPublicKey);

		byte[] box = CryptTool.encryptTextMessage(text, privateKey.key, publicKey.key, DataUtils.hexStringToByteArray(nonce));
		Assert.assertNotNull(box);
		Assert.assertFalse(Common.isEmpty(box));
	}

	@Test
	public void testDerivePublicKey() throws Exception{
		Key privateKey = Key.decodeKey(Common.myPrivateKey);
		Key publicKey = Key.decodeKey(Common.myPublicKey);
		byte[] derivedPublicKey = CryptTool.derivePublicKey(privateKey.key);
		Assert.assertNotNull("derived public key", derivedPublicKey);
		Assert.assertEquals(derivedPublicKey, publicKey.key);
	}
}