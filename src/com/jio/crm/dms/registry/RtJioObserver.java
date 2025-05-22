package com.jio.crm.dms.registry;

import java.util.Observable;
import java.util.Observer;

import com.jio.crm.dms.configurationmanager.InterfaceStatus;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.Constants;

public class RtJioObserver implements Observer {
	@Override
	public void update(Observable arg0, Object arg) {

		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder("Observer received : " + arg, Constants.RTJIOOBSERVER, Constants.UPDATE).writeLog();
		if (arg.toString().equals("200")) {

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder("Start component further task", Constants.RTJIOOBSERVER, Constants.UPDATE)
					.writeLog();
			InterfaceStatus.status.setRegisterWithOAM(true);

		} else if (arg.toString().equals("500")) {

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder("############# OAM IS DOWN #############", Constants.RTJIOOBSERVER, Constants.UPDATE)
					.writeLog();
			InterfaceStatus.status.setRegisterWithOAM(false);
		}
	}

}
