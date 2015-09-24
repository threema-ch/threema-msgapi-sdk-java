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

import ch.threema.apitool.results.CapabilityResult;
import ch.threema.apitool.results.EncryptResult;
import ch.threema.apitool.results.UploadResult;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Facilitates HTTPS communication with the Threema Message API.
 */
public class APIConnector {
	private static final int BUFFER_SIZE = 16384;

	public interface ProgressListener {

		/**
		 * Update the progress of an upload/download process.
		 *
		 * @param progress in percent (0..100)
		 */
		void updateProgress(int progress);
	}

	public class InputStreamLength {
		public final InputStream inputStream;
		public final int length;

		public InputStreamLength(InputStream inputStream, int length) {
			this.inputStream = inputStream;
			this.length = length;
		}
	}

	private final String apiUrl;
	private final PublicKeyStore publicKeyStore;
	private final String apiIdentity;
	private final String secret;

	public APIConnector(String apiIdentity, String secret, PublicKeyStore publicKeyStore) {
		this(apiIdentity, secret, "https://msgapi.threema.ch/", publicKeyStore);
	}

	public APIConnector(String apiIdentity, String secret, String apiUrl, PublicKeyStore publicKeyStore) {
		this.apiIdentity = apiIdentity;
		this.secret = secret;
		this.apiUrl = apiUrl;
		this.publicKeyStore = publicKeyStore;
	}

	/**
	 * Send a text message with server-side encryption.
	 *
	 * @param to recipient ID
	 * @param text message text (max. 3500 bytes)
	 * @return message ID
	 * @throws IOException if a communication or server error occurs
	 */
	public String sendTextMessageSimple(String to, String text) throws IOException {

		Map<String,String> postParams = makeRequestParams();
		postParams.put("to", to);
		postParams.put("text", text);

		return doPost(new URL(this.apiUrl + "send_simple"), postParams);
	}

	/**
	 * Send an end-to-end encrypted message.
	 *
	 * @param to recipient ID
	 * @param nonce nonce used for encryption (24 bytes)
	 * @param box encrypted message data (max. 4000 bytes)
	 * @return message ID
	 * @throws IOException if a communication or server error occurs
	 */
	public String sendE2EMessage(String to, byte[] nonce, byte[] box) throws IOException {

		Map<String,String> postParams = makeRequestParams();
		postParams.put("to", to);
		postParams.put("nonce", DataUtils.byteArrayToHexString(nonce));
		postParams.put("box", DataUtils.byteArrayToHexString(box));

		return doPost(new URL(this.apiUrl + "send_e2e"), postParams);
	}

	/**
	 * Lookup an ID by phone number. The phone number will be hashed before
	 * being sent to the server.
	 *
	 * @param phoneNumber the phone number in E.164 format
	 * @return the ID, or null if not found
	 * @throws IOException if a communication or server error occurs
	 */
	public String lookupPhone(String phoneNumber) throws IOException {

		try {
			Map<String,String> getParams = makeRequestParams();

			byte[] phoneHash = CryptTool.hashPhoneNo(phoneNumber);

			return doGet(new URL(this.apiUrl + "lookup/phone_hash/" + DataUtils.byteArrayToHexString(phoneHash)), getParams);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	/**
	 * Lookup an ID by email address. The email address will be hashed before
	 * being sent to the server.
	 *
	 * @param email the email address
	 * @return the ID, or null if not found
	 * @throws IOException if a communication or server error occurs
	 */
	public String lookupEmail(String email) throws IOException {

		try {
			Map<String,String> getParams = makeRequestParams();

			byte[] emailHash = CryptTool.hashEmail(email);

			return doGet(new URL(this.apiUrl + "lookup/email_hash/" + DataUtils.byteArrayToHexString(emailHash)), getParams);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	/**
	 * Lookup a public key by ID.
	 *
	 * @param id the ID whose public key is desired
	 * @return the corresponding public key, or null if not found
	 * @throws IOException if a communication or server error occurs
	 */
	public byte[] lookupKey(String id) throws IOException {
		byte[] key = this.publicKeyStore.getPublicKey(id);
		if(key == null) {
			try {
				Map<String, String> getParams = makeRequestParams();
				String pubkeyHex = doGet(new URL(this.apiUrl + "pubkeys/" + id), getParams);
				key = DataUtils.hexStringToByteArray(pubkeyHex);
			} catch (FileNotFoundException e) {
				return null;
			}
		}
		return key;
	}

	/**
	 * Lookup the capabilities of a ID
	 *
	 * @param threemaId The ID whose capabilities should be checked
	 * @return The capabilities, or null if not found
	 * @throws IOException
	 */
	public CapabilityResult lookupKeyCapability(String threemaId) throws IOException {
		String res = doGet(new URL(this.apiUrl + "capabilities/" + threemaId),
				makeRequestParams());
		if(res != null) {
			return new CapabilityResult(threemaId, res.split(","));
		}
		return null;
	}

	public Integer lookupCredits() throws IOException {
		String res = doGet(new URL(this.apiUrl + "credits"),
				makeRequestParams());
		if(res != null) {
			return Integer.valueOf(res);
		}
		return null;
	}
	/**
	 * Upload a file.
	 *
	 * @param fileEncryptionResult The result of the file encryption (i.e. encrypted file data)
	 * @return the result of the upload
	 * @throws IOException
	 */
	public UploadResult uploadFile(EncryptResult fileEncryptionResult) throws  IOException{

		String attachmentName = "blob";
		String attachmentFileName = "blob.file";
		String crlf = "\r\n";
		String twoHyphens = "--";

		char[] chars = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
		String boundary = "";
		SecureRandom rand = new SecureRandom();
		int count = rand.nextInt(11) + 30;
		for (int i = 0; i < count; i++) {
			boundary += chars[rand.nextInt(chars.length)];
		}


		String queryString = makeUrlEncoded(makeRequestParams());
		URL url = new URL(this.apiUrl + "upload_blob?" + queryString);

		HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);

		connection.setRequestMethod("POST");
		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Cache-Control", "no-cache");
		connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

		DataOutputStream request = new DataOutputStream(connection.getOutputStream());

		request.writeBytes(twoHyphens + boundary + crlf);
		request.writeBytes("Content-Disposition: form-data; name=\"" + attachmentName + "\";filename=\"" + attachmentFileName + "\"" + crlf);
		request.writeBytes(crlf);
		request.write(fileEncryptionResult.getResult());
		request.writeBytes(crlf);
		request.writeBytes(twoHyphens + boundary + twoHyphens + crlf);

		String response = null;
		int responseCode = connection.getResponseCode();

		if(responseCode == 200) {
			InputStream is = connection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			response = br.readLine();
			br.close();
		}

		connection.disconnect();

		return new UploadResult(responseCode, response != null ? DataUtils.hexStringToByteArray(response) : null);
	}

	/**
	 * Download a file given its blob ID.
	 *
	 * @param blobId The blob ID of the file
	 * @return Encrypted file data
	 * @throws IOException
	 */
	public byte[] downloadFile(byte[] blobId) throws IOException {
		return this.downloadFile(blobId, null);
	}

	/**
	 * Download a file given its blob ID.
	 *
	 * @param blobId The blob ID of the file
	 * @param progressListener An object that will receive progress information, or null
	 * @return Encrypted file data
	 * @throws IOException
	 */
	public byte[] downloadFile(byte[] blobId, ProgressListener progressListener) throws IOException {
		String queryString = makeUrlEncoded(makeRequestParams());
		URL blobUrl = new URL(String.format(this.apiUrl + "blobs/%s?%s",
				DataUtils.byteArrayToHexString(blobId),
				queryString));

		HttpsURLConnection connection = (HttpsURLConnection)blobUrl.openConnection();
		connection.setConnectTimeout(20*1000);
		connection.setReadTimeout(20*1000);
		connection.setDoOutput(false);

		InputStream inputStream = connection.getInputStream();
		int contentLength = connection.getContentLength();
		InputStreamLength isl = new InputStreamLength(inputStream, contentLength);


        /* Content length known? */
		byte[] blob;
		if (isl.length != -1) {
			blob = new byte[isl.length];
			int offset = 0;
			int readed;

			while (offset < isl.length && (readed = isl.inputStream.read(blob, offset, isl.length - offset)) != -1) {
				offset += readed;

				if (progressListener != null) {
					progressListener.updateProgress(100 * offset / isl.length);
				}
			}

			if (offset != isl.length) {
				throw new IOException("Unexpected read size. current: " + offset + ", excepted: " + isl.length);
			}
		} else {
            /* Content length is unknown - need to read until EOF */

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[BUFFER_SIZE];

			int read;
			while ((read = isl.inputStream.read(buffer)) != -1) {
				bos.write(buffer, 0, read);
			}

			blob = bos.toByteArray();
		}
		if (progressListener != null) {
			progressListener.updateProgress(100);
		}

		return blob;
	}

	private Map<String,String> makeRequestParams() {
		Map<String,String> postParams = new HashMap<String,String>();

		postParams.put("from", apiIdentity);
		postParams.put("secret", secret);
		return postParams;
	}

	private String doGet(URL url, Map<String,String> getParams) throws IOException {

		if (getParams != null) {
			String queryString = makeUrlEncoded(getParams);

			url = new URL(url.toString() + "?" + queryString);
		}

		HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
		connection.setDoOutput(false);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false);
		connection.setRequestMethod("GET");
		connection.setUseCaches(false);

		InputStream is = connection.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String response = br.readLine();
		br.close();

		connection.disconnect();

		return response;
	}

	private String doPost(URL url, Map<String,String> postParams) throws IOException {

		byte[] postData = makeUrlEncoded(postParams).getBytes("UTF-8");

		HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Charset", "utf-8");
		connection.setRequestProperty("Content-Length", Integer.toString(postData.length));
		connection.setUseCaches(false);

		OutputStream os = connection.getOutputStream();
		os.write(postData);
		os.flush();
		os.close();

		InputStream is = connection.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String response = br.readLine();
		br.close();

		connection.disconnect();

		return response;
	}

	private String makeUrlEncoded(Map<String,String> params) {
		StringBuilder s = new StringBuilder();

		for (Map.Entry<String,String> param : params.entrySet()) {
			if (s.length() > 0)
				s.append('&');

			s.append(param.getKey());
			s.append('=');
			try {
				s.append(URLEncoder.encode(param.getValue(), "UTF-8"));
			} catch (UnsupportedEncodingException ignored) {}
		}

		return s.toString();
	}

}
