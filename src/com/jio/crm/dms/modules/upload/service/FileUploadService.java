package com.jio.crm.dms.modules.upload.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
//import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.SerializationUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.eclipse.jetty.http.HttpStatus;

import com.elastic.search.bean.Page;
import com.elastic.search.bean.SearchResult;
import com.elastic.search.exception.ElasticSearchException;
import com.elastic.search.launcher.SessionFactory;
import com.elastic.search.service.Session;
import com.elastic.search.service.impl.ElasticUUIDGenerator;
import com.elastic.search.service.impl.ObjectMapperHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jio.crm.dms.bean.ArchivalBean;
import com.jio.crm.dms.bean.ArchivalMetaData;
import com.jio.crm.dms.bean.ArchivalStatus;
import com.jio.crm.dms.bean.DocumentArchivalConfiguration;
import com.jio.crm.dms.bean.DocumentCategory;
import com.jio.crm.dms.bean.FileDocument;
import com.jio.crm.dms.clearcodes.ClearCodeLevel;
import com.jio.crm.dms.clearcodes.ClearCodes;
import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.core.BaseResponse;
import com.jio.crm.dms.countermanager.CounterNameEnum;
import com.jio.crm.dms.exceptions.BaseException;
import com.jio.crm.dms.hdfs.HdfsConfigurationManager;
import com.jio.crm.dms.hdfs.HdfsUtilsService;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.modules.upload.helper.UOLColumnEnum;
import com.jio.crm.dms.modules.upload.repository.FileUploadRepository;
import com.jio.crm.dms.node.startup.DmsBootStrapper;
import com.jio.crm.dms.utils.ArchivalUtility;
import com.jio.crm.dms.utils.Constants;
import com.jio.crm.dms.utils.FTPUtil;
import com.jio.crm.dms.utils.MimeTypes;
import com.jio.crm.dms.utils.MinioUtilService;
import com.jio.crm.dms.utils.RequestToBeanMapper;
import com.jio.crm.dms.utils.ServiceCommunicator;
import com.jio.resttalk.service.impl.RestTalkBuilder;
import com.jio.resttalk.service.response.RestTalkResponse;
import com.jio.subscriptions.modules.bean.CompanyDetail;
import com.jio.subscriptions.modules.bean.CustomerFWA;
import com.jio.subscriptions.modules.bean.DeactivedFileDocument;
import com.jio.subscriptions.modules.bean.Document;
import com.jio.subscriptions.modules.bean.GstnDetails;
import com.jio.subscriptions.modules.bean.Order;
import com.jio.subscriptions.modules.bean.OrderFWA;
import com.jio.subscriptions.modules.bean.PoaDetails;
import com.jio.subscriptions.modules.bean.PoiDetails;
import com.jio.telco.framework.clearcode.ClearCodeAsnPojo;
import com.jio.telco.framework.clearcode.ClearCodeAsnUtility;

import au.com.bytecode.opencsv.CSVReader;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;

/**
 * 
 * @author Ghajnafar.Shahid
 *
 */
public class FileUploadService {

	private static final String CONTENT_DISPOSIOTION = "Content-Disposition";
	private static final String CONTENT_ATTACHMENT = "inline; filename=\"";
	private FileUploadRepository fileRepositoryService = new FileUploadRepository();

	/**
	 * @param req
	 * @param uiFileName
	 * @return
	 * @throws Exception
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 */
	public BaseResponse<Map<String, String>> uploadfile(HttpServletRequest req, String uiFileName) throws Exception {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();
		FileDocument document = getDocumentObjetfromRequestParams(req);
		if (document.getProductName() == null || document.getProductName().isEmpty()) {
			throw new BaseException(HttpStatus.BAD_REQUEST_400,
					"productName is mandatory parameter, please send in query params");
		}
		Map<String, String> fileObject = new HashMap<>();
		String orderId = document.getOrderId();
		if (orderId == null || orderId.isEmpty()) {
			orderId = "temp";
		}
		document.setOrderId(orderId);

		if (document.getDocumentCategory() == null || document.getDocumentCategory().isEmpty()) {
			throw new BaseException(400, Constants.DOCUMENTCATEGORYVALUE);
		}
		document.setDocumentCategory(document.getDocumentCategory());

		// compare with document category
		if (!checkForDocumentCategory(document))
			throw new BaseException(400, "Please enter correct document Category value!!!!");

		SessionFactory sessionFactory = SessionFactory.getSessionFactory();
		Session session = sessionFactory.getSession();

		// to check condition of duplicate document category
		List<FileDocument> fileDocumnets = this.fileRepositoryService.archivalFiles(session, orderId, null);
		for (FileDocument fileDocument : fileDocumnets) {
			if (fileDocument.getDocumentCategory().equalsIgnoreCase(document.getDocumentCategory())) {
				try {
					deleteFile(fileDocument.getProductName(), fileDocument.getOrderId(), fileDocument.getFileName());
				} catch (Exception e) {
					DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getLogBuilder(
							Constants.EXECUTING + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
							.writeLog();
				}
			}
		}

		String extension = FilenameUtils.getExtension(uiFileName);

		// get base file Name
		String fileNameTemp = FilenameUtils.getBaseName(uiFileName);

		byte[] byteArray = IOUtils.toByteArray(req.getInputStream());
		// byte[] bytes = IOUtils.toByteArray(body);

		if (byteArray.length == 0) {

			throw new BaseException(HttpStatus.BAD_REQUEST_400, Constants.FILEZEROBYTESENDCORRECTFILE);
		}

		Date date = new Date();
		String file = fileNameTemp + "_" + ElasticUUIDGenerator.getInstance().getUniqueUUID();
		String fileName = file + "." + extension;

		document.setUploadedOn(date);
		double size = (double) (byteArray.length) / 1024;
		document.setSize(String.format("%.2f", size));

		String uploadFilePath = getFilePath(document.getProductName(), orderId);
		String filepath = uploadFilePath + Constants.DELIMITER + fileName;

		fileName = file + "." + extension;
		filepath = uploadFilePath + Constants.DELIMITER + fileName;

		// SwitchCase statements
		switch (ConfigParamsEnum.FILESYSTEM.getIntValue()) {

		case 0:
			// hdfs
			if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")
					|| extension.equalsIgnoreCase("png")) {
				Configuration conf = HdfsConfigurationManager.getConfiguration();

				FileSystem fs = FileSystem.get(conf);
				uiFileName = fileNameTemp + ".pdf";
				fileName = file + ".pdf";
				filepath = uploadFilePath + Constants.DELIMITER + fileName;
				FileToPdfConverter.getIndtance().convertImageToPdf(byteArray, filepath, fs);

			} else {
				hdfsFileUpload(filepath, fileName, ccAsnPojo, byteArray);
			}
			break;

		case 1:

			if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")
					|| extension.equalsIgnoreCase("png")) {

				byteArray = FileToPdfConverter.getIndtance().convertImageToPdfForMinio(byteArray);

				uiFileName = fileNameTemp + ".pdf";
				fileName = file + ".pdf";
				filepath = uploadFilePath + Constants.DELIMITER + fileName;
			}
			// ftp
			ftpFileUpload(document, uiFileName, fileName, ccAsnPojo, fileObject, byteArray);

			break;

		case 2:
			// minio
			if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")
					|| extension.equalsIgnoreCase("png")) {

				byteArray = FileToPdfConverter.getIndtance().convertImageToPdfForMinio(byteArray);

				uiFileName = fileNameTemp + ".pdf";
				fileName = file + ".pdf";
				filepath = uploadFilePath + Constants.DELIMITER + fileName;
			}
			MinioUtilService.getInstance().uploadFile(uploadFilePath.replace(".", "") + Constants.DELIMITER + fileName,
					byteArray, fileName, "application/json", -1);

			break;

		}

		fileObject.put(Constants.FILENAME, fileName);
		fileObject.put(Constants.ACTUALFILENAME, uiFileName);
		document.setActualFileName(uiFileName);
		document.setFileName(fileName);
		document.setFilePath(filepath);
		fileRepositoryService.saveDocument(session, document);
		session.flush();

		final BaseResponse<Map<String, String>> baseResponse = new BaseResponse<>(fileObject);
		CounterNameEnum.CNTR_UPLOAD_FILE_SUCCESS.increment();
		ccAsnPojo.addClearCode(ClearCodes.UPLOAD_FILE_SUCCESS.getValue(), ClearCodeLevel.SUCCESS);
		ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);

		return baseResponse;
	}

	public Map<String, String> ftpFileUpload(FileDocument document, String uiFileName, String fileName,
			ClearCodeAsnPojo ccAsnPojo, Map<String, String> fileObject, byte[] byteArray)
			throws IOException, BaseException {

		File file = new File(fileName);
		FileUtils.copyInputStreamToFile(new ByteArrayInputStream(byteArray), file);

		String SFTPHOST = ConfigParamsEnum.UPLOADSFTPHOST.getStringValue();
		int SFTPPORT = ConfigParamsEnum.UPLOADSFTPPORT.getIntValue();
		String SFTPUSER = ConfigParamsEnum.UPLOADSFTPUSER.getStringValue();
		String SFTPPASS = ConfigParamsEnum.UPLOADSFTPPASS.getStringValue();
		String dir = ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue().replace(".", "");

		JSch jsch = new JSch();
		try {
			callToFTP(SFTPHOST, SFTPPORT, SFTPUSER, SFTPPASS, file, jsch, dir, document);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, Constants.ERRORINUPLOADFILE,
					this.getClass().getName(), Constants.UPLOADFILE);
			CounterNameEnum.CNTR_UPLOAD_FILE_FAILURE.increment();
			ccAsnPojo.addClearCode(ClearCodes.UPLOAD_FILE_FAILURE.getValue(), ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			throw new BaseException(HttpStatus.BAD_REQUEST_400, e.getMessage());
		}

		if (file.delete()) {
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder("File got deleted successfully" + file.getAbsolutePath(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();
		}

		fileObject.put(Constants.FILENAME, fileName);
		fileObject.put(Constants.ACTUALFILENAME, uiFileName);
		return fileObject;

	}

	public void callToFTP(String SFTPHOST, int SFTPPORT, String SFTPUSER, String SFTPPASS, File file, JSch jsch,
			String path, FileDocument document) throws JSchException, IOException, BaseException {
		com.jcraft.jsch.Session session1;

		session1 = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
		session1.setPassword(SFTPPASS);
		FTPUtil.uploadDirectory(file.getAbsolutePath(), path, session1, null, document);

	}

	public String getFtpFilePath(String sftpWorkDir, String productName, String orderId) {

		return sftpWorkDir + Constants.DELIMITER + productName + Constants.DELIMITER + orderId;
	}

	public void hdfsFileUpload(String filepath, String fileName, final ClearCodeAsnPojo ccAsnPojo, byte[] byteArray)
			throws BaseException {
		// get file extensions
		String extension = FilenameUtils.getExtension(fileName);
		//

		// write file in HDFS
		try {
			Configuration conf = HdfsConfigurationManager.getConfiguration();

			FileSystem fs = FileSystem.get(conf);
			if (extension.equalsIgnoreCase("pdf")) {
				FileToPdfConverter.getIndtance().compressPdfFile(byteArray, filepath, fs);
			} else {

				Path path = new Path(filepath);
				try (FSDataOutputStream out = fs.create(path)) {
					out.write(byteArray);

				}
			}

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, Constants.ERRORINUPLOADFILE,
					this.getClass().getName(), Constants.UPLOADFILE);
			CounterNameEnum.CNTR_UPLOAD_FILE_FAILURE.increment();
			ccAsnPojo.addClearCode(ClearCodes.UPLOAD_FILE_FAILURE.getValue(), ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			throw new BaseException(HttpStatus.BAD_REQUEST_400, e.getMessage());
		}

	}

	/**
	 * @param req
	 * @param uiFileName
	 * @return
	 * @throws Exception
	 */
	public BaseResponse<Map<String, String>> uploadUolFile(HttpServletRequest req, String uiFileName) throws Exception {

		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();

		FileDocument document = getDocumentObjetfromRequestParams(req);

		Map<String, String> fileObject = new HashMap<>();

		String orderId = document.getOrderId();
		String productName = document.getProductName();

		if (orderId == null || orderId.isEmpty()) {
			orderId = "temp";
		}
		document.setOrderId(orderId);

		if (document.getDocumentCategory() == null || document.getDocumentCategory().isEmpty()) {
			throw new BaseException(400, Constants.DOCUMENTCATEGORYVALUE);
		}
		document.setDocumentCategory(document.getDocumentCategory());

		// compare with document category
		if (!checkForDocumentCategory(document))
			throw new BaseException(400, "Please enter correct document Category value!!!!");

		SessionFactory sessionFactory = SessionFactory.getSessionFactory();
		Session session = sessionFactory.getSession();

		// to check condition of duplicate document category
		List<FileDocument> fileDocumnets = this.fileRepositoryService.archivalFiles(session, orderId, null);
		for (FileDocument fileDocument : fileDocumnets) {
			if (fileDocument.getDocumentCategory().equalsIgnoreCase(document.getDocumentCategory())) {
				try {
					deleteFile(fileDocument.getProductName(), fileDocument.getOrderId(), fileDocument.getFileName());
				} catch (Exception e) {
					DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getLogBuilder(
							Constants.EXECUTING + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
							.writeLog();
				}
			}
		}

		boolean isCosSpecific = document.isCosSpecific();
		document.setActualFileName(uiFileName);

		// get file extensions
		String extension = FilenameUtils.getExtension(uiFileName);
		String fileNameTemp = FilenameUtils.getBaseName(uiFileName);

		// generate UUID file name
		Date date = new Date();
		String file = fileNameTemp + "_" + ElasticUUIDGenerator.getInstance().getUniqueUUID();
		String fileName = file + "." + extension;
		document.setFileName(fileName);
		document.setUploadedOn(date);

		// constructs path of the directory to save uploaded file
		String uploadFilePath = getFilePath(productName, orderId);
		String filePath = uploadFilePath + Constants.DELIMITER + fileName;

		// write file in HDFS
		try {

			byte[] byteArray = IOUtils.toByteArray(req.getInputStream());

			double size = (double) (byteArray.length) / 1024;
			document.setSize(String.format("%.2f", size));

			Map<String, String> columns = Arrays.stream(UOLColumnEnum.values())
					.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

			// checking validation of UOL file
			if (!validateColumns(columns, byteArray, isCosSpecific)) {
				CounterNameEnum.CNTR_UPLOAD_UOL_FILE_FAILURE.increment();
				throw new BaseException(HttpStatus.BAD_REQUEST_400, "UOL File is not Valid");
			}

			switch (ConfigParamsEnum.FILESYSTEM.getIntValue()) {
			case 0:
				// upload by hdfs
				hdfsFileUpload(filePath, fileName, ccAsnPojo, byteArray);
				break;

			case 1:
				// upload by ftp
				uploadUOLbyFTP(filePath.replace(".", ""), fileName, ccAsnPojo, byteArray, document);

				break;

			case 2:
				// upload by minio
				MinioUtilService.getInstance().uploadFile(
						uploadFilePath.replace(".", "") + Constants.DELIMITER + fileName, byteArray, fileName,
						"application/json", -1);
				break;
			}

			fileObject.put(Constants.FILENAME, fileName);
			fileObject.put(Constants.ACTUALFILENAME, uiFileName);

			document.setFilePath(filePath);
			try {
				fileRepositoryService.saveDocument(session, document);
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeExceptionLog();
			}
			session.flush();
			final BaseResponse<Map<String, String>> baseResponse = new BaseResponse<>(fileObject);
			CounterNameEnum.CNTR_UPLOAD_UOL_FILE_SUCCESS.increment();
			ccAsnPojo.addClearCode(ClearCodes.UPLOAD_UOL_FILE_SUCCESS.getValue(), ClearCodeLevel.SUCCESS);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);

			return baseResponse;

		} catch (

		IOException e) {
			CounterNameEnum.CNTR_UPLOAD_UOL_FILE_FAILURE.increment();
			ccAsnPojo.addClearCode(ClearCodes.UPLOAD_UOL_FILE_FAILURE.getValue(), ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			throw new BaseException(HttpStatus.BAD_REQUEST_400, Constants.FILEWRITTINGEXCEPTION);
		}

	}

	public void uploadUOLbyFTP(String filePath, String fileName, ClearCodeAsnPojo ccAsnPojo, byte[] byteArray,
			FileDocument document) throws IOException, BaseException {
		File file = new File(fileName);
		FileUtils.copyInputStreamToFile(new ByteArrayInputStream(byteArray), file);
		String SFTPHOST = ConfigParamsEnum.UPLOADSFTPHOST.getStringValue();
		int SFTPPORT = ConfigParamsEnum.UPLOADSFTPPORT.getIntValue();
		String SFTPUSER = ConfigParamsEnum.UPLOADSFTPUSER.getStringValue();
		String SFTPPASS = ConfigParamsEnum.UPLOADSFTPPASS.getStringValue();
		String dir = ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue().replace(".", "");
		JSch jsch = new JSch();
		try {
			callToFTP(SFTPHOST, SFTPPORT, SFTPUSER, SFTPPASS, file, jsch, dir, document);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, Constants.ERRORINUPLOADFILE,
					this.getClass().getName(), Constants.UPLOADFILE);
			CounterNameEnum.CNTR_UPLOAD_FILE_FAILURE.increment();
			ccAsnPojo.addClearCode(ClearCodes.UPLOAD_FILE_FAILURE.getValue(), ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			throw new BaseException(HttpStatus.BAD_REQUEST_400, e.getMessage());
		}

		if (file.delete()) {
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder("File got deleted successfully" + file.getAbsolutePath(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();
		}

	}

	public void uploadUOLbyHdfs(FileDocument document, Date date, String fileName, String uploadFilePath,
			byte[] byteArray) throws IOException, ElasticSearchException {
		Configuration conf = HdfsConfigurationManager.getConfiguration();

		FileSystem fs = FileSystem.get(conf);
		String uploadPath = uploadFilePath + Constants.DELIMITER + fileName;
		Path path = new Path(uploadPath);
		document.setFilePath(uploadPath);
		document.setUploadedOn(date);
		SessionFactory factory = SessionFactory.getSessionFactory();
		Session session = factory.getSession();
		session.beginTransaction();
		try {
			fileRepositoryService.saveDocument(session, document);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}
		session.flush();
		try (FSDataOutputStream out = fs.create(path)) {
			out.write(byteArray);

		}
	}

	/**
	 * @param response
	 * @param fileName
	 * @param request
	 * @param cafNumber
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws BaseException
	 * @throws ElasticSearchException
	 * @throws IllegalArgumentException
	 * @throws XmlParserException
	 * @throws ServerException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidResponseException
	 * @throws InternalException
	 * @throws InsufficientDataException
	 * @throws ErrorResponseException
	 * @throws InvalidKeyException
	 */
	public void getFile(HttpServletResponse response, String fileName, String productName, String orderId,
			String fileDownloadName) throws IOException, BaseException, ElasticSearchException, InvalidKeyException,
			ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException,
			NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException {

		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();

		// check for order id
		if (orderId == null || orderId.isEmpty()) {
			orderId = "temp";
		}
		String targetPath = getFilePath(productName, orderId) + Constants.DELIMITER + fileName;
		// switch statements

		switch (ConfigParamsEnum.FILESYSTEM.getIntValue()) {
		case 0:
			// get file from hdfs
			getFileFromHDFS(response, fileName, targetPath, fileDownloadName, ccAsnPojo);
			break;

		case 1:
			// get file from ftp
			getFileFromFTP(response, fileName, ccAsnPojo);
			break;

		case 2:
			// get file from minio
			targetPath = getFilePath(productName, orderId).replace(".", "") + Constants.DELIMITER + fileName;
			// minioFileUploadService.getFile(response, fileName, targetPath,
			// fileDownloadName);
			MinioUtilService.getInstance().getFile(response, fileName, targetPath, fileDownloadName);
			break;
		}
	}

	public void getFileFromFTP(HttpServletResponse response, String fileName, ClearCodeAsnPojo ccAsnPojo)
			throws ElasticSearchException, BaseException {
		Session session = SessionFactory.getSessionFactory().getSession();
		Map<String, String> filters = new HashMap<>();

		filters.put(Constants.FILENAME, fileName);

		FileDocument document = fileRepositoryService.getFileDocument(session, filters);

		// fetch from ftp server
		String SFTPHOST = ConfigParamsEnum.UPLOADSFTPHOST.getStringValue();
		int SFTPPORT = ConfigParamsEnum.UPLOADSFTPPORT.getIntValue();
		String SFTPUSER = ConfigParamsEnum.UPLOADSFTPUSER.getStringValue();
		String SFTPPASS = ConfigParamsEnum.UPLOADSFTPPASS.getStringValue();

		JSch jsch = new JSch();
		try {
			callToFTPForDownloadFile(SFTPHOST, SFTPPORT, SFTPUSER, SFTPPASS, document, jsch, response);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, Constants.ERRORINUPLOADFILE,
					this.getClass().getName(), Constants.UPLOADFILE);
			CounterNameEnum.CNTR_UPLOAD_FILE_FAILURE.increment();
			ccAsnPojo.addClearCode(ClearCodes.UPLOAD_FILE_FAILURE.getValue(), ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			throw new BaseException(HttpStatus.BAD_REQUEST_400, e.getMessage());
		}

	}

	public String getDocumentCategoryFromFileName(String input) {
		// Find the index of the last underscore
		int lastIndex = input.lastIndexOf('_');
		int dotIndex = input.lastIndexOf('.');

		if (lastIndex != -1 && lastIndex < dotIndex && dotIndex != -1) {
			// Extract the substring after the last underscore
			return input.substring(lastIndex + 1, dotIndex);

		} else {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getLogBuilder(
					"Underscore not found in the input string or it is the last character." + this.getClass().getName()
							+ "." + Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

		}
		return input;

	}

	private void callToFTPForDownloadFile(String SFTPHOST, int SFTPPORT, String SFTPUSER, String SFTPPASS,
			FileDocument document, JSch jsch, HttpServletResponse response) throws JSchException, IOException {
		com.jcraft.jsch.Session session1;

		session1 = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
		session1.setPassword(SFTPPASS);
		FTPUtil.getFileFromFtp(document, session1, response);

	}

	private void getFileFromHDFS(HttpServletResponse response, String fileName, String targetPath,
			String fileDownloadName, final ClearCodeAsnPojo ccAsnPojo) throws IOException, BaseException {

		// getting file from HDFS
		Configuration conf = HdfsConfigurationManager.getConfiguration();

		FileSystem fileSystem = FileSystem.get(conf);
		Path path = new Path(targetPath);
		if (fileSystem.exists(path)) {

			try (FSDataInputStream in = fileSystem.open(path)) {
				long length = fileSystem.getFileStatus(path).getLen();
				String extension = FilenameUtils.getExtension(fileName);
				String contentType = MimeTypes.getMimeType(extension);
				response.setContentType(contentType);

				if (fileDownloadName != null) {
					fileName = fileDownloadName + "." + extension;
				}
				response.setHeader(CONTENT_DISPOSIOTION, CONTENT_ATTACHMENT + fileName + "\"");
				response.setContentLength((int) length);

				OutputStream os = response.getOutputStream();

				try {

					byte[] buffer = new byte[1024];
					int numBytes = 0;
					while ((numBytes = in.read(buffer)) > 0) {
						os.write(buffer, 0, numBytes);
					}

				} finally {
					os.flush();
					os.close();

					fileSystem.close();
				}
				ccAsnPojo.addClearCode(ClearCodes.GET_FILE_SUCCESS.getValue(), ClearCodeLevel.SUCCESS);
				ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
				CounterNameEnum.CNTR_GET_FILE_SUCCESS.increment();
			}
		} else {
			CounterNameEnum.CNTR_GET_FILE_FAILURE.increment();
			ccAsnPojo.addClearCode(ClearCodes.GET_FILE_FAILURE.getValue(), ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			throw new BaseException(HttpStatus.BAD_REQUEST_400, Constants.FILENOTFOUND);

		}
	}

	public InputStream getArchivalFile(String fileName, String productName, String orderId)
			throws IOException, BaseException {

		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		// check for order id
		if (orderId == null || orderId.isEmpty()) {
			orderId = "temp";
		}

		String targetPath = getFilePath(productName, orderId) + Constants.DELIMITER + fileName;

		// get files

		InputStream inputStream = null;
		InputStream javaInputStream = null;

		Configuration conf = HdfsConfigurationManager.getConfiguration();

		FileSystem fileSystem = FileSystem.get(conf);
		try {
			inputStream = getFileFromHDFS(fileSystem, targetPath);
			javaInputStream = IOUtils.toBufferedInputStream(inputStream);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
			fileSystem.close();
		}
		return javaInputStream;
	}

	private InputStream getFileFromHDFS(FileSystem fs, String targetPath) throws IOException {
		Path file = new Path(targetPath);
		if (fs.exists(file)) {
			return fs.open(file);
		} else {
			throw new IOException("File does not exist in HDFS or is not a file");
		}
	}

	/**
	 * @param orderId
	 * @param fileName
	 * @param productName
	 * @param columns
	 * @return
	 * @throws BaseException
	 * @throws IOException
	 * @throws ElasticSearchException
	 * @throws JSchException
	 * @throws IllegalArgumentException
	 * @throws XmlParserException
	 * @throws ServerException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidResponseException
	 * @throws InternalException
	 * @throws InsufficientDataException
	 * @throws ErrorResponseException
	 * @throws InvalidKeyException
	 */
	public BaseResponse<Map<String, Boolean>> validateColumns(String orderId, String fileName, String productName,
			Map<String, String> columns) throws BaseException, IOException, ElasticSearchException, JSchException {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();
		byte[] buffer = null;
		String targetPath = ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue() + Constants.DELIMITER + productName
				+ Constants.DELIMITER + orderId + Constants.DELIMITER + fileName;
		switch (ConfigParamsEnum.FILESYSTEM.getIntValue()) {
		case 0:

			buffer = HdfsUtilsService.getInstance().readFile(targetPath);
			break;
		case 1:
			buffer = readFileFromFTP(fileName, orderId);
			break;
		case 2:
			targetPath = targetPath.replace(".", "");
			buffer = MinioUtilService.getInstance().readFileFromMinio(targetPath);
			break;
		}

		CSVReader csvReader = null;
		BaseResponse<Map<String, Boolean>> resp = null;
		try {

			// converting byte array to File Reader
			InputStream input = new ByteArrayInputStream(buffer);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));
			csvReader = new CSVReader(bufferedReader);
			String[] nextRecord;
			List<String> headerList = new ArrayList<>();
			Map<String, Boolean> response = new HashMap<>();
			// we are going to read data line by line

			if ((nextRecord = csvReader.readNext()) != null) {
				for (String cell : nextRecord) {

					headerList.add(cell);
				}
				if (validateColumns(columns, headerList)) {
					response.put(Constants.VALIDATION, true);
				} else {
					response.put(Constants.VALIDATION, false);
				}

			} else {
				response.put(Constants.VALIDATION, false);
			}

			resp = new BaseResponse<>(response);
			CounterNameEnum.CNTR_VALIDATE_COLUMN_SUCCESS.increment();
			ccAsnPojo.addClearCode(ClearCodes.VALIDATE_COLUMN_SUCCESS.getValue(), ClearCodeLevel.SUCCESS);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
		} catch (IOException e) {
			CounterNameEnum.CNTR_VALIDATE_COLUMN_FAILURE.increment();
			ccAsnPojo.addClearCode(ClearCodes.VALIDATE_COLUMN_FAILURE.getValue(), ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			throw new BaseException(HttpStatus.BAD_REQUEST_400, Constants.FILENOTFOUND);
		} finally {
			if (csvReader != null) {
				csvReader.close();
			}
		}
		return resp;

	}

	public byte[] readFileFromFTP(String fileName, String orderId) throws ElasticSearchException, JSchException {
		Session session = SessionFactory.getSessionFactory().getSession();

		Map<String, String> filters = new HashMap<>();

		filters.put(Constants.FILENAME, fileName);
		FileDocument document = fileRepositoryService.getFileDocument(session, filters);

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
		byte[] bytes = null;
		try {

			java.util.Properties config = new java.util.Properties();
			config.put(Constants.STRICTHOSTKEYCONFIGURATION, "no");
			session1.setConfig(config);
			session1.connect();
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					Constants.EXECUTING + Constants.HOSTCONNECTED + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();
			channel = session1.openChannel("sftp");
			channel.connect();
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					Constants.EXECUTING + Constants.SFTPCHANNELCONNECTEDOPENED + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

			channelSftp = (ChannelSftp) channel;
			channelSftp.cd(ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue().replace(".", "/"));
			channelSftp.cd(document.getProductName());
			channelSftp.cd(document.getOrderId());
			// Get the input stream of the file from the FTP server
			try (InputStream in = channelSftp.get(fileName)) {

				bytes = new byte[1024];
				int bytesRead = in.read(bytes);
				if (bytesRead >= 0) {
					DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
							Constants.EXECUTING + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
							.writeLog();
					channelSftp.disconnect();
					session1.disconnect();

					DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
							Constants.EXECUTING + "File Uploaded Successfully" + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();
				} else {
					throw new BaseException(HttpStatus.BAD_REQUEST_400, "Bytes are zero");
				}

			}

		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, e.getMessage(), "Exception found while tranfer the response.",
							Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

		} finally {
			channelSftp.exit();

			try {
				if (channel != null) {
					channel.disconnect();
					DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
							Constants.EXECUTING + "Channel Disconnected" + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

				}
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
						.writeExceptionLog();
			}
			session1.disconnect();

		}

		return bytes;
	}

	/**
	 * @param orderId
	 * @param fileName
	 * @param columns
	 * @return
	 * @throws BaseException
	 * @throws IOException
	 */
	public List<Map<String, Object>> getColumnsValues(String orderId, String fileName, Map<String, String> columns)
			throws BaseException, IOException {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();

		String targetPath = ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue() + Constants.DELIMITER + orderId
				+ Constants.DELIMITER + fileName;

		byte[] buffer = HdfsUtilsService.getInstance().readFile(targetPath);
		CSVReader csvReader = null;

		// making response object
		List<Map<String, Object>> resp = new ArrayList<>();

		try {

			// converting byte array to File Reader
			InputStream input = new ByteArrayInputStream(buffer);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));

			// converting into CSVReader
			csvReader = new CSVReader(bufferedReader);

			// Reading csv File and getting required component values
			List<String[]> rows = csvReader.readAll();
			if (rows.size() > 1) {
				List<String> headerList = Arrays.asList(rows.get(0));
				if (validateColumns(columns, headerList)) {
					Map<String, Integer> columnIndexMapping = getColumnsIndexMapping(columns, headerList);
					rows.remove(0);
					for (String[] row : rows) {
						Map<String, Object> obj = new HashMap<>();
						columnIndexMapping.forEach((key, index) -> {
							obj.put(key, filterResult(row, index));
						});
						resp.add(obj);
					}
				} else {
					CounterNameEnum.CNTR_GET_COLUMN_VALUES_FAILURE.increment();
					ccAsnPojo.addClearCode(ClearCodes.GET_COLUMN_VALUES_FAILURE.getValue(), ClearCodeLevel.FAILURE);
					ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
					throw new BaseException(HttpStatus.BAD_REQUEST_400, "Column is missing");
				}
			} else {
				CounterNameEnum.CNTR_GET_COLUMN_VALUES_FAILURE.increment();
				ccAsnPojo.addClearCode(ClearCodes.GET_COLUMN_VALUES_FAILURE.getValue(), ClearCodeLevel.FAILURE);
				ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
				throw new BaseException(HttpStatus.BAD_REQUEST_400, "Data Rows Not Present");
			}
			CounterNameEnum.CNTR_GET_COLUMN_VALUES_SUCCESS.increment();
			ccAsnPojo.addClearCode(ClearCodes.GET_COLUMN_VALUES_SUCCESS.getValue(), ClearCodeLevel.SUCCESS);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);

		} catch (IOException e) {
			CounterNameEnum.CNTR_GET_COLUMN_VALUES_FAILURE.increment();
			ccAsnPojo.addClearCode(ClearCodes.GET_COLUMN_VALUES_FAILURE.getValue(), ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			throw new BaseException(HttpStatus.BAD_REQUEST_400, Constants.FILENOTFOUND);
		} finally {
			if (csvReader != null) {
				csvReader.close();
			}
		}
		return resp;
	}

	private String filterResult(String[] row, Integer index) {

		String data = String.valueOf(row[index]);
		if (data.startsWith("`"))
			return data.substring(1, data.length());
		return data;
	}

	/**
	 * @param columns
	 * @param headerList
	 * @return
	 */
	public boolean validateColumns(Map<String, String> columns, List<String> headerList) {
		for (String column : columns.keySet()) {
			if (headerList.contains(columns.get(column))) {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param columns
	 * @param headerList
	 * @return
	 */
	public Map<String, Integer> getColumnsIndexMapping(Map<String, String> columns, List<String> headerList) {
		Map<String, Integer> map = new HashMap<>();
		for (String column : columns.keySet()) {
			int index = headerList.indexOf(columns.get(column));
			map.put(column, index);
		}

		return map;
	}

	/**
	 * @param columns
	 * @param buffer
	 * @return
	 * @throws IOException
	 * @throws BaseException
	 */
	public boolean validateColumns(Map<String, String> columns, byte[] buffer, boolean isCosSpecific)
			throws IOException, BaseException {
		boolean isValidated = false;
		CSVReader csvReader = null;
		try {
			// converting byte array to File Reader
			InputStream input = new ByteArrayInputStream(buffer);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));

			// converting into CSVReader
			csvReader = new CSVReader(bufferedReader);

			// Reading csv File and getting required column values
			List<String[]> rows = csvReader.readAll();
			if (!rows.isEmpty()) {
				List<String> headerList = Arrays.asList(rows.get(0));
				isValidated = validateColumns(columns, headerList);

			}
		} catch (Exception e) {
			CounterNameEnum.CNTR_UPLOAD_UOL_FILE_FAILURE.increment();
			throw new BaseException(HttpStatus.BAD_REQUEST_400, "UOL File is not Valid");
		} finally {
			if (csvReader != null) {
				csvReader.close();
			}
		}

		return isValidated;
	}

	public BaseResponse<Map<String, String>> deleteFile(String productName, String orderId, String fileName)
			throws IOException, BaseException, ElasticSearchException, JSchException {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();

		// check for order id
		if (orderId == null || orderId.isEmpty()) {
			orderId = "temp";
		}

		// get file extensions
		String extension = FilenameUtils.getExtension(fileName);

		switch (ConfigParamsEnum.FILESYSTEM.getIntValue()) {
		case 0:
			// delete from hdfs

			deleteFromHDFS(productName, orderId, fileName, ccAsnPojo, extension);

			break;
		case 1:

			deleteFromFTP(orderId, fileName, ccAsnPojo);

			break;
		case 2:
			// delete from minio

			String filepath = getFilePath(productName, orderId).replace(".", "") + Constants.DELIMITER + fileName;

			// minioFileUploadService.deleteFile(filepath, fileName);
			MinioUtilService.getInstance().deleteFile(filepath);

			break;

		}
		// delete file from data base also
		deleteFromDatabase(fileName);
		Map<String, String> fileObject = new HashMap<>();
		fileObject.put(Constants.FILENAME, fileName);

		BaseResponse<Map<String, String>> response = new BaseResponse<>(fileObject);

		return response;
	}

	public void deleteFromFTP(String orderId, String fileName, ClearCodeAsnPojo ccAsnPojo)
			throws ElasticSearchException, JSchException, BaseException {
		Session session = SessionFactory.getSessionFactory().getSession();

		Map<String, String> filters = new HashMap<>();

		filters.put(Constants.FILENAME, fileName);
		FileDocument document = fileRepositoryService.getFileDocument(session, filters);

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

		try {

			java.util.Properties config = new java.util.Properties();
			config.put(Constants.STRICTHOSTKEYCONFIGURATION, "no");
			session1.setConfig(config);
			session1.connect();
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					Constants.EXECUTING + Constants.HOSTCONNECTED + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();
			channel = session1.openChannel("sftp");
			channel.connect();
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					Constants.EXECUTING + Constants.SFTPCHANNELCONNECTEDOPENED + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

			channelSftp = (ChannelSftp) channel;
			channelSftp.cd(ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue().replace(".", "/"));
			channelSftp.cd(document.getProductName());
			channelSftp.cd(document.getOrderId());

			// delete file from server
			channelSftp.rm(document.getFileName());

			try {
				if (channel != null) {
					channel.disconnect();
					DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
							Constants.EXECUTING + "Channel Disconnected" + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

				}
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
						.writeExceptionLog();
			} finally {
				session1.disconnect();
			}
		} catch (Exception e) {
			CounterNameEnum.CNTR_DELETE_FILE_FAILURE.increment();
			ccAsnPojo.addClearCode(ClearCodes.DELETE_FILE_FAILURE.getValue(), ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			throw new BaseException(400, e.getMessage());
		}

	}

	private void deleteFromHDFS(String productName, String orderId, String fileName, final ClearCodeAsnPojo ccAsnPojo,
			String extension) throws IOException, BaseException {
		// delete pdf file of image
		if (extension.toLowerCase().equals("jpg") || extension.toLowerCase().equals("jpeg")
				|| extension.toLowerCase().equals("gif") || extension.toLowerCase().equals("png")) {
			String pdfFileName = FilenameUtils.getBaseName(fileName) + ".pdf";
			String pdfTargetPath = getFilePath(productName, orderId) + Constants.DELIMITER + pdfFileName;
			HdfsUtilsService.getInstance().deleteFile(pdfTargetPath);
		}

		String targetPath = getFilePath(productName, orderId) + Constants.DELIMITER + fileName;
		try {

			HdfsUtilsService.getInstance().deleteFile(targetPath);
			CounterNameEnum.CNTR_DELETE_FILE_SUCCESS.increment();
			ccAsnPojo.addClearCode(ClearCodes.DELETE_FILE_SUCCESS.getValue(), ClearCodeLevel.SUCCESS);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
		} catch (BaseException e) {
			CounterNameEnum.CNTR_DELETE_FILE_FAILURE.increment();
			ccAsnPojo.addClearCode(ClearCodes.DELETE_FILE_FAILURE.getValue(), ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			throw e;
		}
	}

	public BaseResponse<Map<String, List<FileDocument>>> searchFileDocuments(FileDocument document) {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();

		HashMap<String, String> filters = new HashMap<>();

		if (document.getOrderId() != null) {
			filters.put(Constants.ORDERID, document.getOrderId());
		}
		if (document.getMobileNumber() != null) {
			filters.put("mobileNumber", document.getMobileNumber());
		}
		if (document.getCustomerId() != null) {
			filters.put(Constants.CUSTOMER_ID, document.getCustomerId());
		}
		if (document.getDocumentCategory() != null) {
			filters.put("documentCategory", document.getDocumentCategory());
		}
		if (document.getDocumentNumber() != null) {
			filters.put("documentNumber", document.getDocumentNumber());
		}

		SessionFactory factory = SessionFactory.getSessionFactory();
		Session session = factory.getSession();
		Page page = session.defaultPage();
		page.setPageLength(1000);
		BaseResponse<Map<String, List<FileDocument>>> res = null;
		try {
			session.beginTransaction();
			SearchResult<FileDocument> result = null;
			Map<String, List<FileDocument>> data = null;
			if (filters.isEmpty()) {
				data = new HashMap<String, List<FileDocument>>();
			} else {
				result = fileRepositoryService.getAllDocumentByFilters(session, filters, page);
				data = createResponseJsonForSearch(result);
			}
			res = new BaseResponse<Map<String, List<FileDocument>>>(data);
		} catch (Exception e) {
			ccAsnPojo.addClearCode(ClearCodes.SEARCH_FILE_DOCUMENT_FAILURE.getValue(), ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);

		}

		CounterNameEnum.CNTR_SEARCH_FILE_DOCUMENT_SUCCESS.increment();
		ccAsnPojo.addClearCode(ClearCodes.SEARCH_FILE_DOCUMENT_SUCCESS.getValue(), ClearCodeLevel.SUCCESS);
		ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);

		return res;
	}

	public Map<String, List<FileDocument>> createResponseJsonForSearch(SearchResult<FileDocument> result) {
		List<FileDocument> documents = new ArrayList<>(result.getResult());
		Map<String, List<FileDocument>> resp = new HashMap<>();
		for (FileDocument document : documents) {
			String orderId = document.getOrderId();
			List<FileDocument> list = null;
			if (resp.get(orderId) != null) {
				list = resp.get(orderId);
				list.add(document);
				resp.put(orderId, list);
			} else {
				list = new ArrayList<>();
				list.add(document);
				resp.put(orderId, list);
			}

		}
		return resp;

	}

	public Map<String, Boolean> moveFileFromTemp(Order order) throws BaseException, JSchException, SftpException {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();
		Map<String, Boolean> resp = new HashMap<>();
		boolean isMoved = false;
		String orderId = order.getOrderId();

		SessionFactory factory = SessionFactory.getSessionFactory();
		Session session = factory.getSession();
		String productName = null;
		if (order.getProductOffering() != null && order.getProductOffering().get(0).getProduct() != null) {

			productName = order.getProductOffering().get(0).getProduct().get(0).getName();
		} else {
			if (order.getPlan() != null) {
				productName = order.getPlan().getName();
			}
		}

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [ Product name: " + productName + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		Map<String, String> docList = getlistoforderdocument(order, productName);

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [ Doc list: " + docList + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		List<String> docIds = new ArrayList<>(docList.values());

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [ Doc docIds: " + docIds + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		switch (ConfigParamsEnum.FILESYSTEM.getIntValue()) {
		case 0:
			// update metadata
			updateMetaData(session, docIds, productName, orderId);
			// from hdfs
			isMoved = moveFileFromTempToOrderHDFS(isMoved, orderId, productName, docIds);
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					"Executing [ Successfully Move from Temp to : " + docIds + orderId + this.getClass().getName() + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

			CounterNameEnum.CNTR_MOVE_FILE_FROM_TEMP_TO_ORDER_SUCCESS.increment();
			ccAsnPojo.addClearCode(ClearCodes.MOVE_FILE_FROM_TEMP_TO_ORDER_SUCCESS.getValue(), ClearCodeLevel.SUCCESS);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			break;
		case 1:
			// update metadata
			updateMetaData(session, docIds, productName, orderId);
			// from ftp
			isMoved = moveFileFromTempToOrderFTP(isMoved, orderId, productName, docIds);
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
					"Executing [ Successfully Move from Temp to : " + docIds + orderId + this.getClass().getName() + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

			CounterNameEnum.CNTR_MOVE_FILE_FROM_TEMP_TO_ORDER_SUCCESS.increment();
			ccAsnPojo.addClearCode(ClearCodes.MOVE_FILE_FROM_TEMP_TO_ORDER_SUCCESS.getValue(), ClearCodeLevel.SUCCESS);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			break;
		case 2:
			// minio
			isMoved = moveFileFromTempToOrderMinio(isMoved, orderId, productName, docIds);

			break;
		}

		resp.put("isMoved", isMoved);
		return resp;
	}

	public boolean moveFileFromTempToOrderFTP(boolean isMoved, String orderId, String productName, List<String> docIds)
			throws JSchException, SftpException {
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
		config.put(Constants.STRICTHOSTKEYCONFIGURATION, "no");
		session1.setConfig(config);
		session1.connect();
		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
				Constants.EXECUTING + Constants.HOSTCONNECTED + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();
		channel = session1.openChannel("sftp");
		channel.connect();

		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
				Constants.EXECUTING + Constants.SFTPCHANNELCONNECTEDOPENED + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

		channelSftp = (ChannelSftp) channel;
		for (String docId : docIds) {

			try {
				String srcPath = ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue().replace(".", "")
						+ Constants.DELIMITER + productName + Constants.DELIMITER + "temp" + Constants.DELIMITER
						+ docId;
				String descPath = ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue().replace(".", "")
						+ Constants.DELIMITER + productName + Constants.DELIMITER + orderId + Constants.DELIMITER
						+ docId;

				channelSftp.cd(ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue().replace(".", "") + Constants.DELIMITER
						+ productName);
				try {
					if (channelSftp.lstat(orderId) != null) {
						channelSftp.cd(orderId);
					}
				} catch (Exception e) {
					channelSftp.mkdir(orderId);
					channelSftp.cd(orderId);
				}

				channelSftp.rename(srcPath, descPath);
				isMoved = true;
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeExceptionLog();
			}
		}

		session1.disconnect();
		channelSftp.disconnect();

		return isMoved;
	}

	private boolean moveFileFromTempToOrderHDFS(boolean isMoved, String orderId, String productName,
			List<String> docIds) {
		String srcPath;
		String destPath;
		for (String docId : docIds) {
			srcPath = getFilePath(productName, "temp") + Constants.DELIMITER + docId;
			destPath = getFilePath(productName, orderId);
			try {
				HdfsUtilsService.getInstance().renamePath(srcPath, destPath, docId);
			} catch (IOException e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeExceptionLog();

			}
			isMoved = true;
		}
		return isMoved;
	}

	private boolean moveFileFromTempToOrderMinio(boolean isMoved, String orderId, String productName,
			List<String> docIds) {
		String srcPath;
		String destPath;
		for (String docId : docIds) {
			srcPath = getFilePath(productName, "temp") + Constants.DELIMITER + docId;
			destPath = getFilePath(productName, orderId);
			srcPath = srcPath.substring(1);
			destPath = destPath.substring(1);
			try {

				if (MinioUtilService.getInstance().copyObject(srcPath, destPath + Constants.DELIMITER + docId)) {
					MinioUtilService.getInstance().moveObject(srcPath);
					isMoved = true;
				}
			} catch (IOException e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeExceptionLog();
				isMoved = false;

			}
		}
		return isMoved;
	}

	public Map<String, Boolean> moveFileFromTempToLocationId(String locationId, Map<String, String> docList,
			String productName) throws BaseException {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();
		Map<String, Boolean> resp = new HashMap<>();
		boolean isMoved = false;
		String orderId = locationId;

		try {
			SessionFactory factory = SessionFactory.getSessionFactory();
			Session session = factory.getSession();

			List<String> docIds = new ArrayList<>(docList.values());

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							"Executing [ Doc docIds: " + docIds + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();

			switch (ConfigParamsEnum.FILESYSTEM.getIntValue()) {
			// from hdfs
			case 0:
				updateMetaData(session, docIds, productName, orderId);
				isMoved = tempToOrderByUsingHDFS(productName, isMoved, orderId, docIds);
				break;
			case 1:
				// from ftp
				updateMetaData(session, docIds, productName, orderId);
				isMoved = tempToOrderByUsingFTP(productName, isMoved, orderId, docIds);
				break;
			case 2:
				// from minio
				updateMetaData(session, docIds, productName, orderId);
				moveFileFromTempToOrderMinio(isMoved, orderId, productName, docIds);
				break;

			}
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

			isMoved = false;

			CounterNameEnum.CNTR_MOVE_FILE_FROM_TEMP_TO_LOCATION_ID_FAILURE.increment();
			ccAsnPojo.addClearCode(ClearCodes.MOVE_FILE_FROM_TEMP_TO_LOCATION_ID_FAILURE.getValue(),
					ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			throw new BaseException(HttpStatus.BAD_REQUEST_400, Constants.FILENOTFOUND);
		}

		CounterNameEnum.CNTR_MOVE_FILE_FROM_TEMP_TO_LOCATION_ID_SUCCESS.increment();
		ccAsnPojo.addClearCode(ClearCodes.MOVE_FILE_FROM_TEMP_TO_LOCATION_ID_SUCCESS.getValue(),
				ClearCodeLevel.SUCCESS);
		ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
		resp.put("isMoved", isMoved);
		return resp;
	}

	public void updateMetaDataInFTP(Session session, String docId, String orderId, String descPath)
			throws ElasticSearchException {
		List<FileDocument> documents = new ArrayList<>();

		FileDocument document = fileRepositoryService.getFileDocument(session, docId);
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + document + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		document.setOrderId(orderId);
		document.setFilePath(descPath);
		documents.add(document);

	}

	public boolean tempToOrderByUsingFTP(String productName, boolean isMoved, String orderId, List<String> docIds)
			throws JSchException, SftpException {

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
		config.put(Constants.STRICTHOSTKEYCONFIGURATION, "no");
		session1.setConfig(config);
		session1.connect();
		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
				Constants.EXECUTING + Constants.HOSTCONNECTED + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();
		channel = session1.openChannel("sftp");
		channel.connect();

		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
				Constants.EXECUTING + Constants.SFTPCHANNELCONNECTEDOPENED + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

		channelSftp = (ChannelSftp) channel;
		for (String docId : docIds) {

			try {
				String srcPath = ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue().replace(".", "")
						+ Constants.DELIMITER + productName + Constants.DELIMITER + "temp" + Constants.DELIMITER
						+ docId;
				String descPath = ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue().replace(".", "")
						+ Constants.DELIMITER + productName + Constants.DELIMITER + orderId + Constants.DELIMITER
						+ docId;

				try {
					if (channelSftp.lstat(orderId) != null) {
						channelSftp.cd(orderId);
					}
				} catch (Exception e) {
					channelSftp.mkdir(orderId);
					channelSftp.cd(orderId);
				}

				channelSftp.rename(srcPath, descPath);
				isMoved = true;
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeExceptionLog();
			}
		}

		session1.disconnect();
		channelSftp.disconnect();

		return isMoved;
	}

	public String replaceDocId(String orderId, String temp, String docId) {
		int indexOfIdentifier = docId.indexOf(temp);
		if (indexOfIdentifier != -1) {
			int start = indexOfIdentifier;
			int end = docId.indexOf('_', start);

			if (end != -1) {
				String prefix = docId.substring(0, start);
				String suffix = docId.substring(end);
				return prefix + orderId + suffix;
			}
		}
		return docId;

	}

	public boolean tempToOrderByUsingHDFS(String productName, boolean isMoved, String orderId, List<String> docIds) {
		String srcPath;
		String destPath;
		for (String docId : docIds) {
			srcPath = getFilePath(productName, "temp") + Constants.DELIMITER + docId;
			destPath = getFilePath(productName, orderId);
			try {
				HdfsUtilsService.getInstance().renamePath(srcPath, destPath, docId);
			} catch (IOException e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeExceptionLog();

			}
			isMoved = true;
		}
		return isMoved;
	}

	public void copyEnterpriseDocumentToOrder(String docReferenceId, String productName, Order order) {

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [docReferenceId: " + docReferenceId + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		Session session = SessionFactory.getSessionFactory().getSession();

		try {
			HashMap<String, String> filters = new HashMap<>();
			filters.put(Constants.FILENAME, docReferenceId);
			filters.put(Constants.ORDERID, "temp");
			FileDocument fileDocument = fileRepositoryService.getFileDocument(session, filters);

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							"Executing [fileDocument: " + fileDocument + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();

			FileDocument newDocumentToOrder = (FileDocument) SerializationUtils.clone(fileDocument);
			newDocumentToOrder.setId(null);
			newDocumentToOrder.setOrderId(order.getOrderId());
			String filePath = getFilePath(productName, order.getOrderId()) + Constants.DELIMITER + docReferenceId;
			newDocumentToOrder.setFilePath(filePath);
			newDocumentToOrder.setUploadedOn(new Date());
			newDocumentToOrder.setCustomerId(order.getCompanyDetail().getHqId());

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							"Executing [newDocumentToOrder: " + newDocumentToOrder + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();
			fileRepositoryService.saveDocument(session, newDocumentToOrder);
			HdfsUtilsService.getInstance().copyFilefromOnePathToAnother(fileDocument.getFilePath(), filePath);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

		}
	}

	/**
	 * @param orderId
	 * @return
	 * @throws BaseException
	 * @throws Exception
	 */
	public Map<String, String> getlistoforderdocument(Order orderById, String productName) throws BaseException {

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		if (orderById == null) {

			throw new BaseException(HttpStatus.BAD_REQUEST_400, "INVALID ORDER ID");
		}

		Map<String, String> documentList = new HashMap<>();

		if (orderById.getCompanyDetail().getEnterpriseDocument().getDocumentType() != null) {
			CompanyDetail companyDetail = orderById.getCompanyDetail();

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							"Executing [ref id: " + companyDetail.getEnterpriseDocument().getDocReferenceId()
									+ this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();
			// copy enterprise document to this order
			copyEnterpriseDocumentToOrder(companyDetail.getEnterpriseDocument().getDocReferenceId(), productName,
					orderById);
		}

		if (orderById.getPoiDetails() != null) {
			PoiDetails poiDetails = orderById.getPoiDetails();
			documentList.put(poiDetails.getDocumentType(), poiDetails.getDocReferenceId());
		}

		if (orderById.getPoaDetails() != null) {
			PoaDetails poaDetails = orderById.getPoaDetails();
			documentList.put(poaDetails.getDocumentType(), poaDetails.getDocReferenceId());
		}

		if (orderById.getGstnDetails() != null) {
			GstnDetails gstnDetails = orderById.getGstnDetails();

			if (gstnDetails != null && gstnDetails.getGstnDetail() != null) {

				if (gstnDetails.getGstnDetail().getDocumentType() != null) {
					documentList.put(gstnDetails.getGstnDetail().getDocumentType(),
							gstnDetails.getGstnDetail().getDocReferenceId());

				}

				if (gstnDetails.getGstnExemption() != null
						&& gstnDetails.getGstnExemption().getDocumentType() != null) {

					documentList.put(gstnDetails.getGstnExemption().getDocumentType(),
							gstnDetails.getGstnExemption().getDocReferenceId());

				}
			}
		}

		if (orderById.getUolDetails() != null) {
			Document uolDetails = orderById.getUolDetails();
			documentList.put(uolDetails.getDocumentType(), uolDetails.getDocReferenceId());

		}

		if (orderById.getOtherDocuments() != null) {

			for (Document doc : orderById.getOtherDocuments()) {
				if (doc.getDocumentType() != null) {

					documentList.put(doc.getDocumentType(), doc.getDocReferenceId());
				}
			}

		}

		return documentList;

	}

	public void updateMetaData(Session session, List<String> docIds, String productName, String orderId) {
		List<FileDocument> documents = new ArrayList<>();
		for (String fileName : docIds) {
			FileDocument document = null;
			try {
				document = fileRepositoryService.getFileDocument(session, fileName);
				DappLoggerService.GENERAL_INFO_LOG
						.getLogBuilder(
								Constants.EXECUTING + document + this.getClass().getName() + "."
										+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeLog();
				document.setOrderId(orderId);
				String filePath = getFilePath(productName, orderId) + Constants.DELIMITER + fileName;
				document.setFilePath(filePath);
				documents.add(document);

				DappLoggerService.GENERAL_INFO_LOG
						.getLogBuilder(
								Constants.EXECUTING + document + this.getClass().getName() + "."
										+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeLog();

			} catch (Exception e) {

				DappLoggerService.GENERAL_ERROR_FAILURE_LOG
						.getExceptionLogBuilder(e, e.getMessage() + fileName + document, this.getClass().getName(),
								Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeExceptionLog();

			}
		}
		try {

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(
							Constants.EXECUTING + documents + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();
			fileRepositoryService.updateFiledocuments(session, documents);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getLogBuilder(
							Constants.EXECUTING + documents + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getLogBuilder(
							Constants.EXECUTING + documents + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();
		}

	}

	public String getFilePath(String productName, String orderId) {
		if (productName != null && orderId != null) {
			return ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue() + Constants.DELIMITER + productName
					+ Constants.DELIMITER + orderId;
		} else {
			return ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue() + Constants.DELIMITER + orderId;
		}

	}

	public String getFtpFilePath(String productName, String orderId) {
		return ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue().replace(".", "") + Constants.DELIMITER + productName
				+ Constants.DELIMITER + orderId;
	}

	// this function card Type Column values is same or not in each row
	public boolean validateCardType(List<String[]> rows, String column) {
		List<String> rowdata = Arrays.asList(rows.get(0));
		int index = rowdata.indexOf(column);

		rowdata = Arrays.asList(rows.get(1));
		String firstColumnValue = rowdata.get(index);
		for (int i = 2; i < rows.size(); i++) {
			if (!rows.get(i)[index].equals(firstColumnValue)) {
				return false;
			}
		}
		return true;
	}

	public FileDocument getDocumentObjetfromRequestParams(HttpServletRequest req) throws BaseException {
		FileDocument document = new FileDocument();

		Enumeration<String> params = req.getParameterNames();
		while (params.hasMoreElements()) {
			String value = params.nextElement();
			Field field;
			try {
				field = FileDocument.class.getDeclaredField(value);
				// Set the accessibility as true
				field.setAccessible(true);
				if (value.equals("cosSpecific")) {
					field.setBoolean(document, Boolean.parseBoolean(req.getParameter(value)));
				} else {
					field.set(document, req.getParameter(value));
				}
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1) {
				// logger
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG
						.getLogBuilder(
								"Executing [ Not Required this Param::  " + value + this.getClass().getName() + "."
										+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeLog();

			}
		}

		return document;

	}

	public BaseResponse<DocumentCategory> fetchDocumentCategory() {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();
		SessionFactory factory = SessionFactory.getSessionFactory();
		Session session = factory.getSession();
		BaseResponse<DocumentCategory> resp = null;
		try {
			DocumentCategory data = fileRepositoryService.getDocumentCategory(session);
			resp = new BaseResponse<>(data);
		} catch (Exception e) {
			CounterNameEnum.CNTR_FETCH_DOCUMENT_CATEGORY_FAILURE.increment();
			ccAsnPojo.addClearCode(ClearCodes.FETCH_DOCUMENT_CATEGORY_FAILURE.getValue(), ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();

		}
		ccAsnPojo.addClearCode(ClearCodes.FETCH_DOCUMENT_CATEGORY_SUCCESS.getValue(), ClearCodeLevel.SUCCESS);
		ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
		CounterNameEnum.CNTR_FETCH_DOCUMENT_CATEGORY_SUCCESS.increment();
		return resp;
	}

	public static void pushDocumentCategory() throws IOException {
		InputStream inputStream = null;
		try {
			SessionFactory factory = SessionFactory.getSessionFactory();
			Session session = factory.getSession();
			String profile = System.getProperty(Constants.PROFILE);
			if (profile != null && profile.equals("dev")) {
				inputStream = new FileInputStream("./configuration/documentCategory.yml");
			} else {
				inputStream = new FileInputStream("../configuration/documentCategory.yml");
			}
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			mapper.findAndRegisterModules();
			DocumentCategory documentCategory = mapper.readValue(inputStream, DocumentCategory.class);
			documentCategory.setId("12345");
			FileUploadRepository.pushDocumentCategory(session, documentCategory);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
						.writeExceptionLog();
			}

		}

	}

	public boolean deleteFromDatabase(String fileName) throws BaseException {
		SessionFactory factory = SessionFactory.getSessionFactory();
		Session session = factory.getSession();
		try {
			FileDocument fileDocument = fileRepositoryService.getFileDocument(session, fileName);

			fileRepositoryService.deleteFileDocument(session, fileDocument);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
			throw new BaseException(HttpStatus.BAD_REQUEST_400, Constants.FILENOTFOUND);
		}
		return true;
	}

	public BaseResponse<Map<String, String>> changeFilePathToDeactivatedFolder(String customerId)
			throws ElasticSearchException, IOException, BaseException, JSchException, SftpException {
		SessionFactory factory = SessionFactory.getSessionFactory();
		Session session = factory.getSession();
		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();
		SearchResult<FileDocument> document = fileRepositoryService.getFileDataOfCustomer(session, customerId);
		if (document != null) {
			for (FileDocument documents : document.getResult()) {
				if (documents.getDocumentCategory() != null
						&& documents.getDocumentCategory().equalsIgnoreCase("Electronically generated CAF")) {
					String json1 = ObjectMapperHelper.getInstance().getEntityObjectMapper()
							.writeValueAsString(documents);
					DeactivedFileDocument fileDeactivated = RequestToBeanMapper.getBeanFromString(json1,
							DeactivedFileDocument.class);
					String srcPath = documents.getFilePath();
					String productName = documents.getProductName();
					String fileName = documents.getFileName();

					// hdfs
					switch (ConfigParamsEnum.FILESYSTEM.getIntValue()) {
					case 0:
						deactivatedFolderThroughHDFS(session, ccAsnPojo, documents, fileDeactivated, srcPath,
								productName, fileName);
						break;
					case 1:

						if (srcPath.startsWith("./")) {
							srcPath = srcPath.substring(1);
						}
						deactivatedFolderThroughFTP(session, ccAsnPojo, documents, fileDeactivated, srcPath, fileName,
								productName);
						break;
					case 2:
						deactivatedFolderThroughMINIO(session, ccAsnPojo, documents, fileDeactivated, srcPath,
								productName, fileName);
						break;

					}

				}
			}

		}
		return null;
	}

	public void deactivatedFolderThroughFTP(Session session, ClearCodeAsnPojo ccAsnPojo, FileDocument documents,
			DeactivedFileDocument fileDeactivated, String srcPath, String fileName, String productName)
			throws JSchException, SftpException {
		String deactivatedFilePathThroughFTP = getDeactivatedFilePathThroughFTP(fileName, productName);
		fileDeactivated.setFilePath(deactivatedFilePathThroughFTP);

		try {
			fileRepositoryService.deleteFileDocument(session, documents);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}
		try {
			fileRepositoryService.saveDeactivatedFileDocument(session, fileDeactivated);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}

		// to save the file document in purging folder
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
		config.put(Constants.STRICTHOSTKEYCONFIGURATION, "no");
		session1.setConfig(config);
		session1.connect();
		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
				Constants.EXECUTING + Constants.HOSTCONNECTED + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();
		channel = session1.openChannel("sftp");
		channel.connect();

		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
				Constants.EXECUTING + Constants.SFTPCHANNELCONNECTEDOPENED + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				"", Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

		channelSftp = (ChannelSftp) channel;
		channelSftp.cd(ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue().replace(".", ""));

		try {
			if (channelSftp.lstat(Constants.PURGING) != null) {
				channelSftp.cd(Constants.PURGING);
			}
		} catch (Exception e) {
			channelSftp.mkdir(Constants.PURGING);
			channelSftp.cd(Constants.PURGING);
		}

		try {

			if (channelSftp.lstat(productName) != null) {
				channelSftp.cd(productName);
			}
		} catch (Exception e) {
			channelSftp.mkdir(productName);
			channelSftp.cd(productName);
		}
		try {
			channelSftp.rename(srcPath, deactivatedFilePathThroughFTP);

		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		} finally {

			session1.disconnect();
			channelSftp.disconnect();
		}
		CounterNameEnum.CNTR_CHANGE_FILE_DOCUMENT_TO_DEACTIVATED_FOLDER_SUCCESS.increment();
		ccAsnPojo.addClearCode(ClearCodes.CNTR_CHANGE_FILE_DOCUMENT_TO_DEACTIVATED_FOLDER_SUCCESS.getValue(),
				ClearCodeLevel.SUCCESS);
		ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
	}

	public String getDeactivatedFilePathThroughFTP(String fileName, String productName) {
		return ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue().replace(".", "") + Constants.DELIMITER + "Purging"
				+ Constants.DELIMITER + productName + Constants.DELIMITER + fileName;
	}

	public void deactivatedFolderThroughHDFS(Session session, final ClearCodeAsnPojo ccAsnPojo, FileDocument documents,
			DeactivedFileDocument fileDeactivated, String srcPath, String productName, String fileName)
			throws IOException {
		String destPath = getDeactivatedFilePath(productName, fileName);

		fileDeactivated.setFilePath(destPath);

		try {
			fileRepositoryService.deleteFileDocument(session, documents);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}
		try {
			fileRepositoryService.saveDeactivatedFileDocument(session, fileDeactivated);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}

		// to save the file document in purging folder

		HdfsUtilsService.getInstance().renamePath(srcPath, destPath, documents.getFileName());

		CounterNameEnum.CNTR_CHANGE_FILE_DOCUMENT_TO_DEACTIVATED_FOLDER_SUCCESS.increment();
		ccAsnPojo.addClearCode(ClearCodes.CNTR_CHANGE_FILE_DOCUMENT_TO_DEACTIVATED_FOLDER_SUCCESS.getValue(),
				ClearCodeLevel.SUCCESS);
		ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
	}

	public void deactivatedFolderThroughMINIO(Session session, final ClearCodeAsnPojo ccAsnPojo, FileDocument documents,
			DeactivedFileDocument fileDeactivated, String srcPath, String productName, String fileName)
			throws IOException {
		String destPath = getDeactivatedFilePath(productName, fileName);

		fileDeactivated.setFilePath(destPath);

		try {
			fileRepositoryService.deleteFileDocument(session, documents);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}
		try {
			fileRepositoryService.saveDeactivatedFileDocument(session, fileDeactivated);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}

		// to save the file document in purging folder

		if (MinioUtilService.getInstance().copyObject(srcPath, destPath.replace(".", "/"))) {
			MinioUtilService.getInstance().moveObject(srcPath);
		}
		CounterNameEnum.CNTR_CHANGE_FILE_DOCUMENT_TO_DEACTIVATED_FOLDER_SUCCESS.increment();
		ccAsnPojo.addClearCode(ClearCodes.CNTR_CHANGE_FILE_DOCUMENT_TO_DEACTIVATED_FOLDER_SUCCESS.getValue(),
				ClearCodeLevel.SUCCESS);
		ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
	}

	private String getDeactivatedFilePath(String productName, String fileName) {
		return ConfigParamsEnum.FILE_UPLOAD_DIR.getStringValue() + Constants.DELIMITER + "Purging" + Constants.DELIMITER
				+ productName + Constants.DELIMITER + fileName;

	}

	public BaseResponse<Map<String, String>> uploadbasedondocumentcategory(HttpServletRequest req, String uiFileName)
			throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();

		BaseResponse<Map<String, String>> baseResponse = uploadfile(req, uiFileName);

		CounterNameEnum.CNTR_UPLOAD_FILE_BY_DOCUMENT_CATEGORY_SUCCESS.increment();
		ccAsnPojo.addClearCode(ClearCodes.UPLOAD_FILE_SUCCESS.getValue(), ClearCodeLevel.SUCCESS);
		ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);

		return baseResponse;
	}

	public boolean checkForDocumentCategory(FileDocument document) throws ElasticSearchException {
		SessionFactory factory = SessionFactory.getSessionFactory();
		Session session = factory.getSession();
		session.beginTransaction();
		Map<String, String> filters = new HashMap<>();
		filters.put("id", "12345");
		DocumentCategory documentCategory = session.getObject(DocumentCategory.class, filters);
		List<String> documentCategoryList = documentCategory.getDocumentCategory();

		for (String doc : documentCategoryList) {
			if (doc.equalsIgnoreCase(document.getDocumentCategory())) {
				return true;
			}

		}
		return false;
	}

	public List<Object> getFileList(String orderId, String cafNumber)
			throws IOException, BaseException, ElasticSearchException {
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();

		// check for order id
		if (orderId == null || orderId.isEmpty()) {
			orderId = "temp";
		}

		try {
			SearchResult<FileDocument> fileList = getFileListBasedOnOrderIdAndCafNumber(orderId, cafNumber);
			List<Object> listValues = new ArrayList<>();
			Map<String, String> mapValues = new HashMap<>();

			for (FileDocument document : fileList.getResult()) {
				String documentCategory = document.getDocumentCategory();
				String filePaths = document.getFilePath();
				mapValues.put(documentCategory, filePaths);

			}
			listValues.add(mapValues);
			CounterNameEnum.CNTR_GET_FILE_BY_DOCUMENT_TYPE_SUCCESS.increment();
			ccAsnPojo.addClearCode(ClearCodes.GET_FILE_BY_DOCUMENT_TYPE_SUCCESS.getValue(), ClearCodeLevel.SUCCESS);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			return listValues;
		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, "Error in getting file list",
					this.getClass().getName(), "getFile");
			CounterNameEnum.CNTR_GET_FILE_BY_DOCUMENT_TYPE_FAILURE.increment();
			ccAsnPojo.addClearCode(ClearCodes.GET_FILE_BY_DOCUMENT_TYPE_FALIURE.getValue(), ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);

		}
		return new ArrayList<>();

	}

	private SearchResult<FileDocument> getFileListBasedOnOrderIdAndCafNumber(String orderId, String cafNumber)
			throws ElasticSearchException {
		SessionFactory factory = SessionFactory.getSessionFactory();
		Session session = factory.getSession();
		Map<String, String> filters = new HashMap<>();
		if (orderId != null)
			filters.put(Constants.ORDERID, orderId);
		if (cafNumber != null)
			filters.put(Constants.CAFNUMBER, cafNumber);
		return session.get(FileDocument.class, filters);

	}

	@SuppressWarnings("unused")
	public BaseResponse<Map<String, String>> uploadBasedOnMandatory(HttpServletRequest req, String uiFileName,
			String protocol) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();

		FileDocument document = getDocumentObjetfromRequestParams(req);
		if (document.getOrderId() == null || document.getOrderId().isEmpty()) {
			throw new BaseException(HttpStatus.BAD_REQUEST_400,
					"orderId is mandatory parameter, please send in query params");
		}
		Map<String, String> fileObject = new HashMap<>();
		String orderId = document.getOrderId();
		String externalId = document.getExternalId();
		String filepath = null;
		String filePathUrl = null;

		String finalUrl = null;
		String ftpfilePathUrl = null;
		BaseResponse<Map<String, String>> baseResponse = new BaseResponse<>();

		if (document.getDocumentCategory() == null || document.getDocumentCategory().isEmpty()) {
			throw new BaseException(400, Constants.DOCUMENTCATEGORYVALUE);
		}
		document.setDocumentCategory(document.getDocumentCategory());
		byte[] byteArray = IOUtils.toByteArray(req.getInputStream());

		if (byteArray.length == 0) {

			throw new BaseException(HttpStatus.BAD_REQUEST_400, Constants.FILEZEROBYTESENDCORRECTFILE);
		}
		SessionFactory factory = SessionFactory.getSessionFactory();
		Session session = factory.getSession();

		// get file extensions
		String extension = FilenameUtils.getExtension(uiFileName);

		// get base file Name
		String fileNameTemp = FilenameUtils.getBaseName(uiFileName);
		// generate UUID file name
		Date date = new Date();

		String file = fileNameTemp + "_" + ElasticUUIDGenerator.getInstance().getUniqueUUID();
		String fileName = file + "." + extension;
		String uploadFilePath = getFilePath(document.getProductName(), orderId);
		// String ftpUploadFilePath = getFTPFilePaths(orderId);
		filepath = uploadFilePath + Constants.DELIMITER + fileName;
		if (protocol != null && protocol.equalsIgnoreCase("http")) {

			// DMS info
			String elbInfo = null;
			String profile = System.getProperty(Constants.PROFILE);
			if (profile == null || !profile.equals("dev")) {
				elbInfo = ServiceCommunicator.elbDmsInfo();

			}

			finalUrl = elbInfo + Constants.ELBDMS;

			filePathUrl = finalUrl + "/file/downloadByUrl" + uploadFilePath.replace(".", "") + Constants.DELIMITER
					+ fileName;

		} else {

			// secure

			String elbInfo = null;
			String profile = System.getProperty(Constants.PROFILE);
			if (profile == null || !profile.equals("dev")) {
				elbInfo = ServiceCommunicator.gethttpsDMSELBInfo();

			}
			finalUrl = elbInfo + Constants.ELBDMS;

			filePathUrl = finalUrl + "/file/downloadByUrl" + uploadFilePath.replace(".", "") + Constants.DELIMITER
					+ fileName;
		}

		switch (ConfigParamsEnum.FILESYSTEM.getIntValue()) {
		case 0:

			if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")
					|| extension.equalsIgnoreCase("png")) {
				Configuration conf = HdfsConfigurationManager.getConfiguration();

				FileSystem fs = FileSystem.get(conf);
				uiFileName = fileNameTemp + ".pdf";
				fileName = file + ".pdf";
				filepath = uploadFilePath + Constants.DELIMITER + fileName;
				FileToPdfConverter.getIndtance().convertImageToPdf(byteArray, filepath, fs);

			} else {
				hdfsFileUpload(filepath, fileName, ccAsnPojo, byteArray);
			}

			break;
		case 1:
			if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")
					|| extension.equalsIgnoreCase("png")) {

				uiFileName = fileNameTemp + ".pdf";
				fileName = file + ".pdf";
				filepath = uploadFilePath + Constants.DELIMITER + fileName;
				filePathUrl = finalUrl + "/file/downloadByUrl" + uploadFilePath.replace(".", "") + Constants.DELIMITER
						+ fileName;
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				com.itextpdf.text.Document doc = new com.itextpdf.text.Document();

				PdfWriter.getInstance(doc, byteArrayOutputStream);
				doc.open();
				Image image = Image.getInstance(byteArray);
				doc.add(image);
				doc.close();
				byteArray = byteArrayOutputStream.toByteArray();

			}
			// ftp
			ftpFileUpload(document, uiFileName, fileName, ccAsnPojo, fileObject, byteArray);

			break;
		case 2:

			// minioUtilService.minioFileUploadBYURL(filePathUrl, fileName, byteArray);
			// uploadFilePath = uploadFilePath.replace(".", "");
			// filepath = uploadFilePath + Constants.DELIMITER + fileName;
			MinioUtilService.getInstance().uploadFile(uploadFilePath.replace(".", "") + Constants.DELIMITER + fileName,
					byteArray, fileName, "application/json", -1);
			break;

		}
		document.setActualFileName(uiFileName);
		document.setFileName(fileName);
		document.setFilePath(filepath);
		document.setExternalId(externalId);
		double size = (double) (byteArray.length) / 1024;
		document.setSize(String.format("%.2f", size));
		document.setUploadedOn(date);
		document.setFilePathUrl(filePathUrl);

		session.beginTransaction();
		fileRepositoryService.saveDocument(session, document);
		session.flush();

		fileObject.put(Constants.ORDERID, orderId);
		fileObject.put("filePathUrl", filePathUrl);
		fileObject.put("externalId", externalId);
		baseResponse = new BaseResponse<>(fileObject);
		CounterNameEnum.CNTR_FILE_UPLOAD_SUCCESS.increment();
		ccAsnPojo.addClearCode(ClearCodes.FILE_UPLOAD_SUCCESS.getValue(), ClearCodeLevel.SUCCESS);
		ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
		return baseResponse;

	}

	public void getFileByUrl(HttpServletRequest req, HttpServletResponse response)
			throws IOException, BaseException, InvalidKeyException, ErrorResponseException, InsufficientDataException,
			InternalException, InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException,
			IllegalArgumentException, ElasticSearchException {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();

		String fileDownloadName = req.getParameter("actualFileName");
		String pathInfo = req.getPathInfo();
		String s[] = pathInfo.split("/");
		String fileName = null;
		fileName = s[s.length - 1];
		switch (ConfigParamsEnum.FILESYSTEM.getIntValue()) {
		case 0:
			String info = pathInfo.substring(pathInfo.indexOf("/CRM_DMS"));
			fileName = info.substring(info.lastIndexOf("/") + 1);
			String pathHDFS = pathInfo.replace("downloadByUrl/", "");
			getFileFromHDFS(response, fileName, "./" + pathHDFS, fileName, ccAsnPojo);
			break;
		case 1:
			String info1 = pathInfo.substring(pathInfo.indexOf("/CRM_DMS"));
			fileName = info1.substring(info1.lastIndexOf("/") + 1);
			getFileByURLByUsingFTP(req, response, ccAsnPojo, fileName);
			break;
		case 2:
			if (!pathInfo.startsWith("/")) {
				pathInfo = "/" + pathInfo;
			}
			// Remove the "downloadByUrl/" prefix
			String pathMinio = pathInfo.replace("downloadByUrl/", "");

			MinioUtilService.getInstance().getFile(response, fileName, pathMinio, fileDownloadName);

			break;
		}
	}

	public void getFileByURLByUsingFTP(HttpServletRequest req, HttpServletResponse response, ClearCodeAsnPojo ccAsnPojo,
			String fileName) throws BaseException, ElasticSearchException {

		Session session = SessionFactory.getSessionFactory().getSession();
		Map<String, String> filters = new HashMap<>();

		filters.put(Constants.FILENAME, fileName);

		FileDocument document = fileRepositoryService.getFileDocument(session, filters);

		String SFTPHOST = ConfigParamsEnum.UPLOADSFTPHOST.getStringValue();
		int SFTPPORT = ConfigParamsEnum.UPLOADSFTPPORT.getIntValue();
		String SFTPUSER = ConfigParamsEnum.UPLOADSFTPUSER.getStringValue();
		String SFTPPASS = ConfigParamsEnum.UPLOADSFTPPASS.getStringValue();

		JSch jsch = new JSch();
		try {
			callToFTPForDownloadFile(SFTPHOST, SFTPPORT, SFTPUSER, SFTPPASS, document, jsch, response);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, Constants.ERRORINUPLOADFILE,
					this.getClass().getName(), Constants.UPLOADFILE);
			CounterNameEnum.CNTR_UPLOAD_FILE_FAILURE.increment();
			ccAsnPojo.addClearCode(ClearCodes.UPLOAD_FILE_FAILURE.getValue(), ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			throw new BaseException(HttpStatus.BAD_REQUEST_400, e.getMessage());
		}

	}

	public void getFileByURLByUsingHDFS(HttpServletRequest req, HttpServletResponse response,
			final ClearCodeAsnPojo ccAsnPojo) throws IOException, BaseException {
		// getting file from HDFS
		Configuration conf = HdfsConfigurationManager.getConfiguration();
		FileSystem fileSystem = FileSystem.get(conf);
		String pathInfo = req.getPathInfo();
		String info = pathInfo.substring(pathInfo.indexOf("/CRM_DMS"));
		String fileName = info.replace("/CRM_DMS", "./CRM_DMS");

		Path path = new Path(fileName);
		if (fileSystem.exists(path)) {

			try (FSDataInputStream in = fileSystem.open(path)) {
				long length = fileSystem.getFileStatus(path).getLen();
				String extension = FilenameUtils.getExtension(fileName);
				String contentType = MimeTypes.getMimeType(extension);
				response.setContentType(contentType);

				response.setHeader(CONTENT_DISPOSIOTION, CONTENT_ATTACHMENT + fileName + "\"");
				response.setContentLength((int) length);

				OutputStream os = response.getOutputStream();

				try {

					byte[] buffer = new byte[1024];
					int numBytes = 0;
					while ((numBytes = in.read(buffer)) > 0) {
						os.write(buffer, 0, numBytes);
					}

				} finally {
					os.flush();
					os.close();

					fileSystem.close();
				}
				ccAsnPojo.addClearCode(ClearCodes.GET_FILE_BY_URL_SUCCESS.getValue(), ClearCodeLevel.SUCCESS);
				ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
				CounterNameEnum.CNTR_GET_FILE_BY_URL_SUCCESS.increment();
			}
		} else {
			CounterNameEnum.CNTR_GET_FILE_BY_URL_FAILURE.increment();
			ccAsnPojo.addClearCode(ClearCodes.GET_FILE_BY_URL_FAILURE.getValue(), ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			throw new BaseException(HttpStatus.BAD_REQUEST_400, Constants.FILENOTFOUND);

		}
	}

	public void archivalfiles(HttpServletRequest req, ArchivalBean beanFromRequest)
			throws ElasticSearchException, IOException, BaseException, InterruptedException {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();

		SessionFactory sessionFactory = SessionFactory.getSessionFactory();
		Session session = sessionFactory.getSession();

		String orderId = beanFromRequest.getOrderId();
		String cafNumber = beanFromRequest.getCafNumber();

		List<FileDocument> fileDocumnets = this.fileRepositoryService.archivalFiles(session, orderId, null);

		OrderFWA order = session.getObject(OrderFWA.class, "orderId.keyword", orderId);
		List<String> documentType = new ArrayList<>();

		String createCsvFile = null;

		for (FileDocument document : fileDocumnets) {
			documentType.add(document.getDocumentCategory());
		}

		for (DocumentArchivalConfiguration archivalBean : beanFromRequest.getDocumentArchivalConfiguration()) {

			List<ArchivalMetaData> listArchival = new ArrayList<>();

			// remove duplicates of document category
			List<String> removeDuplicatesOfCategory = removeDuplicatesOfCategory(archivalBean.getArchivalDocumentType(),
					documentType);

			List<FileDocument> fileDocument = new ArrayList<>();
			for (String category : removeDuplicatesOfCategory) {
				for (FileDocument document : fileDocumnets) {
					if (document.getDocumentCategory().equalsIgnoreCase(category)) {
						fileDocument.add(document);
					}
				}
			}

			String system = archivalBean.getSystem();
			String type = archivalBean.getType();

			for (String documentTypes : removeDuplicatesOfCategory) {
				ArchivalMetaData archivalMetaData = new ArchivalMetaData();
				archivalMetaData.setDocumentType(documentTypes);
				archivalMetaData.setStatus(ArchivalStatus.A);
				archivalMetaData.setArchivalSystem(system);
				archivalMetaData.setCafNumber(cafNumber);
				archivalMetaData.setType(type);
				archivalMetaData.setCustomerId(order.getCustomerId());
				archivalMetaData.setUpdatedOn(new Date().toString());
				archivalMetaData.setOrderId(orderId);
				listArchival.add(archivalMetaData);
			}
			try {
				fileRepositoryService.bulksave(listArchival, session);
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeExceptionLog();
			}

			String fetchFilesIndividually = toFetchFilesIndividually(fileDocument, cafNumber, orderId);

			if (type.equalsIgnoreCase("FTP")) {

				Thread.sleep(10000);

				List<ArchivalMetaData> archivalMetaData = fileRepositoryService
						.getArchivalMetaData(order.getCustomerId(), session).stream()
						.filter(s -> s.getStatus().equals(ArchivalStatus.A)).collect(Collectors.toList());

				DappLoggerService.GENERAL_INFO_LOG
						.getLogBuilder(
								order.getCustomerId() + this.getClass().getName() + "."
										+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeLog();
				DappLoggerService.GENERAL_INFO_LOG
						.getLogBuilder(
								archivalMetaData + this.getClass().getName() + "."
										+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeLog();

				// csv file
				createCsvFile = createCsvFile(archivalMetaData, orderId);

				// sending into local server via ftp
				if (sendFileToLocalServer(fetchFilesIndividually, createCsvFile)) {

					for (ArchivalMetaData data : archivalMetaData) {
						data.setStatus(ArchivalStatus.U);
						data.setPath(ConfigParamsEnum.SFTPWORKINGDIR.getStringValue());
						try {
							session.merge(data);
						} catch (Exception e) {
							DappLoggerService.GENERAL_ERROR_FAILURE_LOG
									.getExceptionLogBuilder(e, e.getMessage(), this.getClass().getName(),
											Thread.currentThread().getStackTrace()[1].getMethodName())
									.writeExceptionLog();
						}
					}

					// delete single files locally
					File orderNumberFile = new File(fetchFilesIndividually);
					File parentFile = orderNumberFile.getParentFile();
					File deleteFile = new File(parentFile.getAbsolutePath());
					FileUtils.deleteDirectory(deleteFile);

					if (orderNumberFile.delete()) {
						DappLoggerService.GENERAL_INFO_LOG
								.getLogBuilder(Constants.FILEGOTDELETEDSUCCESSFULLY + orderNumberFile.getAbsolutePath(),
										this.getClass().getName(),
										Thread.currentThread().getStackTrace()[1].getMethodName())
								.writeLog();

					}

					if (parentFile.delete()) {
						DappLoggerService.GENERAL_INFO_LOG
								.getLogBuilder(Constants.FILEGOTDELETEDSUCCESSFULLY + parentFile.getAbsolutePath(),
										this.getClass().getName(),
										Thread.currentThread().getStackTrace()[1].getMethodName())
								.writeLog();

					}

					orderNumberFile.deleteOnExit();

					parentFile.deleteOnExit();
					CounterNameEnum.CNTR_ARCHIVAL_FILES_VIA_FTP_SUCCESS.increment();
					ccAsnPojo.addClearCode(ClearCodes.ARCHIVAL_FILES_VIA_FTP_SUCCESS.getValue(),
							ClearCodeLevel.SUCCESS);
					ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
					DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
							"Executing [Successfully sent file to ftp and updated in db " + this.getClass().getName()
									+ "." + Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
							.writeLog();
				} else {
					DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getLogBuilder(
							"Executing [Failure while sending file to ftp and updated in db "
									+ this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
							.writeLog();
					CounterNameEnum.CNTR_ARCHIVAL_FILES_VIA_FTP_FAILURE.increment();
					ccAsnPojo.addClearCode(ClearCodes.ARCHIVAL_FILES_VIA_FTP_FAILURE.getValue(),
							ClearCodeLevel.SUCCESS);
					ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
					throw new BaseException(400, "Files Not Transferred Successfully TO FTP");

				}

			} else if (type.equalsIgnoreCase("NAS")) {

				// List<ArchivalMetaData> archivalMetaData =
				// fileRepositoryService.getArchivalMetaData(orderId, session)
				// .stream().filter(s ->
				// s.getStatus().equals(ArchivalStatus.A)).collect(Collectors.toList());
				//
				// // csv file
				// createCsvFile = createCsvFile(archivalMetaData, orderId);
				//
				//// // sending to system via path
				//// if (sendFileToNASLocation(fetchFilesIndividually, createCsvFile)) {
				//// for (ArchivalMetaData data : archivalMetaData) {
				//// data.setStatus(ArchivalStatus.U);
				//// data.setPath(ConfigParamsEnum.NAS_LOCATION.getStringValue());
				//// session.merge(data);
				// }
				//
				// // delete single files locally
				// File orderNumberFile = new File(fetchFilesIndividually);
				// File parentFile = orderNumberFile.getParentFile();
				// orderNumberFile.delete();
				// orderNumberFile.deleteOnExit();
				// parentFile.delete();
				// parentFile.deleteOnExit();
				// }
				// } else {
				// throw new BaseException(400, "Files Not Transferred Successfully To NAS
				// Location");
				// }

			}

		}

	}

	public String toFetchFilesIndividually(List<FileDocument> fileDocument, String cafNumber, String orderId)
			throws IOException, BaseException {

		List<InputStream> list = new ArrayList<>();
		for (FileDocument fileDocumnent : fileDocument) {
			String productName = fileDocumnent.getProductName();
			String fileName = fileDocumnent.getFileName();
			String id = fileDocumnent.getOrderId();

			InputStream archivalFile = getArchivalFile(fileName, productName, id);

			list.add(archivalFile);
		}

		String path = ArchivalUtility.getInstance().getArchivalPath(orderId);

		for (InputStream stream : list) {
			String filePathAfterMergingPdf = path + ElasticUUIDGenerator.getInstance().getUniqueUUID() + ".pdf";
			FileUtils.copyInputStreamToFile(stream, new File(filePathAfterMergingPdf));
		}

		return path;

	}

	public List<String> removeDuplicatesOfCategory(List<String> archivalDocumentType, List<String> documentType) {

		Set<String> intersect = new HashSet<>(archivalDocumentType);
		intersect.retainAll(documentType);

		return new ArrayList<>(intersect);

	}

	public String createCsvFile(List<ArchivalMetaData> collect, String orderId) throws IOException {

		String path;
		String profile = System.getProperty(Constants.PROFILE);
		if (profile != null && profile.equals("dev")) {
			path = "./configuration/" + orderId + ".csv";
		} else {
			path = "../configuration/" + orderId + ".csv";
		}

		FileWriter writer = new FileWriter(path);
		try {

			// Writing header
			writer.append("cafNumber,type,timeStamp,documentType,customerId,archivalSystem,status\n");

			// Writing data rows
			for (ArchivalMetaData data : collect) {
				writer.append(data.getCafNumber()).append(",").append(data.getType()).append(",")
						.append(data.getUpdatedOn()).append(",").append(data.getDocumentType()).append(",")
						.append(data.getCustomerId()).append(",").append(data.getArchivalSystem()).append(",")
						.append(data.getStatus().name()).append("\n");
			}
			writer.flush();
			writer.close();

			return path;
		} catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		} finally {
			writer.close();
		}

		return null;

	}

	public File getFilesFromDmsThroughCafAndOrderId(List<FileDocument> fileDocumnets, String cafNumber, String orderId)
			throws IOException, BaseException, DocumentException {

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + "] ",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		List<InputStream> list = new ArrayList<>();
		for (FileDocument fileDocumnent : fileDocumnets) {
			String productName = fileDocumnent.getProductName();

			String fileName = fileDocumnent.getFileName();

			String id = fileDocumnent.getOrderId();

			InputStream archivalFile = getArchivalFile(fileName, productName, id);

			list.add(archivalFile);
		}

		String filePathAfterMergingPdf = "ArchivalDoument.pdf";

		// code to merge pdfs into single pdf
		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder("Merging all pdfs into single pdf " + orderId,
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

		mergePdfFile(list, filePathAfterMergingPdf);

		// to send data to the respective platforms
		return new File(filePathAfterMergingPdf);

	}

	private boolean sendFileToLocalServer(String filePathAfterMergingPdf, String createCsvFile) {

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [ Sending file Via FTP" + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		String SFTPHOST = ConfigParamsEnum.SFTPHOST.getStringValue();
		int SFTPPORT = ConfigParamsEnum.SFTPPORT.getIntValue();
		String SFTPUSER = ConfigParamsEnum.SFTPUSER.getStringValue();
		String SFTPPASS = ConfigParamsEnum.SFTPPASS.getStringValue();

		JSch jsch = new JSch();

		com.jcraft.jsch.Session session = null;
		try {
			session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);

			session.setPassword(SFTPPASS);

			// FTPUtil.uploadDirectory(filePathAfterMergingPdf, SFTPWORKINGDIR, session,
			// createCsvFile);
			return true;

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}
		return false;
	}

	private void mergePdfFile(List<InputStream> list, String path) throws IOException, DocumentException {
		try {
			com.itextpdf.text.Document document = new com.itextpdf.text.Document();
			PdfCopy copy = new PdfCopy(document, new FileOutputStream(path));
			document.open();
			for (InputStream inputStream : list) {
				PdfReader reader = new PdfReader(inputStream);
				int numPages = reader.getNumberOfPages();
				for (int pageNum = 1; pageNum <= numPages; pageNum++) {
					document.newPage();
					PdfImportedPage importedPage = copy.getImportedPage(reader, pageNum);
					copy.addPage(importedPage);
				}
				reader.close();
			}
			document.close();
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder("Merging all pdfs into single pdf completed Successfully",
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getLogBuilder("Merging all pdfs into single pdf not done Successfully", this.getClass().getName(),
							Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();
		}
	}

	/*
	 * Reload document category
	 */
	public void reloadDocumentCategory() throws IOException {
		pushDocumentCategory();
	}

	public void responsefromarchivaldocument(HttpServletRequest req, String csvData) throws BaseException, Exception {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		Map<String, String> mapOfDocumentCategoryAndStatus = mapOfDocumentCategoryAndStatus(csvData);
		SessionFactory sessionFactory = SessionFactory.getSessionFactory();
		Session session = sessionFactory.getSession();

		String customerId = null;
		String cafNumber = null;
		List<FileDocument> documents = new ArrayList<>();
		for (String s : mapOfDocumentCategoryAndStatus.keySet()) {
			if (s.equalsIgnoreCase(Constants.CUSTOMER_ID)) {
				customerId = mapOfDocumentCategoryAndStatus.get(s);
			}
			if (s.equalsIgnoreCase(Constants.CAFNUMBER)) {
				cafNumber = mapOfDocumentCategoryAndStatus.get(s);
			}
			if (mapOfDocumentCategoryAndStatus.get(s).equalsIgnoreCase("S")) {

				if (customerId != null) {
					Map<String, Object> filters = new HashMap<>();
					filters.put(Constants.CUSTOMER_ID, customerId);
					List<FileDocument> collect = session.get(FileDocument.class, filters).getResult().stream()
							.collect(Collectors.toList());
					for (FileDocument document : collect) {
						if (document.getDocumentCategory().equalsIgnoreCase(s)) {
							documents.add(document);
						}
					}

					List<ArchivalMetaData> archivalMetaData = fileRepositoryService.getArchivalMetaData(customerId,
							session);

					// update status as S in db
					for (ArchivalMetaData metaData : archivalMetaData) {
						if (metaData.getDocumentType().equalsIgnoreCase(s)) {
							metaData.setStatus(ArchivalStatus.S);
							session.merge(metaData);
						}
					}
				}

			} else if (mapOfDocumentCategoryAndStatus.get(s).equalsIgnoreCase("F")) {

				List<ArchivalMetaData> archivalMetaData = fileRepositoryService.getArchivalMetaData(customerId,
						session);

				for (ArchivalMetaData metaData : archivalMetaData) {
					if (metaData.getDocumentType().equalsIgnoreCase(s)) {
						metaData.setStatus(ArchivalStatus.F);
						session.merge(metaData);
					}
				}

			} else if (mapOfDocumentCategoryAndStatus.get(s).equalsIgnoreCase("R")) {
				DappLoggerService.GENERAL_INFO_LOG
						.getLogBuilder(
								Constants.EXECUTING + this.getClass().getName() + "."
										+ Thread.currentThread().getStackTrace()[1].getMethodName() + "] ",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeLog();
			}

		}

		if (documents != null) {
			deleteAndAddMergedPdf(req, session, customerId, cafNumber, documents);
		}

	}

	public void deleteAndAddMergedPdf(HttpServletRequest req, Session session, String customerId, String cafNumber,
			List<FileDocument> documents) throws BaseException, Exception {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + "] ",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		// merged to single pdf
		File parentFile = getFilesFromDmsThroughCafAndOrderId(documents, cafNumber, documents.get(0).getOrderId());

		// delete the data from db and hdfs
		// for (FileDocument file : documents) {
		// // deleteFile(file.getProductName(), file.getOrderId(), file.getFileName(),
		// // cafNumber, null);
		// }

		InputStream stream = new FileInputStream(parentFile);

		// sending the merged doc to dms
		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder("Sending final merged CafDoc to DMS ms " + customerId,
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();
		FileDocument cafUpdatedFile = sendToDmsMs(stream, customerId, documents.get(0).getOrderId(),
				documents.get(0).getProductName(), parentFile.getPath(), "Electronically generated CAF", cafNumber);

		CustomerFWA customer = session.getObject(CustomerFWA.class, "customerId.keyword", customerId);

		// update in customer pojo
		customer.getCafDetails().setActualFileName(cafUpdatedFile.getActualFileName());
		customer.getCafDetails().setFileName(cafUpdatedFile.getFileName());

		session.merge(customer);

		CounterNameEnum.CNTR_RESPONSE_CHECK_SUCCESS.increment();

		// delete file

		if (parentFile.delete()) {
			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder(Constants.FILEGOTDELETEDSUCCESSFULLY + parentFile.getAbsolutePath(),
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();

		}

	}

	public FileDocument sendToDmsMs(InputStream stream, String customerId, String orderId, String productName,
			String initialCafFileName, String documentCategory, String cafNumber) {
		String elbInfo = ServiceCommunicator.elbInfo();
		String finalUrl = elbInfo + Constants.ELBDMS;

		try {
			RestTalkResponse response = new RestTalkBuilder().Post(finalUrl)
					.addCustomHeader("X-Event-Name", "UPLOAD_FILE").addQueryParam("productName", productName)
					.addQueryParam(Constants.FILENAME, initialCafFileName).addQueryParam(Constants.ORDERID, orderId)
					.addQueryParam(Constants.CUSTOMER_ID, customerId).addQueryParam(Constants.CAFNUMBER, cafNumber)
					.addQueryParam("documentCategory", documentCategory).addRequestData(stream).send();

			if (response.getHttpStatusCode() == 200 || response.getHttpStatusCode() == 202) {
				DappLoggerService.GENERAL_INFO_LOG
						.getLogBuilder(
								"Executing [Merged File sent succesfully to DMS Ms" + this.getClass().getName() + "."
										+ Thread.currentThread().getStackTrace()[1].getMethodName() + "] ",
								this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeLog();

				JsonObject convertToJson = convertToJson(response);

				return RequestToBeanMapper.getBeanFromString(convertToJson.toString(), FileDocument.class);

			}
		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getLogBuilder(
							"Executing [ Failure while sending file from CIS to DMS ms" + this.getClass().getName()
									+ "." + Thread.currentThread().getStackTrace()[1].getMethodName() + "] ",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();

		}
		return null;
	}

	public JsonObject convertToJson(RestTalkResponse response) {
		JsonObject jsonData = null;
		jsonData = new JsonParser().parse(response.answeredContent().responseString()).getAsJsonObject();
		if (jsonData.get("app-data") != null)
			jsonData = (JsonObject) jsonData.get("app-data");
		return jsonData;
	}

	public Map<String, String> mapOfDocumentCategoryAndStatus(String csvData) {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + "] ",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		String[] rows = csvData.split("\n");

		Map<String, String> map = new LinkedHashMap<>();

		if (rows.length > 1) {
			for (int i = 1; i < rows.length; i++) {
				String[] values = rows[i].split(",");

				if (values.length > 0) {
					String documentCategory = values[3];
					String status = values[6];
					String customerId = values[4];
					String cafNumber = values[0];
					map.put(Constants.CUSTOMER_ID, customerId);
					map.put(Constants.CAFNUMBER, cafNumber);
					map.put(documentCategory, status);

				}
			}
		}
		return map;
	}

	public void addCustomerIdInDocument(String orderId, String customerId) throws ElasticSearchException {
		SessionFactory sessionFactory = SessionFactory.getSessionFactory();
		Session session = sessionFactory.getSession();
		fileRepositoryService.addCustomerIdInDocument(session, orderId, customerId);
	}
}
