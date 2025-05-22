package com.jio.crm.dms.cli;

public class DMSMSCLICommandExecutionException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final int statusCode;

	/**
	 * constructor
	 * 
	 * @param message
	 * @param statusCode
	 */
	public DMSMSCLICommandExecutionException(String message, int statusCode) {
		super(message);
		this.statusCode = statusCode;
	}

	/**
	 * @return the status
	 */
	public int getStatus() {
		return statusCode;
	}
	
}
