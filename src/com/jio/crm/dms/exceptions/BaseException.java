package com.jio.crm.dms.exceptions;

public class BaseException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int status;
	private String message;

	public BaseException(int status, String message) {
		super();
		this.status = status;
		this.message = message;
	}

	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public BaseException() {
		super();
	}

	public BaseException(String message, int status) {
		super(message);
		this.status = status;
		this.message = message;
	}

}
