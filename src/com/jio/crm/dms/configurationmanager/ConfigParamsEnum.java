package com.jio.crm.dms.configurationmanager;

import static java.util.stream.Collectors.toMap;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import com.atom.OAM.Client.Management.OamClientManager;
import com.jio.crm.dms.logger.DappLogManager;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.Constants;

/**
 * CMNConfigParamsEnum contains all the configuration parameter sheet of CMN
 * 
 * @author Milindkumar.S
 *
 */

public enum ConfigParamsEnum {

	/*****************************************************************************************************************/
	/**************************************
	 * Configurable Parameter Sheet
	 ***********************************************/
	/*****************************************************************************************************************/
	HTTP_STACK_HOST_IP("HTTP_STACK_HOST_IP", null, null, false, null, null, null, false, true, false,Constants.HTTP_IP), 
	HTTP_STACK_PORT("HTTP_STACK_PORT", null, null, false, null, null, null, false, true, false,Constants.HTTP_PORT),
	PRODUCT_NAME("productname", null, null, false, null, null, null, true, true,false, Constants.PRODUCTNAME), 
	PRODUCT_VERSION("productversion", null, null, false, null,null, null, true, true, false, Constants.PRODUCTVERSION),
	APPLICATION_LOG_LEVEL("Application_Log_Level", null, null, false, null, null, null, true, true,false, null),
	MAX_LOG_FILESIZE("Max_Log_Filesize", null, null, false, null,null, null, true, true, false,"Max_Log_Filesize"),
	MAX_BACKUP_INDEX("Max_Backup_Index", null,null, false, null, null, null, true, true, false,"maxBackupIndex"),
	LOG_FILE_PATH("Max_Backup_Index", null,null, false, null, null, null, true, true, false,"logfilepath"),
	COUNTER_PURGE_DURATION("counter_purge_duration", null, null, false,null, null, null, true, true, false,"counterPurgeDuration"),
	CORE_POOL_SIZE("corePoolsize", null, null, false,null, null, null, true, true, false,"corePoolsize"),
	MAX_CORE_POOL_SIZE("maximumPoolSize", null,null, false, null, null,null, true, true, false,"maximumPoolSize"),
	KEEP_ALIVE_TIME("keepAliveTime",null, null, false,null, null, null,true, true, false,"keepAliveTime"),
	WORK_QUEUE_SIZE("workQueueSize",null, null,false, null,null, null,true, true,false,"workQueueSize"),
	ERM_EVENT_CONTEXT("ERM_EVENT_CONTEXT",null, null,false, null,null, null,true, true,false,"ERM_EVENT_CONTEXT"),
	HTTPPORT("httpPort",null, null,false, null,null, null,true, true,false,"httpPort"),
	HTTPIP("httpIP",null, null,false, null,null, null,true, true,false,"httpIP"),
	USERNAME("username",null, null,false, null,null, null,true, true,false,"username"),
	PASSWORD("password",null, null,false, null,null, null,true, true,false,"password"),
	
	// ftp and csv parameter
	FTPUSERNAME("FtpUserName", null, null, false, null, null, null, true, true, false, "FtpUserName"),
	FTPREMOTEHOST("FtpRemoteHost", null, null, false, null, null, null, true, true, false,"FtpRemoteHost"),
	FTPREMOTEPORT("FtpRemotePort", null, null, false, null, null, null, true, true, false,"FtpRemotePort"),
	FTPPASSWORD("FtpPassword", null, null, false, null, null, null, true, true, false,"FtpPassword"),
	FTPPATH("FtpPath", null, null, false, null, null, null, true, true, false,"FtpPath"),

	/********************
	 * ES Configuration ENUM
	 *******************************************/
	ES_CLUSTERNAME("clustername", null, null, false, null, null, null, true, true, false, "clustername"),
	ES_INDEXNAME("indexname", null, null, false, null, null, null, true, true, false, "indexname"),
	ES_TYPENAME("typename",null, null, false, null, null, null, true, true, false, "typename"),
	ES_XPACKUNAME("xpname", null,null, false, null, null, null, true, true, false, "xpname"), 
	ES_XPACKPASSWORD("xpassword",null, null, false, null, null, null, true, true, false,"xpassword"),
	ES_MASTERNODEIPANDPORT("masteripandport", null, null, false, null,null, null, true, true, false,"masteripandport"),
	ES_CORDINATORNODEIPANDPORT("cordinatoripandport", null,null, false, null, null, null, true, true, false,"cordinatoripandport"),
	ES_BULKACTION("ES_Bulk_Action", null, null, false, null, null, null, true, true, false,"ES_Bulk_Action"),
	ES_FLUSHINTERVAL("ES_Flush_Interval", null, null, false, null, null, null, true, true,false, "ES_Flush_Interval"),
	ES_CONCURRENTREQUESTS("ES_Concurrent_Requests", null, null, false,null, null, null, true, true, false,"ES_Concurrent_Requests"),
	ES_INITIAL_DELAY_BACKOFF_POLICY("ES_Initial_Delay_BackoffPolicy",null, null, false, null, null, null, true, true, false,"ES_Initial_Delay_BackoffPolicy"), 
	ES_MAX_NO_OF_RETRIES_BACKOFFPOLICY("ES_Max_No_Of_Retries_BackoffPolicy", null, null, false, null, null, null,true, true, false,"ES_Max_No_Of_Retries_BackoffPolicy"),
	ES_ARCHIVE_TTL("ES_archive_ttl",null, null, false, null, null, null, true, true, false,"ES_archive_ttl"),
	ES_ARCHIVE_TTL_PERIOD("ES_archive_ttl_period",null, null, false, null, null, null, true, true, false,"ES_archive_ttl_period"),
	PRODUCTVERSION_ES("productversion_es",null, null, false, null, null, null, true, true,false, "productversion_es"),
	
	
	
	//jbc cosntants
	
	
	NODEID("nodeId",null, null, false, null, null, null, true, true,false, "nodeId"),
	TENANTID("tenantid",null, null, false, null, null, null, true, true,false, "tenantid"),
	CONTRACTID("contractid",null, null, false, null, null, null, true, true,false, "contractid"),
	NETWORKID("networkid",null, null, false, null, null, null, true, true,false, "networkid"),
	KEYPHRASE("keyPhrase",null, null, false, null, null, null, true, true,false, "keyPhrase"),
	TRUSTSTOREPASSWORD("trustStorePassword",null, null, false, null, null, null, true, true,false, "trustStorePassword"),
	KEYSTOREFILEPATH("keyStoreFile", null, null, false, null, null, null, false, true, false,"keyStoreFile"), 
	TRUSTSTOREFILEPATH("trustStoreFilePath", null, null, false, null, null, null, false, true, false,"trustStoreFilePath"), 
	NODEIDJBC("nodeIDJBC", null, null, false, null, null, null, false, true, false,"nodeIDJBC"), 
	IDENTIFIER("identifier",null, null, false, null, null, null, true, true,false, "identifier"),
	TOKEN("token",null, null, false, null, null, null, true, true,false, "token"),
	
	
	//cluster 
	CLUSTERNAME("clusterName",null, null, false, null, null, null, true, true,false, "clusterName"),
	NODENAME("nodeName",null, null, false, null, null, null, true, true,false, "nodeName"),
	NODEIP("nodeIp",null, null, false, null, null, null, true, true,false, "nodeIp"),
	CLUSTERADDRESSES("clusterAddresses",null, null, false, null, null, null, true, true,false, "clusterAddresses"),
	NODENAMES("nodeNames",null, null, false, null, null, null, true, true,false, "nodeNames"),
	LOCALRECONNECTIONINTERVAL("localReconnectionInterval",null, null, false, null, null, null, true, true,false, "localReconnectionInterval"),
	REMOTEPORT("remotePort",null, null, false, null, null, null, true, true,false, "remotePort"),
	ISGEONODE("isgeoNode",null, null, false, null, null, null, true, true,false, "isgeoNode"),
	LOCALJETTYNODEPORT("localJettyNodePort",null, null, false, null, null, null, true, true,false, "localJettyNodePort"),
	ERMIP("ermIp",null, null, false, null, null, null, true, true,false, "ermIp"),
	ERMPORT("ermPort",null, null, false, null, null, null, true, true,false, "ermPort"),
	JMXWEBPORT("jmxWebPort",null, null, false, null, null, null, true, true,false, "jmxWebPort"),
	JMXSERVERPORT("jmxServerPort",null, null, false, null, null, null, true, true,false, "jmxServerPort"),
	LOCALIP("localIp",null, null, false, null, null, null, true, true,false, "localIp"),
	NODENAMESINTHECLUSTER("nodeNamesInTheCluster",null, null, false, null, null, null, true, true,false, "nodeNamesInTheCluster"),
	CSVFILEREADJOB("csvFileReadJob",null, null, false, null, null, null, true, true,false, "csvFileReadJob"),
	CSVFILEUPLOADJOB("csvFileUploadJob",null, null, false, null, null, null, true, true,false, "csvFileUploadJob"),
	SCEGROUPID("sceGroupId",null, null, false, null, null, null, true, true,false, "sceGroupId"),
	PUBLISHERTOPIC("publisherTopic",null, null, false, null, null, null, true, true,false, "publisherTopic"),
	KAFKABROKERIPPORT("kafkaBrokerIpPort",null, null, false, null, null, null, true, true,false, "kafkaBrokerIpPort"),
	CHAINCODENAME("chainCodeName",null, null, false, null, null, null, true, true,false, "chainCodeName"),
	TOPICINSEQUENCE("topicInSequence",null, null, false, null, null, null, true, true,false, "topicInSequence"),
	TOPICINPARALLER("topicInParaller",null, null, false, null, null, null, true, true,false, "topicInParaller"),
	SALESINR("salesinr",null, null, false, null, null, null, true, true,false, "salesinr"),
	SALEQUANTITY("salequantity",null, null, false, null, null, null, true, true,false, "salequantity"),
	JIOPHONE("jiophone",null, null, false, null, null, null, true, true,false, "jiophone"),
	MIFI("mifi",null, null, false, null, null, null, true, true,false, "mifi"),
	RECHARGE("recharge",null, null, false, null, null, null, true, true,false, "recharge"),
	MNP("mnp",null, null, false, null, null, null, true, true,false, "mnp"),
	//clear Codes 
	XDR_RECORD_LIMIT_PER_FILE("XDR_RECORD_LIMIT_PER_FILE",null, null, false, null, null, null, true, true,false, "XDR_RECORD_LIMIT_PER_FILE"),
	XDR_LOCAL_DUMP_PATH("XDR_LOCAL_DUMP_PATH",null, null, false, null, null, null, true, true,false, "XDR_LOCAL_DUMP_PATH"),
	XDR_FTP_ENABLED("XDR_FTP_ENABLED",null, null, false, null, null, null, true, true,false, "XDR_FTP_ENABLED"),
	XDR_REMOTE_ADDRESSES("XDR_REMOTE_ADDRESSES",null, null, false, null, null, null, true, true,false, "XDR_REMOTE_ADDRESSES"),
	XDR_REMOTE_USER_NAME("XDR_REMOTE_USER_NAME",null, null, false, null, null, null, true, true,false, "XDR_REMOTE_USER_NAME"),
	XDR_REMOTE_PASSWORD("XDR_REMOTE_PASSWORD",null, null, false, null, null, null, true, true,false, "XDR_REMOTE_PASSWORD"),
	XDR_REMOTE_DUMP_PATH("XDR_REMOTE_DUMP_PATH",null, null, false, null, null, null, true, true,false, "XDR_REMOTE_DUMP_PATH"),
	JIODEVOPS_FOR_CLEAR_CODE("JIODEVOPS_FOR_CLEAR_CODE",null, null, false, null, null, null, true, true,false, "JIODEVOPS_FOR_CLEAR_CODE"),

	// webhooks config
	WEBHOOKS_QUEUE_SIZE("webHooksPoolsize", null, null, false,null, null, null, true, true, false,"webHooksPoolsize"),
	
	// for Service Communicator Protocol
	ELB_HOSTED_PORT("ELB_HOSTED_PORT",null, null, false, null, null, null, true, true,false, "ELB_HOSTED_PORT"),
	ELB_HOSTED_IP("ELB_HOSTED_IP",null, null, false, null, null, null, true, true,false, "ELB_HOSTED_IP"),
	ELB_HOSTED_PROTOCOL("ELB_HOSTED_PROTOCOL",null, null, false, null, null, null, true, true,false, "ELB_HOSTED_PROTOCOL"),
	
	ELB_HOSTED_PORT_SECURE("ELB_HOSTED_PORT_SECURE",null, null, false, null, null, null, true, true,false, "ELB_HOSTED_PORT_SECURE"),
	ELB_HOSTED_PROTOCOL_SECURE("ELB_HOSTED_PROTOCOL_SECURE",null, null, false, null, null, null, true, true,false, "ELB_HOSTED_PROTOCOL_SECURE"),

	OAM_IP("OAM_IP",null, null, false, null, null, null, true, true,false,"OAM_IP"),
	OAM_HTTP_PORT("OAM_HTTP_PORT",null, null, false, null, null, null, true, true,false,"OAM_HTTP_PORT"),
	
	// File Upload directory
	FILE_UPLOAD_DIR("FILE_UPLOAD_DIR",null, null, false, null, null, null, true, true,false, "FILE_UPLOAD_DIR"),
	
	//FTP REQUEST 
	SFTPHOST("SFTPHOST",null, null, false, null, null, null, true, true,false, "SFTPHOST"),
	SFTPPORT("SFTPPORT",null, null, false, null, null, null, true, true,false, "SFTPPORT"),
	SFTPUSER("SFTPUSER",null, null, false, null, null, null, true, true,false, "SFTPUSER"),
	SFTPPASS("SFTPPASS",null, null, false, null, null, null, true, true,false, "SFTPPASS"),
	SFTPWORKINGDIR("SFTPWORKINGDIR",null, null, false, null, null, null, true, true,false, "SFTPWORKINGDIR"),
	
	//NAS location
	NAS_LOCATION("NAS_LOCATION",null, null, false, null, null, null, true, true,false, "NAS_LOCATION"),
	
	//upload file through ftp
	FILESYSTEM("FILESYSTEM",null, null, false, null, null, null, true, true,false, "FILESYSTEM"),
	UPLOADSFTPHOST("UPLOADSFTPHOST",null, null, false, null, null, null, true, true,false, "UPLOADSFTPHOST"),
	UPLOADSFTPPORT("UPLOADSFTPPORT",null, null, false, null, null, null, true, true,false, "UPLOADSFTPPORT"),
	UPLOADSFTPUSER("UPLOADSFTPUSER",null, null, false, null, null, null, true, true,false, "UPLOADSFTPUSER"),
	UPLOADSFTPPASS("UPLOADSFTPPASS",null, null, false, null, null, null, true, true,false, "UPLOADSFTPPASS"),
	MINIO_USER("MINIO_USER",null, null, false, null, null, null, true, true,false, "MINIO_USER"),
	MINIO_PASSWORD("MINIO_PASSWORD",null, null, false, null, null, null, true, true,false, "MINIO_PASSWORD"),
	MINIO_URL("MINIO_URL",null, null, false, null, null, null, true, true,false, "MINIO_URL"), 
	BUCKETNAME("BUCKETNAME",null, null, false, null, null, null, true, true,false, "BUCKETNAME");
	
	// System Category Parameters
	private static Map<String, ConfigParamsEnum> enumCliMap;
	private Object value;
	private String cliArg;
	private String description;
	private boolean readOnly;
	private String category;
	private String validator;
	private ConfigParamRange paramRange;
	private boolean visibilityInEms;
	private boolean valueRequired;
	private boolean oamParam;
	private String key;

	private ConfigParamsEnum() {

	}

	private ConfigParamsEnum(String cliArg, Object value, String description, boolean readOnly, String category,
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
		this.oamParam = oamParam;
		this.key = key;
	}

	/**
	 * 
	 * @param value
	 * @param traceParam
	 * @param sentByCli
	 * @param isBulkset
	 * @return
	 */
	@SuppressWarnings("squid:S3066")
	public void setParamValue(String value) {
		try {
			this.value = value;
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();			
			
			
		}
		
	}

	public byte setValue(String value, TraceParam traceParam, boolean sentByCli, boolean isBulkset) {

		try {
			switch (this) {
			case APPLICATION_LOG_LEVEL:
				DappLogManager.getInstance().setCurrentLogLevel(value);
				setParamValue(value);
				break;

			case CORE_POOL_SIZE:
				ConfigParamsEnum.CORE_POOL_SIZE.setParamValue(value, null, sentByCli, isBulkset);
				break;
			case COUNTER_PURGE_DURATION:
				ConfigParamsEnum.COUNTER_PURGE_DURATION.setParamValue(value, null, sentByCli, isBulkset);
				break;

			case MAX_CORE_POOL_SIZE:
				ConfigParamsEnum.MAX_CORE_POOL_SIZE.setParamValue(value, null, sentByCli, isBulkset);
				break;

			case WORK_QUEUE_SIZE:
				ConfigParamsEnum.WORK_QUEUE_SIZE.setParamValue(value, null, sentByCli, isBulkset);
				break;
			
			case WEBHOOKS_QUEUE_SIZE:
				ConfigParamsEnum.WEBHOOKS_QUEUE_SIZE.setParamValue(value, null, sentByCli, isBulkset);

			default:

			}
		} catch (Exception e) {

			return (byte) 1;
		}
		return (byte) 0;
	}

	public void setParamValue(String value, TraceParam traceParam, boolean sentByCli, boolean isBulkset) {
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

		enumCliMap = EnumSet.allOf(ConfigParamsEnum.class).stream().filter(e -> e.cliArg != null)
				.collect(toMap(ConfigParamsEnum::getCliArg, Function.identity()));

	}

	public static Map<String, ConfigParamsEnum> getEnumCliMap() {
		return enumCliMap;
	}

	/**
	 * This method return the parameter of Enum used for CMN Command Line Interface
	 * given the cli argument name.
	 * 
	 * @param cliArgName
	 * @return
	 */
	public static ConfigParamsEnum getEnumFromCliName(String cliArgName) {
		return enumCliMap.get(cliArgName);
	}

	private String getOamConfigValue() {
		return OamClientManager.getInstance().getOAMClientParam(this.key);
	}

	protected static int size() {
		return ConfigParamsEnum.values().length;
	}

	public Object getValue() {
		return oamParam ? getOamConfigValue() : value;
	}

	public int getIntValue() {
		return Integer.parseInt(oamParam ? getOamConfigValue() : value + "");
	}

	public long getLongValue() {
		return Long.parseLong(oamParam ? getOamConfigValue() : value + "");
	}

	public String getStringValue() {
		return oamParam ? getOamConfigValue() : (value == null) ? null : (value + "");
	}

	public byte getByteValue() {
		return Byte.parseByte(oamParam ? getOamConfigValue() : value + "");
	}

	public float getFloatValue() {
		return Float.parseFloat(oamParam ? getOamConfigValue() : value + "");
	}

	public double getDoubleValue() {
		return Double.parseDouble(oamParam ? getOamConfigValue() : value + "");
	}

	public boolean getBooleanValue() {
		return Boolean.parseBoolean(oamParam ? getOamConfigValue() : value + "");
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

	public boolean isOamParam() {
		return oamParam;
	}

	public String getKey() {
		return key;
	}

	/***************************************************************************************/
	/***********************************
	 * Setter of Fields
	 ***********************************/
	/***************************************************************************************/

	protected void setValue(Object value) {
		this.value = value;
	}

	protected void setCliArg(String cliArg) {
		this.cliArg = cliArg;
	}

	protected void setDescription(String description) {
		this.description = description;
	}

	protected void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	protected void setCategory(String category) {
		this.category = category;
	}

	protected void setValidator(String validator) {
		this.validator = validator;
	}

	protected void setParamRange(ConfigParamRange paramRange) {
		this.paramRange = paramRange;
	}

	protected void setVisibilityInEms(boolean visibilityInEms) {
		this.visibilityInEms = visibilityInEms;
	}

	protected void setValueRequired(boolean valueRequired) {
		this.valueRequired = valueRequired;
	}

	protected void setOamParam(boolean oamParam) {
		this.oamParam = oamParam;
	}

	protected void setKey(String key) {
		this.key = key;
	}

}
