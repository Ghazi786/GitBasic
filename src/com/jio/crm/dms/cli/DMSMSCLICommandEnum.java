package com.jio.crm.dms.cli;

import java.util.HashMap;
import com.jio.crm.dms.clicommands.*;


public enum DMSMSCLICommandEnum {

	GET_PARAM("getparam", new DMSMSGetParamCommand()),
	
	GET_MOD_STAT("getmodstat", new DMSMSGetModStatusCommand()),

	APP_INFO("appinfo" , new DMSMSApplicationInfoCommand()),
	
	KAFKA_INFO("kafkainfo" , new DMSMSGetKafkaInfoCommand()),
	
	NH_INFO("nhinfo" , new DMSMSNotificationHubInfoCommand()),
	
	REGISTER_WITH_NH("registerwithnh" , new  DMSMSRegisterNotificationHubCommand()),
	
	UNREGISTER_WITH_NH("unregisterwithnh" , new  DMSMSUnregsiterNotificationHubCommand()),
	
	THREAD_INFO("threadinfo" , new  DMSMSGetThreadInfoCommand()),
	
	POOL_INFO("poolinfo" , new  DMSMSGetPoolInfoCommand()),
	
	GET_PARAMS("getparams" , new  DMSMSGetParamsCommand()),
	
	GET_ALARMS("getalarms" , new  DMSMSGetAlarmsCommand()),
	
	CLEAR_ALARMS("clearalarm" , new  DMSMSClearAlarmsCommand()),
	
	GET_COUNTER_CATEGORY("getcountercategory" , new  DMSMSGetCounterCategoryCommand()),
	
	GET_COUNTERS("getcounters" , new  DMSMSGetCountersCommand()),
	
	DUMP_COUNTERS("dumpcounters" , new  DMSMSDumpCountersCommand()),
	
	GET_SUBSCRIBED_SYSTEM("getSubscribedSystem" , new  DMSMSGetSubscribedSystemCommand()),	// not working
	
	GET_PARTNERED_SYSTEM("getPartnerSystem" , new  DMSMSGetPartneredSystemCommand()),		// not working
	
	REREGISTER_WITH_OAM("reRegisterwithOAM"  , new  DMSMSReregisterWithOAMCommand()),
	
	DEREGISTER_OAM("deRegisterOAM" , new  DMSMSDeregisterWithOAMCommand()),
	
	FMS_INFO("fmsinfo" , new  DMSMSFMSInfoCommand()),
	
	SUBSCRIBE_WTIH_FMS("subscribewithfms" , new  DMSMSSubscribeWithFMSCommand()),
	
	UNSUBSCRIBE_WTIH_FMS("unsubscribewithfms" , new  DMSMSUnsubscribeWithFMSCommand());
	
	private String commandName;
	private DMSMSAbstractCLICommand command;
	private static final HashMap<String, DMSMSCLICommandEnum> commandEnumMap = new HashMap<>();

	static {
		for (DMSMSCLICommandEnum commandEnum : DMSMSCLICommandEnum.values()) {
			commandEnumMap.put(commandEnum.getCommandName(), commandEnum);
			commandEnum.command.setCommandName(commandEnum.commandName);
		}
	}

	private DMSMSCLICommandEnum(String commandName, DMSMSAbstractCLICommand command) {
		this.commandName = commandName;
		this.command = command;
	}
	
	/**
	 * executes a CLI command and return its output
	 * 
	 * @param jsonObject
	 *            the input JSON object
	 * @return the output of command (if applicable)
	 * @throws InvalidRequestParamValueException 
	 */
	public String execute(DMSMSCLIPojo cliData) {
		return this.command.execute(cliData);
	}

	String getCommandName() {
		return commandName;
	}

	DMSMSAbstractCLICommand getCommand() {
		return command;
	}

	/**
	 * returns the enumeration instance from the command name
	 * 
	 * @param command
	 *            name of the command
	 * @return the enumeration instance
	 */
	public static final DMSMSCLICommandEnum getEnumFromCommandName(String command) {
		return commandEnumMap.get(command);
	}
	
}
