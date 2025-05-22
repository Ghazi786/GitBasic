package com.jio.crm.dms.kafka;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.poi.ss.formula.functions.T;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jio.crm.dms.logger.DappLoggerService;

public class ObjectDeserialize implements Deserializer<T> {

	@Override
	public void close() {
		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				"Executing [ " + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();
	}

	@Override
	public T deserialize(String arg0, byte[] arg1) {
		ObjectMapper mapper = com.jio.crm.dms.utils.ObjectMapperHelper.getInstance().getEntityObjectMapper();
		T obj = null;
		try {
			obj = mapper.readValue(arg1, T.class);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}
		return obj;
	}

	@Override
	public void configure(Map arg0, boolean arg1) {
		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				"Executing [ " + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();
	}

}
