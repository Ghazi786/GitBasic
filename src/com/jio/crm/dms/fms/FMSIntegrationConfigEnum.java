package com.jio.crm.dms.fms;

import static java.util.stream.Collectors.toMap;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.jio.crm.dms.configurationmanager.ConfigParamRange;
import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;

/**
 * FMS Integration Service Configurations
 * 
 * @author Kiran.Jangid
 *
 */

@SuppressWarnings("squid:S3066")
public enum FMSIntegrationConfigEnum {

	SERVICE_URL("fms_service_url", null, null, false, null, null, null, false, true, false, "fms_service_url"),

	IS_SERVICE_SECURE("fms_service_is_secure", null, null, false, null, null, null, false, true, false,
			"fms_service_is_secure"),

	SERVICE_KEYSTORE("fms_service_keystore_path", null, null, false, null, null, null, false, true, false,
			"fms_service_keystore_path"),

	SERVICE_KEYPHRASE("fms_service_keystore_password", null, null, false, null, null, null, false, true, false,
			"fms_service_keystore_password"),

	TRUST_STORE_PASSWORD("fms_service_truststore_password", null, null, false, null, null, null, false, true, false,
			"fms_service_truststore_password"),

	SERVICE_TRUST_STORE_PATH("fms_service_truststore_path", null, null, false, null, null, null, false, true, false,
			"fms_service_truststore_path"),

	SERVICE_IDENTIFIER("fms_service_identifier", null, null, false, null, null, null, false, true, false,
			"fms_service_identifier"),

	SERVICE_CONTEXT("fms_service_context", null, null, false, null, null, null, false, true, false,
			"fms_service_context"),

	API_KEY(null);

	// System Category Parameters
	private static Map<String, FMSIntegrationConfigEnum> enumCliMap;
	private Object value;
	private String cliArg;
	private String description;
	private boolean readOnly;
	private String category;
	private String validator;
	private ConfigParamRange paramRange;
	private boolean visibilityInEms;
	private boolean valueRequired;
	private String key;

	private FMSIntegrationConfigEnum() {

	}

	private FMSIntegrationConfigEnum(String value) {
		this.setParamValue(value);
	}

	private FMSIntegrationConfigEnum(String cliArg, Object value, String description, boolean readOnly, String category,
			String validator, ConfigParamRange paramRange, boolean visibilityInEms, boolean valueRequired,
			boolean oamParam, String key) {
		this.cliArg = cliArg;
		this.value = value;
		this.description = description;
		this.readOnly = readOnly;
		this.category = category;
		this.validator = validator;
		this.paramRange = paramRange;
		this.visibilityInEms = visibilityInEms;
		this.valueRequired = valueRequired;
		this.key = key;
	}

	public void setParamValue(String value) {
		try {
			this.value = value;
		} catch (Exception fne) {

		}

	}

	/**
	 * This method is used to load all the Constants in a HashMap<String,String>
	 * enumCliMap for which cliArg is not null key as cliArg and value as identity.
	 */
	public static void loadEnumCliMap() {

		enumCliMap = EnumSet.allOf(FMSIntegrationConfigEnum.class).stream().filter(e -> e.cliArg != null)
				.collect(toMap(FMSIntegrationConfigEnum::getCliArg, Function.identity()));

	}

	public static Map<String, FMSIntegrationConfigEnum> getEnumCliMap() {
		return enumCliMap;
	}

	/**
	 * This method return the parameter of Enum used for CMN Command Line Interface
	 * given the cli argument name.
	 * 
	 * @param cliArgName
	 * @return
	 */
	public static FMSIntegrationConfigEnum getEnumFromCliName(String cliArgName) {
		return enumCliMap.get(cliArgName);
	}

	public static int size() {
		return ConfigParamsEnum.values().length;
	}

	public Object getValue() {
		return value;
	}

	public int getIntValue() {
		return Integer.parseInt(value + "");
	}

	public long getLongValue() {
		return Long.parseLong(value + "");
	}

	public String getStringValue() {
		return value + "";
	}

	public byte getByteValue() {
		return Byte.parseByte(value + "");
	}

	public float getFloatValue() {
		return Float.parseFloat(value + "");
	}

	public double getDoubleValue() {
		return Double.parseDouble(value + "");
	}

	public boolean getBooleanValue() {
		return Boolean.parseBoolean(value + "");
	}

	public AtomicBoolean getAtomicBooleanValue() {
		return (AtomicBoolean) value;
	}

	/***************************************************************************************/
	/***********************************
	 * Getter of Fields
	 ***********************************/
	/***************************************************************************************/
	public String getCliArg() {
		return cliArg;
	}

	public String getDescription() {
		return description;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public String getCategory() {
		return category;
	}

	public String getValidator() {
		return validator;
	}

	public ConfigParamRange getParamRange() {
		return paramRange;
	}

	public boolean isVisibilityInEms() {
		return visibilityInEms;
	}

	public boolean isValueRequired() {
		return valueRequired;
	}

	public String getKey() {
		return key;
	}

	/***************************************************************************************/
	/***********************************
	 * Setter of Fields
	 ***********************************/
	/***************************************************************************************/

	void setValue(Object value) {
		this.value = value;
	}

	void setCliArg(String cliArg) {
		this.cliArg = cliArg;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setValidator(String validator) {
		this.validator = validator;
	}

	public void setParamRange(ConfigParamRange paramRange) {
		this.paramRange = paramRange;
	}

	void setVisibilityInEms(boolean visibilityInEms) {
		this.visibilityInEms = visibilityInEms;
	}

	void setValueRequired(boolean valueRequired) {
		this.valueRequired = valueRequired;
	}

	void setKey(String key) {
		this.key = key;
	}

}
