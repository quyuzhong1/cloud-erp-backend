package com.erp.server.dmp.push.service.kingdee.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.constant.SystemConstants;
import com.common.business.dto.KingdeeParamDTO;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastJsonUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.constant.CfgApiAuthContant;
import com.erp.model.dmp.dto.CfgApiAuthDTO;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.entity.CfgApiAuthEntity;
import com.erp.model.dmp.entity.CfgApiFieldMapValueEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiFieldTypeEnum;
import com.erp.model.dmp.enums.ApiGroupTypeEnum;
import com.erp.model.dmp.enums.KingdeeDocStatusEnum;
import com.erp.model.dmp.enums.KingdeePushModuleEnum;
import com.erp.rpc.oms.feign.OmsTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.sdk.third.kingdee.utils.KingdeeUtils;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.service.CfgApiAuthService;
import com.erp.server.dmp.service.CfgApiFieldMapService;
import com.erp.server.dmp.service.CfgApiFieldMapValueService;
import com.erp.server.dmp.service.PlatformService;
import com.kingdee.bos.webapi.entity.*;
import com.kingdee.bos.webapi.sdk.K3CloudApi;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
 * @date 2023/3/3 11:53
 */
@Slf4j
@Service
public class KingdeeCommonServiceImpl implements KingdeeCommonService {

    @Resource
    private CfgApiAuthService cfgApiAuthService;

    @Resource
    private CfgApiFieldMapValueService cfgApiFieldMapValueService;

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

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private OmsTaskFeign omsTaskFeign;


    @Override
    public JSONObject makeApiFieldJson(Map<String, Object> map, String apiPlatformId, Integer moduleType) {

        JSONObject json = new JSONObject(new LinkedHashMap());
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

        //正常级别数据
        List<CfgApiFieldMapDTO> mainList = mapList.stream().filter(obj -> ApiGroupTypeEnum.NORMAL.getCode().equals(obj.getGroupType()) || ApiGroupTypeEnum.PARENT.getCode().equals(obj.getGroupType())).collect(Collectors.toList());


        //无值直接返回
        if (CollectionUtils.isEmpty(mainList)) {
            return json;
        }
        //给常规参数填充数据
        for (CfgApiFieldMapDTO cfgApiFieldMapDTO : mainList) {
            //常规参数填充数据
            if (ApiGroupTypeEnum.NORMAL.getCode().equals(cfgApiFieldMapDTO.getGroupType())) {
                formatJsonObject(cfgApiFieldMapDTO, json, map, cfgApiFieldMapValueList);
                continue;
            }
            //集合项填充数据
            if (ApiGroupTypeEnum.PARENT.getCode().equals(cfgApiFieldMapDTO.getGroupType())) {
                handleJsonDetail(cfgApiFieldMapDTO, mapList, map, cfgApiFieldMapValueList, json);
            }
        }
        return json;
    }

    @Override
    public PlatformEntity getPlatformEntity(Map<String, Object> map, String typeName) {
        //传入map数据不能为空
        if (CollectionUtils.isEmpty(map)) {
            log.error("同步数据不存在！");
            return null;
        }
        PlatformEntity platformEntity = platformService.getByName(typeName);
        return platformEntity;
    }

    @Override
    public PlatformEntity getPlatformEntity(String platformName) {
        return platformService.getByName(platformName);
    }


    @Override
    public JSONObject view(KingdeeApiUtils apiUtils, String apiPlatformId, Map<String, Object> map) {
        /**
         * 1、优先根据创建组织id查询
         * 2、有配置则优先根据配置查询，优先根据第一创建组织查询，未查到则根据第二创建组织查询
         * 3、无创建组织id及配置 ，则根据唯迹集团查询
         */
        String syncKingdeeId = (String) map.get("syncKingdeeId");
        String number = (String) map.get("code");
        Integer createOrgId = map.containsKey("createOrgId") ? Integer.valueOf(map.get("createOrgId").toString()) : null;

        JSONObject model;
        //1、有传创建组织id则根据创建组织id查询
        if (ObjectUtils.isNotEmpty(createOrgId)) {
            return handleViewJson(apiUtils, syncKingdeeId, number, createOrgId);
        }
        //2、未传组织且未配置组织则默认唯迹查询
        CfgApiAuthEntity authEntity = cfgApiAuthService.getByKey(CfgApiAuthContant.KINGDEE_CREATE_ORG_Id, "", apiPlatformId);
        if (ObjectUtils.isEmpty(authEntity)) {
            createOrgId = MathUtil.ONE;
            //未配置数据
            return handleViewJson(apiUtils, syncKingdeeId, number, createOrgId);
        }
        //3、根据配置表数据查询
        CfgApiAuthDTO.KingDeeCreateOrgDTO kingDeeCreateOrgDTO = JSONUtil.toBean(authEntity.getValue(), CfgApiAuthDTO.KingDeeCreateOrgDTO.class);
        //根据一级创建组织查询
        try {
            createOrgId = ObjectUtils.isEmpty(kingDeeCreateOrgDTO.getFirstOrgId()) ? createOrgId : kingDeeCreateOrgDTO.getFirstOrgId();
            model = handleViewJson(apiUtils, syncKingdeeId, number, createOrgId);
        } catch (Exception e) {
            log.error("未查询到有效数据，apiPlatformId = {}，id = {}，number = {}，firstOrgId = {}", apiPlatformId, syncKingdeeId, number, kingDeeCreateOrgDTO.getFirstOrgId());
            //根据二级创建组织查询
            createOrgId = ObjectUtils.isEmpty(kingDeeCreateOrgDTO.getSecondOrgId()) ? createOrgId : kingDeeCreateOrgDTO.getSecondOrgId();
            model = handleViewJson(apiUtils, syncKingdeeId, number, createOrgId);
        }
        return model;
    }


    @Override
    public JSONObject queryGroupInfo(KingdeeApiUtils apiUtils, String id, String code) {
        String sign = "";
        if (StringUtils.isNotBlank(id)) {
            sign = id;
        } else {
            sign = code;
        }
        JSONObject model = apiUtils.queryGroupInfo(sign);
        return model;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean excuteOperation(KingdeeApiUtils apiUtils, Map<String, Object> map, String number, String operate) {
        LinkedHashMap<String, Object> viewMap = new LinkedHashMap<>();

        String syncKingdeeId = (String) map.get("syncKingdeeId");
        Object createOrgId = map.get("createOrgId");

        if (StringUtils.isNotBlank(syncKingdeeId)) {
            viewMap.put("ids", syncKingdeeId);
        } else {
            viewMap.put("numbers", Arrays.asList(number));
        }
        if (ObjectUtils.isNotEmpty(createOrgId)) {
            //创建组织
            viewMap.put("CreateOrgId", Integer.valueOf(createOrgId.toString()));
        }

        //金蝶操作编码
        String operateNumber = SyncOperateEnum.getNameByCode(operate);

        apiUtils.excuteOperation(operateNumber, JSONUtil.toJsonStr(viewMap));
        return Boolean.TRUE;
    }

    /**
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     * @param number
     * @description: 删除（状态判断）
     * @author Will
     * @date: 2023/9/25 15:06
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleDelete(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type, String number) {
        //删除之前判断状态
        Boolean isDelete = this.updateApproved(apiUtils, platformEntity, map, type, KingdeeDocStatusEnum.REAPPROVE, Boolean.TRUE);
        if (isDelete) {
            //删除
            this.delete(apiUtils, platformEntity, map, type, number);
        }
    }

    /**
     * 处理作废
     *
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     * @param operate
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleInvalid(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type, String operate) {

        //金蝶id
        String syncKingdeeId = (String) map.get("syncKingdeeId");
        if (StringUtils.isBlank(syncKingdeeId)) {
            return;
        }
        //作废之前判断状态
        Boolean ifInvalid = this.updateApproved(apiUtils, platformEntity, map, type, KingdeeDocStatusEnum.REAPPROVE, Boolean.TRUE);
        if (ifInvalid) {
            //业务编码
            String code = (String) map.get("code");
            //作废
            this.excuteOperation(apiUtils, map, code, operate);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type, String number) {

        JSONObject jsonObject = new JSONObject();
        //金蝶id
        String syncKingdeeId = (String) map.get("syncKingdeeId");
        //业务id
        String businessId = (String) map.get("id");

        jsonObject.set("Ids", syncKingdeeId);
        //删除
        apiUtils.delete(JSONUtil.toJsonStr(jsonObject));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void customerGroupDelete(KingdeeApiUtils apiUtils, String syncKingdeeId, String groupFieldKey) {
        //删除客户分组
        apiUtils.customerGroupDelete(syncKingdeeId, groupFieldKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean customerGroupSaveOrUpdate(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, JSONObject json, KingdeeParamDTO.SaveParamDTO param, Integer type) {
        String msg = "新增数据";
        if (CollectionUtils.isNotEmpty(param.getNeedUpDateFields())) {
            msg = "修改数据";
        }
        log.warn("msg>>>>>{}，param>>>>>>>{},json>>>>>>>>{}", msg, JSONUtil.toJsonStr(param), json);
        //保存客户分组
        RepoRet repoRet = apiUtils.customerGroupSave(param);
        if (ObjectUtil.isNotEmpty(repoRet.getResult()) && repoRet.getResult().getResponseStatus().getSuccessEntitys() != null) {
            ArrayList<SuccessEntity> successEntitys = repoRet.getResult().getResponseStatus().getSuccessEntitys();
            if (CollectionUtils.isNotEmpty(successEntitys)) {
                //数据id
                String id = successEntitys.get(MathUtil.ZERO).getId();

                //金蝶id
                map.put("syncKingdeeId", id);
                //更新业务表中的金蝶id
                updateBusinessSyncKingdeeStatus(type, String.valueOf(map.get("id")), SyncStatusEnum.SUCCESS_SYNC.getCode(), id,"");
            }
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdate(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, JSONObject json, KingdeeParamDTO.SaveParamDTO param, Integer type) {
        String msg = "新增数据";
        if (CollectionUtils.isNotEmpty(param.getNeedUpDateFields())) {
            msg = "修改数据";
        }
        log.warn("msg>>>>>{}，param>>>>>>>{},json>>>>>>>>{}", msg, JSONUtil.toJsonStr(param), json);
        RepoResult save = apiUtils.saveKingDee(param);
        if (!save.getResponseStatus().isIsSuccess()) {
            throw new ServiceException(ApiError.ERROR_ADD_KINGDEE_DATA);
        }
        //数据id
        String id = save.getId();
        RepoStatus repoStatus=save.getResponseStatus();
        String kingdeeCode="";
        if(repoStatus.isIsSuccess()){
            kingdeeCode= repoStatus.getSuccessEntitys().get(0).getNumber();
        }
        //金蝶id
        map.put("syncKingdeeId", id);
        //提交
        submit(map, apiUtils, id, type);
        //更新业务表中的金蝶id
        updateBusinessSyncKingdeeStatus(type, String.valueOf(map.get("id")), "", id,kingdeeCode);
        return Boolean.TRUE;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateCustomerContact(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, JSONObject json, KingdeeParamDTO.SaveParamDTO param, Integer type) {
        String msg = "新增数据";
        if (CollectionUtils.isNotEmpty(param.getNeedUpDateFields())) {
            msg = "修改数据";
        }
        log.info("msg>>>>>{}，param>>>>>>>{}", msg, param);
        RepoResult save = apiUtils.saveKingDee(param);
        //数据id
        String id = save.getId();
        //金蝶id
        map.put("syncKingdeeId", id);
        //更新业务表中的金蝶id
        updateBusinessSyncKingdeeStatus(type, String.valueOf(map.get("id")), SyncStatusEnum.SUCCESS_SYNC.getCode(), id,"");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean push(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils sourceApiUtils, KingdeeApiUtils apiUtils, JSONObject jsonMap, KingdeeParamDTO.SaveParamDTO param, Integer type, JSONObject json) {
        //下推
        RepoResult result = sourceApiUtils.push(jsonMap);
        //数据id
        String id = result.getResponseStatus().getSuccessEntitys().get(0).getId();
        //金蝶id
        map.put("syncKingdeeId", id);
        //更新业务表中的金蝶id
        updateBusinessSyncKingdeeStatus(type, String.valueOf(map.get("id")), "", id,"");
        //给修改json对象赋值ID
        KingdeeUtils.makeFieldJson(json, "FId", ".", id);
        StringBuffer allKey = FastJsonUtil.getAllKey(json);
        ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
        param.setNeedUpDateFields(apiFieldList);
        saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
        return Boolean.TRUE;
    }

    /**
     * @param apiUtils
     * @param id
     * @description: 提交
     * @author Will
     * @date: 2023/1/16 16:39
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(Map<String, Object> map, KingdeeApiUtils apiUtils, String id, Integer type) {
        //提交
        List<String> ids = new ArrayList<>();
        ids.add(id);

        apiUtils.submit(ids);
        log.info("提交成功,数据Id = 【{}】", JSONUtil.toJsonStr(ids));
        //提交成功后继续审核直至已审核
        Boolean audit = audit(map, apiUtils, id, type);
        return audit;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handleAudit(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, Integer type) {
        //根据状态审核
        return this.updateApproved(apiUtils, platformEntity, map, type, KingdeeDocStatusEnum.APPROVED, Boolean.TRUE);
    }

    /**
     * @param map
     * @param apiUtils
     * @param id
     * @return String 返回审核状态
     * @description: 审核
     * @author Will
     * @date: 2023/1/16 16:39
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean audit(Map<String, Object> map, KingdeeApiUtils apiUtils, String id, Integer type) {
        LinkedHashMap<String, Object> viewMap = new LinkedHashMap<>();
        viewMap.put("Id", id);
        JSONObject model = apiUtils.getViewJson(JSONUtil.toJsonStr(viewMap));
        //单据状态
        String documentStatus = (String) model.get("DocumentStatus");
        if (!KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //非已审核继续审核
            ArrayList<String> ids = new ArrayList<>();
            ids.add(id);
            apiUtils.auditById(ids);
            //审核成功操作日志
            log.info("审核成功,数据【{}】", JSONUtil.toJsonStr(viewMap));
            //当审核状态非已审核时继续审核
            audit(map, apiUtils, id, type);
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean handleUnAudit(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, Integer type) {
        //根据状态反审核
        return this.updateApproved(apiUtils, platformEntity, map, type, KingdeeDocStatusEnum.REAPPROVE, Boolean.TRUE);
    }

    @Override
    public Boolean unAudit(KingdeeApiUtils apiUtils, String id) {
        //已审核则要先反审
        ArrayList<String> ids = new ArrayList<>();
        ids.add(id);
        apiUtils.unAuditById(ids);
        //反审核成功操作日志
        log.info("反审核成功,数据【{}】", id);
        return Boolean.TRUE;
    }

    @Override
    public Boolean cancelAssign(KingdeeApiUtils apiUtils, String id) {
        //审核中需要撤销
        ArrayList<String> ids = new ArrayList<>();
        ids.add(id);
        apiUtils.cancelAssign(ids);
        //撤销成功
        log.info("撤销成功,数据【{}】", id);
        return Boolean.TRUE;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBusinessSyncKingdeeStatus(Integer code, String businessId, String status, String kingdeeId,String kingdeeCode) {
        //更新业务单据状态
        Map<String, Object> params = new HashMap<>(MathUtil.THREE);
        params.put("code", code.toString());
        params.put("businessId", businessId);
        params.put("status", status);
        params.put("kingdeeId", kingdeeId);
        params.put("kingdeeCode", kingdeeCode);
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
        if (SystemConstants.SCM.equals(system)) {
            scmTaskFeign.updateBusinessSyncKingdeeStatus(params);
        }
        if (SystemConstants.WMS.equals(system)) {
            wmsTaskFeign.updateBusinessSyncKingdeeStatus(params);
        }
        if (SystemConstants.OMS.equals(system)) {
            omsTaskFeign.updateBusinessSyncKingdeeStatus(params);
        }
    }


    /**
     * @param parentMap               1级（数据结果为集合）数据
     * @param mapList
     * @param map                     来源数据值
     * @param cfgApiFieldMapValueList 值映射数据
     * @param json                    当前级别json
     * @description:
     * @author Will
     * @date: 2023/5/30 11:35
     */
    private void handleJsonDetail(CfgApiFieldMapDTO parentMap, List<CfgApiFieldMapDTO> mapList, Map<String, Object> map, List<CfgApiFieldMapValueEntity> cfgApiFieldMapValueList, JSONObject json) {
        CfgApiFieldMapDTO cfgApiFieldMapDTO = mapList.stream().filter(obj -> obj.getId().equals(parentMap.getId())).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(parentMap)) {
            return;
        }
        String apiField = cfgApiFieldMapDTO.getApiField();
        //业务系统传参
        JSONArray JsonArray = JSONUtil.parseArray(JSONUtil.toJsonStr(map.get(cfgApiFieldMapDTO.getSelfField())));
        List<Map<String, Object>> listMap = JsonArray.stream().map(BeanUtil::beanToMap).collect(Collectors.toList());
        //集合子项参数配置
        List<CfgApiFieldMapDTO> childList = mapList.stream().filter(obj -> obj.getParentId().equals(cfgApiFieldMapDTO.getId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(childList)) {
            return;
        }
        //json集合
        JSONArray jsonArray = new JSONArray();
        if (CollectionUtils.isNotEmpty(listMap)) {
            for (Map<String, Object> fieldMap : listMap) {
                JSONObject detailJson = new JSONObject(new LinkedHashMap());
                //给集合填充数据
                for (CfgApiFieldMapDTO child : childList) {

                    //下级明细处理集合数据
                    if (ApiGroupTypeEnum.PARENT.getCode().equals(child.getGroupType())) {
                        handleJsonDetail(child, mapList, fieldMap, cfgApiFieldMapValueList, detailJson);
                    }
                    //下级明细数据填充
                    formatJsonObject(child, detailJson, fieldMap, cfgApiFieldMapValueList);
                }
                jsonArray.add(detailJson);
            }
        }
        KingdeeUtils.makeFieldJson(json, apiField, ".", jsonArray);
    }

    /**
     * 填充数据
     */
    private void formatJsonObject(CfgApiFieldMapDTO cfgApiFieldMapDTO, JSONObject json, Map<String, Object> map, List<CfgApiFieldMapValueEntity> cfgApiFieldMapValueList) {
        //无本身字段时取默认值
        if (StringUtils.isBlank(cfgApiFieldMapDTO.getSelfField())) {
            KingdeeUtils.makeFieldJson(json, cfgApiFieldMapDTO.getApiField(), ".", cfgApiFieldMapDTO.getDefaultValue());
            return;
        }
        //直接复制值
        if (ApiFieldTypeEnum.FIELD_VALUE_COPY.getCode().equals(cfgApiFieldMapDTO.getFieldType())) {
            Object value = map.get(cfgApiFieldMapDTO.getSelfField());
            //当传入的值是空时取默认
            if (ObjectUtils.isEmpty(value) || StringUtils.isBlank(String.valueOf(value))) {
                value = cfgApiFieldMapDTO.getDefaultValue();
            }
            String format = "";

            //当传入的值是空时取默认
            if (ObjectUtils.isEmpty(value) || StringUtils.isBlank(String.valueOf(value))) {
                value = cfgApiFieldMapDTO.getDefaultValue();
            }
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
            KingdeeUtils.makeFieldJson(json, cfgApiFieldMapDTO.getApiField(), ".", StrUtil.isNotBlank(format) ? format : value);
            return;
        }
        if (ApiFieldTypeEnum.FIELD_VALUE_MAP.getCode().equals(cfgApiFieldMapDTO.getFieldType())) {
            //无值映射则直接返回
            if (CollectionUtils.isEmpty(cfgApiFieldMapValueList)) {
                return;
            }
            //根据值映射转换
            String apiValue = cfgApiFieldMapValueList.stream()
                    .filter(obj -> obj.getFieldMapId().equals(cfgApiFieldMapDTO.getId()) && obj.getSelfValue().equals(String.valueOf(map.get(cfgApiFieldMapDTO.getSelfField()))))
                    .map(CfgApiFieldMapValueEntity::getApiValue)
                    .findFirst()
                    .orElse("");
            KingdeeUtils.makeFieldJson(json, cfgApiFieldMapDTO.getApiField(), ".", apiValue);
        }
    }

    /**
     * @param orderNo      业务唯一键
     * @param apiUtils     kingdee client
     * @param modelType    业务类型
     * @param platformCode 平台code
     * @param dataMap      业务数据
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
            throw new ServiceException(ApiError.ERROR_97025);
        }
        JSONObject json = kingdeeCommonService.makeApiFieldJson(dataMap, platformEntity.getId(), modelType);
        RepoResult saveResult = apiUtils.saveKingDee(new KingdeeParamDTO.SaveParamDTO(json));
        boolean save = saveResult.getResponseStatus().isIsSuccess();
        if (!save) {
            log.error("KingdeeCommonServiceImpl>>>addKingdeeRecord>>>调用金蝶保存接口失败saveResult:{}", saveResult);
            throw new ServiceException(ApiError.ERROR_KINGDEE_SAVE);
        }
        return saveResult.getId();
    }

    /**
     * 自动生成销售变更单
     *
     * @param paramMap
     * @return cn.hutool.json.JSONObject
     * @author yl
     * @date 2023-06-07 10:46
     */
    @SneakyThrows
    @Override
    public String createkingdeeSoChange(Map<String, Object> paramMap) {

        //读取配置，初始化SDK
        KingdeeApiUtils apiUtils = new KingdeeApiUtils(KingdeePushModuleEnum.SAL_SALEORDER_CHANGE.getCode());
        K3CloudApi client = apiUtils.client;
        RepoResult repoResult = client.CheckAuthInfo();
        repoResult.getId();
        String url = KingdeeUtils.SO_CHANGE_URL;
        String paramStr = JSONUtil.toJsonStr(paramMap);
        log.warn("KingdeeCommonServiceImpl.createkingdeeSoChange  paramStr==={}", paramStr);
        String result = client.execute(url, new Object[]{paramStr});
        log.warn("KingdeeCommonServiceImpl.createkingdeeSoChange  result>>>>>>{}", result);

        return result;
    }

    @Override
    public Boolean save(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, JSONObject json, KingdeeParamDTO.SaveParamDTO param, Integer type) {
        String msg = "新增数据";
        if (CollectionUtils.isNotEmpty(param.getNeedUpDateFields())) {
            msg = "修改数据";
        }
        log.warn("msg>>>>>{}，param>>>>>>>{},json>>>>>>>>{}", msg, JSONUtil.toJsonStr(param), json);
        RepoResult save = apiUtils.saveKingDee(param);
        if (!save.getResponseStatus().isIsSuccess()) {
            throw new ServiceException(ApiError.ERROR_ADD_KINGDEE_DATA);
        }
        //数据id
        String id = save.getId();
        RepoStatus repoStatus=save.getResponseStatus();
        String kingdeeCode="";
        if(repoStatus.isIsSuccess()){
            kingdeeCode= repoStatus.getSuccessEntitys().get(0).getNumber();
        }
        //金蝶id
        map.put("syncKingdeeId", id);
        //更新业务表中的金蝶id
        updateBusinessSyncKingdeeStatus(type, String.valueOf(map.get("id")), "", id,kingdeeCode);
        return Boolean.TRUE;

    }

    /**
     * @param apiUtils
     * @param kingdeeId
     * @param number
     * @param createOrgId
     * @return JSONObject
     * @description: 根据创建组织id查询
     * @author Will
     * @date: 2023/7/3 10:23
     */
    private JSONObject handleViewJson(KingdeeApiUtils apiUtils, String kingdeeId, String number, Integer createOrgId) {
        JSONObject jsonObject = new JSONObject();
        if (StringUtils.isNotBlank(kingdeeId)) {
            jsonObject.set("id", kingdeeId);
        } else {
            jsonObject.set("number", number);
        }
        //创建组织
        jsonObject.set("CreateOrgId", createOrgId);

        log.info("view方法数据查询,viewJson = {}", JSONUtil.toJsonStr(jsonObject));
        JSONObject model = apiUtils.getViewJson(JSONUtil.toJsonStr(jsonObject));
        return model;
    }


    /**
     * @param apiUtils
     * @param platformEntity
     * @param map
     * @param type
     * @param docStatusEnum
     * @param isNeedError
     * @return Boolean
     * @description: 更新状态
     * @author Will
     * @date: 2023/9/25 15:00
     */
    private Boolean updateApproved(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type, KingdeeDocStatusEnum docStatusEnum, Boolean isNeedError) {

        //金蝶id
        String syncKingdeeId = (String) map.get("syncKingdeeId");
        JSONObject model;
        try {
            model = view(apiUtils, platformEntity.getId(), map);
        } catch (Exception e) {
            if (isNeedError) {
                return Boolean.FALSE;
            } else {
                //操作失败
                throw new ServiceException(ApiError.ERROR_NOT_EXIST_KINGDEE_DATA);
            }
        }
        //判断状态是否一致
        String documentStatus = (String) model.get("DocumentStatus");
        //金蝶id
        String id = String.valueOf(model.get("Id"));
        if (StringUtils.isBlank(syncKingdeeId)) {
            map.put("syncKingdeeId", id);
            syncKingdeeId = id;
        }
        if (docStatusEnum.getCode().equals(documentStatus)) {
            return Boolean.TRUE;
        }
        //重新审核
        if (KingdeeDocStatusEnum.REAPPROVE.equals(docStatusEnum)) {
     /*       //审核中需要撤销
            if (StrUtil.equals(KingdeeDocStatusEnum.APPROVING.getCode(), documentStatus)) {
                return this.cancelAssign(apiUtils,syncKingdeeId);
            }*/
            return this.unAudit(apiUtils, syncKingdeeId);
        }
        //审核通过
        if (KingdeeDocStatusEnum.APPROVED.equals(docStatusEnum)) {
            if (StrUtil.equals(KingdeeDocStatusEnum.SAVED.getCode(), documentStatus) || StrUtil.equals(KingdeeDocStatusEnum.REAPPROVE.getCode(), documentStatus)) {
                return this.submit(map, apiUtils, syncKingdeeId, type);
            }
            return this.audit(map, apiUtils, syncKingdeeId, type);
        }
        return Boolean.TRUE;
    }

}
