package com.jio.crm.dms.utils;

import java.io.File;

public class ArchivalUtility {

	private static ArchivalUtility archivalUtility = new ArchivalUtility();

	private ArchivalUtility() {

	}

	public static ArchivalUtility getInstance() {
		if (archivalUtility == null) {
			archivalUtility = new ArchivalUtility();
		}
		return archivalUtility;
	}

	public String getArchivalPath(String orderId) {

		String path = "./archival/" + orderId + Constants.DELIMITER;
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}

		return path;

	}
}
