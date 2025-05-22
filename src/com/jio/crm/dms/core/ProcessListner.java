package com.jio.crm.dms.core;


/**
 * Process Listener that provide async response of request
 * 
 * @author Kiran.Jangid
 *
 */
@FunctionalInterface
public interface ProcessListner {

	/**
	 * 
	 * @param response
	 */
	
	void completed(BaseResponse<?> response);

}
