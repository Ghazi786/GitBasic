package com.jio.crm.dms.core;

import java.util.concurrent.atomic.AtomicBoolean;

import com.jio.crm.dms.logger.DappLoggerService;
import com.rancore.ha.cluster.ClusterManager;

import io.netty.channel.Channel;

public class ChannelManager {

	private static AtomicBoolean selectActive = new AtomicBoolean(true);

	public static Channel getChannelByRoundRobin() {
		Channel activeChannel = ClusterManager.clusterManager.getActiveChannel();
		Channel standBychannel = ClusterManager.clusterManager.getStandByChannel();

		try {
			if (activeChannel == null && standBychannel == null)
				return null;
			if (selectActive.get()) {
				selectActive.set(false);

				if (activeChannel != null) {
					return activeChannel;
				} else {
					return standBychannel;

				}

			} else {
				selectActive.set(true);
				if (standBychannel != null) {
					return standBychannel;
				} else {
					return activeChannel;

				}
			}
		} catch (Exception e) {
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage()).writeExceptionLog();
		}
		return null;

	}

	private ChannelManager() {
		super();

	}
}
