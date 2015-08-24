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

package ch.threema.apitool.results;

/**
 * Result of a capability lookup
 */
public class CapabilityResult {
	private final String key;
	private final String[] capabilities;

	public CapabilityResult(String key, String[] capabilities) {
		this.key = key;
		this.capabilities = capabilities;
	}

	/**
	 * Get all capabilities as a string array.
	 */
	public String[] getCapabilities() {
		return capabilities;
	}

	/**
	 * Check whether the Threema ID can receive text
	 */
	public boolean canText() {
		return this.can("text");
	}

	/**
	 * Check whether the Threema ID can receive images
	 */
	public boolean canImage() {
		return this.can("image");
	}

	/**
	 * Check whether the Threema ID can receive videos
	 */
	public boolean canVideo() {
		return this.can("video");
	}

	/**
	 * Check whether the Threema ID can receive audio
	 */
	public boolean canAudio() {
		return this.can("audio");
	}

	/**
	 * Check whether the Threema ID can receive files
	 */
	public boolean canFile() {
		return this.can("file");
	}

	private boolean can(String key) {
		for(String k: this.capabilities) {
			if(k.equals(key)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(this.key).append(": ");
		for(int n = 0; n < this.capabilities.length; n++) {
			if(n > 0) {
				b.append(",");
			}
			b.append(this.capabilities[n]);
		}
		return b.toString();
	}

	public String getKey() {
		return key;
	}
}
