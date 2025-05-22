/**
 * 
 */
package com.jio.crm.dms.bean;

import java.io.Serializable;
import java.util.Date;

import com.elastic.search.annotation.Entity;
import com.elastic.search.annotation.Id;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Ghajnafar.Shahid
 *
 */
@Entity(name = "document")
@JsonIgnoreProperties(ignoreUnknown = true)
public class FileDocument implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	private String name;
	private String productName;
	private String documentCategory;
	private String mobileNumber;
	private String orderId;
	private String customerId;
	private String documentNumber;
	private String fileName;
	private String actualFileName;
	private String size;
	private String circle;
	private String cafNumber;
	private boolean cosSpecific;

	private String attendedBy;
	private String emailAddress;
	private String alternateMobileNumber;
	private String filePath;
	private Date uploadedOn;

	private String externalId;
	private String filePathUrl;
	
	private String archivalStatus;
	private String resultStatus;
	

	public String getArchivalStatus() {
		return archivalStatus;
	}

	public void setArchivalStatus(String archivalStatus) {
		this.archivalStatus = archivalStatus;
	}

	public String getResultStatus() {
		return resultStatus;
	}

	public void setResultStatus(String resultStatus) {
		this.resultStatus = resultStatus;
	}

	public String getFilePathUrl() {
		return filePathUrl;
	}

	public void setFilePathUrl(String filePathUrl) {
		this.filePathUrl = filePathUrl;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public boolean isCosSpecific() {
		return cosSpecific;
	}

	public void setCosSpecific(boolean cosSpecific) {
		this.cosSpecific = cosSpecific;
	}

	public String getCafNumber() {
		return cafNumber;
	}

	public void setCafNumber(String cafNumber) {
		this.cafNumber = cafNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getAlternateMobileNumber() {
		return alternateMobileNumber;
	}

	public void setAlternateMobileNumber(String alternateMobileNumber) {
		this.alternateMobileNumber = alternateMobileNumber;
	}

	public String getDocumentCategory() {
		return documentCategory;
	}

	public void setDocumentCategory(String documentCategory) {
		this.documentCategory = documentCategory;
	}

	public String getAttendedBy() {
		return attendedBy;
	}

	public void setAttendedBy(String attendedBy) {
		this.attendedBy = attendedBy;
	}

	public String getCircle() {
		return circle;
	}

	public void setCircle(String circle) {
		this.circle = circle;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getActualFileName() {
		return actualFileName;
	}

	public void setActualFileName(String actualFileName) {
		this.actualFileName = actualFileName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

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

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Date getUploadedOn() {
		return uploadedOn;
	}

	public void setUploadedOn(Date uploadedOn) {
		this.uploadedOn = uploadedOn;
	}

	@Override
	public String toString() {
		return "FileDocument [id=" + id + ", name=" + name + ", productName=" + productName + ", documentCategory="
				+ documentCategory + ", mobileNumber=" + mobileNumber + ", orderId=" + orderId + ", customerId="
				+ customerId + ", documentNumber=" + documentNumber + ", fileName=" + fileName + ", actualFileName="
				+ actualFileName + ", size=" + size + ", circle=" + circle + ", cosSpecific=" + cosSpecific
				+ ", attendedBy=" + attendedBy + ", emailAddress=" + emailAddress + ", alternateMobileNumber="
				+ alternateMobileNumber + ", filePath=" + filePath + ", uploadedOn=" + uploadedOn + "]";
	}

}
