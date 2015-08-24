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

import ch.threema.apitool.APIConnector;
import ch.threema.apitool.console.commands.fields.TextField;
import ch.threema.apitool.console.commands.fields.ThreemaIDField;
import ch.threema.apitool.Key;

public class FetchPublicKey extends Command {
	private final ThreemaIDField threemaIdField;
	private final ThreemaIDField fromField;
	private final TextField secretField;

	public FetchPublicKey() {
		super("Fetch Public Key",
				"Lookup the public key for the given ID.");

		this.threemaIdField = this.createThreemaId("id");
		this.fromField = this.createThreemaId("from");
		this.secretField = this.createTextField("secret");
	}

	@Override
	protected void execute() throws Exception {
		String threemaId = this.threemaIdField.getValue();
		String from = this.fromField.getValue();
		String secret = this.secretField.getValue();

		APIConnector apiConnector = this.createConnector(from, secret);
		byte[] publicKey = apiConnector.lookupKey(threemaId);
		if (publicKey != null) {
			System.out.println(new Key(Key.KeyType.PUBLIC, publicKey).encode());
		}
	}
}
