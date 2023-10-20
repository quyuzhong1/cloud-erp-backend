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
import com.common.business.enums.SyncOperateEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastJsonUtil;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.common.message.enums.ApiModuleTypeEnum;
import com.erp.model.dmp.constant.CfgApiAuthContant;
import com.erp.model.dmp.dto.ApiPlmSyncLogDTO;
import com.erp.model.dmp.dto.ApiSyncTaskDTO;
import com.erp.model.dmp.dto.CfgApiAuthDTO;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.entity.CfgApiAuthEntity;
import com.erp.model.dmp.entity.CfgApiFieldMapValueEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.*;
import com.erp.rpc.oms.feign.OmsTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.dmp.push.service.kingdee.KingdeeCommonService;
import com.erp.server.dmp.service.*;
import com.erp.server.dmp.utils.KingdeeApiUtils;
import com.erp.server.dmp.utils.KingdeeUtils;
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

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private OmsTaskFeign omsTaskFeign;

    @Resource
    private ApiSyncTaskService apiSyncTaskService;


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
    public PlatformEntity getPlatformEntity(Map<String, Object> map, Integer type) {
        //传入map数据不能为空
        if (CollectionUtils.isEmpty(map)) {
            log.error("同步数据不存在！");
            return null;
        }
        PlatformEntity platformEntity = platformService.getByName(PlatformEnum.KINGDEE.getDesc());
        if (ObjectUtils.isEmpty(platformEntity)) {
            log.error("第三方平台【{}】未找到！", PlatformEnum.KINGDEE.getDesc());
            kingdeeCommonService.insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), "", String.format("第三方平台【{}】未找到！", PlatformEnum.KINGDEE.getDesc()), type, ApiSendStatusEnum.FAILURE.getCode());
        }
        return platformEntity;
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
    public Boolean excuteOperation(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type, String number, String operate) {
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

        try {
            apiUtils.excuteOperation(operateNumber, JSONUtil.toJsonStr(viewMap));
        } catch (Exception e) {
            //新增失败时添加日志
            insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), JSONUtil.toJsonStr(viewMap), e.getMessage(), type, ApiSendStatusEnum.FAILURE.getCode());
            return Boolean.FALSE;
        }
        //操作成功添加日志
        insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), JSONUtil.toJsonStr(viewMap), SyncOperateEnum.getDescByCode(operate), type, ApiSendStatusEnum.SUCCESS.getCode());
        return Boolean.TRUE;
    }

    /**
     * 检查并禁用，反禁用
     * 检查是否同步到金蝶 如果没有就不用同步
     * 审核不通过不用同步到金蝶 但是作废缺要同步金蝶 避免这个问题
     * TODO  暂停
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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean checkAndExcuteOperation(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type, String number, String operate) {
        if (map.containsKey("syncKingdeeId")) {
            String syncKingdeeId = (String) map.get("syncKingdeeId");
            //表示没有那么就让其 无需同步
            if (StringUtils.isBlank(syncKingdeeId)) {
                String businessId = String.valueOf(map.get("id"));
                this.updateBusinessSyncKingdeeStatus(type, businessId, SyncStatusEnum.NO_NEED_SYNC.getCode(), "");
            } else {
                this.excuteOperation(apiUtils, platformEntity, map, type, number, operate);
            }
        }

        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type, String number) {
        LinkedHashMap<String, Object> viewMap = new LinkedHashMap<>();
        //金蝶id
        String syncKingdeeId = (String) map.get("syncKingdeeId");

        if (ObjectUtils.isEmpty(syncKingdeeId)) {
            viewMap.put("ids", Arrays.asList(syncKingdeeId));
        } else {
            viewMap.put("numbers", Arrays.asList(number));
        }
        try {
            apiUtils.delete(JSONUtil.toJsonStr(viewMap));
        } catch (Exception e) {
            //新增失败时添加日志及定时任务
            insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), JSONUtil.toJsonStr(viewMap), e.getMessage(), type, ApiSendStatusEnum.FAILURE.getCode());
            return;
        }
        //操作成功添加日志
        insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), JSONUtil.toJsonStr(viewMap), "删除", type, ApiSendStatusEnum.SUCCESS.getCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void customerGroupDelete(KingdeeApiUtils apiUtils, PlatformEntity platformEntity, Map<String, Object> map, Integer type) {
        try {
            apiUtils.customerGroupDelete(String.valueOf(map.get("id")));
        } catch (Exception e) {
            //新增失败时添加日志及定时任务
            insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), JSONUtil.toJsonStr(map), e.getMessage(), type, ApiSendStatusEnum.FAILURE.getCode());
            return;
        }
        //操作成功添加日志
        insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), JSONUtil.toJsonStr(map), "删除", type, ApiSendStatusEnum.SUCCESS.getCode());

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean customerGroupSaveOrUpdate(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, JSONObject json, SaveParam param, Integer type) {
        RepoRet repoRet;
        String msg = "新增数据";
        if (CollectionUtils.isNotEmpty(param.getNeedUpDateFields())) {
            msg = "修改数据";
        }
        try {
            repoRet = apiUtils.customerGroupSave(param);
        } catch (Exception e) {
            //新增失败时添加日志及定时任务
            insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), JSONUtil.toJsonStr(json), msg.concat("；").concat(e.getMessage()), type, ApiSendStatusEnum.FAILURE.getCode());
            return Boolean.FALSE;
        }
        if (ObjectUtil.isNotEmpty(repoRet.getResult()) && repoRet.getResult().getResponseStatus().getSuccessEntitys() != null) {
            ArrayList<SuccessEntity> successEntitys = repoRet.getResult().getResponseStatus().getSuccessEntitys();
            if (CollectionUtils.isNotEmpty(successEntitys)) {
                //数据id
                String id = successEntitys.get(MathUtil.ZERO).getId();
                //金蝶id
                map.put("syncKingdeeId", id);
                //更新业务表中的金蝶id
                updateBusinessSyncKingdeeStatus(type, String.valueOf(map.get("id")), SyncStatusEnum.SUCCESS_SYNC.getCode(), id);
                //新增成功操作日志
                insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), JSONUtil.toJsonStr(json), msg, type, ApiSendStatusEnum.SUCCESS.getCode());
            }
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdate(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, JSONObject json, SaveParam param, Integer type) {
        SaveResult save;
        log.info("json>>>>>>>{}",json);
        String msg = "新增数据";
        if (CollectionUtils.isNotEmpty(param.getNeedUpDateFields())) {
            msg = "修改数据";
        }
        try {
            save = apiUtils.save(param);
        } catch (Exception e) {
            //新增失败时添加日志及定时任务
            insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), JSONUtil.toJsonStr(json), msg.concat("；").concat(e.getMessage()), type, ApiSendStatusEnum.FAILURE.getCode());
            return Boolean.FALSE;
        }
        //数据id
        String id = save.getResult().getId();
        //金蝶id
        map.put("syncKingdeeId", id);
        //更新业务表中的金蝶id
        updateBusinessSyncKingdeeStatus(type, String.valueOf(map.get("id")), "", id);
        //新增成功操作日志
        insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), JSONUtil.toJsonStr(json), msg, type, ApiSendStatusEnum.SUCCESS.getCode());
        //提交
        submit(platformEntity, map, apiUtils, id, type);
        return Boolean.TRUE;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateCustomerContact(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, JSONObject json, SaveParam param, Integer type) {
        SaveResult save;
        String msg = "新增数据";
        if (CollectionUtils.isNotEmpty(param.getNeedUpDateFields())) {
            msg = "修改数据";
        }
        try {
            save = apiUtils.save(param);
        } catch (Exception e) {
            //新增失败时添加日志及定时任务
            insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), JSONUtil.toJsonStr(json), msg.concat("；").concat(e.getMessage()), type, ApiSendStatusEnum.FAILURE.getCode());
            return Boolean.FALSE;
        }
        //数据id
        String id = save.getResult().getId();
        //金蝶id
        map.put("syncKingdeeId", id);
        //更新业务表中的金蝶id
        updateBusinessSyncKingdeeStatus(type, String.valueOf(map.get("id")), SyncStatusEnum.SUCCESS_SYNC.getCode(), id);
        //新增成功操作日志
        insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), JSONUtil.toJsonStr(json), msg, type, ApiSendStatusEnum.SUCCESS.getCode());
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)

    public Boolean push(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils sourceApiUtils, KingdeeApiUtils apiUtils, JSONObject jsonMap, SaveParam param, Integer type, JSONObject json) {
        RepoResult result;
        String msg = "下推";
        try {
            result = sourceApiUtils.push(jsonMap);
        } catch (Exception e) {
            //新增失败时添加日志及定时任务
            insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), JSONUtil.toJsonStr(jsonMap), msg.concat("；").concat(e.getMessage()), type, ApiSendStatusEnum.FAILURE.getCode());
            return Boolean.FALSE;
        }
        //数据id
        String id = result.getResponseStatus().getSuccessEntitys().get(0).getId();
        //金蝶id
        map.put("syncKingdeeId", id);
        //更新业务表中的金蝶id
        updateBusinessSyncKingdeeStatus(type, String.valueOf(map.get("id")), "", id);
        //新增成功操作日志
        insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), JSONUtil.toJsonStr(json), msg, type, ApiSendStatusEnum.SUCCESS.getCode());
        //给修改json对象赋值ID
        KingdeeUtils.makeFieldJson(json, "FId", ".", id);
        StringBuffer allKey = FastJsonUtil.getAllKey(json);
        ArrayList<String> apiFieldList = (ArrayList) Arrays.stream(allKey.toString().split(",")).collect(Collectors.toList());
        param.setNeedUpDateFields(apiFieldList);
        saveOrUpdate(platformEntity, map, apiUtils, json, param, type);
        return Boolean.TRUE;
    }

    /**
     * @param platformEntity
     * @param apiUtils
     * @param id
     * @description: 提交
     * @author Will
     * @date: 2023/1/16 16:39
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submit(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, String id, Integer type) {
        //提交
        List<String> ids = new ArrayList<>();
        ids.add(id);
        try {
            apiUtils.submit(ids);
        } catch (Exception e) {
            //提交失败操作日志及定时任务
            log.error("提交失败", e);
            insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), JSONUtil.toJsonStr(ids), e.getMessage(), type, ApiSendStatusEnum.FAILURE.getCode());
            return Boolean.FALSE;
        }
        log.info("提交成功,数据Id = 【{}】", JSONUtil.toJsonStr(ids));
        //提交成功操作日志
        insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), JSONUtil.toJsonStr(ids), "提交成功", type, ApiSendStatusEnum.SUCCESS.getCode());
        //提交成功后继续审核直至已审核
        Boolean audit = audit(platformEntity, map, apiUtils, id, type);
        return audit;
    }

    /**
     * @param platformEntity
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
    public Boolean audit(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, String id, Integer type) {
        LinkedHashMap<String, Object> viewMap = new LinkedHashMap<>();
        viewMap.put("Id", id);
        JSONObject model = apiUtils.getViewJson(JSONUtil.toJsonStr(viewMap));
        //单据状态
        String documentStatus = (String) model.get("DocumentStatus");
        if (!KingdeeDocStatusEnum.APPROVED.getCode().equals(documentStatus)) {
            //非已审核继续审核
            ArrayList<String> ids = new ArrayList<>();
            ids.add(id);
            try {
                apiUtils.auditById(ids);
            } catch (Exception e) {
                //审核失败操作日志及定时任务
                log.error("审核失败", JSONUtil.toJsonStr(viewMap));
                insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), JSONUtil.toJsonStr(viewMap), e.getMessage(), type, ApiSendStatusEnum.FAILURE.getCode());
                return Boolean.FALSE;
            }
            //审核成功操作日志
            log.info("审核成功,数据【{}】", JSONUtil.toJsonStr(viewMap));
            insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), JSONUtil.toJsonStr(viewMap), "审核成功", type, ApiSendStatusEnum.SUCCESS.getCode());
            //当审核状态非已审核时继续审核
            audit(platformEntity, map, apiUtils, id, type);
        }
        //更新业务单据状态
        kingdeeCommonService.updateBusinessSyncKingdeeStatus(type, map.get("id").toString(), SyncStatusEnum.SUCCESS_SYNC.getCode(), id);
        return Boolean.TRUE;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)

    public Boolean unAudit(PlatformEntity platformEntity, Map<String, Object> map, KingdeeApiUtils apiUtils, String id, Integer type) {
        //审核中或已审核则要先反审
        ArrayList<String> ids = new ArrayList<>();
        ids.add(id);
        try {
            apiUtils.unAuditById(ids);
        } catch (Exception e) {
            //反审核失败操作日志及定时任务
            log.error("反审核失败 >>>{}", e);
            insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), id, e.getMessage(), type, ApiSendStatusEnum.FAILURE.getCode());
            return Boolean.FALSE;
        }
        //反审核成功操作日志
        log.info("反审核成功,数据【{}】", id);
        insertLogWriteBackSyncKingdeeStatus(platformEntity, String.valueOf(map.get("id")), id, "反审核成功", type, ApiSendStatusEnum.SUCCESS.getCode());
        return Boolean.TRUE;
    }


    /**
     * @param platformEntity
     * @param businessId
     * @param jsonData
     * @param msg
     * @description: 操作成功添加日志
     * @author Will
     * @date: 2023/1/16 18:34
     */
    @Override
    public void insertSyncLog(PlatformEntity platformEntity, String businessId,
                              String jsonData, String msg, Integer type, Integer status) {
        //新增日志信息
        ApiPlmSyncLogDTO apiPlmSyncLogDTO = new ApiPlmSyncLogDTO();
        apiPlmSyncLogDTO.setApiPlatformId(ObjectUtils.isEmpty(platformEntity) ? "" : platformEntity.getId());
        apiPlmSyncLogDTO.setApiPlatform(ObjectUtils.isEmpty(platformEntity) ? "" : platformEntity.getName());
        apiPlmSyncLogDTO.setModuleType(type);
        apiPlmSyncLogDTO.setBusinessId(businessId);
        apiPlmSyncLogDTO.setStatus(status);
        apiPlmSyncLogDTO.setMsg(msg);
        apiPlmSyncLogDTO.setRequestParamJson(jsonData);
        apiPlmSyncLogService.insert(apiPlmSyncLogDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)

    public void insertLogWriteBackSyncKingdeeStatus(PlatformEntity platformEntity, String businessId,
                                                    String jsonData, String msg, Integer type, Integer status) {
        //新增任务
        insertSyncTask(platformEntity, businessId, jsonData, type, status, msg);
        //新增日志
        insertSyncLog(platformEntity, businessId, jsonData, msg, type, status);
        //更新金蝶同步状态
        this.updateBusinessSyncKingdeeStatus(type, businessId, SyncStatusEnum.getCodeBySendStatus(status), "");
    }

    /**
     * @param platformEntity
     * @param businessId
     * @param jsonData
     * @param type
     * @param status
     * @description: 新增推送任务
     * @author Will
     * @date: 2023/7/10 19:14
     */
    private void insertSyncTask(PlatformEntity platformEntity, String businessId,
                                String jsonData, Integer type, Integer status, String msg) {

        ApiSyncTaskDTO apiSyncTaskDTO = new ApiSyncTaskDTO();
        apiSyncTaskDTO.setApiPlatformId(platformEntity.getId());
        apiSyncTaskDTO.setApiPlatform(platformEntity.getName());
        apiSyncTaskDTO.setModuleType(type);
        apiSyncTaskDTO.setRequestParamJson(jsonData);
        apiSyncTaskDTO.setBusinessId(businessId);
        apiSyncTaskDTO.setStatus(status);
        apiSyncTaskDTO.setMsg(msg);
        apiSyncTaskService.addOrUpdateApiSyncTask(apiSyncTaskDTO);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)

    public void updateBusinessSyncKingdeeStatus(Integer code, String businessId, String status, String kingdeeId) {
        //更新业务单据状态
        Map<String, Object> params = new HashMap<>(MathUtil.THREE);
        params.put("code", code.toString());
        params.put("businessId", businessId);
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
     * @param parentMap              1级（数据结果为集合）数据
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
            kingdeeCommonService.insertSyncLog(platformEntity, orderNo, JSONUtil.toJsonStr(dataMap), "未配置同步字段", modelType, ApiSendStatusEnum.FAILURE.getCode());
            throw new ServiceException(ApiError.ERROR_97025);
        }
        JSONObject json = kingdeeCommonService.makeApiFieldJson(dataMap, platformEntity.getId(), modelType);
        SaveResult saveResult = apiUtils.save(new SaveParam<>(json));
        boolean save = saveResult.isSuccessfully();
        kingdeeCommonService.insertSyncLog(platformEntity, orderNo, JSONUtil.toJsonStr(dataMap), "保存直接调拨单到金蝶", modelType, save ? ApiSendStatusEnum.SUCCESS.getCode() : ApiSendStatusEnum.FAILURE.getCode());
        if (!save) {
            log.error("KingdeeCommonServiceImpl>>>addKingdeeRecord>>>调用金蝶保存接口失败saveResult:{}", saveResult);
            throw new ServiceException(ApiError.ERROR_KINGDEE_SAVE);
        }
        return saveResult.getResult().getId();
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

    /**
     * @param apiUtils
     * @param id
     * @param number
     * @param createOrgId
     * @return JSONObject
     * @description: 根据创建组织id查询
     * @author Will
     * @date: 2023/7/3 10:23
     */
    private JSONObject handleViewJson(KingdeeApiUtils apiUtils, String id, String number, Integer createOrgId) {
        LinkedHashMap<String, Object> viewMap = new LinkedHashMap<>();
        if (StringUtils.isNotBlank(id)) {
            viewMap.put("id", id);
        } else {
            viewMap.put("number", number);
        }
        //创建组织
        viewMap.put("CreateOrgId", createOrgId);

        log.info("view方法数据查询,viewJson = {}", JSONUtil.toJsonStr(viewMap));
        JSONObject model = apiUtils.getViewJson(JSONUtil.toJsonStr(viewMap));
        return model;
    }
}
