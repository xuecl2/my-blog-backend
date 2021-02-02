package xuecl.myblog.scheduledtask;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import xuecl.myblog.service.impl.BaseService;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;

import xuecl.myblog.entity.RspBody;

// @Configuration // 1.主要用于标记配置类，兼备Component的效果。
// @EnableScheduling   // 2.开启定时任务
// @PropertySource(value = {"classpath:config/fileTransferConfig.properties" })
public class DataBackup {
    private static String sqlFileRoot;
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    private static Logger logger = BaseService.logger;

    @Scheduled(cron = "0 0 0 * * ?")
    public void sqlFileCreate() {
        String sqlString = insertSqlStringCreate();
        String filename = "bakup_" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".sql";
        File sqlFile = new File(sqlFileRoot + filename);
        sqlFile.getParentFile().mkdirs();
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(sqlFile, "utf8");
            pw.write(sqlString);
            pw.flush();
        } catch (Exception e) {
            exceptionHandler(e);
        } finally {
            try {
                pw.close();
            } catch (Exception e) {
                exceptionHandler(e);
            }
        }
    }

    private String insertSqlStringCreate() {
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList("select * from blog");
        StringBuffer sqlString= new StringBuffer();
        int count = 0;
        for(Map<String, Object> map: resultList) {
            StringBuffer str1 = new StringBuffer("insert into blog (");
            StringBuffer str2 = new StringBuffer("values(");
            for(Entry<String, Object> entry: map.entrySet()) {
                str1.append(entry.getKey() + ",");
                Object value = entry.getValue();
                if(value != null && "String".equals(value.getClass().getSimpleName())) {
                    str2.append("'" + value.toString().replace("'", "''") + "'");
                }else {
                    // if(value != null) {
                    //     System.out.println(value.getClass().getName());
                    // }
                    str2.append(value);
                }
                str2.append(",");
            }
            str1.deleteCharAt(str1.length() - 1).append(") ");
            str2.deleteCharAt(str2.length() - 1).append(");\n");
            sqlString.append(str1);
            sqlString.append(str2);
            count ++;
        }
        logger.info("sql生成成功， 共生成" + count + "条记录");
        System.out.println(sqlString);
        return sqlString.toString();
    }

    @Value("${sqlFileRoot}")
    public void setSqlFileRoot(String sqlFileRoot) {
        DataBackup.sqlFileRoot = sqlFileRoot; 
    }

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