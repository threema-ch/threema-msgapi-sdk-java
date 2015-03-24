/*
 * $Id$
 *
 * Copyright (c) 2014 Threema GmbH. All rights reserved.
 *
 * This software is intended for use by Threema Message API customers only. Distribution prohibited.
 */

package ch.threema.apitool.exceptions;

/**
 * Exception that gets thrown when an attempt has been made to decrypt a message
 * of a type that is not supported by this library.
 */
public class UnsupportedMessageTypeException extends MessageParseException {
}
