/**
 * 
 */
package com.jio.crm.dms.modules.upload.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.elastic.search.bean.Page;
import com.elastic.search.bean.SearchResult;
import com.elastic.search.exception.ElasticSearchException;
import com.elastic.search.launcher.SessionFactory;
import com.elastic.search.service.Session;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jio.crm.dms.bean.FileDocument;
import com.jio.crm.dms.clearcodes.ClearCodeLevel;
import com.jio.crm.dms.clearcodes.ClearCodes;
import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.core.BaseResponse;
import com.jio.crm.dms.countermanager.CounterNameEnum;
import com.jio.crm.dms.hdfs.HdfsConfigurationManager;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.modules.upload.repository.FileUploadRepository;
import com.jio.crm.dms.node.startup.DmsBootStrapper;
import com.jio.crm.dms.utils.Constants;
import com.jio.crm.dms.utils.MinioUtilService;
import com.jio.telco.framework.clearcode.ClearCodeAsnPojo;
import com.jio.telco.framework.clearcode.ClearCodeAsnUtility;

/**
 * @author Ghajnafar.Shahid
 *
 */
public class ZipFileService {

	/**
	 * 
	 */
	private ExcelService excelService = new ExcelService();
	private FileUploadRepository fileRepositoryService = new FileUploadRepository();
	private MinioUtilService minioUtilService = new MinioUtilService();

	public BaseResponse<Object> downloadFileInBulk(String productName, String orderId, HttpServletResponse resp) {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [ " + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();
		try {
			Configuration conf = HdfsConfigurationManager.getConfiguration();

			FileSystem fileSystem = FileSystem.get(conf);

			Date date = new Date();
			String zipFilename = orderId + "_" + date.getTime() + ".zip";

			resp.setContentType("application/zip");
			resp.setHeader("Content-Disposition", "attachment; filename=" + zipFilename);

			ServletOutputStream outs = resp.getOutputStream();
			ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(outs));

			switch (ConfigParamsEnum.FILESYSTEM.getIntValue()) {
			case 0:
				downloadFileInBulkByUsingHDFS(productName, orderId, ccAsnPojo, fileSystem, zipOut);
				break;
			case 1:
				downloadFileInBulkByUsingFTP(productName, orderId, ccAsnPojo, zipOut);
				break;
			case 2:
				downloadFileInBulkByUsingMinio(productName, orderId, ccAsnPojo, fileSystem, zipOut);
				break;
			}

		} catch (Exception e) {
			ccAsnPojo.addClearCode(ClearCodes.DOWNLOAD_FILE_IN_BULK_FAILURE.getValue(), ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			CounterNameEnum.CNTR_DOWNLOAD_FILE_IN_BULK_FAILURE.increment();

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}
		return null;

	}

	public void downloadFileInBulkByUsingFTP(String productName, String orderId, ClearCodeAsnPojo ccAsnPojo,
			ZipOutputStream zipOut) throws IOException, ElasticSearchException, JSchException, SftpException {

		Session session = SessionFactory.getSessionFactory().getSession();
		List<String> filepath = new ArrayList<>();
		Map<String, String> filters = new HashMap<>();
		filters.put("orderId", orderId);
		Page page = new Page();
		page.setPageLength(100000);
		SearchResult<FileDocument> allDocumentByFilters = fileRepositoryService.getAllDocumentByFilters(session,
				filters, page);
		List<FileDocument> collect = allDocumentByFilters.getResult().stream().collect(Collectors.toList());
		for (FileDocument document : collect) {
			filepath.add(document.getFileName());
		}

		// fetch from ftp server
		String SFTPHOST = ConfigParamsEnum.UPLOADSFTPHOST.getStringValue();
		int SFTPPORT = ConfigParamsEnum.UPLOADSFTPPORT.getIntValue();
		String SFTPUSER = ConfigParamsEnum.UPLOADSFTPUSER.getStringValue();
		String SFTPPASS = ConfigParamsEnum.UPLOADSFTPPASS.getStringValue();
		JSch jsch = new JSch();
		com.jcraft.jsch.Session session1;

		session1 = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
		session1.setPassword(SFTPPASS);
		Channel channel = null;
		ChannelSftp channelSftp = new ChannelSftp();
		java.util.Properties config = new java.util.Properties();
		config.put("StrictHostKeyChecking", "no");
		session1.setConfig(config);
		session1.connect();
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + "Host Connected" + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						"", Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		channel = session1.openChannel("sftp");
		channel.connect();
		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
				Constants.EXECUTING + "sftp channel opened and connected." + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

		channelSftp = (ChannelSftp) channel;
		channelSftp.cd(ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue().replace(".", ""));
		channelSftp.cd(productName);
		channelSftp.cd(orderId);
		for (String file : filepath) {
			try (InputStream in = channelSftp.get(file)) {
				BufferedInputStream bis = null;
				bis = new BufferedInputStream(in, 1024);
				ZipEntry ze = new ZipEntry(file);
				zipOut.putNextEntry(ze);
				byte[] data = new byte[1024];
				int count;
				while ((count = bis.read(data, 0, 1024)) != -1) {
					zipOut.write(data, 0, count);
				}

				zipOut.closeEntry();
				bis.close();
			} catch (Exception e) {
				throw new IOException();
			}

		}
		zipOut.close();
		channelSftp.disconnect();
		session1.disconnect();

		CounterNameEnum.CNTR_DOWNLOAD_FILE_IN_BULK_SUCCESS.increment();
		ccAsnPojo.addClearCode(ClearCodes.DOWNLOAD_FILE_IN_BULK_SUCCESS.getValue(), ClearCodeLevel.SUCCESS);
		ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);

	}

	public void downloadFileInBulkByUsingHDFS(String productName, String orderId, final ClearCodeAsnPojo ccAsnPojo,
			FileSystem fileSystem, ZipOutputStream zipOut) throws IOException {
		String pathTemp = ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue() + Constants.DELIMITER + productName
				+ Constants.DELIMITER + orderId;
		Path orderPath = new Path(pathTemp);
		List<String> list = getAllFilePath(orderPath, fileSystem);
		for (String fileName : list) {
			String filePtah = pathTemp + Constants.DELIMITER + fileName;
			Path paths = new Path(filePtah);
			try (FSDataInputStream in = fileSystem.open(paths);
					BufferedInputStream bis = new BufferedInputStream(in, 1024);) {
				ZipEntry ze = new ZipEntry(fileName);
				zipOut.putNextEntry(ze);
				byte[] data = new byte[1024];
				int count;
				while ((count = bis.read(data, 0, 1024)) != -1) {
					zipOut.write(data, 0, count);
				}

				zipOut.closeEntry();
				bis.close();
			} catch (Exception e) {
				throw new IOException();
			}
		}

		zipOut.close();

		CounterNameEnum.CNTR_DOWNLOAD_FILE_IN_BULK_SUCCESS.increment();
		ccAsnPojo.addClearCode(ClearCodes.DOWNLOAD_FILE_IN_BULK_SUCCESS.getValue(), ClearCodeLevel.SUCCESS);
		ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
	}

	@SuppressWarnings("unused")
	public void downloadFileInBulkByUsingMinio(String productName, String orderId, final ClearCodeAsnPojo ccAsnPojo,
			FileSystem fileSystem, ZipOutputStream zipOut) throws IOException {
		String pathTemp = ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue() + Constants.DELIMITER + productName
				+ Constants.DELIMITER + orderId;
		pathTemp = pathTemp.replace(".", "");
		List<String> list = minioUtilService.getAllFilePath(pathTemp);
		for (String fileName : list) {
			String filePtah = pathTemp + Constants.DELIMITER + fileName;
			Path paths = new Path(filePtah);
			try {
				InputStream in = minioUtilService.getFileStream(filePtah);
				BufferedInputStream bis = null;
				bis = new BufferedInputStream(in, 1024);
				ZipEntry ze = new ZipEntry(fileName);
				zipOut.putNextEntry(ze);
				byte[] data = new byte[1024];
				int count;
				while ((count = bis.read(data, 0, 1024)) != -1) {
					zipOut.write(data, 0, count);
				}

				zipOut.closeEntry();
				bis.close();
			} catch (Exception e) {
				throw new IOException();
			}
		}

		zipOut.close();

		CounterNameEnum.CNTR_DOWNLOAD_FILE_IN_BULK_SUCCESS.increment();
		ccAsnPojo.addClearCode(ClearCodes.DOWNLOAD_FILE_IN_BULK_SUCCESS.getValue(), ClearCodeLevel.SUCCESS);
		ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
	}

	public BaseResponse<List<FileDocument>> downloadfileinbulkfomexcel(HttpServletRequest req,
			HttpServletResponse res) {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [ " + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();
		List<FileDocument> documents = new ArrayList<>();
		XSSFWorkbook workbook = null;
		try {

			byte[] byteArray = IOUtils.toByteArray(req.getInputStream());

			InputStream input = new ByteArrayInputStream(byteArray);

			workbook = new XSSFWorkbook(input);
			// read Excel file to search document from database
			XSSFSheet sheet = workbook.getSheetAt(0);
			HashMap<String, List<FileDocument>> searchparams = excelService.readSearchParameter(sheet, workbook);

			// making document object, to search from db
			List<FileDocument> search1 = searchparams.get("s1");
			HashMap<String, String> filters = null;
			SessionFactory factory = SessionFactory.getSessionFactory();
			Session session = factory.getSession();
			SearchResult<FileDocument> result = null;

			for (int i = 0; i < search1.size(); i++) {
				filters = new HashMap<>();
				FileDocument tempDocument = search1.get(i);
				if (tempDocument.getCustomerId() != null && !tempDocument.getCustomerId().isEmpty()) {
					filters.put("customerId", tempDocument.getCustomerId());
				}
				if (tempDocument.getMobileNumber() != null && !tempDocument.getMobileNumber().isEmpty()) {
					filters.put("mobileNumber", tempDocument.getMobileNumber());

				}
				if (tempDocument.getOrderId() != null && !tempDocument.getOrderId().isEmpty()) {
					filters.put("orderId", tempDocument.getOrderId());
				}
				if (tempDocument.getDocumentCategory() != null && !tempDocument.getDocumentCategory().isEmpty()) {
					filters.put("documentCategory", tempDocument.getDocumentCategory());
				}
				if (tempDocument.getDocumentNumber() != null && !tempDocument.getDocumentNumber().isEmpty()) {
					filters.put("documentNumber", tempDocument.getDocumentNumber());
				}
				// fetching document from DB
				Page page = session.defaultPage();
				page.setPageLength(1000);
				if (filters.isEmpty()) {
					continue;
				}
				result = fileRepositoryService.getAllDocumentByFilters(session, filters, page);
				System.out.println("result:" + result.getDocumentsCount());

				int lastColumn = sheet.getRow(0).getPhysicalNumberOfCells() - 1;
				if (result.getResult().size() != 0) {
					Row row = sheet.getRow(i + 1);

					XSSFCellStyle style = workbook.createCellStyle();

					style.setFillForegroundColor(IndexedColors.GREEN.getIndex());

					style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

					Cell cellTemp = row.createCell(lastColumn);

					cellTemp.setCellValue("File Found for this Data");

					// sheet.autoSizeColumn(lastColumn);

					cellTemp.setCellStyle(style);
					System.out.println("endinggg.....here");
				} else {
					Row row = sheet.getRow(i + 1);
					Cell cellTemp = row.createCell(lastColumn);
					XSSFCellStyle style = workbook.createCellStyle();
					style.setFillForegroundColor(IndexedColors.RED.getIndex());
					style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
					cellTemp.setCellValue("File Not Found for this data");
					// sheet.autoSizeColumn(lastColumn);
					cellTemp.setCellStyle(style);
				}

				// adding to list
				documents.addAll(result.getResult());
				System.out.println("documents:" + documents);

			}

			// creating zip file of found documents
			Configuration conf = HdfsConfigurationManager.getConfiguration();
			FileSystem fileSystem = FileSystem.get(conf);
			Date date = new Date();
			String zipFileName = date.getTime() + "_bulkFile.zip";
			System.out.println("zipFileName:" + zipFileName);
			res.setContentType("application/zip");
			res.setHeader("Content-Disposition", "attachment; filename=" + zipFileName);

			try {

				ServletOutputStream outs = res.getOutputStream();
				ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(outs));

				appendRequestedExcelFilewithStatus(zipOut, workbook);

				// from hdfs
				switch (ConfigParamsEnum.FILESYSTEM.getIntValue()) {
				case 0:
					createZipFileByUsingHDFS(zipOut, fileSystem, documents);
					CounterNameEnum.CNTR_DOWNLOAD_FILE_IN_BULK_EXCEL_SUCCESS.increment();
					ccAsnPojo.addClearCode(ClearCodes.DOWNLOAD_FILE_IN_BULK_EXCEL_SUCCESS.getValue(),
							ClearCodeLevel.FAILURE);
					ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
					break;
				case 1:
					createZipFileByUsingFTP(zipOut, documents);
					CounterNameEnum.CNTR_DOWNLOAD_FILE_IN_BULK_EXCEL_SUCCESS.increment();
					ccAsnPojo.addClearCode(ClearCodes.DOWNLOAD_FILE_IN_BULK_EXCEL_SUCCESS.getValue(),
							ClearCodeLevel.FAILURE);
					ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
					break;
				case 2:
					createZipFileByUsingMinio(zipOut, fileSystem, documents);
					CounterNameEnum.CNTR_DOWNLOAD_FILE_IN_BULK_EXCEL_SUCCESS.increment();
					ccAsnPojo.addClearCode(ClearCodes.DOWNLOAD_FILE_IN_BULK_EXCEL_SUCCESS.getValue(),
							ClearCodeLevel.FAILURE);
					ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
					break;

				}

			} catch (Exception e) {
				ccAsnPojo.addClearCode(ClearCodes.DOWNLOAD_FILE_IN_BULK_EXCEL_FAILURE.getValue(),
						ClearCodeLevel.FAILURE);
				ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
				CounterNameEnum.CNTR_DOWNLOAD_FILE_IN_BULK_EXCEL_FAILURE.increment();

			}

		} catch (Exception e) {
			ccAsnPojo.addClearCode(ClearCodes.DOWNLOAD_FILE_IN_BULK_EXCEL_FAILURE.getValue(), ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			CounterNameEnum.CNTR_DOWNLOAD_FILE_IN_BULK_EXCEL_FAILURE.increment();

			try {
				workbook.close();
			} catch (IOException e1) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e1, e1.getMessage())
						.writeExceptionLog();
			}
		}
		return new BaseResponse<>(documents);

	}

	public void createZipFileByUsingFTP(ZipOutputStream zipOut, List<FileDocument> documents)
			throws JSchException, IOException, SftpException {
		String productName = null;
		String orderId = null;

		for (FileDocument document : documents) {
			String filePath = document.getFilePath();
			String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
			productName = document.getProductName();
			orderId = document.getOrderId();
			// fetch from ftp server
			String SFTPHOST = ConfigParamsEnum.UPLOADSFTPHOST.getStringValue();
			int SFTPPORT = ConfigParamsEnum.UPLOADSFTPPORT.getIntValue();
			String SFTPUSER = ConfigParamsEnum.UPLOADSFTPUSER.getStringValue();
			String SFTPPASS = ConfigParamsEnum.UPLOADSFTPPASS.getStringValue();
			JSch jsch = new JSch();
			com.jcraft.jsch.Session session1;

			session1 = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
			session1.setPassword(SFTPPASS);
			Channel channel = null;
			ChannelSftp channelSftp = new ChannelSftp();
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session1.setConfig(config);
			session1.connect();
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					Constants.EXECUTING + "Host Connected" + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();
			channel = session1.openChannel("sftp");
			channel.connect();
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					Constants.EXECUTING + "sftp channel opened and connected." + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

			channelSftp = (ChannelSftp) channel;
			channelSftp.cd(ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue().replace(".", ""));
			channelSftp.cd(productName);
			channelSftp.cd(orderId);

			try (InputStream in = channelSftp.get(fileName)) {
				BufferedInputStream bis = null;
				bis = new BufferedInputStream(in, 1024);
				ZipEntry ze = new ZipEntry(fileName);
				zipOut.putNextEntry(ze);
				byte[] data = new byte[1024];
				int count;
				while ((count = bis.read(data, 0, 1024)) != -1) {
					zipOut.write(data, 0, count);
				}
				// close entry every time
				zipOut.closeEntry();
				bis.close();

			} catch (Exception e) {
				throw new IOException();
			} finally {
				channelSftp.disconnect();
				session1.disconnect();
			}

			zipOut.close();

		}

	}

	public void createZipFileByUsingHDFS(ZipOutputStream zipOut, FileSystem fileSystem, List<FileDocument> documents)
			throws IOException {

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		HashMap<String, String> check = new HashMap<>();
		for (FileDocument document : documents) {
			String filePath = document.getFilePath();
			// try {
			String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
			Path paths = new Path(filePath);
			System.out.println("paths after for loop:" + paths);

			// check if file is already written or file not found in hdfs then continue to
			// next
			if (!fileSystem.exists(paths) || check.get(filePath) != null) {
				System.out.println("File Not Found on the path : " + filePath);
				continue;
			}
			check.put(filePath, "true");
			try (FSDataInputStream in = fileSystem.open(paths)) {

				System.out.println("paths:" + paths);
				System.out.println("Inside method:");
				BufferedInputStream bis = null;
				bis = new BufferedInputStream(in, 1024);
				ZipEntry ze = new ZipEntry(fileName);
				zipOut.putNextEntry(ze);
				byte[] data = new byte[1024];
				int count;
				while ((count = bis.read(data, 0, 1024)) != -1) {
					zipOut.write(data, 0, count);
				}
				// close entry every time
				zipOut.closeEntry();
				bis.close();
			} catch (Exception e) {

				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeExceptionLog();

				throw new IOException();
			}

		}
		zipOut.close();

	}

	public void createZipFileByUsingMinio(ZipOutputStream zipOut, FileSystem fileSystem, List<FileDocument> documents)
			throws IOException {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		HashMap<String, String> check = new HashMap<>();
		for (FileDocument document : documents) {
			String filePath = document.getFilePath();
			// try {
			String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
			filePath = filePath.substring(1);

			// check if file is already written or file not found in minio then continue to
			// next
			if (!minioUtilService.isObjectExist(filePath) || check.get(filePath) != null) {
				continue;
			}
			check.put(filePath, "true");
			try {
				InputStream in = minioUtilService.getFileStream(filePath);
				BufferedInputStream bis = null;
				bis = new BufferedInputStream(in, 1024);
				ZipEntry ze = new ZipEntry(fileName);
				zipOut.putNextEntry(ze);
				byte[] data = new byte[1024];
				int count;
				while ((count = bis.read(data, 0, 1024)) != -1) {
					zipOut.write(data, 0, count);
				}
				// close entry every time
				zipOut.closeEntry();
				bis.close();
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeExceptionLog();

				throw new IOException();
			}

		}
		zipOut.close();

	}

	public void appendRequestedExcelFilewithStatus(ZipOutputStream zipOut, XSSFWorkbook workbook) throws IOException {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [ " + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		BufferedInputStream bis = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		workbook.write(bos);
		byte[] barray = bos.toByteArray();
		InputStream is = new ByteArrayInputStream(barray);
		bis = new BufferedInputStream(is, 1024);
		ZipEntry ze = new ZipEntry("status.xlsx");
		zipOut.putNextEntry(ze);
		byte[] data = new byte[1024];
		int count;
		while ((count = bis.read(data, 0, 1024)) != -1) {
			zipOut.write(data, 0, count);
		}
		// close entry every time
		zipOut.closeEntry();
		bis.close();
	}

	public static List<String> getAllFilePath(Path filePath, FileSystem fs) throws IOException {
		List<String> fileList = new ArrayList<>();
		FileStatus[] fileStatus = fs.listStatus(filePath);
		for (FileStatus fileStat : fileStatus) {
			if (fileStat.isDirectory()) {
				fileList.addAll(getAllFilePath(fileStat.getPath(), fs));
			} else {
				String fileName = fileStat.getPath().toString();
				fileList.add(fileName.substring(fileName.lastIndexOf('/') + 1));
			}
		}
		return fileList;
	}

}
