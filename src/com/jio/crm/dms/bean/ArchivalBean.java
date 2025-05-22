package com.jio.crm.dms.bean;

import java.io.Serializable;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ArchivalBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<DocumentArchivalConfiguration> documentArchivalConfiguration;

	private String cafNumber;
	private String orderId;

	public String getCafNumber() {
		return cafNumber;
	}

	public void setCafNumber(String cafNumber) {
		this.cafNumber = cafNumber;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public List<DocumentArchivalConfiguration> getDocumentArchivalConfiguration() {
		return documentArchivalConfiguration;
	}

	public void setDocumentArchivalConfiguration(List<DocumentArchivalConfiguration> documentArchivalConfiguration) {
		this.documentArchivalConfiguration = documentArchivalConfiguration;
	}

}
