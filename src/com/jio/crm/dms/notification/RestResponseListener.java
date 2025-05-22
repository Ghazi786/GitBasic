package com.jio.crm.dms.notification;

public interface RestResponseListener {
	public void onComplete(RestResponse response);
	public void onFailure(Exception error);
}
