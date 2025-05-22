package com.jio.crm.dms.utils;

public enum Environment {
	DEV("DEV"), IOT("IOT"), REPLICA("Replica"), PRODUCTION("Production");

	String name;

	Environment(String name) {
		this.name = name;
	}

	public static Environment getByName(String name) {
		
		if (name.equalsIgnoreCase(DEV.name)) {
			return Environment.DEV;
		} else if (name.equalsIgnoreCase(IOT.name)) {
			return Environment.IOT;
		} else if (name.equalsIgnoreCase(REPLICA.name)) {
			return Environment.REPLICA;
		} else if (name.equalsIgnoreCase(PRODUCTION.name)) {
			return Environment.PRODUCTION;
		} else {
			return null;
		}
	}
}
