package com.erp.server.dmp.push.service.kingdee;

import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.kingdee.bos.webapi.entity.SaveParam;

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
     * @param apiPlatformId
     * @param moduleType
     * @return JSONObject
     */
    JSONObject makeApiFieldJson(Map<String, Object> map,String apiPlatformId,Integer moduleType);
    /**
     * 操作成功添加日志
     * @author Will
     * @date: 2023/3/3 14:05
     * @param platformEntity
     * @param businessId
     * @param jsonData
     * @param msg
     */
    void insertSyncLog(PlatformEntity platformEntity,String businessId,String jsonData,String msg,Integer type,Integer status);
    /**
     * @description: 添加日志并且回写金蝶同步状态
     * @author Will
     * @date: 2023/3/30 10:06
     * @param platformEntity
     * @param businessId
     * @param jsonData
     * @param msg
     * @param type
     * @param status
     */
    void insertLogWriteBackSyncKingdeeStatus(PlatformEntity platformEntity,String businessId,
                                                    String jsonData,String msg,Integer type,Integer status);
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
     * @description: 审核
     * @author Will
     * @date: 2023/3/30 9:46
     * @param platformEntity
     * @param map
     * @param apiUtils
     * @param id
     * @param type
     */
     void audit(PlatformEntity platformEntity,Map<String, Object> map,KingdeeApiUtils apiUtils,String id,Integer type);

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
    Boolean unAudit(PlatformEntity platformEntity,Map<String, Object> map,KingdeeApiUtils apiUtils,String id,Integer type);
    /**
     * @description: 根据id、编码查询，优先根据id查没有就根据编码
     * @author Will
     * @date: 2023/4/7 11:29
     * @param apiUtils
     * @param id
     * @param number
     * @return JSONObject
     */
    JSONObject view (KingdeeApiUtils apiUtils,String id,String number);

    /**
     * @description: 查询平台
     * @author Will
     * @date: 2023/4/7 11:39
     * @param map
     * @param type
     * @return PlatformEntity
     */
    PlatformEntity getPlatformEntity (Map<String, Object> map,Integer type);
}
