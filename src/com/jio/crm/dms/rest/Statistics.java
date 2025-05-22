package com.jio.crm.dms.rest;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.startup.DmsBootStrapper;
import com.jio.crm.dms.utils.Constants;
import com.jio.crm.dms.utils.DappParameters;
import com.jio.telco.framework.pool.PoolingManager;

/**
 * @author Pramod.Jundre, Puspesh.Prakash
 *
 */
public class Statistics implements StatisticsMBean {

	private static Statistics st = new Statistics();

	private Statistics() {

	}

	public static Statistics getInstance() {
		if (st == null) {
			st = new Statistics();
		}
		return st;
	}

	public void startStatisticsMbeanService() {
		try {
			MBeanServer counterMbeanServer = ManagementFactory.getPlatformMBeanServer();
			ObjectName adapterName = new ObjectName(DappParameters.MBEAN_NAME_STATISTICS);
			counterMbeanServer.registerMBean(this, adapterName);

		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, e.getMessage(), this.getClass().getName(), "startStatisticsMbeanService")
					.writeExceptionLog();
		}

	}

	@Override
	public boolean fetchJettyStats() {
		BufferedWriter bufferedWriter = null;
		FileWriter fileWriter = null;
		try {
			String result = DmsBootStrapper.getInstance().getJettyRestEngine().jettyStats();

			File fileDir = new File(Constants.DUMP_PATH_JETTY);

			if (!fileDir.exists())
				fileDir.mkdirs();

			File file = new File(fileDir, "jettystats_"
					+ (new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()))
					+ ".txt");

			if (!file.exists()) {
				if (file.createNewFile()) {
					DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
							"Executing [ File created" + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
							.writeLog();
				} else {
					DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getLogBuilder(
							"Executing [ File Not created" + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
							.writeLog();
				}
			}

			fileWriter = new FileWriter(fileDir + Constants.DELIMITER + file.getName(), true);

			if (fileWriter != null) {
				bufferedWriter = new BufferedWriter(fileWriter);
			}

			bufferedWriter.write(result);

			bufferedWriter.close();
			fileWriter.close();

			if (new File(fileDir + Constants.DELIMITER + file.getName()).length() == 0) {
				return false;
			}
		}

		catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, e.getMessage(), this.getClass().getName(), "fetchJettyStats")
					.writeExceptionLog();

			return false;
		}

		finally {

			try {

				if (bufferedWriter != null) {
					bufferedWriter.close();
				}

				if (fileWriter != null) {
					fileWriter.close();

				}

			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
						.writeExceptionLog();

			}

		}

		return true;
	}

	@Override
	public boolean fetchPoolingStats() {
		FileWriter fileWriter = null;
		BufferedWriter bufferedWriter = null;
		try {
			String result = PoolingManager.getPoolingManager().getPoolStatistics();

			File fileDir = new File(Constants.DUMP_PATH_OBJECTPOOL);

			if (!fileDir.exists())
				fileDir.mkdirs();

			File file = new File(fileDir, "objectpoolstats_"
					+ (new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()))
					+ ".txt");

			if (!file.exists()) {
				if (file.createNewFile()) {
					DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
							"Executing [ File created" + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
							.writeLog();
				} else {
					DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getLogBuilder(
							"Executing [ File Not created" + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
							.writeLog();
				}
			}

			fileWriter = new FileWriter(fileDir + Constants.DELIMITER + file.getName(), true);

			if (fileWriter != null) {
				bufferedWriter = new BufferedWriter(fileWriter);
			}

			bufferedWriter.write(result);

			bufferedWriter.close();
			fileWriter.close();

			if (new File(fileDir + Constants.DELIMITER + file.getName()).length() == 0) {
				return false;
			}
		}

		catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, e.getMessage(), this.getClass().getName(), "fetchPoolingStats")
					.writeExceptionLog();

			return false;
		} finally {

			try {

				if (bufferedWriter != null) {
					bufferedWriter.close();
				}

				if (fileWriter != null) {
					fileWriter.close();

				}

			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
						.writeExceptionLog();

			}

		}
		return true;
	}

	@Override
	public String fetchPoolStatistics() {
		return PoolingManager.getPoolingManager().getPoolStatistics();
	}

}
