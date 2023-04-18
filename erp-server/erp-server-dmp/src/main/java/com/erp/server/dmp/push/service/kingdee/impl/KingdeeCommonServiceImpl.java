package com.erp.server.dmp.push.service.kingdee.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.constant.SystemConstants;
import com.common.business.enums.SyncKingdeeOperateEnum;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.dto.ApiPlmSyncLogDTO;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.entity.CfgApiFieldMapValueEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.*;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.service.ApiPlmSyncLogService;
import com.erp.server.dmp.service.CfgApiFieldMapService;
import com.erp.server.dmp.service.CfgApiFieldMapValueService;
import com.erp.server.dmp.service.PlatformService;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.server.dmp.utils.KingdeeUtils;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.kingdee.bos.webapi.entity.SaveParam;
import com.kingdee.bos.webapi.entity.SaveResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    @Resource
    private KingdeeCommonService kingdeeCommonService;

    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private PlatformService platformService;

    @Resource
    private CfgApiFieldMapService cfgApiFieldMapService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    public JSONObject makeApiFieldJson(Map<String, Object> map,String apiPlatformId,Integer moduleType) {

        JSONObject json = new JSONObject();
        //查询配置字段
        CfgApiFieldMapDTO dto = new CfgApiFieldMapDTO();
        dto.setApiPlatformId(apiPlatformId);
        dto.setModuleType(moduleType);
        List<CfgApiFieldMapDTO> mapList = cfgApiFieldMapService.getByParams(dto);

        //未配置发送字段
        if (CollectionUtils.isEmpty(mapList)) {
            return json;
        }

        List<String> fieldMapIds = mapList.stream().map(CfgApiFieldMapDTO::getId).collect(Collectors.toList());
        //查询配置的值映射
        List<CfgApiFieldMapValueEntity> cfgApiFieldMapValueList = cfgApiFieldMapValueService.listByFieldMapIds(fieldMapIds);

       List<CfgApiFieldMapDTO> mainList = mapList.stream().filter(obj -> ApiGroupTypeEnum.NORMAL.getCode().equals(obj.getGroupType()) ||  ApiGroupTypeEnum.PARENT.getCode().equals(obj.getGroupType())).collect(Collectors.toList());

        //无值直接返回
        if (CollectionUtils.isEmpty(mainList)) {
            return json;
        }

        for (CfgApiFieldMapDTO cfgApiFieldMapDTO : mainList) {
            String apiField = cfgApiFieldMapDTO.getApiField();
            //给集合父项填充数据
            if (ApiGroupTypeEnum.PARENT.getCode().equals(cfgApiFieldMapDTO.getGroupType())) {
                //业务系统传参
                JSONArray JsonArray = JSONArray.parseArray(JSONObject.toJSONString(map.get(cfgApiFieldMapDTO.getSelfField())));
                List<Map<String, Object>> listMap = JsonArray.stream().map(BeanUtil::beanToMap).collect(Collectors.toList());
                //集合子项参数配置
                List<CfgApiFieldMapDTO> childList = mapList.stream().filter(obj -> ApiGroupTypeEnum.CHILD.getCode().equals(obj.getGroupType()) && obj.getParentId().equals(cfgApiFieldMapDTO.getId())).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(childList)) {
                    continue;
                }
                //json集合
                List<JSONObject> detailList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(listMap)) {
                    for (Map<String,Object> fieldMap: listMap) {
                        JSONObject detailJson = new JSONObject();
                        //给集合填充数据
                        childList.forEach(obj-> formatJsonObject(obj, detailJson, fieldMap, cfgApiFieldMapValueList));
                        detailList.add(detailJson);
                    }
                }
                KingdeeUtils.makeFieldJson(json,apiField,".",detailList);

            }

            //给常规参数填充数据
            formatJsonObject(cfgApiFieldMapDTO,json,map,cfgApiFieldMapValueList);
        }
        return json;
    }

    @Override
    public PlatformEntity getPlatformEntity (Map<String, Object> map,Integer type) {
        //传入map数据不能为空
        if (CollectionUtils.isEmpty(map)) {
            log.error("同步数据不存在！");
            return null;
        }
        PlatformEntity platformEntity = platformService.getByName(PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            log.error("第三方平台【{}】未找到！",PlatformEnum.KINGDEE.getDesc());
            kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")),"",String.format("第三方平台【{}】未找到！",PlatformEnum.KINGDEE.getDesc()),type, ApiSendStatusEnum.FAILURE.getCode());
        }
        return platformEntity;
    }


    @Override
    public JSONObject view (KingdeeApiUtils apiUtils,String id,String number) {
        LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();

        if (StringUtils.isNotBlank(id)) {
            viewMap.put("id",id);
        } else {
            viewMap.put("number",number);
            //现默认唯迹科技
            viewMap.put("CreateOrgId",1);
        }
        JSONObject model = apiUtils.getViewJson(JSONArray.toJSONString(viewMap));
        return model;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void excuteOperation (KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type,String number,String operate) {
        LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
        //金蝶id
        String syncKingdeeId = (String) map.get("syncKingdeeId");

        if (ObjectUtils.isEmpty(syncKingdeeId)) {
            viewMap.put("ids",Arrays.asList(syncKingdeeId));
        } else {
            viewMap.put("numbers",Arrays.asList(number));
        }
        //金蝶操作编码
        String operateNumber = SyncKingdeeOperateEnum.getNameByCode(operate);

        try {
            apiUtils.excuteOperation(operateNumber,JSONArray.toJSONString(viewMap));
        } catch (Exception e) {
            //新增失败时添加日志及定时任务
            insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")),JSONArray.toJSONString(viewMap),e.getMessage(),type,ApiSendStatusEnum.FAILURE.getCode());
            return;
        }
        //操作成功添加日志
        insertLogWriteBackSyncKingdeeStatus(platformEntity,String.valueOf(map.get("id")),JSONArray.toJSONString(viewMap),SyncKingdeeOperateEnum.getDescByCode(operate),type,ApiSendStatusEnum.SUCCESS.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete (KingdeeApiUtils apiUtils,PlatformEntity platformEntity,Map<String, Object> map,Integer type,String number) {
        LinkedHashMap<String,Object> viewMap = new LinkedHashMap<>();
        //金蝶id
        String syncKingdeeId = (String) map.get("syncKingdeeId");

        if (ObjectUtils.isEmpty(syncKingdeeId)) {
            viewMap.put("ids",Arrays.asList(syncKingdeeId));
        } else {
            viewMap.put("numbers",Arrays.asList(number));
        }
        try {
            apiUtils.delete(JSONArray.toJSONString(viewMap));
        } catch (Exception e) {
            //新增失败时添加日志及定时任务
            insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")),JSONArray.toJSONString(viewMap),e.getMessage(),type,ApiSendStatusEnum.FAILURE.getCode());
            return;
        }
        //操作成功添加日志
        insertLogWriteBackSyncKingdeeStatus(platformEntity,String.valueOf(map.get("id")),JSONArray.toJSONString(viewMap),"删除",type,ApiSendStatusEnum.SUCCESS.getCode());
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdate(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, JSONObject json, SaveParam param,Integer type) {
        SaveResult save;
        String msg = "新增数据";
        if (CollectionUtils.isNotEmpty(param.getNeedUpDateFields())) {
            msg = "修改数据";
        }
        try {
            save = apiUtils.save(param);
        } catch (Exception e) {
            //新增失败时添加日志及定时任务
            insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")),JSONObject.toJSONString(json),msg.concat("；").concat(e.getMessage()),type,ApiSendStatusEnum.FAILURE.getCode());
            return;
        }
        //数据id
        String id = save.getResult().getId();
        //金蝶id
        map.put("syncKingdeeId",id);
        //更新业务表中的金蝶id
        updateBusinessSyncKingdeeStatus(type,String.valueOf(map.get("id")),"",id);
        //新增成功操作日志
        insertLogWriteBackSyncKingdeeStatus(platformEntity,String.valueOf(map.get("id")),JSONObject.toJSONString(json),msg,type,ApiSendStatusEnum.SUCCESS.getCode());
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
    @Transactional(rollbackFor = Exception.class)
    public void submit(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, String id,Integer type) {
        //提交
        List<String> ids = new ArrayList<>();
        ids.add(id);
        try {
            apiUtils.submit(ids);
        } catch (Exception e) {
            //提交失败操作日志及定时任务
            log.error("提交失败",e);
            insertLogWriteBackSyncKingdeeStatus(platformEntity,String.valueOf(map.get("id")),JSONObject.toJSONString(ids),e.getMessage(),type,ApiSendStatusEnum.FAILURE.getCode());
            return;
        }
        log.info("提交成功,数据Id = 【{}】", JSONObject.toJSONString(ids));
        //提交成功操作日志
        insertLogWriteBackSyncKingdeeStatus(platformEntity,String.valueOf(map.get("id")),JSONObject.toJSONString(ids),"提交成功",type,ApiSendStatusEnum.SUCCESS.getCode());
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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(PlatformEntity platformEntity,Map<String, Object> map,KingdeeApiUtils apiUtils,String id,Integer type) {
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
                insertLogWriteBackSyncKingdeeStatus(platformEntity,String.valueOf(map.get("id")),"审核失败",e.getMessage(),type,ApiSendStatusEnum.FAILURE.getCode());
                return;
            }
            //审核成功操作日志
            log.info("审核成功,数据【{}】", JSONObject.toJSONString(viewMap));
            insertLogWriteBackSyncKingdeeStatus(platformEntity,String.valueOf(map.get("id")),JSONObject.toJSONString(viewMap),"审核成功",type,ApiSendStatusEnum.SUCCESS.getCode());
            //当审核状态非已审核时继续审核
            audit(platformEntity, map,apiUtils,id,type);
        }
        //更新业务单据状态
        kingdeeCommonService.updateBusinessSyncKingdeeStatus(type,map.get("id").toString(),SyncKingdeeStatusEnum.SUCCESS_SYNC.getCode(),"");
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unAudit(PlatformEntity platformEntity,Map<String, Object> map,KingdeeApiUtils apiUtils,String id,Integer type) {
        //审核中或已审核则要先反审
        ArrayList<String> ids = new ArrayList<>();
        ids.add(id);
        try {
            apiUtils.unAuditById(ids);
        } catch (Exception e) {
            //反审核失败操作日志及定时任务
            log.error("反审核失败",e);
            insertLogWriteBackSyncKingdeeStatus(platformEntity,String.valueOf(map.get("id")),"反审核失败",e.getMessage(),type,ApiSendStatusEnum.FAILURE.getCode());
            return Boolean.FALSE;
        }
        //反审核成功操作日志
        log.info("反审核成功,数据【{}】", id);
        insertLogWriteBackSyncKingdeeStatus(platformEntity,String.valueOf(map.get("id")),id,"反审核成功",type,ApiSendStatusEnum.SUCCESS.getCode());
        return Boolean.TRUE;
    }


    /**
     * @description: 操作成功添加日志
     * @author Will
     * @date: 2023/1/16 18:34
     * @param platformEntity
     * @param businessId
     * @param jsonData
     * @param msg
     */
    @Override
    public void insertSyncLog(PlatformEntity platformEntity,String businessId,
                                 String jsonData,String msg,Integer type,Integer status) {
        //新增日志信息
        ApiPlmSyncLogDTO apiPlmSyncLogDTO = new ApiPlmSyncLogDTO();
        apiPlmSyncLogDTO.setApiPlatformId(platformEntity.getId());
        apiPlmSyncLogDTO.setApiPlatform(platformEntity.getName());
        apiPlmSyncLogDTO.setModuleType(type);
        apiPlmSyncLogDTO.setBusinessId(businessId);
        apiPlmSyncLogDTO.setStatus(status);
        apiPlmSyncLogDTO.setMsg(msg);
        apiPlmSyncLogDTO.setRequestParamJson(jsonData);
        apiPlmSyncLogService.insert(apiPlmSyncLogDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertLogWriteBackSyncKingdeeStatus(PlatformEntity platformEntity,String businessId,
                                                    String jsonData,String msg,Integer type,Integer status) {
        //新增日志
        insertSyncLog(platformEntity,businessId,jsonData,msg,type,status);
        //更新金蝶同步状态
        if (ApiSendStatusEnum.FAILURE.getCode().equals(status)) {
            this.updateBusinessSyncKingdeeStatus(type,businessId, SyncKingdeeStatusEnum.FAILED_SYNC.getCode(),"");
        }
    }

    @Override
    public void updateBusinessSyncKingdeeStatus(Integer code,String businessId,String status,String kingdeeId){
        //更新业务单据状态
        Map<String,String> params = new HashMap<>(MathUtil.THREE);
        params.put("code",code.toString());
        params.put("businessId",businessId);
        params.put("status", status);
        params.put("kingdeeId", kingdeeId);

        ApiModuleTypeEnum apiModuleTypeEnum = Arrays.stream(ApiModuleTypeEnum.values()).filter(obj -> obj.getCode().equals(code)).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(apiModuleTypeEnum)) {
            return;
        }
        String system = apiModuleTypeEnum.getSystem();

        if (SystemConstants.PLM.equals(system)) {
            plmTaskFeign.updateBusinessSyncKingdeeStatus(params);
        }
        if (SystemConstants.SYS.equals(system)) {
            sysUserFeign.updateBusinessSyncKingdeeStatus(params);
        }

    }

    /**
     * 填充数据
     */
    private void formatJsonObject (CfgApiFieldMapDTO cfgApiFieldMapDTO,JSONObject json,Map<String, Object> map,List<CfgApiFieldMapValueEntity> cfgApiFieldMapValueList){
        //无本身字段时取默认值
        if (StringUtils.isBlank(cfgApiFieldMapDTO.getSelfField())) {
            KingdeeUtils.makeFieldJson(json,cfgApiFieldMapDTO.getApiField(),".",cfgApiFieldMapDTO.getDefaultValue());
            return;
        }
        //直接复制值
        if (ApiFieldTypeEnum.FIELD_VALUE_COPY.getCode().equals(cfgApiFieldMapDTO.getFieldType())) {
            Object value = map.get(cfgApiFieldMapDTO.getSelfField());
            String format = "";
            if (value instanceof LocalDateTime) {
                LocalDateTime value1 = (LocalDateTime) value;
                format = value1.format(DateTimeFormatter.ofPattern(DateUtil.fmt));
            }
            if (value instanceof LocalDate) {
                LocalDate value1 = (LocalDate) value;
                format = value1.format(DateTimeFormatter.ofPattern(DateUtil.fmt_day));
            }
            if (value instanceof LocalTime) {
                LocalTime value1 = (LocalTime) value;
                format = value1.format(DateTimeFormatter.ofPattern(DateUtil.fmt_hms));
            }

            KingdeeUtils.makeFieldJson(json,cfgApiFieldMapDTO.getApiField(),".", StrUtil.isNotBlank(format) ? format : value);
            return;
        }
        if (ApiFieldTypeEnum.FIELD_VALUE_MAP.getCode().equals(cfgApiFieldMapDTO.getFieldType())) {
            //无值映射则直接返回
            if (CollectionUtils.isEmpty(cfgApiFieldMapValueList)) {
                return;
            }
            //根据值映射转换
            String apiValue = cfgApiFieldMapValueList.stream()
                    .filter(obj -> obj.getFieldMapId().equals(cfgApiFieldMapDTO.getId()) && obj.getSelfValue().equals(map.get(cfgApiFieldMapDTO.getSelfField())))
                    .map(CfgApiFieldMapValueEntity::getApiValue)
                    .findFirst()
                    .orElse(null);
            KingdeeUtils.makeFieldJson(json,cfgApiFieldMapDTO.getApiField(),".",apiValue);
        }
    }

    /**
     *
     * @param orderNo 业务唯一键
     * @param apiUtils  kingdee client
     * @param modelType 业务类型
     * @param platformCode 平台code
     * @param dataMap 业务数据
     * @return
     */
    @Override
    public String addKingdeeRecord(String orderNo, KingdeeApiUtils apiUtils, Integer modelType, String platformCode, Map<String, Object> dataMap) {
        PlatformEntity platformEntity = platformService.getByName(platformCode);
        if (ObjectUtils.isEmpty(platformEntity)) {
            throw new ServiceException(ApiError.Default);
        }
        CfgApiFieldMapDTO dto = new CfgApiFieldMapDTO(platformEntity.getId(), modelType);
        List<CfgApiFieldMapDTO> mapList = cfgApiFieldMapService.getByParams(dto);
        //未配置发送字段
        if (CollectionUtil.isEmpty(mapList)) {
            log.error(ApiError.ERROR_97025.msg);
            //错误日志
            kingdeeCommonService.insertSyncLog(platformEntity, orderNo, JSONUtil.toJsonStr(dataMap),"未配置同步字段", modelType, ApiSendStatusEnum.FAILURE.getCode());
            throw new ServiceException(ApiError.ERROR_97025);
        }
        JSONObject json = kingdeeCommonService.makeApiFieldJson(dataMap, mapList);
        SaveResult saveResult = apiUtils.save(new SaveParam<>(json));
        boolean save = saveResult.isSuccessfully();
        kingdeeCommonService.insertSyncLog(platformEntity, orderNo, JSONUtil.toJsonStr(dataMap),"保存直接调拨单到金蝶", modelType, save ? ApiSendStatusEnum.SUCCESS.getCode() : ApiSendStatusEnum.FAILURE.getCode());
        if (!save) {
            log.error("KingdeeCommonServiceImpl>>>addKingdeeRecord>>>调用金蝶保存接口失败saveResult:{}", saveResult);
            throw new ServiceException(ApiError.ERROR_KINGDEE_SAVE);
        }
        return saveResult.getResult().getId();
    }
}
