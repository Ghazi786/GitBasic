package com.jio.crm.dms.node.startup;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.atom.OAM.Client.Management.OamClientManager;
import com.atom.OAM.Client.Management.OamClientRegistrationNotifier;
import com.atom.OAM.Client.util.OamClientConstants;
import com.elastic.search.config.ElasticConfig;
import com.elastic.search.config.IndexConfig;
import com.elastic.search.launcher.SessionFactory;
import com.j256.simplejmx.server.JmxServer;
import com.j256.simplejmx.web.JmxWebServer;
import com.jio.crm.dms.alarmmanager.AlarmConditionalCntrListener;
import com.jio.crm.dms.alarmmanager.AlarmLoggingListener;
import com.jio.crm.dms.alarmmanager.AlarmNameIntf;
import com.jio.crm.dms.alarmmanager.AlarmNotificationFailure;
import com.jio.crm.dms.alarmmanager.MemoryOverflowListener;
import com.jio.crm.dms.alarmmanager.customAlarmService;
import com.jio.crm.dms.clearcodes.AsnCallBackIntfImpl;
import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.configurationmanager.ExcelWorkbook;
import com.jio.crm.dms.configurationmanager.InterfaceStatus;
import com.jio.crm.dms.configurationmanager.RuntimeConfigurationEngine;
import com.jio.crm.dms.countermanager.CounterManager;
import com.jio.crm.dms.fms.FMSIntegration;
import com.jio.crm.dms.logger.DappLogManager;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.modules.upload.service.FileUploadService;
import com.jio.crm.dms.node.es.ESConnection;
import com.jio.crm.dms.node.shutdown.ServerShutdownHook;
import com.jio.crm.dms.registry.RtJioObserver;
import com.jio.crm.dms.rest.JettyRestEngine;
import com.jio.crm.dms.rest.Statistics;
import com.jio.crm.dms.threadpool.DappThreadPoolExecutor;
import com.jio.crm.dms.utils.Constants;
import com.jio.crm.dms.utils.ExcelBookConstants;
import com.jio.resttalk.service.impl.RestTalkManager;
import com.jio.telco.framework.clearcode.ClearCodeAsnPojo;
import com.jio.telco.framework.clearcode.ClearCodeBuilder;
import com.jio.telco.framework.pool.PoolingManager;
import com.jio.telco.framework.resource.ResourceBuilder;
import com.rjil.management.alarm.db.AlarmDBOperation;

/**
 * @author Main class
 *
 */

public class DmsBootStrapper {
	private static DmsBootStrapper bootStrapper = new DmsBootStrapper();
	private Serializer serializer = new Persister();
	private String componentId = "na";
	private JettyRestEngine jettyRestEngine = null;
	private RuntimeConfigurationEngine configEngine = null;
	private JmxServer jmxServer = null;
	private JmxWebServer jmxWebServer = null;
	private Mbeans mbean = null;
	private DappThreadPoolExecutor dappExecutor = null;
	private ExecutorService threadPoolService = Executors.newCachedThreadPool();

	public static DmsBootStrapper getInstance() {
		synchronized (DmsBootStrapper.class) {
			if (bootStrapper == null) {
				bootStrapper = new DmsBootStrapper();
			}
		}
		return bootStrapper;
	}

	/*
	 * main method to start the Dapp node
	 */
	public static void main(String args[]) throws Exception {

		System.out.println("-------------- Starting Dapp --------------");
		bootStrapper.initializeConfigParamSheet();
		bootStrapper.initLogger();
		bootStrapper.initializeMbeans();
		bootStrapper.initializeManagers();
		bootStrapper.initilizeJetteyRestEngine();
		ESConnection.getInstance().initializeES();
		bootStrapper.initializeClearCode();
		bootStrapper.registerWithOAM();
		bootStrapper.addShutdownHook();

		// Elastic search wrapper
		bootStrapper.initializeElasticSearchSessionFactory();

		// register with FMS
		FMSIntegration.getInstance().registerWithFMS();

		// Push document Category Data
		FileUploadService.pushDocumentCategory();

		bootStrapper.printModulesStatus();

		// to run on local host comment this line.
		String profile = System.getProperty(Constants.PROFILE);
		if (profile == null || !profile.equals("dev")) {
			try {
				OamClientManager.getOamClientForAlarm().raiseAlarmToOamServer(AlarmNameIntf.DMS_STARTUP_SUCCESS,
						AlarmNameIntf.DMS_STARTUP_SUCCESS);
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(), "",
						Thread.currentThread().getStackTrace()[1].getMethodName()).writeExceptionLog();

			}

		}

		System.out.println("-------------- DMS is started successfully --------------");

	}

	/*
	 * start local jmx server for Mbeans
	 */

	private void initializeConfigParamSheet() {
		ExcelWorkbook.getInstance().initialize();

		// loading configuration from excel to ConfigParamEnum
		configEngine = new RuntimeConfigurationEngine();
		System.out.println("Configuration Sheet has been loaded");

	}

	private void initLogger() {
		try {

			String CONFIG_PARAM_PATH = "./configuration/ConfigParamSheet.xlsx";

			String profile = System.getProperty(Constants.PROFILE);
			if (profile == null || !profile.equals("dev")) {
				CONFIG_PARAM_PATH = ExcelBookConstants.CONFIG_PARAM_PATH;
			}

			bootStrapper.setComponentId(OamClientManager.getInstance().readOamClientConfig(CONFIG_PARAM_PATH));
			DappLogManager.getInstance().initLogger(componentId);
			InterfaceStatus.status.setLoggingModule(true);
			System.out.println("Logger has been initialized");
		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), "initLogger");
			InterfaceStatus.status.setLoggingModule(false);
			// To run local host comment this line.
			String profile = System.getProperty(Constants.PROFILE);
			if (profile == null || !profile.equals("dev")) {
				try {
					OamClientManager.getOamClientForAlarm().raiseAlarmToOamServer(AlarmNameIntf.APP_LOGGING_INIT_FAILED,
							AlarmNameIntf.APP_LOGGING_INIT_FAILED);
				} catch (Exception e1) {
					DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e1, e1.getMessage(), "",
							Thread.currentThread().getStackTrace()[1].getMethodName()).writeExceptionLog();

				}
			}
		}

	}

	private void initializeManagers() {

		try {
			RestTalkManager.getInstance().startRestTalk();
			CounterManager.getInstance();

			// thread pool getting initailized
			// dappExecutor
			dappExecutor = new DappThreadPoolExecutor(ConfigParamsEnum.CORE_POOL_SIZE.getIntValue(),
					ConfigParamsEnum.MAX_CORE_POOL_SIZE.getIntValue(), ConfigParamsEnum.KEEP_ALIVE_TIME.getLongValue(),
					TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
			InterfaceStatus.status.setThreadPoolingManager(true);

			System.out.println("Managers have been initialzed");
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, e.getMessage(), this.getClass().getName(), "initializeManagers")
					.writeExceptionLog();
		}

	}

	// Method to add Mbeans
	private void initializeMbeans() {
		try {
			configEngine.startMbean();
			CounterManager.getInstance().startCounterMbeanService();
			Statistics.getInstance().startStatisticsMbeanService();
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, e.getMessage(), this.getClass().getName(), "initializeMbeans")
					.writeExceptionLog();
		}
	}

	public JettyRestEngine getJettyRestEngine() {
		return jettyRestEngine;
	}

	private void initilizeJetteyRestEngine() {

		this.jettyRestEngine = new JettyRestEngine();
		try {
			this.jettyRestEngine.initialise();
		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					DmsBootStrapper.getInstance().getClass().getName(), "initilizeJetteyRestEngine")
					.writeExceptionLog();
		}

	}

	public DappThreadPoolExecutor getDappExecutor() {
		return dappExecutor;
	}

	public Serializer getSerializer() {
		return serializer;
	}

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public RuntimeConfigurationEngine getConfigEngine() {
		return configEngine;
	}

	/**
	 * Register with Oam
	 */

	public void registerWithOAM() {
		try {
			OamClientRegistrationNotifier.getInstance().addObserver(new RtJioObserver());

			String CONFIG_PARAM_PATH = "./configuration/ConfigParamSheet.xlsx";

			AlarmDBOperation dbOperationManager = new customAlarmService();
			boolean dbInitialized = true;

			String profile = System.getProperty(Constants.PROFILE);
			if (profile == null || !profile.equals("dev")) {
				CONFIG_PARAM_PATH = ExcelBookConstants.CONFIG_PARAM_PATH;
			}

			OamClientManager.getInstance().initializeCustomDbWithMultipleIpSupport(
					DappLogManager.getInstance().getLogger(), CONFIG_PARAM_PATH, new AlarmLoggingListener(),
					new AlarmConditionalCntrListener(), null, new MemoryOverflowListener(), dbOperationManager,
					dbInitialized, true, new AlarmNotificationFailure(),
					OamClientManager.getInstance().getOAMClientParam(OamClientConstants.ID));

			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					"Registration done..." + " OamClientID "
							+ OamClientManager.getInstance().getOAMClientParam(OamClientConstants.ID),
					this.getClass().getName(), "registerWithOAM").writeLog();

			InterfaceStatus.status.setRegisterWithOAM(true);
			if (profile == null || !profile.equals("dev")) {
				OamClientManager.getOamClientForAlarm().raiseAlarmToOamServer(AlarmNameIntf.DMS_OAM_CONNECTION_SUCCESS,
						AlarmNameIntf.DMS_OAM_CONNECTION_SUCCESS);
			}

		} catch (Exception e) {

			InterfaceStatus.status.setRegisterWithOAM(false);
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, e.getMessage(), this.getClass().getName(), "registerWithOAM")
					.writeExceptionLog();

			// to run on local host comment this line.
			String profile = System.getProperty(Constants.PROFILE);
			if (profile == null || !profile.equals("dev")) {
				try {
					OamClientManager.getOamClientForAlarm().raiseAlarmToOamServer(
							AlarmNameIntf.DMS_OAM_CONNECTION_FAILURE, AlarmNameIntf.DMS_OAM_CONNECTION_FAILURE);
				} catch (Exception e1) {
					DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e1, e1.getMessage(), "",
							Thread.currentThread().getStackTrace()[1].getMethodName()).writeExceptionLog();
				}
			}

		}
	}

	public String printModulesStatus() {

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(Constants.NEWLINE).append("\nMODULE/INTERFACE\t: STATUS").append(Constants.NEWLINE)
				.append("\n" + "LoggingModule\t\t: " + InterfaceStatus.status.isLoggingModule())
				.append("\n" + "ConfigurationModule\t: " + InterfaceStatus.status.isConfigurationModule())
				.append("\n" + "JettyInterface\t\t: " + InterfaceStatus.status.isJettyInterface())
				.append("\n" + "CounterModule\t\t: " + InterfaceStatus.status.isCounterModule())
				.append("\n" + "Thread PoolingManager   : " + InterfaceStatus.status.isThreadPoolingManager())
				.append("\n" + "Object PoolingManager   : " + InterfaceStatus.status.isObjectPoolingManager())
				.append("\n" + "RegisterWithOAM\t\t: " + InterfaceStatus.status.isRegisterWithOAM())
				.append("\n" + "RegisterWithES\t\t: " + InterfaceStatus.status.isEsConnected())
				.append("\n" + "RegisterWithTIBCO\t\t: "
						+ InterfaceStatus.status.isTibcoIntegrationConfigurationModule())
				.append("\n" + "RegisterWithFMS\t\t: " + InterfaceStatus.status.isFmsIntegrationConfigurationModule())
				.append(Constants.NEWLINE);

		DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder(stringBuilder.toString()).writeLog();

		System.out.println(stringBuilder.toString());

		return stringBuilder.toString();

	}

	/*
	 * stop local jetty server and jmx server
	 */
	public boolean shutdown_node() {
		boolean res = false;

		try {
			JettyRestEngine jettyRestEngine = new JettyRestEngine();
			jettyRestEngine.stopJettyServer();
			jmxServer.unregister(mbean);
			jmxServer.stop();
			jmxWebServer.stop();
			res = true;

		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage());
		}
		return res;
	}

	/*
	 * this method creates SCEconfig to be passed to SCE
	 */

	public void initializeClearCode() {
		try {
			ClearCodeBuilder builder = ResourceBuilder.clearCode();
			builder.setXdrRecordLimitPerFile(ConfigParamsEnum.XDR_RECORD_LIMIT_PER_FILE.getStringValue())
					.setXdrLocalPath(ConfigParamsEnum.XDR_LOCAL_DUMP_PATH.getStringValue())
					.setIsXDRFtpEnabled(ConfigParamsEnum.XDR_FTP_ENABLED.getBooleanValue())
					.setRemoteHostAddress(ConfigParamsEnum.XDR_REMOTE_ADDRESSES.getStringValue())
					.setRemoteUsername(ConfigParamsEnum.XDR_REMOTE_USER_NAME.getStringValue())
					.setRemotePassword(ConfigParamsEnum.XDR_REMOTE_PASSWORD.getStringValue())
					.setRemotePath(ConfigParamsEnum.XDR_REMOTE_DUMP_PATH.getStringValue())
					.setXdrNodeName(ConfigParamsEnum.JIODEVOPS_FOR_CLEAR_CODE.getStringValue())
					.setXdrAsnCallBackIntf(new AsnCallBackIntfImpl());

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, "Error in initializeClearCode",
					this.getClass().getName(), "initializeClearCode");
		}

	}

	public void initializeElasticSearchSessionFactory() {
		try {

			IndexConfig indexConfig = new IndexConfig();
			indexConfig.setIndexName(ConfigParamsEnum.ES_INDEXNAME.getStringValue());
			indexConfig.setType(ConfigParamsEnum.ES_TYPENAME.getStringValue());

			ElasticConfig elasticConfig = new ElasticConfig();
			elasticConfig.setClusterName(ConfigParamsEnum.ES_CLUSTERNAME.getStringValue());
			elasticConfig.setUserName(ConfigParamsEnum.ES_XPACKUNAME.getStringValue());
			elasticConfig.setPassword(ConfigParamsEnum.ES_XPACKPASSWORD.getStringValue());
			elasticConfig.setHost(ConfigParamsEnum.ES_CORDINATORNODEIPANDPORT.getStringValue());

			SessionFactory factory = new SessionFactory.builder(indexConfig).withElasticConfig(elasticConfig)
					.buildSessionFactory();

			boolean isConnected = factory.ping();

			if (isConnected) {
				InterfaceStatus.status.setEsConnected(true);
				DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
						"###################### Connection has been created with ES cluster ######################",
						this.getClass().getName(), "initializeElasticSearchSessionFactory").writeLog();

				String profile = System.getProperty(Constants.PROFILE);
				if (profile == null || !profile.equals("dev")) {

					OamClientManager.getOamClientForAlarm().raiseAlarmToOamServer(
							AlarmNameIntf.DMS_ES_CONNECTION_SUCCESS, AlarmNameIntf.DMS_ES_CONNECTION_SUCCESS);
				}

			} else {
				DappLoggerService.GENERAL_INFO_LOG
						.getLogBuilder("XXXXXXXXXXXXXXXXXXXXXX ES CONNECTION NOT CREATEDXXXXXXXXXXXXXXXXXXXXXX",
								this.getClass().getName(), "createConnectionWithCluster")
						.writeLog();
				String profile = System.getProperty(Constants.PROFILE);
				if (profile == null || !profile.equals("dev")) {

					OamClientManager.getOamClientForAlarm().raiseAlarmToOamServer(
							AlarmNameIntf.DMS_ES_CONNECTION_FAILURE, AlarmNameIntf.DMS_ES_CONNECTION_FAILURE);
				}

			}

		} catch (Exception e) {
			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(e.getMessage(), this.getClass().getName(), "initializeElasticSearchSessionFactory")
					.writeLog();

		}

	}

	// getting clearcode class object from this method
	public ClearCodeAsnPojo getClearCodeObj() {
		ClearCodeAsnPojo cccObj = null;
		try {
			cccObj = (ClearCodeAsnPojo) PoolingManager.getPoolingManager().borrowObject(ClearCodeAsnPojo.class);
			cccObj.setStartTime();
			cccObj.getStartTime();
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, "Error in getClearcodeObj",
					this.getClass().getName(), "getClearCodeObj");
		}
		return cccObj;
	}

	private void addShutdownHook() {
		try {
			Runtime.getRuntime().addShutdownHook(new ServerShutdownHook(DmsBootStrapper.getInstance()));
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}
	}

	public ExecutorService getThreadPoolService() {
		return threadPoolService;
	}

	public void setThreadPoolService(ExecutorService threadPoolService) {
		this.threadPoolService = threadPoolService;
	}

}
