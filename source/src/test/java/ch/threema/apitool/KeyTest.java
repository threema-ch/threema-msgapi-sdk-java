package ch.threema.apitool;

import ch.threema.apitool.exceptions.InvalidKeyException;
import org.junit.Test;

public class KeyTest {

	@Test
	public void testDecodeWrongKey() {
		try {
			Key.decodeKey("imnotarealkey");
		} catch (InvalidKeyException e) {
			return;
		}
		Assert.assertFalse("could parse invalid key", true);
	}

	@Test
	public void testDecodeKeyPrivate() throws Exception {
		Key key = Key.decodeKey("private:1234567890123456789012345678901234567890123456789012345678901234");
		Assert.assertNotNull("key instance", key);

		Assert.assertEquals(key.type, Key.KeyType.PRIVATE);
		Assert.assertEquals(key.key, DataUtils.hexStringToByteArray("1234567890123456789012345678901234567890123456789012345678901234"));
	}
	@Test
	public void testDecodeKeyPublic() throws Exception {
		Key key = Key.decodeKey("public:1234567890123456789012345678901234567890123456789012345678901234");
		Assert.assertNotNull("key instance", key);

		Assert.assertEquals(key.type, Key.KeyType.PUBLIC);
		Assert.assertEquals(key.key, DataUtils.hexStringToByteArray("1234567890123456789012345678901234567890123456789012345678901234"));
	}

	@Test
	public void testEncodePrivate() throws Exception {
		byte[] keyAsByte = DataUtils.hexStringToByteArray(Common.myPrivateKeyExtract);

		Key key = new Key(Key.KeyType.PRIVATE, keyAsByte);
		Assert.assertNotNull("key instance", key);

		Assert.assertEquals(Key.KeyType.PRIVATE, key.type);
		Assert.assertEquals(key.key, keyAsByte);

		Assert.assertEquals(key.encode(), Common.myPrivateKey);
	}
}