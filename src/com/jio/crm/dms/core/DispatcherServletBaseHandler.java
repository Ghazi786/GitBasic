package com.jio.crm.dms.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;

import com.elastic.search.exception.ElasticSearchException;
import com.jio.crm.dms.clearcodes.ClearCodeLevel;
import com.jio.crm.dms.clearcodes.ClearCodes;
import com.jio.crm.dms.core.annotations.hasRoles;
import com.jio.crm.dms.countermanager.CounterNameEnum;
import com.jio.crm.dms.exceptions.AccessDeniedException;
import com.jio.crm.dms.exceptions.BaseException;
import com.jio.crm.dms.exceptions.InvalidDataException;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.startup.DmsBootStrapper;
import com.jio.crm.dms.threadpoolfactory.EventAckAnnotationRequestDispatcher;
import com.jio.crm.dms.utils.CommonRepository;
import com.jio.crm.dms.utils.Constants;
import com.jio.crm.dms.utils.ObjectMapperHelper;
import com.jio.crm.dms.utils.RequestToBeanMapper;
import com.jio.crm.dms.utils.Role;
import com.jio.crm.dms.utils.RoleConstants;
import com.jio.crm.dms.utils.SubscriptionEngineUtils;
import com.jio.subscriptions.modules.bean.Vendor;
import com.jio.telco.framework.clearcode.ClearCodeAsnPojo;
import com.jio.telco.framework.clearcode.ClearCodeAsnUtility;

/**
 * @author Barun.Rai
 *
 */
public class DispatcherServletBaseHandler extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5872479692969508281L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processrequest(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processrequest(req, resp);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processrequest(req, resp);

	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processrequest(req, resp);
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processrequest(req, resp);
	}

	/**
	 * @param response
	 * @param responseBody
	 * @param status
	 * @param responseMessage
	 * @param cursor
	 */
	protected void sendResponse(HttpServletResponse response, Object responseBody, int status, String responseMessage) {
		try {

			if (responseBody instanceof String) {
				String SEPERATOR = ",";
				String ENCLOSED = "\"";
				String SEPERATOR_COL = ":";

				StringBuilder sb = new StringBuilder();
				sb.append("{");
				if (responseBody != null)
					responseBody.toString();

				sb.append(ENCLOSED).append(Constants.RESPONSE_MESSAGE).append(ENCLOSED).append(SEPERATOR_COL)
						.append(ENCLOSED).append(responseMessage).append(ENCLOSED).append(SEPERATOR);
				sb.append(ENCLOSED).append(Constants.RESPONSE_CODE).append(ENCLOSED).append(SEPERATOR_COL)
						.append(status).append(SEPERATOR);
				sb.append(ENCLOSED).append(Constants.CREATED_TIME_STAMP).append(ENCLOSED).append(SEPERATOR_COL)
						.append(System.currentTimeMillis());
				sb.append("}");
				response.setStatus(status);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				PrintWriter printWriter;
				printWriter = response.getWriter();
				printWriter.print(sb.toString());
				printWriter.flush();
				printWriter.close();

			} else {

				Map<String, Object> jsonObj = new HashMap<>();
				if (responseBody != null)
					jsonObj.put(Constants.RESPONSE_BODY, responseBody);
				jsonObj.put(Constants.RESPONSE_MESSAGE, responseMessage);
				jsonObj.put(Constants.RESPONSE_CODE, status);
				jsonObj.put(Constants.CREATED_TIME_STAMP, System.currentTimeMillis());

				response.setStatus(status);
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				PrintWriter printWriter;
				printWriter = response.getWriter();
				printWriter.print(ObjectMapperHelper.getInstance().getEntityObjectMapper().writeValueAsString(jsonObj));
				printWriter.flush();
				printWriter.close();
			}

		} catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}

	}

	private void processrequest(HttpServletRequest req, HttpServletResponse resp) {
		String path = req.getContextPath() + req.getPathInfo();
		String httpMethod = req.getMethod();

		try {

			BaseEventBean eventPojo = SubscriptionEngineUtils.getEventTrackingObject(req);

			Class<?> classObj = null;

			if (BaseContextMapper.getEventClassMapping().containsKey(eventPojo.getEventName())) {
				classObj = Class.forName(BaseContextMapper.getEventClassMapping().get(eventPojo.getEventName()));
			} else {
				classObj = Class.forName(BaseContextMapper.getControllerMapping().get(req.getContextPath()));
			}

			DispatcherBaseController controller = (DispatcherBaseController) classObj.newInstance();

			// check condition for Async Request
			if ((!HttpParamConstants.EXECUTION_TYPE.equals(eventPojo.getExecutionType()))
					&& (BaseContextMapper.getAsyncMapping().contains(path)
							|| BaseContextMapper.getAsyncEventMapping().contains(eventPojo.getEventName())
							|| HttpParamConstants.ASYNC_EXECUTION_TYPE.equals(eventPojo.getExecutionType()))) {

				AsyncContext context = req.startAsync();
				ExecutorService executorService = DmsBootStrapper.getInstance().getDappExecutor();
				executorService
						.execute(new EventAckAnnotationRequestDispatcher(eventPojo, req, resp, controller, context));

				sendResponse(resp, "\"Request Accepted\"", HttpServletResponse.SC_ACCEPTED, "Accepted");

			} else {
				java.lang.reflect.Method method = null;
				if (eventPojo.getEventName() != null && !eventPojo.getEventName().isEmpty()
						&& BaseContextMapper.getEventMapping().containsKey(eventPojo.getEventName().trim())) {

					method = controller.getClass().getMethod(
							BaseContextMapper.getEventMapping().get(eventPojo.getEventName().trim()),
							HttpServletRequest.class, HttpServletResponse.class);

				} else {
					Map<String, String> type = BaseContextMapper.getPathMapping()
							.get(HttpRequestMethod.valueOf(httpMethod).name());
					Set<String> methodsPath = type.keySet();
					String exactPath = path;
					for (String dynamicmethod : methodsPath) {
						if (dynamicmethod.endsWith("/") && path.startsWith(dynamicmethod)) {
							exactPath = dynamicmethod;
						}
					}
					method = controller.getClass()
							.getMethod(BaseContextMapper.getPathMapping()
									.get(HttpRequestMethod.valueOf(httpMethod).name()).get(exactPath),
									HttpServletRequest.class, HttpServletResponse.class);
				}

				// role base authentication
				boolean isAllowed = false;

				String[] role = null;

				List<String> rolesList = null;

				if (method.isAnnotationPresent(hasRoles.class)) {

					String vendorId = RequestToBeanMapper.getVendorId(req);

					if (vendorId == null) {
						throw new InvalidDataException(HttpStatus.BAD_REQUEST_400, RoleConstants.VENDOR_ID_NOT_PRSENT);
					}

					if (!BaseContextMapper.VENDOR_ROLES_LIST.containsKey(vendorId)) {
						Vendor vendor = CommonRepository.getInstance().getVendor(vendorId);
						if (vendor == null) {
							throw new InvalidDataException(HttpStatus.BAD_REQUEST_400, RoleConstants.INCORRECT_ID);
						}
						List<String> roles = vendor.getRoles().stream().map(e -> e.getRoleName().toUpperCase())
								.collect(Collectors.toList());
						BaseContextMapper.VENDOR_ROLES_LIST.put(vendorId, roles);
					}

					rolesList = BaseContextMapper.VENDOR_ROLES_LIST.get(vendorId);

					if (rolesList.isEmpty()) {
						throw new InvalidDataException(HttpStatus.BAD_REQUEST_400, RoleConstants.INVALID_ROLE);
					}

					hasRoles methodAcces = method.getAnnotation(hasRoles.class);

					role = methodAcces.roleName();

					isAllowed = Arrays.stream(role).allMatch(rolesList::contains);
				}

				if (rolesList != null) {
					if (isAllowed || rolesList.contains(Role.ADMINROLE.name())) {
						BaseResponse<?> response = (BaseResponse<?>) method.invoke(controller, req, resp);
						sendResponse(resp, response.getData(), response.getStatus(), response.getMessage());
					}

					else {
						throw new AccessDeniedException(HttpStatus.FORBIDDEN_403, RoleConstants.ACCESS_DENIED);
					}

				}

				else if (role == null) {
					BaseResponse<?> response = (BaseResponse<?>) method.invoke(controller, req, resp);

					if (response != null) {
						sendResponse(resp, response.getData(), response.getStatus(), response.getMessage());
					}
				}

				else {
					throw new AccessDeniedException(HttpStatus.FORBIDDEN_403, RoleConstants.ACCESS_DENIED);
				}

			}
		} catch (Exception e) {

			ClearCodeAsnPojo ccAsnPojo = DmsBootStrapper.getInstance().getClearCodeObj();
			ccAsnPojo.addClearCode(ClearCodes.COMMON_FAILURE.getValue(), ClearCodeLevel.FAILURE);
			ClearCodeAsnUtility.getASNInstance().writeASNForClearCode(ccAsnPojo);
			CounterNameEnum.CNTR_COMMON_FAILURE.increment();

			BaseException exp = new BaseException(HttpStatus.INTERNAL_SERVER_ERROR_500,
					"Server error occurred. Please contact the administrator");

			if (e.getCause() instanceof BaseException) {
				exp = (BaseException) e.getCause();
				sendResponse(resp, null, exp.getStatus(), exp.getMessage());
			} else if (e.getCause() instanceof ElasticSearchException) {
				exp = new BaseException(HttpStatus.INTERNAL_SERVER_ERROR_500, e.getCause().getMessage());
				sendResponse(resp, null, exp.getStatus(), exp.getMessage());
			} else if (e instanceof AccessDeniedException) {
				sendResponse(resp, null, ((AccessDeniedException) e).getStatus(), e.getMessage());
			} else if (e instanceof InvalidDataException) {
				sendResponse(resp, null, ((InvalidDataException) e).getStatus(), e.getMessage());
			} else {
				sendResponse(resp, null, exp.getStatus(), exp.getMessage());
			}
		}
	}

}
