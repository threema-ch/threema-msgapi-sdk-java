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

/**
 * Common Stuff
 */
public abstract class Common {

	public static final String myPrivateKey = "private:94af3260fa2a19adc8e82e82be598be15bc6ad6f47c8ee303cb185ef860e16d2";
	public static final String myPrivateKeyExtract = "94af3260fa2a19adc8e82e82be598be15bc6ad6f47c8ee303cb185ef860e16d2";

	public static final String myPublicKey = "public:3851ad59c96146a05b32e41c0ccd0fd639dc8cd66bf6e1cbd3c8d67e4e8f5531";
	public static final String myPublicKeyExtract = "3851ad59c96146a05b32e41c0ccd0fd639dc8cd66bf6e1cbd3c8d67e4e8f5531";

	public static final String otherPrivateKey = "private:8318e05220acd38e97ba41a9a6318688214219916075ca060f9339a6d1f7fc29";
	public static final String otherPublicKey = "public:10ac7fd937eafb806f9a05bf9afa340a99387b0063cc9cb0d1ea5505d39cc076";

	public static final String echochoPublicKey = "public:4a6a1b34dcef15d43cb74de2fd36091be99fbbaf126d099d47d83d919712c72b";
	public static final String randomNonce = "516f4f1562dda0704a7bae8997cf0b354c6980181152ac32";

	public static boolean isEmpty(byte[] byteArray) {
		if(byteArray == null) {
			return true;
		}
		else {
			for(byte b: byteArray) {
				if(b != 0) {
					return false;
				}
			}
		}

		return true;
	}
}
