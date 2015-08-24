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

import ch.threema.apitool.console.commands.fields.ByteArrayField;
import ch.threema.apitool.console.commands.fields.PrivateKeyField;
import ch.threema.apitool.console.commands.fields.PublicKeyField;
import ch.threema.apitool.CryptTool;
import ch.threema.apitool.DataUtils;
import ch.threema.apitool.messages.ThreemaMessage;

public class DecryptCommand extends Command {
	private final PrivateKeyField privateKeyField;
	private final PublicKeyField publicKeyField;
	private final ByteArrayField nonceField;

	public DecryptCommand() {
		super("Decrypt",
				"Decrypt standard input using the given recipient private key and sender public key. The nonce must be given on the command line, and the box (hex) on standard input. Prints the decrypted message to standard output.");

		this.privateKeyField = this.createPrivateKeyField("privateKey");
		this.publicKeyField = this.createPublicKeyField("publicKey");
		this.nonceField = this.createByteArrayField("nonce");
	}

	@Override
	protected void execute() throws Exception {
		byte[] privateKey = this.privateKeyField.getValue();
		byte[] publicKey = this.publicKeyField.getValue();
		byte[] nonce = this.nonceField.getValue();

		/* read box from stdin */
		byte[] box = DataUtils.hexStringToByteArray(readStream(System.in, "UTF-8"));

		ThreemaMessage message = CryptTool.decryptMessage(box, privateKey, publicKey, nonce);

		System.out.println(message);
	}
}
