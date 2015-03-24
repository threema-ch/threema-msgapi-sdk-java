package ch.threema.apitool;

import java.util.Arrays;

/**
 * Created by se on 17.03.15.
 */
public class Assert extends org.junit.Assert {
	public static void assertEquals(byte[] expected, byte[] actual) {
		assertEquals((String)null, expected, actual);
	}
	public static void assertEquals(String message, byte[] expected, byte[] actual) {
		assertEquals(message, Arrays.toString(expected), Arrays.toString(actual));
	}
}
