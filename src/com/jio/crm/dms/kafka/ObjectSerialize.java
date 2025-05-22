package com.jio.crm.dms.kafka;

import java.util.Map;

import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.ObjectMapperHelper;

public class ObjectSerialize implements Serializer {

	@Override
	public byte[] serialize(String topic, Object data) {
		byte[] retVal = null;
		ObjectMapper objectMapper = ObjectMapperHelper.getInstance().getEntityObjectMapper();
		try {
			retVal = objectMapper.writeValueAsString(data).getBytes();
		} catch (Exception exception) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getLogBuilder(
							"Error in serializing object" + data + this.getClass().getName() + "."
									+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
							this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeLog();
		}
		return retVal;
	}

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
	public void configure(Map arg0, boolean arg1) {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [ " + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

	}

}
