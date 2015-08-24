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

package ch.threema.apitool.messages;

import ch.threema.apitool.DataUtils;
import com.neilalexander.jnacl.NaCl;
import org.apache.commons.io.EndianUtils;

/**
 * An image message that can be sent/received with end-to-end encryption via Threema.
 */
public class ImageMessage extends ThreemaMessage {

	public static final int TYPE_CODE = 0x02;
	private final byte[] blobId;
	private final int size;
	private final byte[] nonce;


	public ImageMessage(byte[] blobId, int size, byte[] nonce) {

		this.blobId = blobId;
		this.size = size;
		this.nonce = nonce;
	}

	public byte[] getBlobId() {
		return this.blobId;
	}


	public int getSize() {
		return this.size;
	}


	public byte[] getNonce() {
		return this.nonce;
	}

	@Override
	public int getTypeCode() {
		return TYPE_CODE;
	}

	@Override
	public String toString() {
		return "blob " + DataUtils.byteArrayToHexString(this.blobId);
	}

	@Override
	public byte[] getData() {
		byte[] data = new byte[BLOB_ID_LEN + 4 + NaCl.NONCEBYTES];
		int pos = 0;
		System.arraycopy(this.blobId, 0, data, pos, BLOB_ID_LEN);
		pos += BLOB_ID_LEN;

		EndianUtils.writeSwappedInteger(data, pos, this.size);
		pos += 4;

		System.arraycopy(this.nonce, 0, data, pos,  NaCl.NONCEBYTES);
		return data;

	}
}
