package com.jio.crm.dms.configurationmanager;

/**
 * This class print the Success or Failure Status of different module used in
 * TRM Application
 * 
 * @author Kiran.Jangid
 *
 */
public class InterfaceStatus {

	public static final InterfaceStatus status = new InterfaceStatus();

	private boolean loggingModule;
	private boolean configurationModule;
	private boolean alarmModule;
	private boolean counterModule;
	private boolean jettyInterface;
	private boolean httpInterface;
	private boolean threadPoolingManager;
	private boolean objectPoolingManager;
	private boolean esConnected;
	private boolean emsConnected;
	private boolean isMasterNode;
	private boolean registerWithOAM;
	private boolean timerStatus;

	private boolean isDirectoryStructureCreated;
	private boolean isBulkProcessorStarted;

	private boolean kafkaConfigurationModule;

	private boolean notificationConfigurationModule;
	private boolean tibcoIntegrationConfigurationModule;
	private boolean fmsIntegrationConfigurationModule;

	private InterfaceStatus() {

	}

	public boolean isRegisterWithOAM() {
		return registerWithOAM;
	}

	public void setRegisterWithOAM(boolean registerWithOAM) {
		this.registerWithOAM = registerWithOAM;
	}

	public boolean isDirectoryStructureCreated() {
		return isDirectoryStructureCreated;
	}

	public void setDirectoryStructureCreated(boolean isDirectoryStructureCreated) {
		this.isDirectoryStructureCreated = isDirectoryStructureCreated;
	}

	public boolean isBulkProcessorStarted() {
		return isBulkProcessorStarted;
	}

	public void setBulkProcessorStarted(boolean isBulkProcessorStarted) {
		this.isBulkProcessorStarted = isBulkProcessorStarted;
	}

	public boolean isMasterNode() {
		return isMasterNode;
	}

	public void setMasterNode(boolean isMasterNode) {
		this.isMasterNode = isMasterNode;
	}

	public boolean isEMSConnected() {
		return emsConnected;
	}

	public void setEMSConnected(boolean eMSConnected) {
		emsConnected = eMSConnected;
	}

	public boolean isEsConnected() {
		return esConnected;
	}

	public void setEsConnected(boolean mongoConnected) {
		this.esConnected = mongoConnected;
	}

	public boolean isLoggingModule() {
		return loggingModule;
	}

	public void setLoggingModule(boolean loggingModule) {
		this.loggingModule = loggingModule;
	}

	public boolean isConfigurationModule() {
		return configurationModule;
	}

	public void setConfigurationModule(boolean configurationModule) {
		this.configurationModule = configurationModule;
	}

	public boolean isAlarmModule() {
		return alarmModule;
	}

	public void setAlarmModule(boolean alarmModule) {
		this.alarmModule = alarmModule;
	}

	public boolean isCounterModule() {
		return counterModule;
	}

	public void setCounterModule(boolean counterModule) {
		this.counterModule = counterModule;
	}

	public boolean isJettyInterface() {
		return jettyInterface;
	}

	public void setJettyInterface(boolean jettyInterface) {
		this.jettyInterface = jettyInterface;
	}

	public boolean isHttpInterface() {
		return httpInterface;
	}

	public void setHttpInterface(boolean httpInterface) {
		this.httpInterface = httpInterface;
	}

	public boolean isThreadPoolingManager() {
		return threadPoolingManager;
	}

	public void setThreadPoolingManager(boolean threadPoolingManager) {
		this.threadPoolingManager = threadPoolingManager;
	}

	public boolean isObjectPoolingManager() {
		return objectPoolingManager;
	}

	public void setObjectPoolingManager(boolean objectPoolingManager) {
		this.objectPoolingManager = objectPoolingManager;
	}

	public boolean isTimerStatus() {
		return timerStatus;
	}

	public void setTimerStatus(boolean timerStatus) {
		this.timerStatus = timerStatus;
	}

	public boolean isKafkaConfigurationModule() {
		return kafkaConfigurationModule;
	}

	public void setKafkaConfigurationModule(boolean kafkaConfigurationModule) {
		this.kafkaConfigurationModule = kafkaConfigurationModule;
	}

	public boolean isNotificationConfigurationModule() {
		return notificationConfigurationModule;
	}

	public void setNotificationConfigurationModule(boolean notificationConfigurationModule) {
		this.notificationConfigurationModule = notificationConfigurationModule;
	}

	public boolean isTibcoIntegrationConfigurationModule() {
		return tibcoIntegrationConfigurationModule;
	}

	public void setTibcoIntegrationConfigurationModule(boolean tibcoIntegrationConfigurationModule) {
		this.tibcoIntegrationConfigurationModule = tibcoIntegrationConfigurationModule;
	}

	public boolean isFmsIntegrationConfigurationModule() {
		return fmsIntegrationConfigurationModule;
	}

	public void setFmsIntegrationConfigurationModule(boolean fmsIntegrationConfigurationModule) {
		this.fmsIntegrationConfigurationModule = fmsIntegrationConfigurationModule;
	}

}
