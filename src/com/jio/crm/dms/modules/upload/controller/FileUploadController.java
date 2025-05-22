package com.jio.crm.dms.modules.upload.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import com.elastic.search.exception.ElasticSearchException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.jio.crm.dms.bean.ArchivalBean;
import com.jio.crm.dms.bean.DocumentCategory;
import com.jio.crm.dms.bean.FileDocument;
import com.jio.crm.dms.core.BaseResponse;
import com.jio.crm.dms.core.DispatcherBaseController;
import com.jio.crm.dms.core.HttpRequestMethod;
import com.jio.crm.dms.core.annotations.Controller;
import com.jio.crm.dms.core.annotations.EventName;
import com.jio.crm.dms.core.annotations.RequestMapping;
import com.jio.crm.dms.countermanager.CounterNameEnum;
import com.jio.crm.dms.exceptions.BaseException;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.modules.upload.service.FileUploadService;
import com.jio.crm.dms.modules.upload.service.ZipFileService;
import com.jio.crm.dms.utils.Constants;
import com.jio.crm.dms.utils.ObjectMapperHelper;
import com.jio.crm.dms.utils.RequestToBeanMapper;
import com.jio.subscriptions.modules.bean.Order;

import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;

/**
 * @author Ghajnafar.Shahid
 *
 */
@Controller
@RequestMapping(name = "/file")
public class FileUploadController implements DispatcherBaseController {

	private FileUploadService fileUploadService = new FileUploadService();
	private ZipFileService zipFileService = new ZipFileService();

	/**
	 * @param req
	 * @param resp
	 * @return
	 * @throws Exception
	 */
	@EventName("UPLOAD_FILE")
	@RequestMapping(name = "/upload", type = HttpRequestMethod.POST)
	public BaseResponse<Map<String, String>> uploadFile(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {

		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		CounterNameEnum.CNTR_UPLOAD_FILE_REQUEST.increment();

		String fileName = req.getParameter(Constants.FILENAME);
		BaseResponse<Map<String, String>> response = this.fileUploadService.uploadfile(req, fileName);
		response.setMessage(Constants.FILEUPLOADEDSUCCESSFULLY);
		return response;

	}

	/**
	 * @param req
	 * @param resp
	 * @return
	 * @throws IllegalArgumentException
	 * @throws XmlParserException
	 * @throws ServerException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidResponseException
	 * @throws InternalException
	 * @throws InsufficientDataException
	 * @throws ErrorResponseException
	 * @throws InvalidKeyException
	 * 
	 */
	@EventName("GET_FILE")
	@RequestMapping(name = "/getFile", type = HttpRequestMethod.GET)
	public BaseResponse<Object> getFile(HttpServletRequest req, HttpServletResponse resp)
			throws BaseException, IOException, ElasticSearchException, InvalidKeyException, ErrorResponseException,
			InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException,
			ServerException, XmlParserException, IllegalArgumentException {

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		CounterNameEnum.CNTR_GET_FILE_REQUEST.increment();

		String fileName = req.getParameter(Constants.FILENAME);
		String orderId = req.getParameter(Constants.ORDERID);
		String fileDownloadName = req.getParameter("actualFileName");
		String productName = req.getParameter(Constants.PRODUCTNAME1);
		this.fileUploadService.getFile(resp, fileName, productName, orderId, fileDownloadName);
		BaseResponse<Object> response = new BaseResponse<>();
		response.setMessage("Fetch File done successfully");
		response.setStatus(HttpStatus.SC_OK);
		return response;

	}

	@EventName("GET_FILE_BY_URL")
	@RequestMapping(name = "/downloadByUrl/", type = HttpRequestMethod.GET)
	public BaseResponse<Object> getFileByUrl(HttpServletRequest req, HttpServletResponse resp)
			throws BaseException, IOException, InvalidKeyException, ErrorResponseException, InsufficientDataException,
			InternalException, InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException,
			IllegalArgumentException, ElasticSearchException {

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		CounterNameEnum.CNTR_GET_FILE_BY_URL_REQUEST.increment();
		this.fileUploadService.getFileByUrl(req, resp);
		return new BaseResponse<>("{}");

	}

	/**
	 * @param req
	 * @param resp
	 * @return
	 * @throws Exception
	 */
	@EventName("UPLOAD_UOL_FILE")
	@RequestMapping(name = "/uploadUol", type = HttpRequestMethod.POST)
	public BaseResponse<Map<String, String>> uploadUolFile(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {

		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		CounterNameEnum.CNTR_UPLOAD_UOL_FILE_REQUEST.increment();

		String fileName = req.getParameter(Constants.FILENAME);
		BaseResponse<Map<String, String>> response = this.fileUploadService.uploadUolFile(req, fileName);
		response.setMessage(Constants.FILEUPLOADEDSUCCESSFULLY);
		return response;
	}

	/**
	 * @param req
	 * @param resp
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws BaseException
	 * @throws ElasticSearchException
	 * @throws JSchException
	 */
	@EventName("VALIDATE_COLUMN")
	@RequestMapping(name = "/validate", type = HttpRequestMethod.POST)
	public BaseResponse<Map<String, Boolean>> validateColumns(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, BaseException, ElasticSearchException, JSchException {

		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		CounterNameEnum.CNTR_VALIDATE_COLUMN_REQUEST.increment();

		String fileName = req.getParameter("fileName");
		String orderId = req.getParameter("orderId");
		String productName = req.getParameter("productName");
		@SuppressWarnings("unchecked")
		Map<String, Map<String, String>> reqJson = ObjectMapperHelper.getInstance().getEntityObjectMapper()
				.readValue(req.getReader(), Map.class);
		Map<String, String> columnList = reqJson.get("column");
		BaseResponse<Map<String, Boolean>> response = this.fileUploadService.validateColumns(orderId, fileName,
				productName, columnList);
		response.setMessage("File Validation Successfully");

		return response;
	}

	/**
	 * @param req
	 * @param resp
	 * @return
	 * @throws JSchException
	 * @throws ElasticSearchException
	 * @throws BaseException
	 * @throws IOException
	 * @throws Exception
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@EventName("DELETE_FILE")
	@RequestMapping(name = "/delete", type = HttpRequestMethod.DELETE)
	public BaseResponse<Map<String, String>> deleteFile(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, BaseException, ElasticSearchException, JSchException {

		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		CounterNameEnum.CNTR_DELETE_FILE_REQUEST.increment();
		String fileName = req.getParameter(Constants.FILENAME);
		String orderId = req.getParameter(Constants.ORDERID);
		String productName = req.getParameter(Constants.PRODUCTNAME1);
		BaseResponse<Map<String, String>> response = this.fileUploadService.deleteFile(productName, orderId, fileName);
		response.setMessage(String.format("File: %s  Deleted Successfully", fileName));
		return response;
	}

	/**
	 * @param req
	 * @param resp
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws BaseException
	 */
	@EventName("DOWNLOAD_FILE_IN_BULK")
	@RequestMapping(name = "/downloadfileinbulk", type = HttpRequestMethod.GET)
	public BaseResponse<Object> downloadFileInBulk(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, BaseException {

		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		CounterNameEnum.CNTR_DOWNLOAD_FILE_IN_BULK_EXCEL_REQUEST.increment();

		String orderId = req.getParameter(Constants.ORDERID);
		String productName = req.getParameter(Constants.PRODUCTNAME1);
		this.zipFileService.downloadFileInBulk(productName, orderId, resp);
		BaseResponse<Object> response = new BaseResponse<>();
		response.setMessage("File download in bulk done successfully");
		response.setStatus(HttpStatus.SC_OK);
		return response;
	}

	/**
	 * @param req
	 * @param resp
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws BaseException
	 */
	@EventName("DOWNLOAD_FILE_IN_BULK_EXCEL")
	@RequestMapping(name = "/downloadfileinbulkfromExcel", type = HttpRequestMethod.POST)
	public BaseResponse<Object> downloadfileinbulkfomexcel(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, BaseException {

		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		CounterNameEnum.CNTR_DOWNLOAD_FILE_IN_BULK_EXCEL_REQUEST.increment();

		this.zipFileService.downloadfileinbulkfomexcel(req, resp);
		BaseResponse<Object> response = new BaseResponse<>();
		response.setMessage("File download in bulk in excel done successfully");
		response.setStatus(HttpStatus.SC_OK);
		return response;
	}

	/**
	 * @param req
	 * @param resp
	 * @return
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 * @throws BaseException
	 */
	@EventName("SEARCH_FILE_DOCUMENT")
	@RequestMapping(name = "/searchFileDocument", type = HttpRequestMethod.POST)
	public BaseResponse<Map<String, List<FileDocument>>> searchFileDocuments(HttpServletRequest req,
			HttpServletResponse resp) throws IOException, BaseException {

		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		CounterNameEnum.CNTR_SEARCH_FILE_DOCUMENT_REQUEST.increment();
		FileDocument document = RequestToBeanMapper.getBeanFromRequest(req, FileDocument.class);
		return this.fileUploadService.searchFileDocuments(document);

	}

	/**
	 * @param req
	 * @param resp
	 * @return
	 * @throws BaseException
	 * @throws JSchException
	 * @throws SftpException
	 * @throws Exception
	 */
	@EventName("MOVE_FILE_FROM_TEMP_TO_ORDER")
	@RequestMapping(name = "/moveFileFromTemp", type = HttpRequestMethod.POST)
	public BaseResponse<Map<String, Boolean>> moveFileFromTemp(HttpServletRequest req, HttpServletResponse resp)
			throws BaseException, JSchException, SftpException {

		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		CounterNameEnum.CNTR_MOVE_FILE_FROM_TEMP_TO_ORDER_REQUEST.increment();
		Order order = RequestToBeanMapper.getBeanFromRequest(req, Order.class);

		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [  Order ID:" + order.getOrderId() + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		Map<String, Boolean> data = this.fileUploadService.moveFileFromTemp(order);
		return new BaseResponse<>(data);

	}

	@EventName("MOVE_FILE_FROM_TEMP_TO_LOCATION_ID")
	@RequestMapping(name = "/moveFileFromTempToLocationId", type = HttpRequestMethod.POST)
	public BaseResponse<Map<String, Boolean>> moveFileFromTempToLocationId(HttpServletRequest req,
			HttpServletResponse resp) throws BaseException {

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		CounterNameEnum.CNTR_MOVE_FILE_FROM_TEMP_TO_LOCATION_ID_REQUEST.increment();
		String locationId = req.getParameter("locationId");
		String productName = req.getParameter(Constants.PRODUCTNAME1);
		Map<String, String> dataFromRequest = RequestToBeanMapper.getDataFromRequest(req);
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [  location ID:" + locationId + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		Map<String, Boolean> data = this.fileUploadService.moveFileFromTempToLocationId(locationId, dataFromRequest,
				productName);
		return new BaseResponse<>(data);

	}

	/**
	 * @param req
	 * @param resp
	 * @return
	 * @throws Exception
	 */
	@EventName("FETCH_DOCUMENT_CATEGORY")
	@RequestMapping(name = "/fetchDocumentCategory", type = HttpRequestMethod.GET)
	public BaseResponse<DocumentCategory> fetchDocumentCategory(HttpServletRequest req, HttpServletResponse resp) {

		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		CounterNameEnum.CNTR_FETCH_DOCUMENT_CATEGORY_REQUEST.increment();
		return this.fileUploadService.fetchDocumentCategory();

	}

	@EventName("CHANGE_FILE_DOCUMENT_TO_DEACTIVATED_FOLDER")
	@RequestMapping(name = "/changeFilePathToDeactivatedFolder", type = HttpRequestMethod.POST)
	public BaseResponse<Object> changeFilePathToDeactivatedFolder(HttpServletRequest req, HttpServletResponse resp)
			throws ElasticSearchException, IOException, BaseException, JSchException, SftpException {

		CounterNameEnum.CNTR_CHANGE_FILE_DOCUMENT_TO_DEACTIVATED_FOLDER_REQUEST.increment();
		// logger
		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder(
				Constants.EXECUTING + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName());
		String customerId = req.getParameter("customerId");
		fileUploadService.changeFilePathToDeactivatedFolder(customerId);
		BaseResponse<Object> response = new BaseResponse<>();
		response.setMessage("File Purging done Successfully");
		response.setStatus(HttpServletResponse.SC_OK);
		return response;
	}

	@EventName("UPLOAD_FILE_BY_DOCUMENT_CATEGORY")
	@RequestMapping(name = "/upload/documentCategory", type = HttpRequestMethod.POST)
	public BaseResponse<Map<String, String>> uploadbasedondocumentcategory(HttpServletRequest req,
			HttpServletResponse resp) throws Exception {
		CounterNameEnum.CNTR_UPLOAD_FILE_BY_DOCUMENT_CATEGORY_REQUEST.increment();
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		String fileName = req.getParameter(Constants.FILENAME);
		BaseResponse<Map<String, String>> response = this.fileUploadService.uploadbasedondocumentcategory(req,
				fileName);
		response.setMessage(Constants.FILEUPLOADEDSUCCESSFULLY);
		return response;
	}

	@EventName("GET_FILE_BY_DOCUMENT_TYPE")
	@RequestMapping(name = "/getFile/documentCategory", type = HttpRequestMethod.GET)
	public BaseResponse<Object> getFileList(HttpServletRequest req, HttpServletResponse resp)
			throws BaseException, IOException, ElasticSearchException {
		CounterNameEnum.CNTR_GET_FILE_BY_DOCUMENT_TYPE_REQUEST.increment();
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		String orderId = req.getParameter(Constants.ORDERID);
		String cafNumber = req.getParameter("cafNumber");
		List<Object> response = this.fileUploadService.getFileList(orderId, cafNumber);
		BaseResponse<Object> baseResponse = new BaseResponse<>(response);
		baseResponse.setMessage("Get file by Document Type is done successfully");
		baseResponse.setStatus(HttpServletResponse.SC_OK);
		return baseResponse;

	}

	@EventName("FILE_UPLOAD")
	@RequestMapping(name = "/fileUploadToUrl", type = HttpRequestMethod.POST)
	public BaseResponse<Map<String, String>> uploadBasedOnMandatory(HttpServletRequest req, HttpServletResponse resp)
			throws Exception {
		CounterNameEnum.CNTR_FILE_UPLOAD_REQUEST.increment();
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		String fileName = req.getParameter(Constants.FILENAME);
		String protocol = req.getParameter("protocol");
		BaseResponse<Map<String, String>> response = this.fileUploadService.uploadBasedOnMandatory(req, fileName,
				protocol);
		response.setMessage(Constants.FILEUPLOADEDSUCCESSFULLY);
		return response;
	}

	@EventName("ARCHIVAL_FILES")
	@RequestMapping(name = "/archivalFiles", type = HttpRequestMethod.POST)
	public BaseResponse<Object> archivalfiles(HttpServletRequest req, HttpServletResponse resp)
			throws ElasticSearchException, IOException, BaseException, InterruptedException {
		CounterNameEnum.CNTR_ARCHIVAL_FILES_REQUEST.increment();
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		ArchivalBean beanFromRequest = RequestToBeanMapper.getBeanFromRequest(req, ArchivalBean.class);
		this.fileUploadService.archivalfiles(req, beanFromRequest);
		BaseResponse<Object> response = new BaseResponse<>();
		response.setMessage("File Archived Successfully");
		response.setStatus(200);
		return response;
	}

	@EventName("RELOAD_DOCUMENT_CATEGORY")
	@RequestMapping(name = "/reloadDocumentCategory", type = HttpRequestMethod.GET)
	public BaseResponse<Object> reloadDocumentCategory(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		CounterNameEnum.CNTR_RELOAD_DOCUMENT_CATEGORY_REQUEST.increment();
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		this.fileUploadService.reloadDocumentCategory();
		BaseResponse<Object> response = new BaseResponse<>();
		response.setMessage("Reloaded Successfully");
		response.setStatus(HttpStatus.SC_OK);
		return response;
	}

	@EventName("RESPONSE_CHECK")
	@RequestMapping(name = "/responsefromArchivalDocument", type = HttpRequestMethod.POST)
	public BaseResponse<Object> responsefromarchivaldocument(HttpServletRequest req, HttpServletResponse resp)
			throws BaseException, Exception {
		CounterNameEnum.CNTR_RESPONSE_CHECK_REQUEST.increment();
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		String csvData = readCSVFromRequest(req);

		this.fileUploadService.responsefromarchivaldocument(req, csvData);
		BaseResponse<Object> response = new BaseResponse<>();
		response.setMessage("Response Received Successfully");
		response.setStatus(200);
		return response;
	}

	public String readCSVFromRequest(HttpServletRequest req) throws IOException {
		StringBuilder csvData = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				csvData.append(line);
				csvData.append("\n");
			}
		}
		return csvData.toString();
	}

	@EventName("ADD_CUSTOMERID_IN_DOCUMENT")
	@RequestMapping(name = "/addCustomerIdInDocument", type = HttpRequestMethod.GET)
	public BaseResponse<Object> addCustomerIdInDocument(HttpServletRequest req, HttpServletResponse resp)
			throws ElasticSearchException {
		CounterNameEnum.CNTR_ADD_CUSTOMERID_IN_DOCUMENT_REQUEST.increment();
		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		String orderId = req.getParameter(Constants.ORDERID);
		String customerId = req.getParameter("customerId");
		this.fileUploadService.addCustomerIdInDocument(orderId, customerId);
		BaseResponse<Object> response = new BaseResponse<>();
		response.setMessage("Added CustomerId In document");
		response.setStatus(HttpStatus.SC_OK);
		return response;
	}
}
