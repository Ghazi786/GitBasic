package com.jio.crm.dms.utils;

import java.util.ArrayList;
import java.util.List;

import com.jio.crm.dms.core.BaseModal;

public class Group {
	String key;
	List<BaseModal> members;

	Group(String key) {
		this.key = key;
		this.members = new ArrayList<>();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<BaseModal> getMembers() {
		return members;
	}

	public void setMembers(List<BaseModal> members) {
		this.members = members;
	}
}
