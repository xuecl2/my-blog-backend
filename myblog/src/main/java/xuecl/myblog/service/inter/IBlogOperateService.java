package xuecl.myblog.service.inter;

import java.util.Map;

import xuecl.myblog.entity.RspBody;

public interface IBlogOperateService {
    RspBody blogOperate (Map<String,Object> data);
}