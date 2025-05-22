package com.jio.crm.dms.utils;

/**
 * Constant that help to parse configurations
 * 
 * @author Kiran.Jangid
 *
 */

public interface ExcelBookConstants {

	String CONFIG_PARAM_PATH = "../configuration/ConfigParamSheet.xlsx";

	/**
	 * 
	 * this interface is used to get the index value of column used in
	 * ConfigParamSheet.
	 */
	public interface ColumnIndex {
		byte INDEX_PARAM_NAME = 1;
		byte INDEX_PARAM_TYPE = 2;
		byte INDEX_PARAM_VALUE = 3;
		byte INDEX_RUNTIME_CONFIG = 4;
		byte INDEX_STARTUP_CONFIG = 5;
		byte INDEX_DESCRIPTION = 6;
		byte INDEX_POSSIBLE_VALUES = 7;
	}

	/**
	 * 
	 * This interface is used to get the index value of Sheets used in
	 * ConfigParamSheet of Transaction Resource Manager
	 *
	 */
	public interface ParamSheet {
		byte CONFIG_SETTINGS_SHEET = 0;
		byte DATABASE_SHEET = 1;
		byte OAM_CLIENT_SHEET = 2;
		byte ALARM_SHEET = 3;
		byte INDEX_POOLING_SHEET = 4;
		byte KAFKA_CONFIG_SHEET = 5;
		int NOTIFICATION_CONFIG_SHEET = 7;
		int TIBCO_INTEGRATION_CONFIG_SHEET = 6;
		int FMS_INTEGRATION_CONFIG_SHEET = 8;
	}
}
