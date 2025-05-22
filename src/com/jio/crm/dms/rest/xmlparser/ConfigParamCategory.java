package com.jio.crm.dms.rest.xmlparser;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
/**
 * POJO class for Configuration Parameter Category.
 * The object of this class can also represented in XML 
 * @author Arun2.Maurya
 *
 */
@Root(name = "configparam")
public class ConfigParamCategory {

	@ElementList(required=true, empty=false, inline=true, type=Param.class)
	private List<Param> paramList;

	@Attribute(name = "type", required = true)
	private String paramType;
	
	@Attribute(name = "fixed", required=false)
    String fixed;

	/**
	 * initialise array list
	 */
	public ConfigParamCategory() {
		paramList = new ArrayList<>();
	}
	/**
	 * 
	 * @param params
	 * @param type
	 */
	public ConfigParamCategory(List<Param> params, String type) {
		this.paramList = params;
		this.paramType = type;
	}
	/**
	 * 
	 * @param params
	 * @param type
	 * @param fixed
	 */
	public ConfigParamCategory(List<Param> params, String type, String fixed) {
		this.paramList = params;
		this.paramType = type;
		this.fixed = fixed;
	}
	
	public String getParamType() {
		return paramType;
	}

	public void setParamType(String paramType) {
		this.paramType = paramType;
	}

	public void setParamList(List<Param> paramList) {
		this.paramList = paramList;
	}
	/**
	 * 
	 * @param params
	 */
	public void addParamList(List<Param> params) {
		this.paramList.addAll(params);
	}
	/**
	 * 
	 * @param param
	 */
	public void addToParamList(Param param) {
		this.paramList.add(param);
	}

	public List<Param> getParamList() {
		return this.paramList;
	}
	/**
	 * this method used to clear list
	 */
	public void clearParamList() {
		this.paramList.clear();
	}
	/**
	 * 
	 * @param key
	 * @param value
	 * @param validator
	 * @param readOnly
	 * @param required
	 */
	public void addToParamList(String key, String value,String validator,boolean readOnly,boolean required) {
		this.paramList.add(new Param(key,value,validator,readOnly,required));
	}
}
