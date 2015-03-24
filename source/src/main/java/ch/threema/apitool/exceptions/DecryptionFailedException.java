/*
 * $Id$
 *
 * Copyright (c) 2014 Threema GmbH. All rights reserved.
 *
 * This software is intended for use by Threema Message API customers only. Distribution prohibited.
 */

package ch.threema.apitool.exceptions;

/**
 * Exception that gets thrown when decryption fails (because the keys are incorrect, or the data is corrupted).
 */
public class DecryptionFailedException extends MessageParseException {
}
