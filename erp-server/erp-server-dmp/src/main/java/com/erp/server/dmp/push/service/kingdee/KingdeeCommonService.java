package com.erp.server.dmp.push.service.kingdee;

import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.kingdee.bos.webapi.entity.SaveParam;

import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/3 11:54
 */
public interface KingdeeCommonService {
    /**
     * 根据录入值和字段配置生成JSONObject
     * @author Will
     * @date: 2023/3/3 12:05
     * @param map
     * @param mapList
     * @return JSONObject
     */
    JSONObject makeApiFieldJson(Map<String, Object> map, List<CfgApiFieldMapDTO> mapList);
    /**
     * 操作失败添加日志
     * @author Will
     * @date: 2023/3/3 14:04
     * @param platformEntity
     * @param map
     * @param jsonData
     * @param msg
     */
    void insertFailureLog(PlatformEntity platformEntity,Map<String, Object> map,String jsonData,String msg,Integer type);
    /**
     * 操作成功添加日志
     * @author Will
     * @date: 2023/3/3 14:05
     * @param platformEntity
     * @param map
     * @param jsonData
     * @param msg
     */
    void insertSuccessLog(PlatformEntity platformEntity,Map<String, Object> map,String jsonData,String msg,Integer type);
    /**
     * @description: 更新业务模块同步状态
     * @author Will
     * @date: 2023/3/10 14:57
     * @param code
     * @param businessId
     * @param status
     */
    void updateBusinessSyncKingdeeStatus(String code,String businessId,String status,String kingdeeId);
    /**
     * @description: 新增或修改
     * @author Will
     * @date: 2023/3/3 14:52
     * @param platformEntity
     * @param map
     * @param apiUtils
     * @param json
     * @param param
     */
    void saveOrUpdate(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, JSONObject json, SaveParam param,Integer type);

    /**
     * 提交及审核
     * @author Will
     * @date: 2023/3/3 14:03
     * @param platformEntity
     * @param map
     * @param apiUtils
     * @param id
     */
    void submit(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, String id,Integer type);

    /**
     *  反审核
     * @author Will
     * @date: 2023/3/3 14:07
     * @param platformEntity
     * @param map
     * @param apiUtils
     * @param id
     * @return String
     */
    String unAudit(PlatformEntity platformEntity,Map<String, Object> map,KingdeeApiUtils apiUtils,String id,Integer type);
}
