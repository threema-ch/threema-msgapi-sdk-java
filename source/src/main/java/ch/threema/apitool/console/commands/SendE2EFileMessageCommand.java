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

package ch.threema.apitool.console.commands;

import ch.threema.apitool.console.commands.fields.FileField;
import ch.threema.apitool.console.commands.fields.PrivateKeyField;
import ch.threema.apitool.console.commands.fields.TextField;
import ch.threema.apitool.console.commands.fields.ThreemaIDField;
import ch.threema.apitool.helpers.E2EHelper;

import java.io.File;

public class SendE2EFileMessageCommand extends Command {
	private final ThreemaIDField threemaId;
	private final ThreemaIDField fromField;
	private final TextField secretField;
	private final PrivateKeyField privateKeyField;
	private final FileField fileField;
	private final FileField thumbnailField;

	public SendE2EFileMessageCommand() {
		super("Send End-to-End Encrypted File Message",
				"Encrypt the file (and thumbnail) and send a file message to the given ID. 'from' is the API identity and 'secret' is the API secret. Prints the message ID on success."
		);
		this.threemaId = this.createThreemaId("to");
		this.fromField = this.createThreemaId("from");
		this.secretField = this.createTextField("secret");
		this.privateKeyField = this.createPrivateKeyField("privateKey");
		this.fileField = this.createFileField("file");
		this.thumbnailField = this.createFileField("thumbnail", false);

	}

	@Override
	protected void execute() throws Exception {
		String to = this.threemaId.getValue();
		String from = this.fromField.getValue();
		String secret = this.secretField.getValue();
		byte[] privateKey = this.privateKeyField.getValue();
		File file = this.fileField.getValue();
		File thumbnail =  this.thumbnailField.getValue();

		E2EHelper e2EHelper = new E2EHelper(this.createConnector(from, secret), privateKey);
		String messageId = e2EHelper.sendFileMessage(to, file, thumbnail);
		System.out.println("MessageId: " + messageId);
	}
}
