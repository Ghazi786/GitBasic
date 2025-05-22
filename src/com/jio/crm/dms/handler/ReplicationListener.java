package com.jio.crm.dms.handler;

import java.net.InetSocketAddress;

import com.jio.crm.dms.core.RPCIdentifierConstant;
import com.jio.crm.dms.core.RPCSerializer;
import com.jio.crm.dms.logger.DappLoggerService;
import com.rjil.rpc.cluster.ClusteringAndReplicationListener;
import com.rjil.rpc.cluster.RPCManager;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

public class ReplicationListener implements ClusteringAndReplicationListener {

	@Override
	public void OnConnectionDown(Channel channel) {
		// update two maps
		String remoteAddress = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder("OnConnectionDown packet received from :" + remoteAddress,
				this.getClass().getName(), "OnConnectionDown").writeLog();

	}

	@Override
	public void onConnectionUp(Channel channel) {
		String remoteAddress = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder("onConnectionUp packet received from :" + remoteAddress,
				this.getClass().getName(), "onConnectionUp").writeLog();

	}

	@Override
	public void onData(ByteBuf buf, boolean arg1, Channel channnel) {
		RPCManager.rpcManager.getLogger().info("Packet received from :" + channnel.remoteAddress());

		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder("onData Packet received from :" + channnel.remoteAddress(),
				this.getClass().getName(), "onData").writeLog();

		switch (buf.readByte()) {
		case RPCIdentifierConstant.CUSTOMER_ID:

			RPCSerializer pojo = new RPCSerializer();
			pojo.decodeAll(buf);
			DappLoggerService.GENERAL_INFO_LOG
					.getLogBuilder("onData :" + pojo.getJsonContent(), this.getClass().getName(), "onData").writeLog();
			break;
		default:
			break;

		}

	}

	@Override
	public void onGeoConnectionDown(Channel ch) {
		DappLoggerService.GENERAL_INFO_LOG.getLogBuilder("Connection down packet received from :" + ch.remoteAddress(),
				this.getClass().getName(), "onGeoConnectionDown").writeLog();
	}

	@Override
	public void onGeoConnectionUp(Channel geoChannel) {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder("Connection up packet received from :" + geoChannel.remoteAddress(),
						this.getClass().getName(), "onGeoConnectionUp")
				.writeLog();

	}

	@Override
	public void onObject(Object arg0, Channel arg1) {
		DappLoggerService.GENERAL_INFO_LOG
				.getLogBuilder(
						"Executing [ " + this.getClass().getName() + "."
								+ Thread.currentThread().getStackTrace()[1].getMethodName() + " ]",
						this.getClass().getName(), Thread.currentThread().getStackTrace()[1].getMethodName())
				.writeLog();

	}

}
