package com.jio.crm.dms.exceptions;

public class InvalidRequestParamValueException extends BaseException {

	private static final long serialVersionUID = 1L;

	public InvalidRequestParamValueException() {
		super();
	}

	public InvalidRequestParamValueException(int status, String message) {
		super(status, message);
	}

}
