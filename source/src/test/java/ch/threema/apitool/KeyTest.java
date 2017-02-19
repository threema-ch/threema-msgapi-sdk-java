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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import ch.threema.apitool.exceptions.InvalidKeyException;

public class KeyTest {

	@Test
	public void testDecodeWrongKey() {
		try {
			Key.decodeKey("imnotarealkey");
			fail("could parse invalid key");
		} catch (InvalidKeyException e) {
			return;
		}
	}

	@Test
	public void testDecodeKeyPrivate() throws Exception {
		Key key = Key.decodeKey("private:1234567890123456789012345678901234567890123456789012345678901234");
		assertNotNull("key instance", key);

		assertEquals(key.type, Key.KeyType.PRIVATE);
		assertArrayEquals(key.key, DataUtils.hexStringToByteArray("1234567890123456789012345678901234567890123456789012345678901234"));
	}

	@Test
	public void testDecodeKeyPublic() throws Exception {
		Key key = Key.decodeKey("public:1234567890123456789012345678901234567890123456789012345678901234");
		assertNotNull("key instance", key);

		assertEquals(key.type, Key.KeyType.PUBLIC);
		assertArrayEquals(key.key, DataUtils.hexStringToByteArray("1234567890123456789012345678901234567890123456789012345678901234"));
	}

	@Test
	public void testEncodePrivate() throws Exception {
		byte[] keyAsByte = DataUtils.hexStringToByteArray(Common.myPrivateKeyExtract);

		Key key = new Key(Key.KeyType.PRIVATE, keyAsByte);
		assertNotNull("key instance", key);

		assertEquals(Key.KeyType.PRIVATE, key.type);
		assertArrayEquals(key.key, keyAsByte);

		assertEquals(key.encode(), Common.myPrivateKey);
	}
}