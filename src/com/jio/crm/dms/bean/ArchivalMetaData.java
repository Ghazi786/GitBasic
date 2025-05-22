package com.jio.crm.dms.bean;

import java.io.Serializable;
import java.util.List;

import com.elastic.search.annotation.Entity;
import com.elastic.search.annotation.Id;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jio.subscriptions.modules.bean.Characteristic;

@Entity(name = "archival_configuration_bean")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArchivalMetaData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8430801231981941239L;

	@Id
	private String id;
	private String cafNumber;
	private String documentType;
	private String path;
	private String archivalSystem;
	private ArchivalStatus status;
	private String updatedOn;
	private String type;
	private String customerId;
	private String orderId;
	private List<Characteristic> characteristic;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCafNumber() {
		return cafNumber;
	}

	public void setCafNumber(String cafNumber) {
		this.cafNumber = cafNumber;
	}

	public String getDocumentType() {
		return documentType;
	}

	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getArchivalSystem() {
		return archivalSystem;
	}

	public void setArchivalSystem(String archivalSystem) {
		this.archivalSystem = archivalSystem;
	}

	public ArchivalStatus getStatus() {
		return status;
	}

	public void setStatus(ArchivalStatus status) {
		this.status = status;
	}

	public String getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(String updatedOn) {
		this.updatedOn = updatedOn;
	}

	public List<Characteristic> getCharacteristic() {
		return characteristic;
	}

	public void setCharacteristic(List<Characteristic> characteristic) {
		this.characteristic = characteristic;
	}

}
