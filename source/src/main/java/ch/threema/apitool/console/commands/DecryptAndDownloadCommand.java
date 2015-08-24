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

import ch.threema.apitool.DataUtils;
import ch.threema.apitool.console.commands.fields.*;
import ch.threema.apitool.helpers.E2EHelper;

import java.nio.file.Path;

public class DecryptAndDownloadCommand extends Command {
	private final ThreemaIDField threemaId;
	private final ThreemaIDField fromField;
	private final TextField secretField;
	private final PrivateKeyField privateKeyField;
	private final ByteArrayField nonceField;
	private final FolderField outputFolderField;
	private final TextField messageIdField;

	public DecryptAndDownloadCommand() {
		super("Decrypt and download",
				"Decrypt a box (box from the stdin) message and download (if the message is a image or file message) the file(s) to the defined directory"
		);
		this.threemaId = this.createThreemaId("id");
		this.fromField = this.createThreemaId("from");
		this.secretField = this.createTextField("secret");
		this.privateKeyField = this.createPrivateKeyField("privateKey");
		this.messageIdField = this.createTextField("messageId");
		this.nonceField = this.createByteArrayField("nonce");
		this.outputFolderField = this.createFolderField("outputFolder", false);
	}

	@Override
	protected void execute() throws Exception {
		String id = this.threemaId.getValue();
		String from = this.fromField.getValue();
		String secret = this.secretField.getValue();
		byte[] privateKey = this.privateKeyField.getValue();
		byte[] nonce = this.nonceField.getValue();
		String messageId = this.messageIdField.getValue();
		Path outputFolder = this.outputFolderField.getValue();

		E2EHelper e2EHelper = new E2EHelper(this.createConnector(from, secret), privateKey);

		byte[] box = DataUtils.hexStringToByteArray(this.readStream(System.in, "UTF-8").trim());

		E2EHelper.ReceiveMessageResult res = e2EHelper.receiveMessage(id, messageId, box, nonce, outputFolder);
		System.out.println(res.toString());
		System.out.println(res.getFiles().toString());
	}
}
