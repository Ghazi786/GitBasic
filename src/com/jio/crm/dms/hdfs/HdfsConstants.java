/**
 * 
 */
package com.jio.crm.dms.hdfs;

import com.jio.crm.dms.logger.DappLoggerService;

/**
 * @author Ghajnafar.Shahid
 *
 */
public class HdfsConstants {

	public static final String CORE_SITE_XML = "../configuration/core-site.xml";
	public static final String HDFS_SITE_XML = "../configuration/hdfs-site.xml";

	private HdfsConstants() {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [ " + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();
	}

}
