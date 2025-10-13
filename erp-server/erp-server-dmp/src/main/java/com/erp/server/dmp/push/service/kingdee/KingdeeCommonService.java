package com.erp.server.dmp.push.service.kingdee;

import cn.hutool.json.JSONObject;
import com.common.business.dto.KingdeeParamDTO;
import com.erp.model.dmp.dto.KingdeeDTO;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;

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
     * @param code
     * @param businessId
     * @param status
     * @description: 更新业务模块同步状态
     * @author Will
     * @date: 2023/3/10 14:57
     */
    void updateBusinessSyncKingdeeStatus(Integer code, String businessId, String status, String kingdeeId,String kingdeeCode);

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
    Boolean saveOrUpdate(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, JSONObject json, KingdeeParamDTO.SaveParamDTO param, Integer type);

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
    Boolean saveOrUpdateCustomerContact(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, JSONObject json, KingdeeParamDTO.SaveParamDTO param, Integer type);


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
    Boolean push(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils sourceApiUtils, KingdeeApiUtils apiUtils, JSONObject jsonMap, KingdeeParamDTO.SaveParamDTO param, Integer type, JSONObject json);

    /**
     * 提交及审核
     *
     * @param map
     * @param apiUtils
     * @param id
     * @author Will
     * @date: 2023/3/3 14:03
     */
    Boolean submit( Map<String, Object> map, KingdeeApiUtils apiUtils, String id, Integer type);

    /**
     * @description: 审核（判断状态）
     * @author Will
     * @date: 2024/4/19 15:48
     * @param platformEntity
     * @param map
     * @param apiUtils
     * @param type
     * @return Boolean
     */
    Boolean handleAudit(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, Integer type);

    /**
     * @param map
     * @param apiUtils
     * @param id
     * @param type
     * @description: 审核
     * @author Will
     * @date: 2023/3/30 9:46
     */
    Boolean audit(Map<String, Object> map, KingdeeApiUtils apiUtils, String id, Integer type);
    /**
     * @description: 反审核（判断状态）
     * @author Will
     * @date: 2023/9/25 15:25
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     * @return Boolean
     */
    Boolean handleUnAudit(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, Integer type);
    /**
     * 反审核
     *
     * @param apiUtils
     * @param id
     * @return String
     * @author Will
     * @date: 2023/3/3 14:07
     */
    Boolean unAudit( KingdeeApiUtils apiUtils, String id);

    /**
     * 撤销
     * @author Will
     * @date: 2024/4/19 15:43
     * @param apiUtils
     * @param id
     * @return Boolean
     */
    Boolean cancelAssign(KingdeeApiUtils apiUtils, String id);

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
     * @param typeName
     * @return PlatformEntity
     * @description: 查询平台
     * @author Will
     * @date: 2023/4/7 11:39
     */
    PlatformEntity getPlatformEntity(Map<String, Object> map, String typeName);

    /**
     * @param platformName 平台名称
     */
    PlatformEntity getPlatformEntity(String platformName);

    /**
     * @param apiUtils
     * @param map
     * @param number
     * @param operate
     * @description: 禁用、反禁用
     * @author Will
     * @date: 2023/4/10 18:03
     */
    Boolean excuteOperation(KingdeeApiUtils apiUtils,  Map<String, Object> map, String number, String operate);

    /**
     * @description: 删除（状态判断）
     * @author Will
     * @date: 2023/9/25 15:07
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     * @param number
     */
    void handleDelete (KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type, String number);


    /** 
     * @description : 作废
     * @param apiUtils
     * @author Lambda
     * @return 
     * @create 2023-12-27 12:12
     */
    void handleInvalid (KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type, String number);
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
     * @param syncKingdeeId
     * @param groupFieldKey
     * @return void
     * @Author Luo_WG
     * @Date 2023/5/26 15:32
     **/
    void customerGroupDelete(KingdeeApiUtils apiUtils, String syncKingdeeId,String groupFieldKey);

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
    Boolean customerGroupSaveOrUpdate(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, JSONObject json, KingdeeParamDTO.SaveParamDTO param, Integer type);

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

    /**
     * 直接添加
     * @description
     * @param
     * @return
     * @date 2024-03-15 18:22
     * @author Lambda
     */
    Boolean save(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, JSONObject json, KingdeeParamDTO.SaveParamDTO param, Integer type);
    /**
     * 判断金蝶单据是否已审核
     * @author will
     * @date 2025/10/13 16:34
     * @param kingdeeDTO
     * @return String
     */
    String checkKingdeeSyncApprove(KingdeeDTO kingdeeDTO);
}





















