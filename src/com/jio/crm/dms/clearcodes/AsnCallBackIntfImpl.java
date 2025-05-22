package com.jio.crm.dms.clearcodes;

import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.telco.framework.clearcode.XdrAsnCallBackIntf;

public class AsnCallBackIntfImpl implements XdrAsnCallBackIntf {
	private static final String CLASS_NAME = AsnCallBackIntfImpl.class.getSimpleName();
	private static final String ERROR_MESSAGE = "ERROR in xdr processing: ";
	private static final String ERROR_STR = "Error ";

	@Override
	public void authFailForXDR(String arg0) {

		DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getErrorLogBuilder(ERROR_STR + ERROR_MESSAGE + arg0, CLASS_NAME,
				"authFailForXDR");

	}

	@Override
	public void connectFailForXDR(String arg0) {

		DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getErrorLogBuilder(ERROR_STR + ERROR_MESSAGE + arg0, CLASS_NAME,
				"connectFailForXDR");

	}

	@Override
	public void ftpFailForXDR(String arg0) {

		DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getErrorLogBuilder(ERROR_STR + ERROR_MESSAGE + arg0, CLASS_NAME,
				"ftpFailForXDR");

	}

	@Override
	public void noSpaceForXDR(String arg0) {

		DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getErrorLogBuilder(ERROR_STR + ERROR_MESSAGE + arg0, CLASS_NAME,
				"noSpaceForXDR");

	}

	@Override
	public void writeFailForXDR(String arg0) {

		DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getErrorLogBuilder(ERROR_STR + ERROR_MESSAGE + arg0, CLASS_NAME,
				"writeFailForXDR");

	}

}
