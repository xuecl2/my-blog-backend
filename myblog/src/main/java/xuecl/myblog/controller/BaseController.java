package xuecl.myblog.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URLDecoder;
import java.sql.PreparedStatement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import xuecl.myblog.entity.RspBody;

@RestController
public class BaseController {
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	private File imgBaseDirSrc = new File("src/main/resources/static/img/"); 
	private File imgBaseDirTar = new File("target/classes/static/img/");	
	private String imgBaseHttpUrl = "img/";
	private static final Logger logger = LoggerFactory.getLogger(BaseController.class);
	
    @SuppressWarnings("unchecked")
	@RequestMapping(path = {"/"},method = RequestMethod.POST)
    public RspBody baseController (@RequestBody String body1){
        try {
			String data = URLDecoder.decode(body1, "UTF-8");
			logger.info(data);
			
			Map<String,Object> map = new HashMap<>();
			ObjectMapper  mapper = new ObjectMapper();
			map =  mapper.readValue(data, HashMap.class);
			logger.info("输入参数 " + data);
			
			switch ((String)map.get("operation")) {
			case "save":{
				Long id = map.get("id") != null && !"".equals(map.get("id"))?((Integer)map.get("id")).longValue():null;
				String title = map.get("title") != null && !"".equals(map.get("title"))?(String)map.get("title"):null;
				String keyWord = map.get("keyWord") != null && !"".equals(map.get("keyWord"))?(String)map.get("keyWord"):null;
				String content = map.get("content") != null?(String)map.get("content"):null;
				String user = map.get("user") != null && !"".equals(map.get("user"))?(String)map.get("user"):null;
				String digest = map.get("digest") != null && !"".equals(map.get("digest"))?(String)map.get("digest"):null;
				long autoGenId = save(title, keyWord, content, user, digest, id);
				if(autoGenId != -1) {
					return RspBody.succesRspCreate("id",autoGenId);
				}
				return RspBody.succesRspCreate(new HashMap<String,Object>());
			}
				
			case "delete":{
				Long id = map.get("id") != null && !"".equals(map.get("id"))?((Integer)map.get("id")).longValue():null;
				delete(id);
				return RspBody.succesRspCreate(new HashMap<String,Object>());
			}
			case "query":{
				Map<String,String> queryParams = new HashMap<>();
				if(map.get("title") != null && !"".equals(map.get("title"))) queryParams.put("blogTitle", (String)map.get("title"));
				if(map.get("keyWord") != null && !"".equals(map.get("keyWord"))) queryParams.put("blogKeyWord", (String)map.get("keyWord"));
				if(map.get("date") != null && !"".equals(map.get("date"))) queryParams.put("modifyDate", (String)map.get("date"));
				List<Map<String,Object>> resultList = query(queryParams);
//				if(resultList.size() == 0) return RspBody.failureRspCreate("无满足条件的记录");
				return RspBody.succesRspCreate("resultList",resultList);
			}
			case "queryById": {
				Long id = map.get("id") != null && !"".equals(map.get("id"))?((Integer)map.get("id")).longValue():null;
				List<Map<String, Object>> resultList = queryById(id);
				if(resultList.size() != 1) return RspBody.failureRspCreate("查询结果条数异常");
				resultList.get(0).put("fileList",queryImgsById(id));
				return RspBody.succesRspCreate("blogObject",resultList.get(0));
			}			
			case "deleteFile": {
				Long id = map.get("id") != null && !"".equals(map.get("id"))?((Integer)map.get("id")).longValue():null;
				String filename = map.get("filename")!= null && !"".equals(map.get("filename"))?((String)map.get("filename")):null;
				if(id == null) return RspBody.failureRspCreate("id不能为空");
				if(filename == null) return RspBody.failureRspCreate("filename不能为空");
				File fileFromSrc = new File(this.imgBaseDirSrc.getAbsoluteFile() + "/" + id + "/" + filename);
				File fileFromTar = new File(this.imgBaseDirTar.getAbsoluteFile() + "/" + id + "/" + filename); 
				if(fileFromSrc.exists() && fileFromTar.exists()) {
					fileFromSrc.delete();
					fileFromTar.delete();
				}else {
					logger.warn("文件不存在" + fileFromSrc.getAbsolutePath() + ";" + fileFromTar.getAbsolutePath());
					return RspBody.failureRspCreate("文件不存在" + fileFromSrc.getAbsolutePath() + ";" + fileFromTar.getAbsolutePath());
				}
				return RspBody.succesRspCreate(new HashMap<String, Object>());
			}
			default:
				break;
			}
		} catch(Exception e) {
			return exceptionHandler(e);
		}
        return null;
    }
    
    private List<Map<String, Object>> query(Map<String,String> queryParams){
    	String queryConditionSql = createQueryConditionSql(queryParams);
    	logger.info("queryConditionSql: " + queryConditionSql);
    	return jdbcTemplate.queryForList("select idblog as id, blogTitle, blogKeyWord, "
    			+ "createDate, createUser, modifyDate, modifyUser, isDel, blogDigest"
    			+ " from blog where " + queryConditionSql + " and isDel = '0';");
    }
    
    private List<Map<String, Object>> queryById(Long id){
    	return jdbcTemplate.queryForList("select idblog as id, blogTitle, blogKeyWord, "
    			+ "blogContent, createDate, createUser, modifyDate, modifyUser, isDel, blogDigest"
    			+ " from blog where idblog = " + id + " and isDel = '0';");
    }
    
    private String createQueryConditionSql(Map<String,String> queryParams) {
    	if(queryParams.size() == 0) return " 1 = 1 ";
    	StringBuilder sql = new StringBuilder();
    	for(Entry<String, String> entry: queryParams.entrySet()) {
    		if(!"blogKeyWord".equals(entry.getKey())) {
    			sql.append(entry.getKey() + "=" + "'" + entry.getValue() + "' ");
    		}else {
    			sql.append(createKeyWordConditionSql(entry.getValue()));
    		}
    		sql.append(" and ");
    	}
    	return sql.substring(0, sql.length() -4);
    }
    
    private String createKeyWordConditionSql(String keyWord) {
    	String[] keyWords = keyWord.trim().split(" ");
    	StringBuilder sql = new StringBuilder("(");
    	List<String> keyWordList = new ArrayList<>();
    	logger.info(Arrays.toString(keyWords));
    	for(String word: keyWords) {
    		if("".equals(word)) continue;
    		keyWordList.add(word);
    	}
    	String[][] matrix = new String[permutation(keyWordList.size())][keyWordList.size()];
    	matrix(keyWordList, 0, matrix);
    	for(int i = 0; i<matrix.length; i++) {
    		sql.append("blogKeyWord like ");
    		for(int j = 0; j<matrix[0].length; j++) {
    			sql.append("'%" + matrix[i][j] + "%'");
    		}
    		sql.append(" or ");
    	}
    	System.out.println("KeyWordConditionSql: " + sql);
    	return sql.substring(0, sql.length() - 3) + ")";
    }
    
    private int permutation(int total, int box){
    	if(box == 0) return 1;
    	return total * permutation(total -1 , box - 1);
    }
    
    private int permutation(int total){
    	return permutation(total, total);
    }
    
    private void matrix (List<String> list, int startIndex, String[][] strMatrix){
    	int currentSize = list.size();
    	for (int i = 0; i< currentSize; i++) {
    		for(int j = 0; j< permutation(currentSize -1); j++) {
    			strMatrix[j + startIndex + i * permutation(currentSize -1)][currentSize -1] = list.get(i);
    		}
    		if(currentSize == 1) continue;
    		List<String> listTmp = listDeepClone(list);
    		listTmp.remove(i);
    		matrix(listTmp, startIndex + i * permutation(currentSize -1), strMatrix);
    	}
    }
    
    private List<String> listDeepClone(List<String> srcList){
    	List<String> destList = new ArrayList<>();
    	for(String str: srcList) {
    		destList.add(str);
    	}
    	return destList;
    }
    
    private void delete(long id) {
    	String updateSql = "update blog set isdel = 1 where idblog = ?";
    	int affectRow = jdbcTemplate.update(updateSql,id);
    	if(affectRow == 0) {
    		throw new RuntimeException("未找到要删除的记录");
    	}
	}

	private long save(String title, String keyWord, String content, String user, String digest, Long id) {
    	if(null == id) {
    		Map<String,String> queryParams = new HashMap<>();
    		queryParams.put("blogTitle", title);
    		if(query(queryParams).size() > 0) {
    			throw new RuntimeException("新增的文章标题已存在");
    		};
    		long autoGenId = insert(title, keyWord, content, user, digest);
    		return autoGenId;
    	}else if(content == null){
    		updateWithoutContent(id, title, keyWord, user, digest);    		
    		return -1;
    	}else {
    		update(id, title, keyWord, content, user, digest);  
    		return -1;
    	}
    }
    
    private long insert(String title, String keyWord, String content, String user, String digest) {
    	String insertSql = "insert into blog (blogTitle,blogKeyWord,blogContent,createDate,createUser,modifyDate,modifyUser,isDel,blogDigest)" + 
    			"values (?,?,?,?,?,?,?,?,?);";
    	String currentDate = currentDate();
    	KeyHolder keyHolder = new GeneratedKeyHolder();
    	jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(insertSql);
                    ps.setString(1, title);
                    ps.setString(2, keyWord);
                    ps.setString(3, content);
                    ps.setString(4, currentDate);
                    ps.setString(5, user);
                    ps.setString(6, currentDate);
                    ps.setString(7, user);
                    ps.setString(8, "0");
                    ps.setString(9, digest);
                    return ps;
    	}, keyHolder);
    	return (long) keyHolder.getKey();
    }
    
    private void update(Long id, String title, String keyWord, String content, String user, String digest) {
    	String updateSql = "update blog set blogTitle = ?, blogKeyWord = ?, blogContent = ?, modifyDate = ?, modifyUser = ?, blogDigest = ? where idblog = ?";
    	String currentDate = currentDate();
    	int affectRow = jdbcTemplate.update(updateSql, title,keyWord,content,currentDate,user,digest,id);
    	if(affectRow == 0) {
    		throw new RuntimeException("未找到要更新的记录");
    	}
    }
    
    private void updateWithoutContent(Long id, String title, String keyWord, String user, String digest) {
    	String updateSql = "update blog set blogTitle = ?, blogKeyWord = ?, modifyDate = ?, modifyUser = ?, blogDigest = ? where idblog = ?";
    	String currentDate = currentDate();
    	int affectRow = jdbcTemplate.update(updateSql, title,keyWord,currentDate,user,digest,id);
    	if(affectRow == 0) {
    		throw new RuntimeException("未找到要更新的记录");
    	}
    }
    
    private String currentDate() {
    	DateFormat dformat = new SimpleDateFormat("yyyyMMdd");
    	return dformat.format(new Date());
    }
    
    // 打印异常信息的堆栈
 	private String getStackTraceString(Throwable ex){//(Exception ex) {   
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
    
 	private RspBody exceptionHandler(Exception e) {
 		logger.error(e.getMessage());
		logger.error(getStackTraceString(e));
		if(e.getMessage() != null && e.getMessage().length() > 100) {
			return RspBody.failureRspCreate(e.getMessage().substring(0,100));
		}
		return RspBody.failureRspCreate(e.getMessage());
 	}
 	
 	@RequestMapping(path = {"/imgUpload/{id}"},method = RequestMethod.POST)
    public RspBody UploadImgController (@PathVariable("id") Long id, @RequestParam("name") String filename, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
 		File newFile = new File(this.imgBaseDirSrc.getAbsolutePath() + "/" + id + "/" + filename);
    	File newFile2 = new File(this.imgBaseDirTar.getAbsolutePath() + "/" + id + "/" + filename);
    	if(newFile2.exists())  return RspBody.failureRspCreate("已存在同名文件，请更改文件名或删除原文件后再上传！");
    	newFile.getParentFile().mkdirs();
    	try(
    			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(newFile));
    			BufferedOutputStream os2 = new BufferedOutputStream(new FileOutputStream(newFile2));
    			BufferedInputStream is = new BufferedInputStream(file.getInputStream())
    			) {
    		int data = 0;
			while((data = is.read()) != -1) {
				os.write(data);
				os2.write(data);
			}
		} catch (Exception e) {
			return exceptionHandler(e);
		} 
    	Map<String, Object> resultMap = new HashMap<>();
    	resultMap.put("url", this.imgBaseHttpUrl + id + "/" + filename);
		return RspBody.succesRspCreate(resultMap);    	
    }
 	
 	private List<String> queryImgsById(Long id){
 		String baseUrl = this.imgBaseHttpUrl + id + "/";
 		List<String> list = new ArrayList<String>();
 		File imgDir = new File(this.imgBaseDirTar.getAbsolutePath() + "/" + id + "/");
 		if(!imgDir.exists()) return new ArrayList<String>();
 		for(String imgName: imgDir.list()) {
 			list.add(baseUrl + imgName);
 		}
 		return list;
    }
}
