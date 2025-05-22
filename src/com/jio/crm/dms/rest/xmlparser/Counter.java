package com.jio.crm.dms.rest.xmlparser;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * 
 * Class to create all the parameter information in a specific format.
 * e.g.
 *
 * <counter type="">
 * <param name="" value="">
 * --------------
 * --------------
 * <param name="" value="">
 * </counter>
 *
 */

@Root(name="counter")
public class Counter {

	@Override
	public String toString() {
		return "Counter [param=" + param + ", counterType=" + counterType + "]";
	}

	@ElementList(name = "param", empty = false, inline = true, required = true, type = Param.class)
	private List<Param> param;

	@Attribute(name="type", required=true)
	private String counterType;

	public Counter(){
		param = new ArrayList<>();
	}
	
	public List<Param> getParam() {
		return param;
	}
	
	public void setParam(List<Param> param) {
		this.param = param;
	}

	public void addToParamList(Param par){
		param.add(par);
	}

	public String getCounterType() {
		return counterType;
	}
	
	public void setCounterType(String counterType) {
		this.counterType = counterType;
	}
}

