package com.erp.server.dmp.push.service.kingdee.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.utils.MathUtil;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.model.dmp.dto.ApiPlmSyncLogDTO;
import com.erp.model.dmp.dto.ApiSyncTaskDTO;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.entity.ApiSyncTaskEntity;
import com.erp.model.dmp.entity.CfgApiFieldMapValueEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.*;
import com.erp.server.dmp.push.service.kingdee.KingdeeProductDetailService;
import com.erp.server.dmp.service.*;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.kingdee.bos.webapi.entity.OperatorResult;
import com.kingdee.bos.webapi.entity.SaveParam;
import com.kingdee.bos.webapi.entity.SaveResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 18:03
 */
@Slf4j
@Service
public class KingdeeProductDetailServiceImpl implements KingdeeProductDetailService {

    @Resource
    private PlatformService platformService;

    @Resource
    private CfgApiFieldMapService cfgApiFieldMapService;

    @Resource
    private CfgApiFieldMapValueService cfgApiFieldMapValueService;

    @Resource
    private ApiPlmSyncLogService apiPlmSyncLogService;

    @Resource
    private ApiSyncTaskService apiSyncTaskService;

    public static void main(String[] args) {
        Map<String, Object> resultMap = new LinkedHashMap<>();

        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(PlatformApiEnum.BD_MATERIAL.getTaskName());
        LinkedList<String> queryFilters = new LinkedList<>();
        queryFilters.add(String.format("FNumber = '%s'", "OJOHNFIDJFI"));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FUseOrgId,FUseOrgId.FNumber,FUseOrgId.FName,FNumber,FName,FSubHeadEntity_FEntryId," +
                "SubHeadEntity_FEntryId,SubHeadEntity1_FEntryId,SubHeadEntity2_FEntryId,SubHeadEntity3_FEntryId,SubHeadEntity4_FEntryId,SubHeadEntity5_FEntryId," +
                "SubHeadEntity6_FEntryId,SubHeadEntity7_FEntryId,FBarCodeEntity_CMK_FEntryId,FSpecialAttributeEntity_FEntryId,FCategoryID,FNETWEIGHT,FLENGTH," +
                "FWIDTH,F_ulz_Qty1";
        List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 100, 1,1);
        System.out.println(queryList);
    }

    @Override
    @Transactional
    public void pushProductDetail(Map<String, Object> map) {
        //传入map数据不能为空
        if (ObjectUtils.isEmpty(map) || map.size() == 0) {
            throw new ServiceException(ApiError.Default);
        }
        PlatformEntity platformEntity = platformService.getByName(PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            throw new ServiceException(ApiError.Default);
        }
        CfgApiFieldMapDTO dto = new CfgApiFieldMapDTO();
        dto.setApiPlatformId(platformEntity.getId());
        dto.setModuleType(ApiModuleTypeEnum.PRODUCTDETAIL.getCode());
        List<CfgApiFieldMapDTO> mapList = cfgApiFieldMapService.getByParams(dto);
        if (CollectionUtils.isEmpty(mapList)) {
            log.error(ApiError.ERROR_97025.msg);
            //新增定时同步任务
            insertApiSyncTask(platformEntity, map);
            return;
        }
        List<String> fieldMapIds = mapList.stream().map(CfgApiFieldMapDTO::getId).collect(Collectors.toList());

        List<CfgApiFieldMapValueEntity> cfgApiFieldMapValueList = cfgApiFieldMapValueService.listByFieldMapIds(fieldMapIds);

        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(PlatformApiEnum.BD_MATERIAL.getTaskName());

        JSONObject json = new JSONObject();
        for (CfgApiFieldMapDTO cfgApiFieldMapDTO : mapList) {
            //第三方系统下划线分割多层结构
            String apiField = cfgApiFieldMapDTO.getApiField();
            if (StringUtils.isBlank(cfgApiFieldMapDTO.getSelfField())) {
                KingdeeUtils.makeFieldJson(json,apiField,"-",cfgApiFieldMapDTO.getDefaultValue());
            } else {
                if (ApiFieldTypeEnum.FIELD_VALUE_COPY.getCode().equals(cfgApiFieldMapDTO.getFieldType())) {
                    KingdeeUtils.makeFieldJson(json,apiField,"-",map.get(cfgApiFieldMapDTO.getSelfField()));
                } else {
                    //根据值映射转换
                    String apiValue = cfgApiFieldMapValueList.stream()
                            .filter(obj -> obj.getFieldMapId().equals(cfgApiFieldMapDTO.getId()) && obj.getSelfValue().equals(map.get(cfgApiFieldMapDTO.getSelfField())))
                            .map(CfgApiFieldMapValueEntity::getApiValue)
                            .findFirst()
                            .orElse(null);
                    KingdeeUtils.makeFieldJson(json,apiField,"-",apiValue);
                }
            }
        }
        //判断金蝶系统是否已存在该数据
        String skuNo = (String)map.get("skuNo");
        LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
        viewMap.put("number",skuNo);
        viewMap.put("CreateOrgId",1);
        JSONObject model = new JSONObject();
        SaveParam param = new SaveParam(json);
        try {
             model = apiUtils.getViewJson(JSONArray.toJSONString(viewMap));
        } catch (Exception e) {
            //未查找到数据，新增数据
            SaveResult save;
            try {
                save = apiUtils.save(param);
            } catch (Exception ex) {
                //新增失败时添加日志及定时任务
                insertFailureLog(platformEntity, map,JSONObject.toJSONString(json),JSONObject.toJSONString(ex));
                return;
            }
            if (save.isSuccessfully()) {
                //新增成功操作日志
                insertSuccessLog(platformEntity,map,JSONObject.toJSONString(json),"新增成功");
                //提交
                submit(platformEntity, map,apiUtils,viewMap,save);
                return;
            } else {
                //添加失败操作日志及定时任务
                insertFailureLog(platformEntity,map,JSONObject.toJSONString(json),JSONObject.toJSONString(save));
                return;
            }
        }
       //查找到数据后，判断其审核状态
        String documentStatus = (String)model.get("DocumentStatus");//单据状态
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //审核中或已审核则要先反审
            documentStatus = unAudit(platformEntity, map,apiUtils, viewMap);
            //反审核不通过直接返回
            if (StringUtils.isBlank(documentStatus)) {
                return;
            }
        }
        //创建状态则直接修改
        if (KingdeeDocStatusEnum.CREATED.getCode().equals(documentStatus) || KingdeeDocStatusEnum.REAPPROVE.getCode().equals(documentStatus)) {

            LinkedList<String> queryFilters = new LinkedList<>();
            queryFilters.add(String.format("FMATERIALID = '%s'", model.get("Id")));
            String filterStr = String.join(" and ", queryFilters);
            //查询子单据id
            String fieldKeys = "FSubHeadEntity_FEntryId,SubHeadEntity_FEntryId,SubHeadEntity1_FEntryId,SubHeadEntity2_FEntryId,SubHeadEntity3_FEntryId,SubHeadEntity4_FEntryId,SubHeadEntity5_FEntryId," +
                    "SubHeadEntity6_FEntryId,SubHeadEntity7_FEntryId";
            List<Map<String, Object>> queryList = apiUtils.queryList(filterStr, fieldKeys, 1000, 1, 0);
            if (CollectionUtils.isEmpty(queryList)) {
                return;
            }
            Map<String, Object> queryMap = queryList.get(0);
            //主单据id
            KingdeeUtils.makeFieldJson(json,"FMATERIALID","-",model.get("Id"));
            Iterator iter = queryMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                KingdeeUtils.makeFieldJson(json, String.valueOf(entry.getKey()),"-",entry.getValue());
            }
            //需要更新的字段
            List<String> apiFieldList = mapList.stream().map(obj -> obj.getApiField()).sorted().distinct().collect(Collectors.toList());
            ArrayList<String> needUpDateFields = new ArrayList<>();
            for (String field:apiFieldList) {
                ArrayList<String> splitFields =(ArrayList<String>) Arrays.stream(field.split("-")).collect(Collectors.toList());
                needUpDateFields.addAll(splitFields);
            }
            param.setNeedUpDateFields(needUpDateFields);
            SaveResult save = new SaveResult();
            try {
                 save = apiUtils.save(param);
            } catch (Exception e) {
                insertFailureLog(platformEntity,map,JSONObject.toJSONString(json),e.getMessage());
                return;
            }
           if (save.isSuccessfully()) {
               //修改成功操作日志
               insertSuccessLog( platformEntity, map, JSONObject.toJSONString(json),"修改成功");
               //提交
               submit(platformEntity, map,apiUtils,viewMap,save);
               return;
           } else {
               //修改失败操作日志及定时任务
               insertFailureLog(platformEntity,map,JSONObject.toJSONString(json),JSONObject.toJSONString(save));
               return;
           }
        }
    }

    /**
     * @description: 提交
     * @author Will
     * @date: 2023/1/16 16:39
     * @param apiUtils
     * @param viewMap
     * @param save
     */
    private void submit(PlatformEntity platformEntity,Map<String, Object> map,KingdeeApiUtils apiUtils,LinkedHashMap<String,Object> viewMap,SaveResult save) {
        String id = save.getResult().getResponseStatus().getSuccessEntitys().get(0).getId();
        //提交
        ArrayList<String> ids = new ArrayList<>();
        ids.add(id);
        OperatorResult submit = new OperatorResult();
        try {
             submit = apiUtils.submit(ids);
        } catch (Exception e) {
            //提交失败操作日志及定时任务
            insertFailureLog(platformEntity,map,"提交失败",e.getMessage());
            return;
        }
        if (submit.isSuccessfully()) {
            //提交成功操作日志
            insertSuccessLog(platformEntity,map,JSONObject.toJSONString(viewMap),"提交成功");
            //提交成功后继续审核直至已审核
            audit(platformEntity, map,apiUtils,viewMap);
        } else {
            //提交失败操作日志及定时任务
            insertFailureLog(platformEntity,map,"提交失败",JSONObject.toJSONString(save));
        }
    }

    /**
     * @description: 审核
     * @author Will
     * @date: 2023/1/16 16:39
     * @param apiUtils
     * @param viewMap
     * @return String 返回审核状态
     */
    private void audit(PlatformEntity platformEntity,Map<String, Object> map,KingdeeApiUtils apiUtils,LinkedHashMap<String,Object> viewMap) {
        JSONObject model = apiUtils.getViewJson(JSONArray.toJSONString(viewMap));
        String documentStatus = (String)model.get("DocumentStatus");//单据状态
        String id = String.valueOf(model.get("Id"));//单据id
        if (!KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //非已审核继续审核
            ArrayList<String> ids = new ArrayList<>();
            ids.add(id);
            OperatorResult operatorResult = new OperatorResult();
            try {
                operatorResult = apiUtils.auditById(ids);
            } catch (Exception e) {
                //审核失败操作日志及定时任务
                insertFailureLog(platformEntity,map,"审核失败",e.getMessage());
                return;
            }
            if (operatorResult.isSuccessfully()) {
                //审核成功操作日志
                insertSuccessLog(platformEntity,map,JSONObject.toJSONString(viewMap),"审核成功");
            } else {
                //审核失败操作日志及定时任务
                insertFailureLog(platformEntity,map,"审核失败",JSONArray.toJSONString(ids));
                return;
            }
            //当审核状态非已审核时继续审核
            audit(platformEntity, map,apiUtils,viewMap);
        }
    }


    /**
     * @description: 反审核
     * @author Will
     * @date: 2023/1/16 16:29
     * @param apiUtils
     * @param viewMap
     * @return String 返回审核状态
     */
    private String unAudit(PlatformEntity platformEntity,Map<String, Object> map,KingdeeApiUtils apiUtils,LinkedHashMap<String,Object> viewMap) {
        JSONObject model = apiUtils.getViewJson(JSONArray.toJSONString(viewMap));
        String documentStatus = (String) model.get("DocumentStatus");//单据状态
        String id = String.valueOf(model.get("Id"));//单据id
        if (KingdeeDocStatusEnum.APPROVING.getCode().equals(documentStatus) || KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //审核中或已审核则要先反审
            ArrayList<String> ids = new ArrayList<>();
            ids.add(id);
            OperatorResult operatorResult = new OperatorResult();
            try {
                operatorResult = apiUtils.unAuditById(ids);
            } catch (Exception e) {
                //反审核失败操作日志及定时任务
                insertFailureLog(platformEntity,map,"反审核失败",e.getMessage());
                return "";
            }
            if (operatorResult.isSuccessfully()) {
                //反审核成功操作日志
                insertSuccessLog(platformEntity,map,JSONObject.toJSONString(viewMap),"反审核成功");
            } else {
                //反审核失败操作日志及定时任务
                insertFailureLog(platformEntity,map,"反审核失败",JSONArray.toJSONString(ids));
                return "";
            }
            //当审核是已审核或者审核中时继续反审核
            String status = unAudit(platformEntity, map,apiUtils, viewMap);
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
    private void insertSuccessLog(PlatformEntity platformEntity,Map<String, Object> map,String jsonData,String msg) {
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
        //发送成功后删除任务表数据
        Map<String, Object> removeMap = new HashMap<>();
        removeMap.put("api_platform_id", platformEntity.getId());
        removeMap.put("module_type", ApiModuleTypeEnum.PRODUCTDETAIL.getCode());
        removeMap.put("business_id", String.valueOf(map.get("id")));
        apiSyncTaskService.removeByMap(removeMap);
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
    private void insertFailureLog(PlatformEntity platformEntity,Map<String, Object> map,String jsonData,String msg) {
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
        insertApiSyncTask(platformEntity, map);
    }

    /**
     * @description: 新增定时同步任务
     * @author Will
     * @date: 2023/1/12 16:41
     * @param platformEntity
     * @param map
     */
    private void insertApiSyncTask(PlatformEntity platformEntity,Map<String, Object> map) {
        //新增或更新定时任务数据重新发送
        ApiSyncTaskDTO apiSyncTaskDTO = new ApiSyncTaskDTO();
        apiSyncTaskDTO.setApiPlatformId(platformEntity.getId());
        apiSyncTaskDTO.setApiPlatform(platformEntity.getName());
        apiSyncTaskDTO.setModuleType(ApiModuleTypeEnum.PRODUCTDETAIL.getCode());
        apiSyncTaskDTO.setBusinessId(String.valueOf(map.get("id")));
        apiSyncTaskDTO.setRetryCount(MathUtil.ZERO);
        //传入参数转json字符串
        String jsonParam = JSONObject.toJSONString(map);
        apiSyncTaskDTO.setRequestParamJson(jsonParam);
        ApiSyncTaskEntity apiSyncTask = apiSyncTaskService.getByApiSyncTask(apiSyncTaskDTO);
        if (ObjectUtils.isEmpty(apiSyncTask)) {
            apiSyncTaskService.insert(apiSyncTaskDTO);
        } else {
            apiSyncTaskDTO.setId(apiSyncTask.getId());
            apiSyncTaskDTO.setRetryCount(MathUtil.add(apiSyncTask.getRetryCount(),1));
            apiSyncTaskService.update(apiSyncTaskDTO);
        }
    }
}
