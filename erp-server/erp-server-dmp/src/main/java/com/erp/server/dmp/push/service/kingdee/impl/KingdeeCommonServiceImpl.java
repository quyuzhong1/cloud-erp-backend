package com.erp.server.dmp.push.service.kingdee.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.erp.model.dmp.dto.ApiPlmSyncLogDTO;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.entity.CfgApiFieldMapValueEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiFieldTypeEnum;
import com.erp.model.dmp.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.enums.ApiSendStatusEnum;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.service.ApiPlmSyncLogService;
import com.erp.server.dmp.service.CfgApiFieldMapValueService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.SaveParam;
import com.kingdee.bos.webapi.entity.SaveResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/3 11:53
 */
@Slf4j
@Service
public class KingdeeCommonServiceImpl implements KingdeeCommonService {

    @Resource
    private CfgApiFieldMapValueService cfgApiFieldMapValueService;

    @Resource
    private ApiPlmSyncLogService apiPlmSyncLogService;



    @Override
    public JSONObject makeApiFieldJson(Map<String, Object> map,List<CfgApiFieldMapDTO> mapList) {
        JSONObject json = new JSONObject();

        List<String> fieldMapIds = mapList.stream().map(CfgApiFieldMapDTO::getId).collect(Collectors.toList());
        //无值直接返回
        if (CollectionUtils.isEmpty(fieldMapIds)) {
            return json;
        }
        //查询配置的值映射
        List<CfgApiFieldMapValueEntity> cfgApiFieldMapValueList = cfgApiFieldMapValueService.listByFieldMapIds(fieldMapIds);

        for (CfgApiFieldMapDTO cfgApiFieldMapDTO : mapList) {
            String apiField = cfgApiFieldMapDTO.getApiField();
            //无本身字段时取默认值
            if (StringUtils.isBlank(cfgApiFieldMapDTO.getSelfField())) {
                KingdeeUtils.makeFieldJson(json,apiField,".",cfgApiFieldMapDTO.getDefaultValue());
                continue;
            }
            //直接复制值
            if (ApiFieldTypeEnum.FIELD_VALUE_COPY.getCode().equals(cfgApiFieldMapDTO.getFieldType())) {
                KingdeeUtils.makeFieldJson(json,apiField,".",map.get(cfgApiFieldMapDTO.getSelfField()));
                continue;
            }
            if (ApiFieldTypeEnum.FIELD_VALUE_MAP.getCode().equals(cfgApiFieldMapDTO.getFieldType())) {
                //无值映射则直接返回
                if (CollectionUtils.isEmpty(cfgApiFieldMapValueList)) {
                    return json;
                }
                //根据值映射转换
                String apiValue = cfgApiFieldMapValueList.stream()
                        .filter(obj -> obj.getFieldMapId().equals(cfgApiFieldMapDTO.getId()) && obj.getSelfValue().equals(map.get(cfgApiFieldMapDTO.getSelfField())))
                        .map(CfgApiFieldMapValueEntity::getApiValue)
                        .findFirst()
                        .orElse(null);
                KingdeeUtils.makeFieldJson(json,apiField,".",apiValue);
            }
        }
        return json;
    }

    @Override
    public void saveOrUpdate(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, JSONObject json, SaveParam param,Integer type) {
        SaveResult save;
        String msg = "新增成功";
        if (CollectionUtils.isNotEmpty(param.getNeedUpDateFields())) {
            msg = "修改成功";
        }
        try {
            save = apiUtils.save(param);
        } catch (Exception e) {
            //新增失败时添加日志及定时任务
            insertFailureLog(platformEntity, map,JSONObject.toJSONString(json),msg.concat("；").concat(e.getMessage()),type);
            return;
        }
        //数据id
        String id = save.getResult().getId();
        //新增成功操作日志
        insertSuccessLog(platformEntity,map,JSONObject.toJSONString(json),msg,type);
        //提交
        submit(platformEntity, map,apiUtils,id,type);
    }

    /**
     * @description: 提交
     * @author Will
     * @date: 2023/1/16 16:39
     * @param platformEntity
     * @param apiUtils
     * @param id
     */
    @Override
    public void submit(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, String id,Integer type) {
        //提交
        List<String> ids = new ArrayList<>();
        ids.add(id);
        try {
            apiUtils.submit(ids);
        } catch (Exception e) {
            //提交失败操作日志及定时任务
            log.error("提交失败",e);
            insertFailureLog(platformEntity,map,"提交失败",e.getMessage(),type);
            return;
        }
        log.info("提交成功,数据Id = 【{}】", JSONObject.toJSONString(ids));
        //提交成功操作日志
        insertSuccessLog(platformEntity,map,JSONObject.toJSONString(ids),"提交成功",type);
        //提交成功后继续审核直至已审核
        audit(platformEntity, map,apiUtils,id,type);
    }

    /**
     * @description: 审核
     * @author Will
     * @date: 2023/1/16 16:39
     * @param platformEntity
     * @param map
     * @param apiUtils
     * @param id
     * @return String 返回审核状态
     */
    private void audit(PlatformEntity platformEntity,Map<String, Object> map,KingdeeApiUtils apiUtils,String id,Integer type) {
        LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
        viewMap.put("Id",id);
        JSONObject model = apiUtils.getViewJson(JSONArray.toJSONString(viewMap));
        //单据状态
        String documentStatus = (String)model.get("DocumentStatus");
        if (!KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //非已审核继续审核
            ArrayList<String> ids = new ArrayList<>();
            ids.add(id);
            try {
                apiUtils.auditById(ids);
            } catch (Exception e) {
                //审核失败操作日志及定时任务
                log.error("审核失败",e);
                insertFailureLog(platformEntity,map,"审核失败",e.getMessage(),type);
                return;
            }
            //审核成功操作日志
            log.info("审核成功,数据【{}】", JSONObject.toJSONString(viewMap));
            insertSuccessLog(platformEntity,map,JSONObject.toJSONString(viewMap),"审核成功",type);
            //当审核状态非已审核时继续审核
            audit(platformEntity, map,apiUtils,id,type);
        }
    }


    @Override
    public String unAudit(PlatformEntity platformEntity,Map<String, Object> map,KingdeeApiUtils apiUtils,String id,Integer type) {
        LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
        viewMap.put("Id",id);
        JSONObject model = apiUtils.getViewJson(JSONArray.toJSONString(viewMap));
        //单据状态
        String documentStatus = (String) model.get("DocumentStatus");
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //审核中或已审核则要先反审
            ArrayList<String> ids = new ArrayList<>();
            ids.add(id);
            try {
                apiUtils.unAuditById(ids);
            } catch (Exception e) {
                //反审核失败操作日志及定时任务
                log.error("反审核失败",e);
                insertFailureLog(platformEntity,map,"反审核失败",e.getMessage(),type);
                return "";
            }
            //反审核成功操作日志
            log.info("反审核成功,数据【{}】", id);
            insertSuccessLog(platformEntity,map,JSONObject.toJSONString(viewMap),"反审核成功",type);
            //当审核是已审核或者审核中时继续反审核
            String status = unAudit(platformEntity, map,apiUtils, id,type);
            //当状态为空时直接返回
            if (StringUtils.isBlank(status)) {
                return status;
            }
            documentStatus = status;
        }
        return  documentStatus;
    }


    /**
     * @description: 操作成功添加日志
     * @author Will
     * @date: 2023/1/16 18:34
     * @param platformEntity
     * @param map
     * @param jsonData
     * @param msg
     */
    @Override
    public void insertSuccessLog(PlatformEntity platformEntity,Map<String, Object> map,String jsonData,String msg,Integer type) {
        //新增日志信息
        ApiPlmSyncLogDTO apiPlmSyncLogDTO = new ApiPlmSyncLogDTO();
        apiPlmSyncLogDTO.setApiPlatformId(platformEntity.getId());
        apiPlmSyncLogDTO.setApiPlatform(platformEntity.getName());
        apiPlmSyncLogDTO.setModuleType(ApiModuleTypeEnum.PRODUCTDETAIL.getCode());
        apiPlmSyncLogDTO.setBusinessId(String.valueOf(map.get("id")));
        apiPlmSyncLogDTO.setStatus(ApiSendStatusEnum.SUCCESS.getCode());
        apiPlmSyncLogDTO.setMsg(msg);
        apiPlmSyncLogDTO.setRequestParamJson(jsonData);
        apiPlmSyncLogService.insert(apiPlmSyncLogDTO);
        //发送成功后更新业务单价状态
       /* Map<String, Object> removeMap = new HashMap<>(MathUtil.THREE);
        removeMap.put("api_platform_id", platformEntity.getId());
        removeMap.put("module_type", ApiModuleTypeEnum.PRODUCTDETAIL.getCode());
        removeMap.put("business_id", String.valueOf(map.get("id")));
        apiSyncTaskService.removeByMap(removeMap);*/
    }

    /**
     * @description: 操作失败添加日志
     * @author Will
     * @date: 2023/1/16 18:34
     * @param platformEntity
     * @param map
     * @param jsonData
     * @param msg
     */
    @Override
    public void insertFailureLog(PlatformEntity platformEntity,Map<String, Object> map,String jsonData,String msg,Integer type) {
        //新增日志信息
        ApiPlmSyncLogDTO apiPlmSyncLogDTO = new ApiPlmSyncLogDTO();
        apiPlmSyncLogDTO.setApiPlatformId(platformEntity.getId());
        apiPlmSyncLogDTO.setApiPlatform(platformEntity.getName());
        apiPlmSyncLogDTO.setModuleType(ApiModuleTypeEnum.PRODUCTDETAIL.getCode());
        apiPlmSyncLogDTO.setBusinessId(String.valueOf(map.get("id")));
        apiPlmSyncLogDTO.setStatus(ApiSendStatusEnum.FAILURE.getCode());
        apiPlmSyncLogDTO.setMsg(msg);
        apiPlmSyncLogDTO.setRequestParamJson(jsonData);
        apiPlmSyncLogService.insert(apiPlmSyncLogDTO);
        //新增定时同步任务
       /* insertApiSyncTask(platformEntity, map);*/
    }

}
