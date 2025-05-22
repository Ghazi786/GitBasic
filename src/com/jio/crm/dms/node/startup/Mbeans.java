package com.jio.crm.dms.node.startup;

import com.j256.simplejmx.common.JmxAttributeField;
import com.j256.simplejmx.common.JmxAttributeMethod;
import com.j256.simplejmx.common.JmxOperation;
import com.j256.simplejmx.common.JmxResource;

@JmxResource(domainName = "j256.simplejmx", description = "Counter that shows how long we have been running")
public class Mbeans {
	private long startMillis = System.currentTimeMillis();
	@JmxAttributeField(isWritable = true, description = "Show the time in seconds if true else milliseconds")
	private boolean showSeconds;

	@JmxAttributeMethod(description = "The time we have been running in seconds or milliseconds")
	public long getRunTime() {
		long diffMillis = System.currentTimeMillis() - startMillis;
		if (showSeconds) {
			return diffMillis / 1000;
		} else {
			return diffMillis;
		}
	}

	@JmxOperation(description = "Reset the start time to the current time millis")
	public String resetStartTime() {
		startMillis = System.currentTimeMillis();
		return "Timer has been reset to current millis";
	}

	@JmxOperation(description = "Add a positive or negative offset to the start time milliseconds", parameterNames = {
			"offset in millis" }, parameterDescriptions = { "offset milliseconds value to add to start time millis" })
	public String addToStartTime(long offset) {
		long old = startMillis;
		startMillis += offset;
		return "Timer value changed from " + old + " to " + startMillis;
	}

}
