package com.jio.crm.dms.configurationmanager;

@SuppressWarnings("squid:S3066")
public enum ELBConfigParamEnum {

	ELB_DMS_MS(null, 0, null),

	ELB_DMS_MS_SECURE(null, 0, null);

	private String ip;

	private int port;

	private String protocol;

	private ELBConfigParamEnum(String ip, int port, String protocol) {
		this.ip = ip;
		this.port = port;
		this.protocol = protocol;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	
}
