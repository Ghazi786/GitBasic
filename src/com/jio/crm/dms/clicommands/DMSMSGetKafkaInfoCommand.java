package com.jio.crm.dms.clicommands;

import com.jio.crm.dms.cli.DMSMSAbstractCLICommand;
import com.jio.crm.dms.cli.DMSMSCLIPojo;
import com.jio.crm.dms.kafka.KafkaConfigEnum;
import com.jio.crm.dms.logger.DappLoggerService;

public class DMSMSGetKafkaInfoCommand extends DMSMSAbstractCLICommand {

	@Override
	public String execute(DMSMSCLIPojo cliData) {
		DappLoggerService.GENERAL_INFO_LOG
		.getLogBuilder(
				"Executing [ " + this.getClass().getName() + "."
						+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
				this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
		.writeLog();
		
		String[] s = KafkaConfigEnum.KAFKA_BROKERS.getStringValue().split(":");
		StringBuilder sbr = new StringBuilder();
		
		sbr.append("------------------------------------------\n");
		sbr.append("Name: Kafka info ");
		sbr.append("\nIP : ");
		sbr.append(s[0]);
		sbr.append("\nPORT : ");
		sbr.append(s[1]);
		sbr.append("\n------------------------------------------\n");
		
		return sbr.toString();
	}

}
