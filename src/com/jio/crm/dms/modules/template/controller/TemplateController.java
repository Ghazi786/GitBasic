/**
 * 
 */
package com.jio.crm.dms.modules.template.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import com.jio.crm.dms.core.BaseResponse;
import com.jio.crm.dms.core.DispatcherBaseController;
import com.jio.crm.dms.core.HttpRequestMethod;
import com.jio.crm.dms.core.annotations.Controller;
import com.jio.crm.dms.core.annotations.EventName;
import com.jio.crm.dms.core.annotations.RequestMapping;
import com.jio.crm.dms.countermanager.CounterNameEnum;
import com.jio.crm.dms.exceptions.BaseException;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.modules.template.service.TemplateService;

/**
 * @author Ghajnafar.Shahid
 *
 */
@Controller
@RequestMapping(name = "/template")
public class TemplateController implements DispatcherBaseController {


	private TemplateService templateService = new TemplateService();

	/**
	 * @param req
	 * @param resp
	 * @return
	 * @throws BaseException
	 * @throws IOException
	 */
	@EventName("GET_TEMPLATE_FOR_FILES_SEARCH")
	@RequestMapping(name = "/getTemplate", type = HttpRequestMethod.GET)
	public BaseResponse<Object> getTemplateForFileSearch(HttpServletRequest req, HttpServletResponse resp)
			throws BaseException, IOException {

		// logger
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [ " + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
		CounterNameEnum.CNTR_GET_TEMPLATE_FOR_FILES_SEARCH_REQUEST.increment();

		templateService.getTemplateForFileSearch(req, resp);
		BaseResponse<Object> response = new BaseResponse<>();
		response.setMessage("Template got successfully for file search");
		response.setStatus(HttpStatus.SC_OK);
		return null;
	}

}
