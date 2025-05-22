package com.jio.crm.dms.kafka;

import static java.util.stream.Collectors.toMap;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.jio.crm.dms.configurationmanager.ConfigParamRange;
import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.logger.DappLoggerService;

@SuppressWarnings("squid:S3066")
public enum KafkaConfigEnum {

	// Kafka Parameters
	KAFKA_BROKERS("KAFKA_BROKERS", null, null, false, null, null, null, true, true, false,
			"KAFKA_BROKERS"), MESSAGE_COUNT("MESSAGE_COUNT", null, null, false, null, null, null, true, true, false,
					"MESSAGE_COUNT"), CLIENT_ID("CLIENT_ID", null, null, false, null, null, null, true, true, false,
							"CLIENT_ID"), GROUP_ID_CONFIG("GROUP_ID_CONFIG", null, null, false, null, null, null, true,
									true, false,
									"GROUP_ID_CONFIG"), MAX_NO_MESSAGE_FOUND_COUNT("MAX_NO_MESSAGE_FOUND_COUNT", null,
											null, false, null, null, null, true, true, false,
											"MAX_NO_MESSAGE_FOUND_COUNT"), OFFSET_RESET_LATEST("OFFSET_RESET_LATEST",
													null, null, false, null, null, null, true, true, false,
													"OFFSET_RESET_LATEST"), OFFSET_RESET_EARLIER("OFFSET_RESET_EARLIER",
															null, null, false, null, null, null, true, true, false,
															"OFFSET_RESET_EARLIER"), MAX_POLL_RECORDS(
																	"MAX_POLL_RECORDS", null, null, false, null, null,
																	null, true, true, false,
																	"MAX_POLL_RECORDS"), ENABLE_AUTO_COMMIT_CONFIG(
																			"ENABLE_AUTO_COMMIT_CONFIG", null, null,
																			false, null, null, null, true, true, false,
																			"ENABLE_AUTO_COMMIT_CONFIG"), POLL_TIME(
																					"POLL_TIME", null, null, false,
																					null, null, null, true, true, false,
																					"POLL_TIME"), UOL_PUSH_TOPIC_NAME(
																							"UOL_PUSH_TOPIC_NAME", null,
																							null, false, null, null,
																							null, true, true, false,
																							"UOL_PUSH_TOPIC_NAME");

	// System Category Parameters
	private static Map<String, KafkaConfigEnum> enumCliMap;
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

	private KafkaConfigEnum() {

	}

	private KafkaConfigEnum(String value) {
		this.setParamValue(value);
	}

	private KafkaConfigEnum(String cliArg, Object value, String description, boolean readOnly, String category,
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
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}

	}

	/**
	 * This method is used to load all the Constants in a HashMap<String,String>
	 * enumCliMap for which cliArg is not null key as cliArg and value as identity.
	 */
	public static void loadEnumCliMap() {

		enumCliMap = EnumSet.allOf(KafkaConfigEnum.class).stream().filter(e -> e.cliArg != null)
				.collect(toMap(KafkaConfigEnum::getCliArg, Function.identity()));

	}

	public static Map<String, KafkaConfigEnum> getEnumCliMap() {
		return enumCliMap;
	}

	/**
	 * This method return the parameter of Enum used for CMN Command Line Interface
	 * given the cli argument name.
	 * 
	 * @param cliArgName
	 * @return
	 */
	public static KafkaConfigEnum getEnumFromCliName(String cliArgName) {
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
