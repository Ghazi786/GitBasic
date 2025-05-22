package com.jio.crm.dms.logger;

import com.jio.crm.dms.node.startup.DmsBootStrapper;
import com.jio.crm.dms.utils.Constants;
import com.jio.telco.framework.logger.LoggerBuilder;
import com.jio.telco.framework.resource.ResourceBuilder;

/**
 * Enumeration of Log Service Method used throughout this project
 */
public enum DappLoggerService {

	MS_INIT_SUCCESS_STATUS(Constants.SUCCESS_STATUS, 2000, "CMN has started SUCCESSFULLY", Constants.LOG_LEVEL_INFO,DmsBootStrapper.class.getSimpleName() + ":main"),
	GENERAL_INFO_LOG(Constants.SUCCESS_STATUS, 2000, null,Constants.LOG_LEVEL_INFO,null), 
	GENERAL_ERROR_FAILURE_LOG(Constants.FAILURE, 4000, null, Constants.LOG_LEVEL_ERROR, null);

	private String type;
	private int code;
	private String description;
	private String logType;
	private String classMethodName;

	private DappLoggerService(String type, int code, String description, String logType, String classMethodName) {
		this.type = type;
		this.code = code;
		this.description = description;
		this.logType = logType;
		this.classMethodName = classMethodName;
	}

	public LoggerBuilder getLogBuilder() {
		return ResourceBuilder.logger().setLogType(this.logType).setServiceStatusType(this.type)
				.setServiceStatusCode(this.code).setServiceStatusDescription(this.description)
				.setClassMethodName(this.classMethodName);
	}

	/**
	 * 
	 * @param description
	 * @return
	 */
	public LoggerBuilder getInfoLogBuilder(String description) {

		return ResourceBuilder.logger().setLogType(this.logType).setServiceStatusType(this.type)
				.setServiceStatusCode(this.code).setServiceStatusDescription(description)
				.setClassMethodName(this.classMethodName);
	}

	/**
	 * 
	 * @param description
	 * @param className
	 * @param methodName
	 * @return
	 */
	public LoggerBuilder getLogBuilder(String description, String className, String methodName) {

		return ResourceBuilder.logger().setLogType(this.logType).setServiceStatusType(this.type)
				.setServiceStatusCode(this.code).setServiceStatusDescription(description)
				.setClassMethodName(className + " : " + methodName);
	}

	/**
	 * 
	 * @param e
	 * @param errorDescription
	 * @return
	 */
	public LoggerBuilder getExceptionLogBuilder(Exception e, String errorDescription) {
		return ResourceBuilder.logger().setLogType(this.logType).setException(e).setErrorMessage(errorDescription)
				.setServiceStatusType(this.type).setServiceStatusCode(this.code)
				.setClassMethodName(this.classMethodName);
	}

	/**
	 * 
	 * @param e
	 * @param errorDescription
	 * @param className
	 * @param methodName
	 * @return
	 */
	public LoggerBuilder getExceptionLogBuilder(Exception e, String errorDescription, String className,
			String methodName) {

		return ResourceBuilder.logger().setLogType(this.logType).setException(e).setErrorMessage(errorDescription)
				.setServiceStatusType(this.type).setServiceStatusCode(this.code)
				.setClassMethodName(className + " : " + methodName);
	}

	/**
	 * 
	 * @param description
	 * @param className
	 * @param methodName
	 * @return
	 */
	public LoggerBuilder getLogBuilderWithFlowID(String description, String flowId, String className,
			String methodName) {

		return ResourceBuilder.logger().setLogType(this.logType).setRequestId(flowId).setServiceStatusType(this.type)
				.setServiceStatusCode(this.code).setServiceStatusDescription(description)
				.setClassMethodName(className + " : " + methodName);
	}

	/**
	 * 
	 * @param e
	 * @param errorDescription
	 * @return
	 */
	public LoggerBuilder getErrorLogBuilder(String errorDescription, String className, String methodName) {
		return ResourceBuilder.logger().setLogType(this.logType).setErrorMessage(errorDescription)
				.setServiceStatusType(this.type).setServiceStatusCode(this.code)
				.setClassMethodName(className + " : " + methodName);
	}

	public LoggerBuilder getExceptionLogBuilderWithFlowID(Exception e, String errorDescription, String flowId,
			String className, String methodName) {

		return ResourceBuilder.logger().setLogType(this.logType).setRequestId(flowId).setException(e)
				.setErrorMessage(errorDescription).setServiceStatusType(this.type).setServiceStatusCode(this.code)
				.setClassMethodName(className + " : " + methodName);
	}
}
