package com.jio.crm.dms.notification.exceptions;

/**
 * 
 * @author Kiran.Jangid
 *
 */

public class RecipientInvalidException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -609961647327913705L;

	private String message;

	public RecipientInvalidException(String message) {
		super();
		this.message = message;
	}

	public RecipientInvalidException() {
		super();
	}

	@Override
	public String toString() {
		return message;
	}

}
