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

import ch.threema.apitool.console.commands.fields.FolderField;
import ch.threema.apitool.console.commands.fields.PrivateKeyField;
import ch.threema.apitool.console.commands.fields.TextField;
import ch.threema.apitool.console.commands.fields.ThreemaIDField;
import ch.threema.apitool.helpers.E2EHelper;

import java.nio.file.Path;

public class SendE2EImageMessageCommand extends Command {
	private final ThreemaIDField toField;
	private final ThreemaIDField fromField;
	private final TextField secretField;
	private final PrivateKeyField privateKeyField;
	private final FolderField imageFilePath;

	public SendE2EImageMessageCommand() {
		super("Send End-to-End Encrypted Image Message",
				"Encrypt standard input and send the message to the given ID. 'from' is the API identity and 'secret' is the API secret. Prints the message ID on success.");

		this.toField = this.createThreemaId("to", true);
		this.fromField = this.createThreemaId("from", true);
		this.secretField = this.createTextField("secret", true);
		this.privateKeyField = this.createPrivateKeyField("privateKey", true);
		this.imageFilePath = this.createFolderField("imageFilePath", true);
	}

	@Override
	protected void execute() throws Exception {
		String to = this.toField.getValue();
		String from = this.fromField.getValue();
		String secret = this.secretField.getValue();
		byte[] privateKey = this.privateKeyField.getValue();
		Path imageFilePath = this.imageFilePath.getValue();

		E2EHelper e2EHelper = new E2EHelper(this.createConnector(from, secret), privateKey);
		String messageId = e2EHelper.sendImageMessage(to, imageFilePath.toString());
		System.out.println("MessageId: " + messageId);
	}
}
