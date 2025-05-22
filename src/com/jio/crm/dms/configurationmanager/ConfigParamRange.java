package com.jio.crm.dms.configurationmanager;

import java.io.Serializable;
import java.util.List;

/**
 * This Class is POJO of ConfigParamRange of DApp
 *
 */
public class ConfigParamRange implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final byte RANGE_TYPE_LIST = 0;
	public static final byte RANGE_TYPE_RANGE = 1;
	private long minValue;
	private long maxValue;
	private List<String> list;

	/**
	 * 
	 * @param minValue
	 * @param maxValue
	 */
	public ConfigParamRange(long minValue, long maxValue) {
		this.minValue = minValue;
		this.maxValue = maxValue;
	}
	
	/**
	 * 
	 * @param list
	 */
	public ConfigParamRange(List<String> list){
		this.list = list;
	}

	public long getMinValue() {
		return minValue;
	}

	public void setMinValue(long minValue) {
		this.minValue = minValue;
	}

	public long getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(long maxValue) {
		this.maxValue = maxValue;
	}

	public List<String> getList() {
		return list;
	}

	public void setList(List<String> list) {
		this.list = list;
	}
	/**
	 * 
	 * @return
	 */
	public byte type() {
		return (this.list == null) ? RANGE_TYPE_RANGE : RANGE_TYPE_LIST;
	}

	@Override
	public String toString() {
		return "DappConfigParamRange [minValue=" + minValue + ", maxValue=" + maxValue + ", list=" + list + "]";
	}

	
}
