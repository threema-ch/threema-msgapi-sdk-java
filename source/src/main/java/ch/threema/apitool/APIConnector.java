/*
 * $Id$
 *
 * Copyright (c) 2014 Threema GmbH. All rights reserved.
 *
 * This software is intended for use by Threema Message API customers only. Distribution prohibited.
 */

package ch.threema.apitool;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Facilitates HTTPS communication with the Threema Message API.
 */
public class APIConnector {

	private static final String API_URL = "https://msgapi.threema.ch/";

	private final String apiIdentity;
	private final String secret;

	public APIConnector(String apiIdentity, String secret) {
		this.apiIdentity = apiIdentity;
		this.secret = secret;
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

		Map<String,String> postParams = makePostParams();
		postParams.put("to", to);
		postParams.put("text", text);

		return doPost(new URL(API_URL + "send_simple"), postParams);
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
	public String sendTextMessageEndToEnd(String to, byte[] nonce, byte[] box) throws IOException {

		Map<String,String> postParams = makePostParams();
		postParams.put("to", to);
		postParams.put("nonce", DataUtils.byteArrayToHexString(nonce));
		postParams.put("box", DataUtils.byteArrayToHexString(box));

		return doPost(new URL(API_URL + "send_e2e"), postParams);
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
			Map<String,String> getParams = makePostParams();

			byte[] phoneHash = CryptTool.hashPhoneNo(phoneNumber);

			return doGet(new URL(API_URL + "lookup/phone_hash/" + DataUtils.byteArrayToHexString(phoneHash)), getParams);
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
			Map<String,String> getParams = makePostParams();

			byte[] emailHash = CryptTool.hashEmail(email);

			return doGet(new URL(API_URL + "lookup/email_hash/" + DataUtils.byteArrayToHexString(emailHash)), getParams);
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

		try {
			Map<String,String> getParams = makePostParams();
			String pubkeyHex = doGet(new URL(API_URL + "pubkeys/" + id), getParams);
			return DataUtils.hexStringToByteArray(pubkeyHex);
		} catch (FileNotFoundException e) {
			return null;
		}
	}


	private Map<String,String> makePostParams() {
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
