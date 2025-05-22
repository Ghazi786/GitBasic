package com.jio.crm.dms.node.shutdown;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.jio.crm.dms.configurationmanager.RuntimeConfigurationEngineMBean;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.utils.DappParameters;

public class GracefulShutdown {

	
	private GracefulShutdown() {

	}

	/**
	 * Gracefully shutdown application
	 * 
	 * @param args
	 * @throws MalformedObjectNameException
	 */
	public static void main(String[] args) throws IOException, MalformedObjectNameException {

		FileInputStream f = null;
		String jmxIP = null;
		String jmxPort = null;
		Properties env = new Properties();
		try {
			jmxIP = InetAddress.getLocalHost().getHostName();
			String os = System.getProperty("os.name").toLowerCase();

			// Check if operating system is windows or linux.
			if (os.indexOf("win") >= 0) {
				f = new FileInputStream("./run-as.bat");
				int i;
				StringBuffer str = new StringBuffer();

				while ((i = f.read()) != -1)
					str.append((char) i);
				f.close();

				String[] str1 = new String(str).split("jmxport=");
				jmxPort = str1[1].substring(0, str1[1].indexOf('\n') - 1);

			} else {
				f = new FileInputStream("./run-as.sh");
				env.load(f);
				jmxPort = env.getProperty("jmxport");

			}

		} finally {
			if (f != null)
				f.close();

		}

		try {
			RuntimeConfigurationEngineMBean configMBean;

			configMBean = getERMRuntimeConfigurationEngineMBeanReference(jmxPort, jmxIP);
			if (configMBean != null)
				configMBean.gracefulSHutdown();
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(),
					"", Thread.currentThread().getStackTrace()[1].getMethodName())
					.writeExceptionLog();
		}

	}

	private static RuntimeConfigurationEngineMBean getERMRuntimeConfigurationEngineMBeanReference(String jmxPort,
			String jmxIP) throws IOException, MalformedObjectNameException {
		JMXServiceURL url;
		MBeanServerConnection mbsc;

		url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + jmxIP + ":" + jmxPort + "/jmxrmi");
		JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
		mbsc = jmxc.getMBeanServerConnection();
		RuntimeConfigurationEngineMBean mbeanProxy = JMX.newMBeanProxy(mbsc,
				new ObjectName(DappParameters.MBEAN_NAME_PARAMS), RuntimeConfigurationEngineMBean.class, true);
		jmxc.close();
		return mbeanProxy;

	}

}
