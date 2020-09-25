package xuecl.myblog.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import xuecl.myblog.entity.RspBody;
import xuecl.myblog.service.inter.IBlogOperateService;
import xuecl.myblog.util.ExceptionHandler;

@RestController
public class BlogHandlerController {
	@Autowired
	private IBlogOperateService blogOperateService;
	private static Logger logger = ExceptionHandler.logger;
    
	@RequestMapping(path = {"/"},method = RequestMethod.POST)
	@SuppressWarnings("uncheck") 
    public RspBody blogHandleController (@RequestBody String body1){
        try {
			logger.info("输入参数: " + body1);
			Map<String,Object> map = new HashMap<>();
			ObjectMapper  mapper = new ObjectMapper();
			map =  mapper.readValue(body1, HashMap.class);
			Map<String, Object> data = (Map<String, Object>)map.get("data");
			if("blogHandle".equals(map.get("tradeCode"))) {
				return blogOperateService.blogOperate(data);
			}else {
				return ExceptionHandler.exceptionHandler(new Exception("没有对应的交易码处理类"));
			}
			
		} catch(Exception e) {
			return ExceptionHandler.exceptionHandler(e);
		}


	}
}