package xuecl.myblog.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import xuecl.myblog.entity.RspBody;

public class BaseController {
	@Autowired
	protected JdbcTemplate jdbcTemplate;
	protected static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    // 打印异常信息的堆栈
 	protected static String getStackTraceString(Throwable ex){//(Exception ex) {   
 		StackTraceElement[] traceElements = ex.getStackTrace();        
 		StringBuilder traceBuilder = new StringBuilder();         
 		if (traceElements != null && traceElements.length > 0) {            
 			for (StackTraceElement traceElement : traceElements) {                
 				traceBuilder.append(traceElement.toString());                
 				traceBuilder.append("\n");            
 			}        
 		}         
 		return traceBuilder.toString();
 	}
    
 	protected static RspBody exceptionHandler(Exception e) {
 		logger.error(e.getMessage());
		logger.error(getStackTraceString(e));
		if(e.getMessage() != null && e.getMessage().length() > 100) {
			return RspBody.failureRspCreate(e.getMessage().substring(0,100));
		}
		return RspBody.failureRspCreate(e.getMessage());
 	}
}
