package com.jio.crm.dms.cli;

public enum DMSMSCLIOptionEnum {

	NAME("name"),

	VALUE("value"),

	SEVERITY("severity"),

	ID("id"),

	CATEGORY("category"),
	
	STATUS("status"),
	
	MODULE("module"),
	
	VNF_ID("vnfId"),
	
	COMMAND_NAME("commandName"),
	
	PARAM("param"),
	
	KEY("key"),

	EVENT("event"),

	OAM_IP("oamIp"),

	OAM_PORT("oamPort"),

	OAM_NETTY_PORT("oamNettyPort");
	
	private String name;

	private DMSMSCLIOptionEnum(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
