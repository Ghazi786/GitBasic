package com.jio.crm.dms.notification.exceptions;

/**
 * 
 * @author Kiran.Jangid
 *
 */

public class RecipientMissingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -609961647327913705L;

	private String message;

	public RecipientMissingException(String message) {
		super();
		this.message = message;
	}

	public RecipientMissingException() {
		super();
	}

	@Override
	public String toString() {
		return message;
	}

}
