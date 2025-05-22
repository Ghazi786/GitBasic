package com.jio.crm.dms.rest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jetty.server.Server;
import org.reflections.Reflections;

import com.jio.crm.dms.configurationmanager.ConfigParamsEnum;
import com.jio.crm.dms.configurationmanager.InterfaceStatus;
import com.jio.crm.dms.core.BaseContextMapper;
import com.jio.crm.dms.core.DispatcherServletBaseHandler;
import com.jio.crm.dms.core.HttpRequestMethod;
import com.jio.crm.dms.core.annotations.Async;
import com.jio.crm.dms.core.annotations.Controller;
import com.jio.crm.dms.core.annotations.EventName;
import com.jio.crm.dms.core.annotations.RequestMapping;
import com.jio.crm.dms.handler.AlarmHandler;
import com.jio.crm.dms.handler.ConfigParamHandler;
import com.jio.crm.dms.handler.CounterHandler;
import com.jio.crm.dms.handler.DMSMSCLIHandler;
import com.jio.crm.dms.handler.RegistryBroadcastHandler;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.telco.framework.jetty.JettyBuilder;
import com.jio.telco.framework.resource.ResourceBuilder;

/**
 * This class provide method for start and stop jetty server & also register
 * various handler to server
 * 
 * @author Kiran.Jangid
 *
 */

public class JettyRestEngine {

	private Server jettyServer;
	private boolean started = false;

	private static String ROOT_CONTEXT = "/DMS_MS";

	/**
	 * this method used to initialize jetty rest engine with parameters 1.Http Stack
	 * Ip and Port which jetty server will bind 2.Number of thread pool for jetty
	 * Rest engine 3.Handler
	 */

	public void initialise() {
		try {
			DappLoggerService.GENERAL_INFO_LOG.getLogBuilder("Initializing the Jetty interfaces of the system....",
					this.getClass().getName(), "initialise").writeLog();

			Set<String> contextList = new HashSet<>();
			// Alpesh : start
			Reflections reflections = new Reflections("com.jio");
			Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Controller.class);

			BaseContextMapper.getPathMapping().put(HttpRequestMethod.GET.name(), new HashMap<String, String>());
			BaseContextMapper.getPathMapping().put(HttpRequestMethod.PUT.name(), new HashMap<String, String>());
			BaseContextMapper.getPathMapping().put(HttpRequestMethod.POST.name(), new HashMap<String, String>());
			BaseContextMapper.getPathMapping().put(HttpRequestMethod.DELETE.name(), new HashMap<String, String>());
			BaseContextMapper.getPathMapping().put(HttpRequestMethod.PATCH.name(), new HashMap<String, String>());

			for (Class<?> controller : annotated) {
				RequestMapping request = controller.getAnnotation(RequestMapping.class);
				String mapping = request.name();
				mapping = ROOT_CONTEXT + mapping;
				contextList.add(mapping);
				BaseContextMapper.getControllerMapping().put(mapping, controller.getName());
				// method mapping

				for (final Method method : controller.getDeclaredMethods()) {
					if (method.isAnnotationPresent(RequestMapping.class)) {
						RequestMapping annotInstance = method.getAnnotation(RequestMapping.class);
						BaseContextMapper.getPathMapping().get(annotInstance.type().name())
								.put(mapping + annotInstance.name(), method.getName());

						if (method.isAnnotationPresent(Async.class)) {
							BaseContextMapper.getAsyncMapping().add(mapping + annotInstance.name());
						}

						if (method.isAnnotationPresent(EventName.class)) {
							EventName event = method.getAnnotation(EventName.class);
							BaseContextMapper.getEventMapping().put(event.value(), method.getName());
							BaseContextMapper.getEventClassMapping().put(event.value(), controller.getName());
							if (method.isAnnotationPresent(Async.class)) {
								BaseContextMapper.getAsyncEventMapping().add(event.value());
							}
						}
					}
				}
			}
				    
			String ipAddress = ConfigParamsEnum.HTTP_STACK_HOST_IP.getStringValue();

			String profile = System.getProperty("profile");
			if (profile != null && profile.equals("dev")) {
				ipAddress = "localhost";
			}

			JettyBuilder jetty = ResourceBuilder.jetty();
			jetty.setIP(ipAddress).setPort(ConfigParamsEnum.HTTP_STACK_PORT.getIntValue()).setMinThreadsForPool(8)
					.setMaxThreadsForPool(8 * Runtime.getRuntime().availableProcessors());

			// register FCAPs Handler added by Kp Jangid
			jetty.addHandler(ROOT_CONTEXT + "/alarm", AlarmHandler.class);
			jetty.addHandler(ROOT_CONTEXT + "/config", ConfigParamHandler.class);
			jetty.addHandler(ROOT_CONTEXT + "/counter", CounterHandler.class);
			jetty.addHandler(ROOT_CONTEXT + "/registrymanager", RegistryBroadcastHandler.class);
			
			// CLI
			jetty.addHandler(ROOT_CONTEXT + "/cli", DMSMSCLIHandler.class);

			// register global
			jetty.addHandler(ROOT_CONTEXT, DispatcherServletBaseHandler.class);

			// register controllers
			for (String cnxt : contextList) {
				jetty.addHandler(cnxt, DispatcherServletBaseHandler.class);
			}

			this.jettyServer = jetty.start();

			// Alpesh : end

			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder("Jetty interfaces have been initialized.", "JettyRestEngine", "initialise")
					.writeLog();
			InterfaceStatus.status.setJettyInterface(true);
			System.out.println("Jetty interfaces have been initialized.");
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e,
					"Problem during Jetty Server Initialization", "JettyRestEngine", "initialise()")
					.writeExceptionLog();
			InterfaceStatus.status.setJettyInterface(false);
		}
	}

	public Server getServer() {
		return jettyServer;
	}

	/**
	 * Stop jetty server
	 */
	public void stopJettyServer() {
		try {
			jettyServer.stop();
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage());
		}
	}

	public boolean isStarted() {
		return started;
	}

	public String jettyStats() {
		StringBuilder str = new StringBuilder();
		str.append("----------------------------------------------------------------------------------------------"
				+ " \n" + "jetty Server Running State                          : " + jettyServer.getState() + " \n"
				+ "Jetty Server URI                                    : " + jettyServer.getURI() + " \n"
				+ "Jetty Server Version                                : " + Server.getVersion() + " \n"
				+ "Jetty Server StopTimeout                            : " + jettyServer.getStopTimeout() + " \n"
				+ "Maximum Number of Threads Available in The Pool     : " + jettyServer.getThreadPool().getThreads()
				+ " \n" + "Maximum Number of IdleThreds Available in The Pool  : "
				+ jettyServer.getThreadPool().getIdleThreads() + " \n"
				+ "Is ThreadPool Low On Threads                        : "
				+ jettyServer.getThreadPool().isLowOnThreads() + " \n"
				+ "-----------------------------------------------------------------------------------------------");

		return str.toString();

	}
}