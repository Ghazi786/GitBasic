package com.jio.crm.dms.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.formula.functions.T;

/**
 * Base controller class which will be implemented by every controllers
 * @author Barun.Rai
 * 
 */
public interface BaseController {

	public  BaseResponse<T> processGet(HttpServletRequest req, HttpServletResponse resp);

	public BaseResponse<T> processPost(HttpServletRequest req, HttpServletResponse resp);

}
