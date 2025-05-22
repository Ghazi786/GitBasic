package com.jio.crm.dms.utils;

public enum Priority {
	UNBREAKNOW(100, 0, "Unbreak Now!", "pink"), NEEDSTRIAGE(90, 0, "Needs Triage", "violet"), HIGH(80, 0, "High", "red"), NORMAL(50, 0, "Normal", "orange"), LOW(25, 0, "Low",
"yellow"), WISHLIST(0, 0, "Wishlist", "sky");
	int value;
	int subpriority;
	String name;
	String color;

	Priority(int value, int subpriority, String name, String color) {
		this.value = value;
		this.subpriority = subpriority;
		this.name = name;
		this.color = color;
	}

	public static Priority getByName(String name) {
		if (name.equalsIgnoreCase(UNBREAKNOW.name)) {
			return Priority.UNBREAKNOW;
		} else if (name.equalsIgnoreCase(NEEDSTRIAGE.name)) {
			return Priority.NEEDSTRIAGE;
		} else if (name.equalsIgnoreCase(HIGH.name)) {
			return Priority.HIGH;
		} else if (name.equalsIgnoreCase(NORMAL.name)) {
			return Priority.NORMAL;
		} else if (name.equalsIgnoreCase(LOW.name)) {
			return Priority.LOW;
		} else if (name.equalsIgnoreCase(WISHLIST.name)) {
			return Priority.WISHLIST;
		} else {
			return null;
		}
	}
}
