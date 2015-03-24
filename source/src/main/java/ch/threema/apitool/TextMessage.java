/*
 * $Id$
 *
 * Copyright (c) 2014 Threema GmbH. All rights reserved.
 *
 * This software is intended for use by Threema Message API customers only. Distribution prohibited.
 */

package ch.threema.apitool;

/**
 * A text message that can be sent/received with end-to-end encryption via Threema.
 */
public class TextMessage extends ThreemaMessage {

	protected static final int TYPE_CODE = 0x01;

	private final String text;

	public TextMessage(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	protected int getTypeCode() {
		return TYPE_CODE;
	}

	@Override
	public String toString() {
		return text;
	}
}
