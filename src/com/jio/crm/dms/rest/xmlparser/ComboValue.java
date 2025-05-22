package com.jio.crm.dms.rest.xmlparser;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name="ComboValue")
public class ComboValue {
	
	@Attribute(name = "value", required = true)
	private String value;
	
	public ComboValue() {
		this.value=null;
	}
	public ComboValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
