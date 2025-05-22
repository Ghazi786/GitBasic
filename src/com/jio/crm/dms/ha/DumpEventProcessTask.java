package com.jio.crm.dms.ha;

import java.util.concurrent.ExecutorService;

import javax.servlet.AsyncContext;

import com.jio.crm.dms.core.BaseEventBean;
import com.jio.crm.dms.core.BaseResponse;
import com.jio.crm.dms.core.DispatcherBaseController;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.startup.DmsBootStrapper;
import com.jio.crm.dms.threadpoolfactory.EventAckAnnotationRequestDispatcher;

public class DumpEventProcessTask implements Runnable {

	private BaseEventBean eventPojo;
	BaseResponse<?> controllerresponse = null;
	Class<?> classObj = null;


	/**
	 * 
	 * @param eventPojo
	 */

	public DumpEventProcessTask(BaseEventBean eventPojo) {
		this.eventPojo = eventPojo;
	}

	/**
	 * 
	 */

	public DumpEventProcessTask() {
		/**
		 * Default
		 */
	}

	@Override
	public void run() {
		
		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				"Executing [ " + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

		
		try {
		DispatcherBaseController controller = (DispatcherBaseController) classObj.newInstance();
		
		AsyncContext context = this.eventPojo.getRequest().startAsync();
		
		ExecutorService executorService = DmsBootStrapper.getInstance().getDappExecutor();
		executorService
				.execute(new EventAckAnnotationRequestDispatcher(this.eventPojo, this.eventPojo.getRequest(), this.eventPojo.getResponse(), controller, context));

		
		} catch(Exception e) {
			DappLoggerService.GENERAL_INFO_LOG
			.getLogBuilder(
					"Error [ " + this.getClass().getName() + "."
							+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName()).writeLog();

		}

	}
	
}
