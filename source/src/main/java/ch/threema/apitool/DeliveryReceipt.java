/*
 * $Id$
 *
 * Copyright (c) 2014 Threema GmbH. All rights reserved.
 *
 * This software is intended for use by Threema Message API customers only. Distribution prohibited.
 */

package ch.threema.apitool;

import java.util.List;

/**
 * A delivery receipt message that can be sent/received with end-to-end encryption via Threema.
 * Each delivery receipt message confirms the receipt of one or multiple regular messages.
 */
public class DeliveryReceipt extends ThreemaMessage {

	protected static final int TYPE_CODE = 0x80;

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
	protected int getTypeCode() {
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

		private Type(int code) {
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
}
