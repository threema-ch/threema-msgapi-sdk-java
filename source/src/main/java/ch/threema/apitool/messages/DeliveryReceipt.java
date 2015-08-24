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

import ch.threema.apitool.MessageId;

import java.util.List;

/**
 * A delivery receipt message that can be sent/received with end-to-end encryption via Threema.
 * Each delivery receipt message confirms the receipt of one or multiple regular messages.
 */
public class DeliveryReceipt extends ThreemaMessage {

	public static final int TYPE_CODE = 0x80;

	private final Type receiptType;
	private final List<MessageId> ackedMessageIds;

	public DeliveryReceipt(Type receiptType, List<MessageId> ackedMessageIds) {
		this.receiptType = receiptType;
		this.ackedMessageIds = ackedMessageIds;
	}

	public Type getReceiptType() {
		return receiptType;
	}

	public List<MessageId> getAckedMessageIds() {
		return ackedMessageIds;
	}

	@Override
	public int getTypeCode() {
		return TYPE_CODE;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Delivery receipt (");
		sb.append(receiptType);
		sb.append("): ");
		int i = 0;
		for (MessageId messageId : ackedMessageIds) {
			if (i != 0)
				sb.append(", ");
			sb.append(messageId);
			i++;
		}
		return sb.toString();
	}

	/**
	 * A delivery receipt type. The following types are defined:
	 *
	 * <ul>
	 *     <li>RECEIVED: the message has been received and decrypted on the recipient's device</li>
	 *     <li>READ: the message has been shown to the user in the chat view
	 *         (note that this status can be disabled)</li>
	 *     <li>USER_ACK: the user has explicitly acknowledged the message (usually by
	 *         long-pressing it and choosing the "acknowledge" option)</li>
	 * </ul>
	 */
	public enum Type {
		RECEIVED(1), READ(2), USER_ACK(3);

		private final int code;

		Type(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public static Type get(int code) {
			for (Type t : values()) {
				if (t.code == code)
					return t;
			}
			return null;
		}
	}

	@Override
	public byte[] getData() {
		//Not implemented yet
		return new byte[0];
	}
}
