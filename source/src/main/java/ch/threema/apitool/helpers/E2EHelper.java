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

package ch.threema.apitool.helpers;

import ch.threema.apitool.APIConnector;
import ch.threema.apitool.CryptTool;
import ch.threema.apitool.exceptions.InvalidKeyException;
import ch.threema.apitool.exceptions.MessageParseException;
import ch.threema.apitool.exceptions.NotAllowedException;
import ch.threema.apitool.messages.FileMessage;
import ch.threema.apitool.messages.ImageMessage;
import ch.threema.apitool.messages.ThreemaMessage;
import ch.threema.apitool.results.CapabilityResult;
import ch.threema.apitool.results.EncryptResult;
import ch.threema.apitool.results.UploadResult;
import com.neilalexander.jnacl.NaCl;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper to handle Threema end-to-end encryption.
 */
public class E2EHelper {
	private final APIConnector apiConnector;
	private final byte[] privateKey;

	public class ReceiveMessageResult {
		private final String messageId;
		private final ThreemaMessage message;
		protected List<File> files = new ArrayList<>();
		protected List<String> errors = new ArrayList<>();

		public ReceiveMessageResult(String messageId, ThreemaMessage message) {
			this.messageId = messageId;
			this.message = message;
		}

		public List<File> getFiles() {
			return this.files;
		}

		public List<String> getErrors() {
			return this.errors;
		}

		public String getMessageId() {
			return messageId;
		}
	}

	public E2EHelper(APIConnector apiConnector, byte[] privateKey) {
		this.apiConnector = apiConnector;
		this.privateKey = privateKey;
	}

	/**
	 * Encrypt a text message and send it to the given recipient.
	 *
	 * @param threemaId target Threema ID
	 * @param text the text to send
	 * @return generated message ID
	 */
	public String sendTextMessage(String threemaId, String text) throws Exception {
		//fetch public key
		byte[] publicKey = this.apiConnector.lookupKey(threemaId);

		if(publicKey == null) {
			throw new Exception("invalid threema id");
		}
		EncryptResult res = CryptTool.encryptTextMessage(text, this.privateKey, publicKey);

		return this.apiConnector.sendE2EMessage(threemaId, res.getNonce(), res.getResult());

	}

	/**
	 * Encrypt an image message and send it to the given recipient.
	 *
	 * @param threemaId target Threema ID
	 * @param imageFilePath path to read image data from
	 * @return generated message ID
	 * @throws NotAllowedException
	 * @throws IOException
	 * @throws InvalidKeyException
	 */
	public String sendImageMessage(String threemaId, String imageFilePath) throws NotAllowedException, IOException, InvalidKeyException {
		//fetch public key
		byte[] publicKey = this.apiConnector.lookupKey(threemaId);

		if(publicKey == null) {
			throw new InvalidKeyException("invalid threema id");
		}

		//check capability of a key
		CapabilityResult capabilityResult = this.apiConnector.lookupKeyCapability(threemaId);
		if(capabilityResult == null || !capabilityResult.canImage()) {
			throw new NotAllowedException();
		}

		byte[] fileData = Files.readAllBytes(Paths.get(imageFilePath));
		if(fileData == null) {
			throw new IOException("invalid file");
		}

		//encrypt the image
		EncryptResult encryptResult = CryptTool.encrypt(fileData, this.privateKey, publicKey);

		//upload the image
		UploadResult uploadResult = apiConnector.uploadFile(encryptResult);

		if(!uploadResult.isSuccess()) {
			throw new IOException("could not upload file (upload response " + uploadResult.getResponseCode() + ")");
		}

		//send it
		EncryptResult imageMessage = CryptTool.encryptImageMessage(encryptResult, uploadResult, privateKey, publicKey);

		return apiConnector.sendE2EMessage(
				threemaId,
				imageMessage.getNonce(),
				imageMessage.getResult());
	}

	/**
	 * Encrypt a file message and send it to the given recipient.
	 * The thumbnailMessagePath can be null.
	 *
	 * @param threemaId target Threema ID
	 * @param fileMessageFile the file to be sent
	 * @param thumbnailMessagePath file for thumbnail; if not set, no thumbnail will be sent
	 * @return generated message ID
	 * @throws InvalidKeyException
	 * @throws IOException
	 * @throws NotAllowedException
	 */
	public String sendFileMessage(String threemaId, File fileMessageFile, File thumbnailMessagePath) throws InvalidKeyException, IOException, NotAllowedException {
		//fetch public key
		byte[] publicKey = this.apiConnector.lookupKey(threemaId);

		if(publicKey == null) {
			throw new InvalidKeyException("invalid threema id");
		}

		//check capability of a key
		CapabilityResult capabilityResult = this.apiConnector.lookupKeyCapability(threemaId);
		if(capabilityResult == null || !capabilityResult.canImage()) {
			throw new NotAllowedException();
		}

		if(!fileMessageFile.isFile()) {
			throw new IOException("invalid file");
		}

		byte[] fileData  = this.readFile(fileMessageFile);

		if(fileData == null) {
			throw new IOException("invalid file");
		}

		//encrypt the image
		EncryptResult encryptResult = CryptTool.encryptFileData(fileData);

		//upload the image
		UploadResult uploadResult = apiConnector.uploadFile(encryptResult);

		if(!uploadResult.isSuccess()) {
			throw new IOException("could not upload file (upload response " + uploadResult.getResponseCode() + ")");
		}

		UploadResult uploadResultThumbnail = null;

		if(thumbnailMessagePath != null && thumbnailMessagePath.isFile()) {
			byte[] thumbnailData = this.readFile(thumbnailMessagePath);
			if(thumbnailData == null) {
				throw new IOException("invalid thumbnail file");
			}

			//encrypt the thumbnail
			EncryptResult encryptResultThumbnail = CryptTool.encryptFileThumbnailData(fileData, encryptResult.getSecret());

			//upload the thumbnail
			uploadResultThumbnail = this.apiConnector.uploadFile(encryptResultThumbnail);
		}

		//send it
		EncryptResult fileMessage = CryptTool.encryptFileMessage(
				encryptResult,
				uploadResult,
				Files.probeContentType(fileMessageFile.toPath()),
				fileMessageFile.getName(),
				(int) fileMessageFile.length(),
				uploadResultThumbnail,
				privateKey, publicKey);

		return this.apiConnector.sendE2EMessage(
				threemaId,
				fileMessage.getNonce(),
				fileMessage.getResult());
	}

	/**
	 * Decrypt a Message and download the blobs of the Message (e.g. image or file)
	 *
	 * @param threemaId Threema ID of the sender
	 * @param messageId Message ID
	 * @param box Encrypted box data of the file/image message
	 * @param nonce Nonce that was used for message encryption
	 * @param outputFolder Output folder for storing decrypted images/files
	 * @return result of message reception
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws MessageParseException
	 */
	public ReceiveMessageResult receiveMessage(String threemaId, String messageId, byte[] box, byte[] nonce, Path outputFolder) throws IOException, InvalidKeyException, MessageParseException {
		//fetch public key
		byte[] publicKey = this.apiConnector.lookupKey(threemaId);

		if(publicKey == null) {
			throw new InvalidKeyException("invalid threema id");
		}

		ThreemaMessage message = CryptTool.decryptMessage(box, this.privateKey, publicKey, nonce);
		if(message == null) {
			return null;
		}


		ReceiveMessageResult result = new ReceiveMessageResult(messageId, message);

		if(message instanceof ImageMessage) {
			//download image
			ImageMessage imageMessage = (ImageMessage)message;
			byte[] fileData = this.apiConnector.downloadFile(imageMessage.getBlobId());

			if(fileData == null) {
				throw new MessageParseException();
			}

			byte[] decryptedFileContent = CryptTool.decrypt(fileData, privateKey, publicKey, nonce);
			File imageFile = new File(outputFolder.toString() + "/" + messageId + ".jpg");
			FileOutputStream fos = new FileOutputStream(imageFile);
			fos.write(decryptedFileContent);
			fos.close();

			result.files.add(imageFile);
		}
		else if(message instanceof FileMessage) {
			//download file
			FileMessage fileMessage = (FileMessage)message;
			byte[] fileData = this.apiConnector.downloadFile(fileMessage.getBlobId());

			byte[] decryptedFileData = CryptTool.decryptFileData(fileData, fileMessage.getEncryptionKey());
			File file = new File(outputFolder.toString() + "/" + messageId + "-" + fileMessage.getFileName());
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(decryptedFileData);
			fos.close();

			result.files.add(file);

			if(fileMessage.getThumbnailBlobId() != null) {
				byte[] thumbnailData = this.apiConnector.downloadFile(fileMessage.getThumbnailBlobId());

				byte[] decryptedThumbnailData = CryptTool.decryptFileThumbnailData(thumbnailData, fileMessage.getEncryptionKey());
				File thumbnailFile = new File(outputFolder.toString() + "/" + messageId + "-thumbnail.jpg");
				fos = new FileOutputStream(thumbnailFile);
				fos.write(decryptedThumbnailData);
				fos.close();

				result.files.add(thumbnailFile);
			}
		}

		return result;
	}

	/**
	 * Read file data from file - store at offset in byte array for in-place encryption
	 * @param file input file
	 * @return file data with padding/offset for NaCl
	 * @throws IOException
	 */
	private byte[] readFile(File file) throws IOException {
		int fileLength = (int)file.length();
		byte[] fileData = new byte[fileLength + NaCl.BOXOVERHEAD];
		IOUtils.readFully(new FileInputStream(file), fileData, NaCl.BOXOVERHEAD, fileLength);
		return fileData;
	}
}
