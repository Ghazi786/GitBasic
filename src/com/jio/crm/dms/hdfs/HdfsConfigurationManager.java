/**
 * 
 */
package com.jio.crm.dms.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

/**
 * @author Ghajnafar.Shahid
 *
 */
public class HdfsConfigurationManager {

	private static String coreSiteXmlPath = HdfsConstants.CORE_SITE_XML;
	private static String hdfsSiteXmlPath = HdfsConstants.HDFS_SITE_XML;
	private static Configuration conf;

	public static synchronized Configuration getConfiguration() {

		if (conf == null) {
			String profile = System.getProperty("profile");
			if (profile != null && profile.equals("dev")) {
				coreSiteXmlPath = "./configuration/core-site.xml";
				hdfsSiteXmlPath = "./configuration/hdfs-site.xml";
			}
			conf = new Configuration();
			Path p1 = new Path(coreSiteXmlPath);
			Path p2 = new Path(hdfsSiteXmlPath);
			conf.addResource(p1);
			conf.addResource(p2);
		}
		return conf;
	}

	private HdfsConfigurationManager() {
		super();
	}

}
