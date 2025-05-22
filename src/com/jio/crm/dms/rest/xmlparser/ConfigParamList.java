package com.jio.crm.dms.rest.xmlparser;

import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import com.atom.OAM.Client.ems.pojo.config.TableConfigParam;

/**
 * 
 * Class to create all the parameter information in a specific format. e.g.
 * 
 * <ConfigParam type=""> <Param name="" value=""> -------------- --------------
 * <Param name="" value=""> </ConfigParam>
 * 
 */
@Root(name = "configparams")
public class ConfigParamList {

	@ElementList(required = false, empty = false, inline = true, type = ConfigParamCategory.class)
	private ArrayList<ConfigParamCategory> configParams;
	@ElementList(required = false, empty = false, inline = true, type = TableConfigParam.class)
	private List<TableConfigParam> configTableConfigParam;

	public ConfigParamList() {
		this.configParams = new ArrayList<>();
		this.configTableConfigParam = new ArrayList<>();
	}

	public void addConfigParams(List<ConfigParamCategory> params) {
		this.configParams.addAll(params);
	}

	public void addToConfigParamList(ConfigParamCategory param) {
		this.configParams.add(param);
	}

	public List<ConfigParamCategory> getConfigParam() {
		return this.configParams;
	}

	public void clearConfigParam() {
		this.configParams.clear();
	}

	public List<TableConfigParam> getTableConfigParam() {
		if (configTableConfigParam == null) {
			configTableConfigParam = new ArrayList<>();
		}
		return configTableConfigParam;
	}

	public void setTableConfigParam(
			List<TableConfigParam> configTableConfigParam) {
		this.configTableConfigParam = configTableConfigParam;
	}

	public void addToTableConfigParam(TableConfigParam tableConfigParam) {
		this.configTableConfigParam.add(tableConfigParam);

	}
}
