package com.jio.crm.dms.configurationmanager;

import java.io.Serializable;

/**
 * POJO to Trace Parameter Changes in Transaction Resource Manager
 * This class implements Serializable so  it can be write on the 
 * disk or can be sent over the network as object.
 *
 */
public class TraceParam implements Serializable{

	
	private static final long serialVersionUID = 1L;
	
	private String paramName;
	private String paramOldValue;
	private String paramNewValue;
	private String changingAgent;
	private String instanceAddr;
	private String timestampOfChange;
	
	
	
	public String getParamName() {
		return paramName;
	}
	public void setParamName(String paramName) {
		this.paramName = paramName;
	}
	public String getParamOldValue() {
		return paramOldValue;
	}
	public void setParamOldValue(String paramOldValue) {
		this.paramOldValue = paramOldValue;
	}
	public String getParamNewValue() {
		return paramNewValue;
	}
	public void setParamNewValue(String paramNewValue) {
		this.paramNewValue = paramNewValue;
	}
	public String getChangingAgent() {
		return changingAgent;
	}
	public void setChangingAgent(String changingAgent) {
		this.changingAgent = changingAgent;
	}
	public String getInstanceAddr() {
		return instanceAddr;
	}
	public void setInstanceAddr(String instanceAddr) {
		this.instanceAddr = instanceAddr;
	}
	public String getTimestampOfChange() {
		return timestampOfChange;
	}
	public void setTimestampOfChange(String timestampOfChange) {
		this.timestampOfChange = timestampOfChange;
	}
	
	
}
