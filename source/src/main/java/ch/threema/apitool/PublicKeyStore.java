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

package ch.threema.apitool;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores and caches public keys for Threema users. Extend this class to provide your
 * own storage implementation, e.g. in a file or database.
 */
public abstract class PublicKeyStore {
	private final Map<String, byte[]> cache = new HashMap<>();

	/**
	 * Get the public key for a given Threema ID. The cache is checked first; if it
	 * is not found in the cache, fetchPublicKey() is called.
	 *
	 * @param threemaId The Threema ID whose public key should be obtained
	 * @return The public key, or null if not found.
	 */
	public final byte[] getPublicKey(String threemaId) {
		synchronized (this.cache) {
			byte[] pk = this.cache.get(threemaId);

			if (pk == null) {
				pk = this.fetchPublicKey(threemaId);
				this.setPublicKey(threemaId, pk);
			}
			return pk;
		}

	}

	/**
	 * Store the public key for a given Threema ID in the cache, and the underlying store.
	 *
	 * @param threemaId The Threema ID whose public key should be stored
	 * @param publicKey The corresponding public key.
	 */
	public final void setPublicKey(String threemaId, byte[] publicKey) {
		if(publicKey != null) {
			synchronized (this.cache) {
				this.cache.put(threemaId, publicKey);
				this.save(threemaId, publicKey);
			}
		}
	}

	/**
	 * Fetch the public key for the given Threema ID from the store. Override to provide
	 * your own implementation to read from the store.
	 *
	 * @param threemaId The Threema ID whose public key should be obtained
	 * @return The public key, or null if not found.
	 */
	abstract protected byte[] fetchPublicKey(String threemaId);

	/**
	 * Save the public key for a given Threema ID in the store. Override to provide
	 * your own implementation to write to the store.
	 *
	 * @param threemaId The Threema ID whose public key should be stored
	 * @param publicKey The corresponding public key.
	 */
	abstract protected void save(String threemaId, byte[] publicKey);
}
