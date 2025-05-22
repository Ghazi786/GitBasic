package com.jio.crm.dms.logger;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.node.startup.DmsBootStrapper;
import com.jio.crm.dms.utils.Constants;
import com.jio.telco.framework.resource.ResourceBuilder;

/**
 * This is Dapp Log Manager class. Instance of this log manager class will be
 * used through out this project. This class is Singleton, thread safe and will
 * load lazily. Its implements Serializable interface so that objects of this
 * class can be written on disk in form of objects.
 * 
 * @author Milindkumar.S
 *
 */
public class DappLogManager implements Serializable {

	private static final long serialVersionUID = 7466777835895550326L;
	private static Logger logger = LogManager.getLogger(DappLogManager.class);
	private Level currentLogLevel;
	private static DappLogManager cmnLoggerInstance = null;

	private DappLogManager() {
		if (cmnLoggerInstance != null) {
			throw new UnsupportedOperationException(
					"Use getInsatnce() method to create instance of BootStrapper class");
		}
	}

	public static synchronized DappLogManager getInstance() {
		if (cmnLoggerInstance == null) {
			cmnLoggerInstance = new DappLogManager();
		}
		return cmnLoggerInstance;
	}

	public void initLogger(String componentId) {
		String hostName = null;
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (Exception unknownHostException) {
			hostName = ConfigParamsEnum.HTTP_STACK_HOST_IP.toString();
			String profile = System.getProperty("profile");
			if (profile != null && profile.equals("dev")) {
				hostName = "localhost";
			}

		}

		String ipAddress = ConfigParamsEnum.HTTP_STACK_HOST_IP.getStringValue();

		String profile = System.getProperty("profile");
		if (profile != null && profile.equals("dev")) {
			ipAddress = "localhost";
		}

		ResourceBuilder.logger().setMicroserviceName(ConfigParamsEnum.PRODUCT_NAME.getStringValue())
				.setMicroserviceHost(hostName).setMsIpAddress(ipAddress).setComponentId(componentId)
				.setProcessId(String.valueOf(DmsBootStrapper.getInstance().getConfigEngine().getProcessId()))
				.setLogFilePath(ConfigParamsEnum.LOG_FILE_PATH.getStringValue())
				.setMaxFileSize(ConfigParamsEnum.MAX_LOG_FILESIZE.getIntValue())
				.setMaxBackupIndex(ConfigParamsEnum.MAX_BACKUP_INDEX.getIntValue()).initializeLogger();

		try {

			ResourceBuilder.logger().setSystemLogLevel(ConfigParamsEnum.APPLICATION_LOG_LEVEL.getStringValue())
					.updateLoggers();

		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, "Error while setting System level log").writeExceptionLog();
		}

	}

	/**
	 * this method is used to set current log level
	 * 
	 * @param level
	 * 
	 */
	public void setCurrentLogLevel(String level) {

		try {
			Level logLevel = Level.getLevel(level.toUpperCase());
			ResourceBuilder.logger().setSystemLogLevel(logLevel.name()).updateLoggers();
			currentLogLevel = logLevel;
		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, "Error while updating current log level").writeExceptionLog();
		}

	}

	public boolean cleanLogDirectory(String path) {
		boolean isDeleted = false;

		try {
			DappLoggerService.GENERAL_INFO_LOG
					.getInfoLogBuilder("Deleting log files from path excluding current log file: " + path).writeLog();

			File directory = new File(path);

			File[] files = directory.listFiles();

			if (files.length == 0) {
				DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("No log exists. Specified directory is empty.")
						.writeLog();

				return isDeleted;
			}

			else {
				for (File file : files) {
					if (file.isDirectory()) {
						FileUtils.deleteDirectory(new File(directory + Constants.DELIMITER + file.getName()));
						isDeleted = true;
					}
				}
			}

			if (isDeleted) {
				DappLoggerService.GENERAL_INFO_LOG
						.getInfoLogBuilder("Log files successfully cleaned from directory: " + path).writeLog();
			}

			else {
				DappLoggerService.GENERAL_INFO_LOG.getInfoLogBuilder("No old logs found in directory: " + path
						+ " to delete. (Current log file will not be deleted.)").writeLog();
			}
		}

		catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();
		}

		return isDeleted;
	}

	public Logger getLogger() {
		return logger;
	}

	public Level getCurrentLogLevel() {
		return currentLogLevel;
	}

}
