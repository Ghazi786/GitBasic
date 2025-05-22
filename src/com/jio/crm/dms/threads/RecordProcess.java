package com.jio.crm.dms.threads;

import org.json.JSONObject;

import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.es.ESOperationImpl;
import com.jio.crm.dms.utils.Constants;

/*
 * Dummy thread process to show the working.
 */
public class RecordProcess implements Runnable {

	JSONObject jsonObject;
	String id;

	public void setRankData(JSONObject jsonObject) {
		this.jsonObject = jsonObject;
	}

	@Override
	public void run() {
		try {
			double token = calculateToken();
			jsonObject.put(Constants.TOKENS, token);
			ESOperationImpl.getInstance().addRecordWithToken(jsonObject, id);
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, "error while calculating token");
		}

	}

	private double calculateToken() {
		return Constants.THOUSAND;

	}

	public void setId(String id) {
		this.id = id;
	}

}
