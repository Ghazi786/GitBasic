/**
 * 
 */
package com.jio.crm.dms.modules.template.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpStatus;

import com.jio.crm.dms.clearcodes.ClearCodeLevel;
import com.jio.crm.dms.clearcodes.ClearCodes;
import com.jio.crm.dms.countermanager.CounterNameEnum;
import com.jio.crm.dms.exceptions.BaseException;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.startup.DmsBootStrapper;
import com.jio.crm.dms.utils.MimeTypes;
import com.jio.telco.framework.clearcode.ClearCodeAsnPojo;
import com.jio.telco.framework.clearcode.ClearCodeAsnUtility;

/**
 * @author Ghajnafar.Shahid
 *
 */
public class TemplateService {
	private static final String CONTENT_DISPOSIOTION = "Content-Disposition";
	private static final String CONTENT_ATTACHMENT = "inline; filename=\"";

	public void getTemplateForFileSearch(HttpServletRequest req, HttpServletResponse resp)
			throws BaseException, IOException {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [ " + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		final ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();
		String fileName = req.getParameter("fileName");
		String filePath = "../configuration/" + fileName;
		String profile = System.getProperty("profile");

		if (profile != null && profile.equals("dev")) {
			filePath = "./configuration/" + fileName;
		}
		try (FileInputStream in = new FileInputStream(filePath)) {

			OutputStream os = resp.getOutputStream();
			String extension = FilenameUtils.getExtension(fileName);
			String contentType = MimeTypes.getMimeType(extension);
			resp.setContentType(contentType);
			resp.setHeader(CONTENT_DISPOSIOTION, CONTENT_ATTACHMENT + fileName + "\"");
			resp.setContentLength(in.available());
			try {
				byte[] buffer = new byte[1024];
				int numBytes = 0;
				while ((numBytes = in.read(buffer)) > 0) {
					os.write(buffer, 0, numBytes);
				}

				CounterNameEnum.CNTR_GET_TEMPLATE_FOR_FILES_SEARCH_SUCCESS.increment();
				ccAsnPojo.addClearCode(ClearCodes.GET_TEMPLATE_FOR_FILES_SEARCH_SUCCESS.getValue(),
						ClearCodeLevel.SUCCESS);
				ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			} catch (Exception e) {
				CounterNameEnum.CNTR_GET_TEMPLATE_FOR_FILES_SEARCH_FAILURE.increment();
				ccAsnPojo.addClearCode(ClearCodes.GET_TEMPLATE_FOR_FILES_SEARCH_FAILURE.getValue(),
						ClearCodeLevel.FAILURE);
				ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
				DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
						.writeExceptionLog();

			} finally {
				try {
					os.flush();
					os.close();
					in.close();
				} catch (IOException e) {
					DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage())
							.writeExceptionLog();

				}
			}
		} catch (IOException e) {
			throw new BaseException(HttpStatus.SC_NOT_FOUND, "Template NOT found for file Name: " + fileName);
		}
	}
}
