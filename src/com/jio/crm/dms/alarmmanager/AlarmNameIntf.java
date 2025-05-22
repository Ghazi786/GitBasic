package com.jio.crm.dms.alarmmanager;

/**
 * 
 * @author Milindkumar.S
 *
 */
public class AlarmNameIntf {

	public static final String DMS_STARTUP_SUCCESS = "DMS is started Succesfully";
	
	//---------------------Database related Alarms--------------------//
	public static final String DMS_ES_CONNECTION_SUCCESS ="DMS_DATABASE_CONNECTION_SUCCESS";
	public static final String DMS_ES_CONNECTION_FAILURE ="DMS_DATABASE_CONNECTION_FAILURE";
	
	//---------------------OAM COnnection Alarms--------------------//
	public static final String DMS_OAM_CONNECTION_SUCCESS ="DMS_OAM_CONNECTION_SUCCESS";
	public static final String DMS_OAM_CONNECTION_FAILURE ="DMS_OAM_CONNECTION_FAILURE";
	
    //-----------------------HDFS related Alarms------------------//
	public static final String DMS_HDFS_CONNECTION_SUCCESS = "DMS_HDFS_CONNECTION_SUCCESS";
	public static final String DMS_HDFS_CONNECTION_FAILURE = "DMS_HDFS_CONNECTION_FAILURE";
	
	public static final String DMS_ES_EVENT_INDEX_CREATION_FAILED = "DMS_ES_EVENT_INDEX_CREATION_FAILED";

	public static final String DMS_EVENT_MAPPING_CREATION_FAIL = "DMS_EVENT_MAPPING_CREATION_FAIL";
	public static final String APP_LOGGING_INIT_FAILED = "APP_LOGGING_INIT_FAILED";
	
	//-------------------Notification Hub Alarms----------------//
	public static final String NH_REGISTRATION_FAILED = "NH_REGISTRATION_FAILED";
	public static final String NH_REGISTRATION_SUCCESS = "NH_REGISTRATION_SUCCESS";

	private AlarmNameIntf(){
	}

}
