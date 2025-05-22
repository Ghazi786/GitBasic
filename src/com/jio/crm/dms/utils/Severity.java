package com.jio.crm.dms.utils;

public enum Severity {
	BLOCKER("BLOCKER"), CRITICAL("CRITICAL"), MAJOR("MAJOR"), MINOR("MINOR");
	
	String name;

	Severity(String name) {
		this.name = name;
	}

	public static Severity getByName(String name) {
		
		if (name.equalsIgnoreCase(BLOCKER.name)) {
			return Severity.BLOCKER;
		} else if (name.equalsIgnoreCase(CRITICAL.name)) {
			return Severity.CRITICAL;
		} else if (name.equalsIgnoreCase(MAJOR.name)) {
			return Severity.MAJOR;
		} else if (name.equalsIgnoreCase(MINOR.name)) {
			return Severity.MINOR;
		} else {
			return null;
		}
	}
}
