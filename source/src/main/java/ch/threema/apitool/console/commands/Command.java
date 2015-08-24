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
import ch.threema.apitool.PublicKeyStore;
import ch.threema.apitool.console.commands.fields.*;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

abstract public class Command {
	private final List<Field> fields = new LinkedList<>();
	private final String subject;
	private final String description;

	public Command(String subject, String description) {
		this.subject = subject;
		this.description = description;
	}

	private void addField(Field f) {
		if(f.isRequired()) {
			int pos = this.fields.size();
			//add after last required
			for(int n = 0; n < this.fields.size(); n++) {
				if(!this.fields.get(n).isRequired()) {
					pos = n;
					break;
				}
			}
			this.fields.add(pos, f);
		}
		else {
			this.fields.add(f);
		}
	}
	protected TextField createTextField(String key) {
		return this.createTextField(key, true);
	}
	protected TextField createTextField(String key, boolean required) {
		TextField field = new TextField(key, required);
		this.addField(field);
		return field;
	}

	protected ThreemaIDField createThreemaId(String key) {
		return this.createThreemaId(key, true);
	}

	protected ThreemaIDField createThreemaId(String key, boolean required) {
		ThreemaIDField field = new ThreemaIDField(key, required);
		this.addField(field);
		return field;
	}

	protected PublicKeyField createPublicKeyField(String key) {
		return this.createPublicKeyField(key, true);
	}

	protected PublicKeyField createPublicKeyField(String key, boolean required) {
		PublicKeyField field = new PublicKeyField(key, required);
		this.addField(field);
		return field;
	}

	protected PrivateKeyField createPrivateKeyField(String key) {
		return this.createPrivateKeyField(key, true);
	}

	protected PrivateKeyField createPrivateKeyField(String key, boolean required) {
		PrivateKeyField field = new PrivateKeyField(key, required);
		this.addField(field);
		return field;
	}

	protected FileField createFileField(String key) {
		return this.createFileField(key, true);
	}

	protected FileField createFileField(String key, boolean required) {
		FileField field = new FileField(key, required);
		this.addField(field);
		return field;
	}

	protected FolderField createFolderField(String key) {
		return this.createFolderField(key, true);
	}

	protected FolderField createFolderField(String key, boolean required) {
		FolderField field = new FolderField(key, required);
		this.addField(field);
		return field;
	}

	protected ByteArrayField createByteArrayField(String key) {
		return this.createByteArrayField(key, true);
	}

	protected ByteArrayField createByteArrayField(String key, boolean required) {
		ByteArrayField field = new ByteArrayField(key, required);
		this.addField(field);
		return field;
	}


	protected APIConnector createConnector(String gatewayId, String secret) {
		return new APIConnector(gatewayId, secret, new PublicKeyStore() {
			@Override
			protected byte[] fetchPublicKey(String threemaId) {
				return null;
			}

			@Override
			protected void save(String threemaId, byte[] publicKey) {
				//do nothing
			}
		});
	}

	protected String readStream(InputStream stream, String charset) throws IOException {
		try {
			Reader reader = new BufferedReader(new InputStreamReader(stream, charset));
			StringBuilder builder = new StringBuilder();
			char[] buffer = new char[8192];
			int read;
			while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
				builder.append(buffer, 0, read);
			}
			return builder.toString();
		} finally {
			stream.close();
		}
	}

	public final void run(String[] arguments) throws Exception {
		int pos = 0;
		for(Field f: this.fields) {
			if(arguments.length > pos) {
				f.setValue(arguments[pos]);
			}
			pos++;
		}

		//validate
		for(Field f: this.fields) {
			if(!f.isValid()) {
				return;
			}
		}

		this.execute();
	}

	public final String getSubject() {
		return this.subject;
	}

	public final String getUsageArguments() {
		StringBuilder usage = new StringBuilder();
		for(Field f: this.fields) {
			usage.append(" ")
					.append(f.isRequired() ? "<" : "[")
					.append(f.getKey())
					.append(f.isRequired() ? ">" : "]");
		}
		return usage.toString().trim();
	}

	public final String getUsageDescription() {
		return this.description;
	}

	protected abstract void execute() throws Exception;
}
