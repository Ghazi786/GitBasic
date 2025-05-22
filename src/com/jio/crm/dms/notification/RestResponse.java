package com.jio.crm.dms.notification;

/**
 * This class is used to map response
 * 
 * @author Kiran.Jangid
 *
 */

public class RestResponse {
	private int status;
	private String errorMessage;
	private String appData;

	public RestResponse() {

	}

	public RestResponse(int status, String errorMessage, String appData) {
		super();
		this.status = status;
		this.errorMessage = errorMessage;
		this.appData = appData;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getAppData() {
		return appData;
	}

	public void setAppData(String appData) {
		this.appData = appData;
	}

}
