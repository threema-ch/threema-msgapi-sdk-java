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

import ch.threema.apitool.console.commands.fields.TextField;
import ch.threema.apitool.CryptTool;
import ch.threema.apitool.DataUtils;

public class HashPhoneCommand extends Command {
	private final TextField phoneNo;

	public HashPhoneCommand() {
		super("Hash Phone Number",
				"Hash a phone number for identity lookup. Prints the hash in hex.");

		this.phoneNo = this.createTextField("phoneNo", true);
	}

	@Override
	protected void execute() throws Exception {
		String phoneNo = this.phoneNo.getValue();

		byte[] phoneHash = CryptTool.hashPhoneNo(phoneNo);
		System.out.println(DataUtils.byteArrayToHexString(phoneHash));
	}
}
