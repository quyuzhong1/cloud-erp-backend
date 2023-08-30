package com.erp.server.dmp.push.service.kingdee;

import cn.hutool.json.JSONObject;
import com.erp.model.dmp.entity.PlatformEntity;
import com.kingdee.bos.webapi.entity.SaveParam;
import com.sdk.third.kingdee.utils.KingdeeApiUtils;

import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @date 2023/3/3 11:54
 */
public interface KingdeeCommonService {
    /**
     * 根据录入值和字段配置生成JSONObject
     *
     * @param map
     * @param apiPlatformId
     * @param moduleType
     * @return JSONObject
     * @author Will
     * @date: 2023/3/3 12:05
     */
    JSONObject makeApiFieldJson(Map<String, Object> map, String apiPlatformId, Integer moduleType);

    /**
     * 操作成功添加日志
     *
     * @param platformEntity
     * @param businessId
     * @param jsonData
     * @param msg
     * @author Will
     * @date: 2023/3/3 14:05
     */
    void insertSyncLog(PlatformEntity platformEntity, String businessId, String jsonData, String msg, Integer type, Integer status);

    /**
     * @param platformEntity
     * @param businessId
     * @param jsonData
     * @param msg
     * @param type
     * @param status
     * @description: 添加日志并且回写金蝶同步状态
     * @author Will
     * @date: 2023/3/30 10:06
     */
    void insertLogWriteBackSyncKingdeeStatus(PlatformEntity platformEntity, String businessId,
                                             String jsonData, String msg, Integer type, Integer status);

    /**
     * @param code
     * @param businessId
     * @param status
     * @description: 更新业务模块同步状态
     * @author Will
     * @date: 2023/3/10 14:57
     */
    void updateBusinessSyncKingdeeStatus(Integer code, String businessId, String status, String kingdeeId);

    /**
     * @param platformEntity
     * @param map
     * @param apiUtils
     * @param json
     * @param param
     * @description: 新增或修改
     * @author Will
     * @date: 2023/3/3 14:52
     */
    Boolean saveOrUpdate(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, JSONObject json, SaveParam param, Integer type);

    /**
     * 新增或修改客户地址
     *
     * @param platformEntity
     * @param map
     * @param apiUtils
     * @param json
     * @param param
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/7/28 10:34
     **/
    Boolean saveOrUpdateCustomerContact(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, JSONObject json, SaveParam param, Integer type);


    /**
     * @param platformEntity
     * @param map
     * @param sourceApiUtils
     * @param apiUtils
     * @param json
     * @param param
     * @description: 下推
     * @author Will
     * @date: 2023/3/3 14:52
     */
    Boolean push(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils sourceApiUtils, KingdeeApiUtils apiUtils, JSONObject jsonMap, SaveParam param, Integer type, JSONObject json);

    /**
     * 提交及审核
     *
     * @param platformEntity
     * @param map
     * @param apiUtils
     * @param id
     * @author Will
     * @date: 2023/3/3 14:03
     */
    Boolean submit(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, String id, Integer type);

    /**
     * @param platformEntity
     * @param map
     * @param apiUtils
     * @param id
     * @param type
     * @description: 审核
     * @author Will
     * @date: 2023/3/30 9:46
     */
    Boolean audit(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, String id, Integer type);

    /**
     * 反审核
     *
     * @param platformEntity
     * @param map
     * @param apiUtils
     * @param id
     * @return String
     * @author Will
     * @date: 2023/3/3 14:07
     */
    Boolean unAudit(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, String id, Integer type);

    /**
     * @param apiUtils
     * @param apiPlatformId
     * @param map
     * @return JSONObject
     * @description: 根据id、编码查询，优先根据id查没有就根据编码
     * @author Will
     * @date: 2023/4/7 11:29
     */
    JSONObject view(KingdeeApiUtils apiUtils, String apiPlatformId, Map<String, Object> map);

    /**
     * 查询客户分组
     *
     * @param apiUtils
     * @param id
     * @return cn.hutool.json.JSONObject
     * @Author Luo_WG
     * @Date 2023/5/26 16:05
     **/
    JSONObject queryGroupInfo(KingdeeApiUtils apiUtils, String id, String code);

    /**
     * @param map
     * @param type
     * @return PlatformEntity
     * @description: 查询平台
     * @author Will
     * @date: 2023/4/7 11:39
     */
    PlatformEntity getPlatformEntity(Map<String, Object> map, Integer type);

    /**
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     * @param number
     * @param operate
     * @description: 禁用、反禁用
     * @author Will
     * @date: 2023/4/10 18:03
     */
    Boolean excuteOperation(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type, String number, String operate);

    /**
     * 检查并禁用，反禁用
     * 检查是否同步到金蝶 如果没有就不用同步
     * 审核不通过不用同步到金蝶 但是作废缺要同步金蝶 避免这个问题
     *
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     * @param number
     * @param operate
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-07-26 10:13
     */

    Boolean checkAndExcuteOperation(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type, String number, String operate);

    /**
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     * @param number
     * @description: 删除
     * @author Will
     * @date: 2023/4/11 18:56
     */
    void delete(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type, String number);

    /**
     * 删除客户分组
     *
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     * @return void
     * @Author Luo_WG
     * @Date 2023/5/26 15:32
     **/
    void customerGroupDelete(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type);

    /**
     * 客户分组新增或修改
     *
     * @param platformEntity
     * @param map
     * @param apiUtils
     * @param json
     * @param param
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/5/26 15:46
     **/
    Boolean customerGroupSaveOrUpdate(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, JSONObject json, SaveParam param, Integer type);

    /**
     * 保存到金蝶数据
     *
     * @param orderNo
     * @param apiUtils
     * @param modelType
     * @param platformCode
     * @param dataMap
     * @return
     */
    String addKingdeeRecord(String orderNo, KingdeeApiUtils apiUtils, Integer modelType, String platformCode, Map<String, Object> dataMap);


    /**
     * 生成销售变更单
     *
     * @param paramMap
     * @return cn.hutool.json.JSONObject
     * @author yl
     * @date 2023-06-07 10:46
     */
    String createkingdeeSoChange(Map<String, Object> paramMap);

}





















