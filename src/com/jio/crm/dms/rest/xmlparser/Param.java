package com.jio.crm.dms.rest.xmlparser;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * 
 * An element of Root CofigParam,Counter i.e One Single Parameter OR  One Single Counter
 *
 */

@Root(name ="param")
public class Param 
{
	@Attribute(name = "name", required = true)
	private String name;
	@Attribute(name = "value", required = true)
	private String value;
	@Attribute(name = "ReadOnly", required = false)
	private Boolean readOnly;
	@Attribute(name = "validator", required = false)
	private String validator;
	@Attribute(name = "MinRange", required = false)
	private Long minRange;
	@Attribute(name = "required", required = false)
	private Boolean required;
	@Attribute(name = "MaxRange", required = false)
	private Long maxRange;
	@ElementList(required=false, empty=false, inline=true, type=ComboValue.class)
	private List<ComboValue> comboValueList=new ArrayList<>();
	
	/**
	 * call super constructor
	 */
	public Param() {
		super();
	}
	
	/**
	 * 
	 * @param name
	 * @param value
	 */
	public Param(String name,String value) {
		this.name=name;
		this.value=value;
	}

	/**
	 * 
	 * @param name
	 * @param value
	 * @param validator
	 * @param readOnly
	 * @param required
	 */
	public Param(String name,String value,String validator,Boolean readOnly,Boolean required) {
		this.name=name;
		this.value=value;
		this.validator=validator;
		this.readOnly=readOnly;
		this.required = required;
	}
	
	/**
	 * 
	 * @param name
	 * @param value
	 * @param validator
	 * @param readOnly
	 * @param required
	 * @param minRange
	 * @param maxRange
	 */
	public Param(String name,String value,String validator,Boolean readOnly,Boolean required,long minRange,long maxRange) {
		this.name=name;
		this.value=value;
		this.validator=validator;
		this.readOnly=readOnly;
		this.maxRange=maxRange;
		this.minRange=minRange;
		this.required=required;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public List<ComboValue> getComboValueList() {
		return comboValueList;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}
	
	public String getValidator() {
		return validator;
	}

	public void setValidator(String validator) {
		this.validator = validator;
	}
	
	/**
	 * 
	 * @param comboValue
	 */
	public void addToComboValueList(ComboValue comboValue) {
		this.comboValueList.add(comboValue);
	}
	
	public long getMinRange() {
		return minRange;
	}

	public void setMinRange(Long minRange) {
		this.minRange = minRange;
	}

	public long getMaxRange() {
		return maxRange;
	}

	public void setMaxRange(Long maxRange) {
		this.maxRange = maxRange;
	}
	
	/**
	 * by default read only flag value is null
	 */
	public void removeReadOnlyFlag(){
		this.readOnly = null;
	}
	
	public void setComboValueList(List<ComboValue> comboValueList) {
		this.comboValueList = comboValueList;
	}

	public Boolean getRequired() {
		return required;
	}

	public void setRequired(Boolean required) {
		this.required = required;
	}

	public Boolean getReadOnly() {
		return readOnly;
	}

	@Override
	public String toString() {
		return "Param [name=" + name + ", value=" + value + ", readOnly=" + readOnly + ", validator=" + validator
				+ ", minRange=" + minRange + ", required=" + required + ", maxRange=" + maxRange + ", comboValueList="
				+ comboValueList + "]";
	}

	
	
}
