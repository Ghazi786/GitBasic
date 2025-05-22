/**
 * 
 */
package com.jio.crm.dms.modules.upload.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.elastic.search.bean.Page;
import com.elastic.search.bean.SearchResult;
import com.elastic.search.exception.ElasticSearchException;
import com.elastic.search.service.Session;
import com.jio.crm.dms.bean.ArchivalMetaData;
import com.jio.crm.dms.bean.DocumentCategory;
import com.jio.crm.dms.bean.FileDocument;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.Constants;
import com.jio.subscriptions.modules.bean.DeactivedFileDocument;

/**
 * @author Ghajnafar.Shahid
 *
 */
public class FileUploadRepository {

	public void saveDocument(Session session, FileDocument document) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [ document :: " + document + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		session.save(document);
	}

	public void getAllDocument(Session session) throws ElasticSearchException {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		session.get(FileDocument.class);
	}

	public SearchResult<FileDocument> getAllDocumentByFilters(Session session, Map<String, String> filters, Page page)
			throws ElasticSearchException {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		return session.get(FileDocument.class, filters, page);
		
	}

	public FileDocument getFileDocument(Session session, String fileName) throws ElasticSearchException {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		return session.getObject(FileDocument.class, "fileName", fileName);

	}

	public FileDocument getFileDocument(Session session, Map<String, String> filters)
			throws ElasticSearchException {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		return session.getObject(FileDocument.class, filters);
	}

	public void updateFiledocuments(Session session, List<FileDocument> filedocuments) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		session.bulk(filedocuments, false);
	}

	public DocumentCategory getDocumentCategory(Session session) throws ElasticSearchException {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		return session.getObject(DocumentCategory.class, "12345");
	}

	public static void pushDocumentCategory(Session session, DocumentCategory documentCategory) throws Exception {
		session.merge(documentCategory);
	}

	public void deleteFileDocument(Session session, FileDocument fileDocument) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [ deleting file document from bean" + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		session.delete(fileDocument);

	}

	public SearchResult<FileDocument> getFileDataOfCustomer(Session session, String customerId)
			throws ElasticSearchException {
		Map<String, Object> filter = new HashMap<>();
		filter.put("customerId", customerId);
		return session.get(FileDocument.class, filter);
	}

	public void saveDeactivatedFileDocument(Session session, DeactivedFileDocument fileDeactivated) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [saving file data in deactivated file document bean " + this.getClass().getName()
								+ "." + Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		session.merge(fileDeactivated);

	}

	public List<FileDocument> archivalFiles(Session session, String orderId, String cafNumber)
			throws ElasticSearchException {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [getting file data from es " + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

		Map<String, Object> filter = new HashMap<>();
		if (cafNumber != null) {
			filter.put("cafNumber", cafNumber);
		}
		if (orderId != null) {
			filter.put("orderId", orderId);
		}

		SearchResult<FileDocument> searchResult = session.get(FileDocument.class, filter);
		return searchResult.getResult().stream().collect(Collectors.toList());

	}

	public void bulksave(List<ArchivalMetaData> listArchival, Session session) throws Exception {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		session.bulk(listArchival, true);

	}

	public List<ArchivalMetaData> getArchivalMetaData(String customerId, Session session)
			throws ElasticSearchException {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						Constants.EXECUTING + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		Map<String, String> filters = new HashMap<>();
		filters.put("customerId", customerId);

		SearchResult<ArchivalMetaData> searchResult = session.get(ArchivalMetaData.class, filters);
		return searchResult.getResult().stream().collect(Collectors.toList());

	}

	public void addCustomerIdInDocument(Session session, String orderId, String customerId)
			throws ElasticSearchException {

		Map<String, Object> filters = new HashMap<>();
		filters.put("orderId.keyword", orderId);
		List<FileDocument> searchResult = session.get(FileDocument.class, filters).getResult().stream()
				.collect(Collectors.toList());
		for (FileDocument document : searchResult) {
			document.setCustomerId(customerId);
			try {
				session.merge(document);
			} catch (Exception e) {
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
						.writeExceptionLog();
			}
		}

	}

}
