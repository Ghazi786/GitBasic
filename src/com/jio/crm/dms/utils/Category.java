package com.jio.crm.dms.utils;

public enum Category {
	FEATUREDOCUMENT("FEATUREDOCUMENT"),
	SRS("SRS"),
	DESIGN("DESIGN"), 
	CODING("CODING"), 
	UNITTESTING("UNITTESTING"), 
	INTEGRATIONTESTING("INTEGRATIONTESTING"), 
	LOADTESTING("LoadTesting"), 
	BUGFIX("BugFix"), 
	DEFECT("Defect"), 
	ENHANCEMENT("Enhancement"), 
	DOCUMENTATION("Documentation");
	
	String name;

	Category(String name) {
		this.name = name;
	}
	
	public static Category getByName(String name) {
		

			if (name.equalsIgnoreCase(FEATUREDOCUMENT.name)) {
				return Category.FEATUREDOCUMENT;
			} else if (name.equalsIgnoreCase(SRS.name)) {
				return Category.SRS;
			} else if (name.equalsIgnoreCase(DESIGN.name)) {
				return Category.DESIGN;
			} else if (name.equalsIgnoreCase(CODING.name)) {
				return Category.CODING;
			} else if (name.equalsIgnoreCase(UNITTESTING.name)) {
				return Category.UNITTESTING;
			} else if (name.equalsIgnoreCase(INTEGRATIONTESTING.name)) {
				return Category.INTEGRATIONTESTING;
			} else if (name.equalsIgnoreCase(LOADTESTING.name)) {
				return Category.LOADTESTING;
			} else if (name.equalsIgnoreCase(BUGFIX.name)) {
				return Category.BUGFIX;
			}else if (name.equalsIgnoreCase(DEFECT.name)) {
				return Category.DEFECT;
			} else if (name.equalsIgnoreCase(ENHANCEMENT.name)) {
				return Category.ENHANCEMENT;
			}else if (name.equalsIgnoreCase(DOCUMENTATION.name)) {
				return Category.DOCUMENTATION;
			}else {
				return null;
			}
		}
	
}
