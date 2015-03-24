/*
 * $Id$
 *
 * Copyright (c) 2014 Threema GmbH. All rights reserved.
 *
 * This software is intended for use by Threema Message API customers only. Distribution prohibited.
 */

package ch.threema.apitool;

/**
 * Encapsulates the 8-byte message IDs that Threema uses.
 */
public class MessageId {

	public static final int MESSAGE_ID_LEN = 8;

	private final byte[] messageId;

	public MessageId(byte[] messageId) {
		if (messageId.length != MESSAGE_ID_LEN)
			throw new IllegalArgumentException("Bad message ID length");

		this.messageId = messageId;
	}

	public MessageId(byte[] data, int offset) {
		if ((offset + MESSAGE_ID_LEN) > data.length)
			throw new IllegalArgumentException("Bad message ID buffer length");

		this.messageId = new byte[MESSAGE_ID_LEN];
		System.arraycopy(data, offset, this.messageId, 0, MESSAGE_ID_LEN);
	}

	public byte[] getMessageId() {
		return messageId;
	}

	@Override
	public String toString() {
		return DataUtils.byteArrayToHexString(messageId);
	}
}
