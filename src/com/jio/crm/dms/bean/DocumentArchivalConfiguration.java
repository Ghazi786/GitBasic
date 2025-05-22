package com.jio.crm.dms.bean;

import java.io.Serializable;
import java.util.List;

public class DocumentArchivalConfiguration implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String system;
	private String type;
	private Server server;
	private String locationPath;
	private List<String> archivalDocumentType;
	

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public String getLocationPath() {
		return locationPath;
	}

	public void setLocationPath(String locationPath) {
		this.locationPath = locationPath;
	}

	public List<String> getArchivalDocumentType() {
		return archivalDocumentType;
	}

	public void setArchivalDocumentType(List<String> archivalDocumentType) {
		this.archivalDocumentType = archivalDocumentType;
	}

}
