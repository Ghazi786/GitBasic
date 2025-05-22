package com.jio.crm.dms.bean;

public enum ArchivalStatus {
	A("Add"), U("Uploaded"), S("Success"), F("Failure"),R("No Response");

	private String description;

	private ArchivalStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
