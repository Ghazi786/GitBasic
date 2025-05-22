package com.jio.crm.dms.exceptions;

public class AccessDeniedException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int status;
	private String message;

	public AccessDeniedException(int status, String message) {
		super();
		this.status = status;
		this.message = message;
	}

	public int getStatus() {
		return status;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public AccessDeniedException() {
		super();
	}

}
