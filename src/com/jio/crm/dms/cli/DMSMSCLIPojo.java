package com.jio.crm.dms.cli;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

public class DMSMSCLIPojo {

	private String commandName;
	private String module;
	private String status;
	private String vnfId;
	private Map<String, String> requestParams;
	private JSONObject requestJson;
	private byte[] requestStream;
	private HttpServletRequest request;
	private HttpServletResponse response;
	public String getCommandName() {
		return commandName;
	}
	public void setCommandName(String commandName) {
		this.commandName = commandName;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getVnfId() {
		return vnfId;
	}
	public void setVnfId(String vnfId) {
		this.vnfId = vnfId;
	}
	public Map<String, String> getRequestParams() {
		return requestParams;
	}
	public void setRequestParams(Map<String, String> requestParams) {
		this.requestParams = requestParams;
	}
	public JSONObject getRequestJson() {
		return requestJson;
	}
	public void setRequestJson(JSONObject requestJson) {
		this.requestJson = requestJson;
	}
	public byte[] getRequestStream() {
		return requestStream;
	}
	public void setRequestStream(byte[] requestStream) {
		this.requestStream = requestStream;
	}
	public HttpServletRequest getRequest() {
		return request;
	}
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
	public HttpServletResponse getResponse() {
		return response;
	}
	public void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	
}
