package xuecl.myblog.service.impl;

import java.math.BigInteger;
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

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import xuecl.myblog.controller.FileHandlerController;
import xuecl.myblog.entity.RspBody;
import xuecl.myblog.service.inter.IBlogOperateService;
import xuecl.myblog.util.ExceptionHandler;

@Service
public class BlogOperateServiceImpl implements IBlogOperateService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private static Logger logger = ExceptionHandler.logger;

    @Override
    public RspBody blogOperate(Map<String, Object> map) {
        try {
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
                if(map.get("queryCondition") != null && !"".equals(map.get("queryCondition"))) queryParams.put("queryCondition", (String)map.get("queryCondition"));
                if(map.get("pageNo") == null) return RspBody.failureRspCreate("查询页码不能为空");  
                if(map.get("rowsPerPage") == null) return RspBody.failureRspCreate("每页条数不能为空");  
                long pageNo = Long.parseLong(map.get("pageNo").toString());
                long rowsPerPage = Long.parseLong(map.get("rowsPerPage").toString());
                List<Map<String,Object>> resultList = query(queryParams, pageNo, rowsPerPage);
//				if(resultList.size() == 0) return RspBody.failureRspCreate("无满足条件的记录");
                return RspBody.succesRspCreate("resultList", resultList, "totalRows", getTotalRows());
            }
            case "queryById": {
                Long id = map.get("id") != null && !"".equals(map.get("id"))?((Integer)map.get("id")).longValue():null;
                List<Map<String, Object>> resultList = queryById(id);
                if(resultList.size() != 1) return RspBody.failureRspCreate("查询结果条数异常");
                resultList.get(0).put("fileList",FileHandlerController.queryImgsById(id));
                return RspBody.succesRspCreate("blogObject",resultList.get(0));
            }
            default:
                break;
            }
        } catch(Exception e) {
            return ExceptionHandler.exceptionHandler(e);
        }
        return null;
    }

    private long getTotalRows() {
        String sql = "select count(*) from blog";
        return ((BigInteger)jdbcTemplate.queryForList(sql).get(0)).longValue();
    }

    private List<Map<String, Object>> query(Map<String,String> queryParams, long pageNo, long rowsPerPage) {
        String queryConditionSql = createQueryConditionSql(queryParams);
        logger.info("queryConditionSql: " + queryConditionSql);
        return jdbcTemplate.queryForList("select idblog as id, blogTitle, blogKeyWord, "
                + "createDate, createUser, modifyDate, modifyUser, isDel, blogDigest"
                + " from blog where " + queryConditionSql + " and isDel = '0' limit " + pageNo + "," + rowsPerPage) ;
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
            if("blogKeyWord".equals(entry.getKey())) {
                sql.append(createKeyWordConditionSql(entry.getValue()));
            }else if("queryCondition".equals(entry.getKey())) {
                sql.append(createQueryConditionSql(entry.getValue()));
            }else {
                sql.append(entry.getKey() + "=" + "'" + entry.getValue() + "' ");
            }
            sql.append(" and ");
        }
        logger.debug("condition sql: " + sql);
        return sql.substring(0, sql.length() - 4);
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

    private String createQueryConditionSql(String condition) {
        String[] keyWords = condition.trim().split(" ");
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
            sql.append("blogTitle like ");
            for(int j = 0; j<matrix[0].length; j++) {
                sql.append("'%" + matrix[i][j] + "%'");
            }
            sql.append(" or ");
            sql.append("blogKeyWord like ");
            for(int j = 0; j<matrix[0].length; j++) {
                sql.append("'%" + matrix[i][j] + "%'");
            }
            sql.append(" or ");
            sql.append("blogDigest like ");
            for(int j = 0; j<matrix[0].length; j++) {
                sql.append("'%" + matrix[i][j] + "%'");
            }
            sql.append(" or ");
            sql.append("blogContent like ");
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
}