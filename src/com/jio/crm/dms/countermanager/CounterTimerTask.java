package com.jio.crm.dms.countermanager;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.Constants;

/**
 * 
 * This class execute task to dump counter in xml file
 * 
 * @author Ashish14.Gupta
 *
 */

public class CounterTimerTask extends TimerTask {

	private String fileName = "CmnCounterPurgeTask";
	private String dirName = "../purge";

	@Override
	public void run() {

		try (FileOutputStream fout = new FileOutputStream(new File(dirName + Constants.DELIMITER + fileName + "-"
				+ new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date())))) {

			File dir = new File(dirName);

			if (!dir.exists())
				dir.mkdir();

			fout.write(CounterManager.getInstance().fetchCounterAsXml().getBytes());
			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder("purging of counter completed", this.getClass().getName(), "run").writeLog();

		} catch (Exception e) {

			DappLoggerService.GENERAL_ERROR_FAILURE_LOG
					.getExceptionLogBuilder(e, e.getMessage(), this.getClass().getName(), "run").writeExceptionLog();
		}

	}

}
