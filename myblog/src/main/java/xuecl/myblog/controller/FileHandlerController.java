package xuecl.myblog.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import xuecl.myblog.entity.RspBody;
import xuecl.myblog.util.ExceptionHandler;

@Controller
@RequestMapping("/file")
// 不加classpath可以run 但是过不了mvn test 我也不懂为什么，先不管
@PropertySource(value = {"classpath:config/fileTransferConfig.properties" })
public class FileHandlerController {
    private static String imgRoot; 
    private static String sqlFileRoot;  
    private static String imgBaseHttpUrl = "file/download";
    private static Logger logger = ExceptionHandler.logger;

    @ResponseBody
    @RequestMapping(path = { "imgUpload/{id}" }, method = RequestMethod.POST)
    public RspBody uploadImg(@PathVariable("id") Long id, @RequestParam("name") String filename,
            @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        File imgRootDir = new File(imgRoot);
        File newFile = new File(imgRootDir.getAbsolutePath() + "/" + id + "/" + filename);
        if (newFile.exists())
            return RspBody.failureRspCreate("已存在同名文件，请更改文件名或删除原文件后再上传！");
        newFile.getParentFile().mkdirs();
        try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(newFile));
                BufferedInputStream is = new BufferedInputStream(file.getInputStream())) {
            int data = 0;
            while ((data = is.read()) != -1) {
                os.write(data);
            }
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("url", imgBaseHttpUrl + "?id=" + id + "&filename=" + URLEncoder.encode(filename, "utf8"));
            return RspBody.succesRspCreate(resultMap);
        } catch (Exception e) {
            return ExceptionHandler.exceptionHandler(e);
        }
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(path = { "delete" }, method = RequestMethod.POST)
    @ResponseBody
    public RspBody deleteFile(@RequestBody String paramsObj) throws JsonMappingException, JsonProcessingException {
        File imgRootDir = new File(imgRoot);
        Map<String, Object> paramsMap = new HashMap<>();
        ObjectMapper paramsMapper = new ObjectMapper();
        paramsMap = paramsMapper.readValue(paramsObj, HashMap.class);
        logger.info("输入参数: " + paramsObj);

        Long id = paramsMap.get("id") != null && !"".equals(paramsMap.get("id"))
                ? ((Integer) paramsMap.get("id")).longValue()
                : null;
        String filename = paramsMap.get("filename") != null && !"".equals(paramsMap.get("filename"))
                ? (String) paramsMap.get("filename")
                : null;

        if (id == null)
            return RspBody.failureRspCreate("id不能为空");
        if (filename == null)
            return RspBody.failureRspCreate("filename不能为空");
        File imgFile = new File(imgRootDir.getAbsoluteFile() + "/" + id + "/" + filename);
        if (imgFile.exists()) {
            imgFile.delete();
        } else {
            logger.warn("文件不存在" + imgFile.getAbsolutePath());
            return RspBody.failureRspCreate("文件不存在" + imgFile.getAbsolutePath());
        }
        return RspBody.succesRspCreate(new HashMap<String, Object>());
    }

    @RequestMapping("download")
    public void download(@RequestParam("id") Long id, @RequestParam("filename") String filename,
            HttpServletResponse response) throws Exception {
        File imgRootDir = new File(imgRoot);
        File imgFile = new File(imgRootDir.getAbsoluteFile() + "/" + id + "/" + filename);
        if (!imgFile.exists()) {
            logger.warn("文件不存在" + imgFile.getAbsolutePath());
        }
        response.setHeader("Cache-Control", "no-cache,no-store");
        try (BufferedOutputStream os = new BufferedOutputStream(response.getOutputStream());
                BufferedInputStream is = new BufferedInputStream(new FileInputStream(imgFile))) {
            int data = 0;
            while ((data = is.read()) != -1) {
                os.write(data);
            }
            os.flush();
        } catch (Exception e) {
            ExceptionHandler.exceptionHandler(e);
        }
    }

    @RequestMapping("downloadSqlFile/{date}")
    public void downloadSqlFile(@PathVariable("date") String date,
            HttpServletResponse response) throws Exception {
        File sqlFileDir = new File(sqlFileRoot);
        File sqlFile = new File(sqlFileDir.getAbsoluteFile() + "/" + "bakup_" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".sql");
        if (!sqlFile.exists()) {
            logger.warn("文件不存在" + sqlFile.getAbsolutePath());
        }
        // 好像必须先设置响应头在获取outputstream
        response.setHeader("content-disposition", "attachment; filename=\"backup_" + date + ".sql\"");
        try (BufferedOutputStream os = new BufferedOutputStream(response.getOutputStream());
                BufferedInputStream is = new BufferedInputStream(new FileInputStream(sqlFile))) {
            int data = 0;
            while ((data = is.read()) != -1) {
                os.write(data);
            }
            os.flush();
        } catch (Exception e) {
            ExceptionHandler.exceptionHandler(e);
        }
    }

    public static List<String> queryImgsById(Long id) {
        File imgRootDir = new File(imgRoot);
        String baseUrl = imgBaseHttpUrl + "?id=" + id;
        List<String> list = new ArrayList<String>();
        File imgDir = new File(imgRootDir.getAbsolutePath() + "/" + id + "/");
        if (!imgDir.exists())
            return new ArrayList<String>();
        for (String imgName : imgDir.list()) {
            try {
                list.add(baseUrl + "&filename=" + URLEncoder.encode(imgName, "utf8"));
            } catch (UnsupportedEncodingException e) {
                ExceptionHandler.exceptionHandler(e);
            }
        }
        return list;
    }   

    @Value("${imgRootDir}")
    public void setImgRoot(String imgRoot) {
        FileHandlerController.imgRoot = imgRoot; 
    }
    @Value("${sqlFileRoot}")
    public void setSqlFileRoot(String sqlFileRoot) {
        FileHandlerController.sqlFileRoot = sqlFileRoot; 
    }
}