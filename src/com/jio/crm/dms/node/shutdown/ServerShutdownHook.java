package com.jio.crm.dms.node.shutdown;

import java.io.IOException;

import com.atom.OAM.Client.Management.OamClientManager;
import com.jio.blockchain.sdk.bootstrap.SDKBootStrap;
import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.crm.dms.node.es.ESConnection;
import com.jio.crm.dms.node.startup.DmsBootStrapper;

public class ServerShutdownHook extends Thread {

	private DmsBootStrapper bootStrapper;

	/**
	 * 
	 * @param bootStrapper
	 */
	public ServerShutdownHook(DmsBootStrapper bootStrapper) {
		this.bootStrapper = bootStrapper;
	}

	@Override
	public void run() {

		if (bootStrapper.getConfigEngine().dumpConfigParams()) {

		}
		try {
			stopMbeans();
			SDKBootStrap.getInstance().shutDownSdk();
			DmsBootStrapper.getInstance().shutdown_node();
			DmsBootStrapper.getInstance().getDappExecutor().shutdown();

			ESConnection.getInstance().getClient().close();

			OamClientManager.getInstance().disconnect();
			bootStrapper.getJettyRestEngine().stopJettyServer();

		} catch (IOException e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();

		}
	}

	private void stopMbeans() {

		bootStrapper.getConfigEngine().stopMbean();

	}

}
