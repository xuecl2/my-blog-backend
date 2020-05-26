package xuecl.myblog.entity;

import java.util.HashMap;
import java.util.Map;

public class RspBody {
	public static final String SUCCESS_CODE = "000000";
	public static final String DEFAULT_FAILURE_CODE = "999999";
	private String responseCode;
	private String responseMessage;
	private Map<String, Object> body;
	
	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	public Map<String, Object> getBody() {
		return body;
	}
	public void setBody(Map<String, Object> body) {
		this.body = body;
	}	
	public static RspBody succesRspCreate(Map<String, Object> body) {
		RspBody rsp = new RspBody();
		rsp.setResponseCode(RspBody.SUCCESS_CODE);
		rsp.setResponseMessage("");
		rsp.setBody(body);
		return rsp;
	}
	public static RspBody succesRspCreate(Object... objs) {
		RspBody rsp = new RspBody();
		rsp.setResponseCode(RspBody.SUCCESS_CODE);
		rsp.setResponseMessage("");
		Map<String, Object> map = new HashMap<>();
		if(!(objs.length % 2 == 0)) throw new RuntimeException("Map的参数长度必须为2的整数倍");
		for(int i = 0; i<objs.length; i+=2) {
			if(!(objs[i] instanceof String)) throw new RuntimeException("Map的奇数位参数必须为String类型");
			map.put((String) objs[i], objs[i+1]);
		}
		rsp.setBody(map);
		return rsp;
	}
	public static RspBody failureRspCreate(String errCode, String errMsg) {
		RspBody rsp = new RspBody();
		rsp.setResponseCode(errCode);
		rsp.setResponseMessage(errMsg);
		rsp.setBody(new HashMap<String,Object>());
		return rsp;
	}
	public static RspBody failureRspCreate(String errMsg) {
		RspBody rsp = new RspBody();
		rsp.setResponseCode(RspBody.DEFAULT_FAILURE_CODE);
		rsp.setResponseMessage(errMsg);
		rsp.setBody(new HashMap<String,Object>());
		return rsp;
	}
}
