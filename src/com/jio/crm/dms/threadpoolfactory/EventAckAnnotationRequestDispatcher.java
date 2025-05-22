package com.jio.crm.dms.threadpoolfactory;

import java.lang.reflect.InvocationTargetException;

import javax.servlet.AsyncContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jio.crm.dms.core.BaseContextMapper;
import com.jio.crm.dms.core.BaseEventBean;
import com.jio.crm.dms.core.BaseResponse;
import com.jio.crm.dms.core.DispatcherBaseController;
import com.jio.crm.dms.core.HttpRequestMethod;
import com.jio.crm.dms.ha.EventDumpTask;
import com.jio.crm.dms.node.startup.DmsBootStrapper;
import com.jio.crm.dms.utils.SubscriptionEngineUtils;

public class EventAckAnnotationRequestDispatcher implements Runnable {

	private BaseEventBean eventPojo;
	private HttpServletRequest request;
	private DispatcherBaseController controller;
	private HttpServletResponse response;
	private AsyncContext context;

	public EventAckAnnotationRequestDispatcher(BaseEventBean eventPojo, HttpServletRequest request,
			HttpServletResponse response, DispatcherBaseController controller, AsyncContext context) {
		super();
		this.eventPojo = eventPojo;
		this.request = request;
		this.controller = controller;
		this.response = response;
		this.context = context;
	}

	@Override
	public void run() {

		BaseResponse<?> controllerresponse = null;
		try {

			DmsBootStrapper.getInstance().getThreadPoolService().execute(new EventDumpTask(eventPojo));

			String method = this.request.getMethod();
			if (HttpRequestMethod.valueOf(method).name() != null) {

				java.lang.reflect.Method methodCalled = null;
				if (eventPojo.getEventName() != null && !eventPojo.getEventName().isEmpty()
						&& BaseContextMapper.getEventMapping().containsKey(eventPojo.getEventName().trim())) {
					methodCalled = controller.getClass().getMethod(
							BaseContextMapper.getEventMapping().get(eventPojo.getEventName().trim()),
							HttpServletRequest.class, HttpServletResponse.class);

				} else {
					String path = request.getPathInfo();
					methodCalled = controller.getClass().getMethod(
							BaseContextMapper.getPathMapping().get(HttpRequestMethod.valueOf(method).name()).get(path),
							HttpServletRequest.class, HttpServletResponse.class);
				}
				controllerresponse = (BaseResponse<?>) methodCalled.invoke(controller, request, response);

			} else {
				controllerresponse = new BaseResponse<>(null, false, "No Such Method", 400, null);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			controllerresponse = new BaseResponse(null, false, "Requested mapping not found", 404, null);
		} finally {
			this.context.complete();
			SubscriptionEngineUtils.sendAsyncResponseToELB(eventPojo, controllerresponse);
		}

	}

}
