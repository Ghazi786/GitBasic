package com.jio.crm.dms.core;

import com.rjil.rpc.cluster.RPCManager;
import com.rjil.rpc.cluster.codec.CustomSerializer;

import io.netty.buffer.ByteBuf;

public class RPCSerializer extends CustomSerializer {

	private String jsonContent;
	private int identifier;

	public RPCSerializer() {
		
	}
	
	public RPCSerializer(String jsonContent, int identifier) {
		this.jsonContent = jsonContent;
		this.identifier = identifier;
	}

	@Override
	public Object decode(byte[] arg0) {
		return null;
	}

	@Override
	public Object decodeAll(ByteBuf buf) {
		this.jsonContent = readString(buf);
		return this;
	}

	@Override
	public byte[] encode() {
		return null;
	}

	@Override
	public ByteBuf encodeAll() {
		ByteBuf buffer = RPCManager.rpcManager.alloc.buffer();
		buffer.writeByte(identifier);
		writeString(buffer, this.jsonContent);
		return buffer;
	}

	public String getJsonContent() {
		return jsonContent;
	}
}