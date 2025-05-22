package com.jio.crm.dms.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jio.crm.dms.bean.FileDocument;
import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.exceptions.BaseException;
import com.jio.crm.dms.logger.DappLoggerService;

public class FTPUtil {

	public static void sendArchivalDocToLocalServer(String serverFilePath, Session session, String localPath) {
		Channel channel = null;
		ChannelSftp channelSftp = new ChannelSftp();
		try {

			java.util.Properties config = new java.util.Properties();
			config.put(Constants.STRICTHOSTKEYCONFIGURATION, "no");
			session.setConfig(config);
			session.connect();
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					Constants.EXECUTING + Constants.HOSTCONNECTED + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();
			channel = session.openChannel("sftp");
			channel.connect();
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					Constants.EXECUTING + "sftp channel opened and connected." + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

			channelSftp = (ChannelSftp) channel;

			channelSftp.cd(serverFilePath.substring(0, serverFilePath.lastIndexOf('/')));

			// Upload the file
			channelSftp.put(localPath, serverFilePath);

			channelSftp.disconnect();
			session.disconnect();

			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					Constants.EXECUTING + "File Uploaded Successfully" + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					Constants.EXCEPTIONWHILETRANSFER, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

		} finally {
			channelSftp.exit();

			try {
				if (channel != null) {
					channel.disconnect();
					DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
							Constants.EXECUTING + Constants.CHANNELDISCONNECTED + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

				}
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
						.writeExceptionLog();
			}
			session.disconnect();

		}
	}

	public static void uploadSingleFile(File localDir, ChannelSftp channelSftp, String createCsvFile)
			throws FileNotFoundException, SftpException {
		File[] subFiles = localDir.listFiles();

		if (subFiles != null && subFiles.length > 0) {
			for (File f : subFiles) {
				channelSftp.put(new FileInputStream(f), f.getName());
				DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
						Constants.EXECUTING + "Pdf File transferred successfully to host." + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

			}
		} else {
			channelSftp.put(new FileInputStream(localDir), localDir.getName());
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					Constants.EXECUTING + "File uploaded successfully to host.via DMS" + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

		}

		if (createCsvFile != null) {
			File file = new File(createCsvFile);
			channelSftp.put(new FileInputStream(file), file.getName());
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					Constants.EXECUTING + "Csv File transferred successfully to host" + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();
		}

	}

	public static void uploadDirectory(String fileDir, String desDir, com.jcraft.jsch.Session session,
			String createCsvFile, FileDocument document) throws IOException, BaseException {

		Channel channel = null;
		ChannelSftp channelSftp = new ChannelSftp();
		try {

			java.util.Properties config = new java.util.Properties();
			config.put(Constants.STRICTHOSTKEYCONFIGURATION, "no");
			session.setConfig(config);
			session.connect();
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					Constants.EXECUTING + Constants.HOSTCONNECTED + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();
			channel = session.openChannel("sftp");
			channel.connect();
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					Constants.EXECUTING + "sftp channel opened and connected." + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

			channelSftp = (ChannelSftp) channel;

			try {
				if (channelSftp.lstat(desDir) != null) {
					channelSftp.cd(desDir);
				}
			} catch (Exception e) {
				channelSftp.mkdir(desDir);
				channelSftp.cd(desDir);
			}
			try {
				if (!(channelSftp.lstat(desDir + Constants.DELIMITER + document.getProductName()) != null)) {
					DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
							Constants.EXECUTING + "Not exist productName folder" + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

				}
			}

			catch (Exception e) {
				// create new productName folder
				channelSftp.mkdir(document.getProductName());
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
						.writeExceptionLog();

			}

			channelSftp.cd(document.getProductName());
			try {
				if (!(channelSftp.lstat(desDir + Constants.DELIMITER + document.getProductName() + Constants.DELIMITER
						+ document.getOrderId()) != null)) {

					DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
							Constants.EXECUTING + "Not exist orderId folder:" + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

				}

			} catch (Exception e) {
				// create new orderId folder
				channelSftp.mkdir(document.getOrderId());
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
						.writeExceptionLog();
			}

			channelSftp.cd(document.getOrderId());

			File localDir = new File(fileDir);

			uploadSingleFile(localDir, channelSftp, createCsvFile);

		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					Constants.EXCEPTIONWHILETRANSFER, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
			throw new BaseException(HttpStatus.BAD_REQUEST_400, e.getMessage());
		} finally {
			channelSftp.exit();

			try {
				if (channel != null) {
					channel.disconnect();
					DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
							Constants.EXECUTING + Constants.CHANNELDISCONNECTED + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

				}
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
						.writeExceptionLog();
			}

			session.disconnect();

		}

	}

	public static void getFileFromFtp(FileDocument document, Session session, HttpServletResponse response) {

		Channel channel = null;
		ChannelSftp channelSftp = new ChannelSftp();
		try {

			java.util.Properties config = new java.util.Properties();
			config.put(Constants.STRICTHOSTKEYCONFIGURATION, "no");
			session.setConfig(config);
			session.connect();
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					Constants.EXECUTING + Constants.HOSTCONNECTED + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();
			channel = session.openChannel("sftp");
			channel.connect();
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					Constants.EXECUTING + "sftp channel opened and connected." + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

			channelSftp = (ChannelSftp) channel;
			channelSftp.cd(ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue().replace(".", "/"));
			channelSftp.cd(document.getProductName());
			channelSftp.cd(document.getOrderId());

			// Get the input stream of the file from the FTP server

			try (InputStream in = channelSftp.get(document.getFileName());
					OutputStream os = response.getOutputStream()) {

				try {

					byte[] buffer = new byte[1024];
					int numBytes = 0;
					while ((numBytes = in.read(buffer)) > 0) {
						os.write(buffer, 0, numBytes);
					}

				} finally {
					os.flush();
					os.close();
				}
			}
			channelSftp.disconnect();
			session.disconnect();

			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					Constants.EXECUTING + "File Downloaded Successfully" + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					Constants.EXCEPTIONWHILETRANSFER, Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

		} finally {
			channelSftp.exit();

			try {
				if (channel != null) {
					channel.disconnect();
					DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
							Constants.EXECUTING + Constants.CHANNELDISCONNECTED + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

				}
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
						.writeExceptionLog();
			}
			session.disconnect();

		}

	}

	private FTPUtil() {
		super();

	}

}
