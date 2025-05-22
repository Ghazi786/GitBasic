package com.jio.crm.dms.core;

public class BaseResponse<T> {
	private T data;
	private boolean success;
	private String message;
	private int status;
	private Cursor cursor;

	public BaseResponse() {
		super();
	}

	public BaseResponse(T data, boolean success, String message, int status, Cursor cursor) {
		super();
		this.data = data;
		this.success = success;
		this.message = message;
		this.status = status;
		this.cursor = cursor;
	}

	public BaseResponse(T data) {
		super();
		this.data = data;
		this.success = true;
		this.message = "SUCCESS";
		this.status = 200;
		this.cursor = null;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Cursor getCursor() {
		return cursor;
	}

	public void setCursor(Cursor cursor) {
		this.cursor = cursor;
	}

}
