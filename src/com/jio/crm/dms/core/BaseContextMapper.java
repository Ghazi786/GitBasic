package com.jio.crm.dms.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jio.crm.dms.utils.MaxSizeHashMap;

public class BaseContextMapper {

	private static final Map<String, Map<String, String>> PATH_MAPPING = new HashMap<>();

	private static final Map<String, String> CONTROLLER_MAPPING = new HashMap<>();

	private static final List<String> ASYNC_MAPPING = new ArrayList<>();

	private static final Map<String, String> EVENT_MAPPING = new HashMap<>();

	private static final Map<String, String> EVENT_CLASS_MAPPING = new HashMap<>();

	private static final List<String> ASYNC_EVENT_MAPPING = new ArrayList<>();

	// role base authentication
	protected static final Map<String, List<String>> VENDOR_ROLES_LIST = new MaxSizeHashMap<>(10000);

	public static Map<String, Map<String, String>> getPathMapping() {
		return PATH_MAPPING;
	}

	public static Map<String, String> getControllerMapping() {
		return CONTROLLER_MAPPING;
	}

	public static List<String> getAsyncMapping() {
		return ASYNC_MAPPING;
	}

	public static Map<String, String> getEventMapping() {
		return EVENT_MAPPING;
	}

	public static Map<String, String> getEventClassMapping() {
		return EVENT_CLASS_MAPPING;
	}

	public static List<String> getAsyncEventMapping() {
		return ASYNC_EVENT_MAPPING;
	}

	private BaseContextMapper() {
		super();

	}

}
