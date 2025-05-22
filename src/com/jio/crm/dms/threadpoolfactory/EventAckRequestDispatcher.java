package com.jio.crm.dms.threadpoolfactory;

import java.io.IOException;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jio.crm.dms.core.BaseController;
import com.jio.crm.dms.core.BaseEventBean;
import com.jio.crm.dms.core.BaseResponse;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.SubscriptionEngineUtils;

public class EventAckRequestDispatcher implements Runnable {

	private BaseEventBean eventPojo;
	private HttpServletRequest request;
	private BaseController controller;
	private HttpServletResponse response;
	private AsyncContext context;

	public EventAckRequestDispatcher(BaseEventBean eventPojo, HttpServletRequest request, HttpServletResponse response,
			BaseController controller, AsyncContext context) {
		super();
		this.eventPojo = eventPojo;
		this.request = request;
		this.controller = controller;
		this.response = response;
		this.context = context;
	}

	@Override
	public void run() {

		String method = this.request.getMethod();
		BaseResponse<?> controllerresponse = null;
		if (method.equals("GET")) {
			controllerresponse = controller.processGet(this.request, this.response);
		} else if (method.equals("POST")) {
			controllerresponse = controller.processPost(this.request, this.response);
		} else {
			controllerresponse = new BaseResponse(null, false, "No Such Method", 400, null);
		}
		SubscriptionEngineUtils.sendResponse(eventPojo, this.response, null, 200, "Success");
		this.context.complete();
		SubscriptionEngineUtils.sendAsyncResponseToELB(eventPojo, controllerresponse);

	}

}
