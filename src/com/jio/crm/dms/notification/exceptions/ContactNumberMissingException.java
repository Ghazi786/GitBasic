package com.jio.crm.dms.notification.exceptions;

/**
 * 
 * @author Kiran.Jangid
 *
 */

public class ContactNumberMissingException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -609961647327913705L;

	private String message;

	public ContactNumberMissingException(String message) {
		super();
		this.message = message;
	}

	public ContactNumberMissingException() {
		super();
	}

	@Override
	public String toString() {
		return message;
	}

}
