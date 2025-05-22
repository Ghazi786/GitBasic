package com.jio.crm.dms.modules.customer.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.elastic.search.exception.ElasticSearchException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.jio.crm.dms.core.BaseResponse;
import com.jio.crm.dms.core.DispatcherBaseController;
import com.jio.crm.dms.core.HttpRequestMethod;
import com.jio.crm.dms.core.annotations.Controller;
import com.jio.crm.dms.core.annotations.EventName;
import com.jio.crm.dms.core.annotations.RequestMapping;
import com.jio.crm.dms.logger.DappLoggerService;

@Controller
@RequestMapping(name = "/customer")
public class CustomerController implements DispatcherBaseController {

	
	@RequestMapping(name = "/getCustomerList", type = HttpRequestMethod.GET)
	@EventName("GET_CUSTOMERS")
	public BaseResponse<String> getCustomerList(HttpServletRequest req, HttpServletResponse resp) throws ElasticSearchException, FileNotFoundException {

		List<InputStream> list = new ArrayList<>();
		File file = new File("C:/Users/harshitha.s/Desktop/Harshitha/Food Bill/Roja");
		if(file.isDirectory()) {
			File[] listFiles = file.listFiles();
			for(File files : listFiles) {
				FileInputStream stream = new FileInputStream(files);
				list.add(stream);
			}
		}
		
		try {
			com.itextpdf.text.Document document = new com.itextpdf.text.Document();
			PdfCopy copy = new PdfCopy(document, new FileOutputStream("RojaBill"));
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
		BaseResponse<String> response = new BaseResponse<>("CUSTOMER");
		return response;

	}


}
