package com.fjsh.rpc.common;

import com.fjsh.rpc.connection.utils.BaseMsg;

public class RpcResponse {

	private String requestId;
    private Throwable error;
    private Object result;
    private BaseMsg baseMsg;
    
	public BaseMsg getBaseMsg() {
		return baseMsg;
	}
	public void setBaseMsg(BaseMsg baseMsg) {
		this.baseMsg = baseMsg;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public Throwable getError() {
		return error;
	}
	public void setError(Throwable error) {
		this.error = error;
	}
	public Object getResult() {
		return result;
	}
	public void setResult(Object result) {
		this.result = result;
	}
    
}
