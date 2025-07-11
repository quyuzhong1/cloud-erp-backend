package com.erp.server.workflow.handler;

/**
 * @description:
 * @author: hcg
 * @date: 2025/5/31 02:22
 */

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.DictBasicEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.workflow.dto.ApproveTaskDetailDTO;
import com.erp.model.workflow.dto.ApproveTaskInfoDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.*;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.workflow.context.ProcessFormFactory;
import com.erp.server.workflow.service.*;
import groovy.util.logging.Slf4j;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *@Author: hcg
 *@CreateTime: 2025-05-31
 *@Description:
 *@Version: 1.0
 */
@lombok.extern.slf4j.Slf4j
@Component
@Slf4j
public class SupplierUpdateBillStatusHandler implements CreateBillHandler {

    @Resource
    ThirdProcessManagementService thirdProcessManagementService;

    @Resource
    ProcessFormFactory processFormFactory;

    @Resource
    SupplierFeign supplierFeign;

    @Resource
    ApproveTaskInfoService approveTaskInfoService;

    @Resource
    ThirdProcessInstanceService thirdProcessInstanceService;

    @Resource
    private ThirdProcessDefinitionService thirdProcessDefinitionService;

    @Resource
    private ProcessManagementService processManagementService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;


    @Override
    public boolean isMatch(String event) {
        return CreateBillHandler.super.isMatch(event);
    }

    @Override
    public CfgQueryOptionBussinessKeyEnum getEvent() {
        return CfgQueryOptionBussinessKeyEnum.SUPPLIER;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void createBill(JSONObject jsonObject, CfgThirdProcessEntity thirdProcessEntity, List<CfgProcessFieldMapEntity> fieldMapList, List<CfgProcessValueMapEntity> valueMapList) {
        DictBasicEnum dictBasicEnum = DictBasicEnum.getByCode(thirdProcessEntity.getOperateType());
        ProcessFormHandler constructBillHandler = processFormFactory.getConstructBillHandler(thirdProcessEntity.getSourcePlatform());
        String status = jsonObject.getStr(FsRequestBodyAttributesEnum.STATUS.getCode());
        if (!status.equals(FSApprovalStatusEnum.APPROVED.getCode())) {
            log.error("FS审批状态不为通过，当前状态：{}", status);
            return;
        }
        if (ObjectUtil.isEmpty(dictBasicEnum)) {
            log.error("采购申请单未找到当前枚举，当前枚举：{}", thirdProcessEntity.getOperateType());
            return;
        }
        if (DictBasicEnum.CREATEANDUPDATE.equals(dictBasicEnum)) {
            JSONArray taskList = jsonObject.getJSONArray(FsRequestBodyAttributesEnum.TASKLIST.getCode());
            JSONObject lastTask = taskList.getJSONObject(taskList.size() - 1);
            String lastUserId = lastTask.getStr(FsRequestBodyAttributesEnum.USERID.getCode());
            Long endTime = lastTask.getLong(FsRequestBodyAttributesEnum.ENDTIME.getCode());
            LocalDateTime approveTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault());
            //解析数据
            Map<String, Object> map = constructBillHandler.constructBill(jsonObject.getJSONArray(FsRequestBodyAttributesEnum.FORM.getCode()), fieldMapList, valueMapList);
            //处理附件信息
            handleSupplierData(map,Boolean.TRUE);
            //生成三方生成查询明细
            List<ApproveTaskDetailDTO.AddDTO> addDTOS = constructBillHandler.generatePullDetailDTO(jsonObject.getJSONArray(FsRequestBodyAttributesEnum.FORM.getCode()), map, fieldMapList);
            //值映射
            SupplierDTO.InsertDTO addDTO = BeanUtil.toBean(map, SupplierDTO.InsertDTO.class);
            //第一条账户设置成默认
            if (CollUtil.isNotEmpty(addDTO.getBankAccountList())) {
                addDTO.getBankAccountList().get(0).setIsDefault(Boolean.TRUE);
            }
            addDTO.setApprovalStatus(ApproveStatusEnum.APPROVE);
            addDTO.setThirdApprovalUserId(lastUserId);
            addDTO.setThirdApproveTime(approveTime);

            //生成三方生成查询主表数据
            ApproveTaskInfoDTO.AddDTO taskInfo = buildApproveTaskInfo(jsonObject, addDTOS,thirdProcessEntity.getBussinessKey());
            //查询三方生成查询
            ApproveTaskInfoEntity taskInfoEntity = approveTaskInfoService.getOne(new LambdaQueryWrapper<ApproveTaskInfoEntity>().eq(ApproveTaskInfoEntity::getThirdInstanceId, jsonObject.getStr(FsRequestBodyAttributesEnum.INSTANCECODE.getCode())).eq(ApproveTaskInfoEntity::getIsDeleted, false));
            if (ObjectUtil.isNotEmpty(taskInfoEntity)) {
                SupplierEntity supplierEntity = FeignQuery.getById(SupplierEntity.class, taskInfoEntity.getBussinessId());
                if (ObjectUtil.isNotEmpty(supplierEntity)) {
                    throw new ServiceException(ApiError.ERROR_EXIST_BILL, CharSequenceUtil.format("供应商{}",supplierEntity.getCode()));
                }
                //判断是否存在三方生成查询数据，存在则删除
                approveTaskInfoService.deleteByThird(taskInfo.getType(),taskInfo.getThirdInstanceId(),taskInfo.getThirdApprovalCode());
            }
            // 第二步：保存供应商信息
            BatchResultDTO batchResultDTO = new BatchResultDTO();
            String reason = "";
            String taskStatus = ApproveTaskStatusEnum.SUCCESS.getCode();
            try {
                //添加供应商
                ValidatorUtil.validateEntity(addDTO);
                batchResultDTO = supplierFeign.add(addDTO);
            } catch (Exception e) {
                taskStatus = ApproveTaskStatusEnum.FAIL.getCode();
                reason = e.getMessage();
            }
            taskInfo.setBussinessKey(thirdProcessEntity.getBussinessKey());
            taskInfo.setBussinessCode(batchResultDTO.getCode());
            taskInfo.setBussinessId(batchResultDTO.getId());
            taskInfo.setHappenTime(LocalDateTime.now());
            taskInfo.setStatus(taskStatus);
            taskInfo.setReason(reason);
            approveTaskInfoService.add(taskInfo);
        }
        thirdProcessManagementService.addOrUpdate(jsonObject,thirdProcessEntity.getSourcePlatform());
    }

    /**
     * 附件处理
     * @author will
     * @date 2025/7/4 15:58
     * @param map
     * @return void
     */
    private void handleSupplierData(Map<String, Object> map,Boolean isDmpAdd) {

        //付款条件
        Object paymentCondition = map.get("paymentCondition");
        List<BaseDropDownDTO.DisabledDTO> paymentConditionList = scmTaskFeign.listPaymentCondition();

        String paymentConditionCode;
        //dmp新增根据名称匹配，重新生成根据code匹配
        if (Boolean.TRUE.equals(isDmpAdd)) {
             paymentConditionCode = paymentConditionList.stream().
                    filter(req -> CharSequenceUtil.equals(String.valueOf(paymentCondition),req.getValue()))
                    .map(BaseDropDownDTO.DisabledDTO::getCode)
                    .findFirst().orElse("");
        } else {
             paymentConditionCode = paymentConditionList.stream().
                     map(BaseDropDownDTO.DisabledDTO::getCode)
                    .filter(code -> CharSequenceUtil.equals(String.valueOf(paymentCondition), code))
                    .findFirst().orElse("");
        }
        // 如果付款条件不存在，抛出异常
        if (CharSequenceUtil.isBlank(paymentConditionCode)) {
            log.error("付款条件未找到，当前付款条件：{}", paymentCondition);
            throw new ServiceException(ApiError.ERROR_NOT_FOUND, CharSequenceUtil.format("付款条件【{}】", paymentCondition));
        }
        map.put("paymentCondition", paymentConditionCode);

        List<Object> credentialList = (List<Object>) map.get("credentialList");
        if (CollUtil.isEmpty(credentialList)) {
            return;
        }
        // 创建新列表存储处理后的凭证
        List<Map<String, Object>> processedList = new ArrayList<>();

        for (Object object : credentialList) {
            // 将原始对象转为可修改的 Map
            Map<String, Object> credentialMap = JSONUtil.parseObj(object).toBean(Map.class);

            //名称
            Object name = credentialMap.get("name");
            List<DictBasicEntity> disabledList = FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType, com.erp.model.scm.enums.DictBasicEnum.CREDENTIAL_TYPE.getType()).list();
            String credentialCode = disabledList.stream().
                    filter(req -> CharSequenceUtil.equals(String.valueOf(name),req.getName()))
                    .map(DictBasicEntity::getValue)
                    .findFirst().orElse("");
            // 如果凭证类型不存在，抛出异常
            if (CharSequenceUtil.isBlank(credentialCode)) {
                log.error("凭证类型未找到，当前凭证类型：{}", name);
                throw new ServiceException(ApiError.ERROR_NOT_FOUND, CharSequenceUtil.format("凭证类型【{}】", name));
            }
            credentialMap.put("code", credentialCode);

            if (isDmpAdd) {
                //有效期
                Object effectiveDate = credentialMap.get("effectiveDate");
                if (ObjectUtil.isNotEmpty(effectiveDate)) {
                    credentialMap.put("effectiveDate", LocalDateTimeUtil.ofDate((TemporalAccessor) effectiveDate));
                }
                //失效期
                Object expireDate = credentialMap.get("expireDate");
                if (ObjectUtil.isNotEmpty(expireDate)) {
                    credentialMap.put("expireDate", LocalDateTimeUtil.ofDate((TemporalAccessor) expireDate));
                }
            }

            Object attachmentObject = credentialMap.get("attachment");
            if (ObjectUtil.isEmpty(attachmentObject)) {
                processedList.add(credentialMap);
                continue;
            }
            // 处理附件
            Map<String, Object> attachment = JSONUtil.parseObj(attachmentObject).toBean(Map.class);
            List<String> attachmentUrlList = new ArrayList<>();
            List<String> attachmentNameList = new ArrayList<>();

            attachment.forEach((key, value) -> {
                attachmentNameList.add(key);
                attachmentUrlList.add(String.valueOf(value));
            });

            // 更新凭证对象
            credentialMap.put("attachmentUrlList", attachmentUrlList);
            credentialMap.put("attachmentNameList", attachmentNameList);

            processedList.add(credentialMap);
        }

        // 将处理后的列表更新回原始 map
        map.put("credentialList", processedList);
    }

    @Override
    public void afreshGenerate(Map<String, Object> map, CfgThirdProcessEntity thirdProcessEntity, ApproveTaskInfoEntity taskInfo) {
        ThirdProcessInstanceEntity instanceEntity = thirdProcessInstanceService.getOne(new LambdaQueryWrapper<ThirdProcessInstanceEntity>().eq(ThirdProcessInstanceEntity::getInstanceCode, taskInfo.getThirdInstanceId()));
        DictBasicEnum dictBasicEnum = DictBasicEnum.getByCode(thirdProcessEntity.getOperateType());
        if (ObjectUtil.isEmpty(dictBasicEnum)) {
            log.error("供应商未找到该枚举类型，当前枚举：{}", thirdProcessEntity.getOperateType());
            return;
        }
        if (DictBasicEnum.UPDATEFIELDORSTATUS.equals(dictBasicEnum)) {
            log.error("供应商不支持仅更新字段/状态类型");
            return;
        }
        if (!instanceEntity.getStatus().equals(FSApprovalStatusEnum.APPROVED.getCode())) {
            log.error("FS审批状态不为通过，当前状态：{}", instanceEntity.getStatus());
            return;
        }
        SupplierEntity supplierEntity = FeignQuery.getById(SupplierEntity.class, taskInfo.getBussinessId());
        if (ObjectUtil.isNotEmpty(supplierEntity)) {
            throw new ServiceException(ApiError.ERROR_EXIST_BILL, CharSequenceUtil.format("供应商{}",supplierEntity.getCode()));
        }
        if (DictBasicEnum.CREATEANDUPDATE.equals(dictBasicEnum)) {
            //更新单据状态为审核通过
            JSONArray taskList = JSONUtil.parseArray(instanceEntity.getTaskList());
            JSONObject lastTask = taskList.getJSONObject(taskList.size() - 1);
            String lastUserId = lastTask.getStr(FsRequestBodyAttributesEnum.USERID.getCode());
            Long endTime = lastTask.getLong(FsRequestBodyAttributesEnum.ENDTIME.getCode());
            LocalDateTime approveTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault());

            //处理附件信息
            handleSupplierData(map,Boolean.FALSE);

            //值映射
            SupplierDTO.InsertDTO addDTO = BeanUtil.toBean(map, SupplierDTO.InsertDTO.class);
            //第一条账户设置成默认
            if (CollUtil.isNotEmpty(addDTO.getBankAccountList())) {
                addDTO.getBankAccountList().get(0).setIsDefault(Boolean.TRUE);
            }
            addDTO.setApprovalStatus(ApproveStatusEnum.APPROVE);
            addDTO.setThirdApprovalUserId(lastUserId);
            addDTO.setThirdApproveTime(approveTime);

            // 第二步：保存供应商信息
            BatchResultDTO batchResultDTO = new BatchResultDTO();
            String reason = "";
            String taskStatus = ApproveTaskStatusEnum.SUCCESS.getCode();
            try {
                // 第二步：保存供应商信息
                ValidatorUtil.validateEntity(addDTO);
                batchResultDTO = supplierFeign.add(addDTO);
            } catch (Exception e) {
                taskStatus = ApproveTaskStatusEnum.FAIL.getCode();
                reason = e.getMessage();
            }
            taskInfo.setBussinessKey(thirdProcessEntity.getBussinessKey());
            taskInfo.setBussinessCode(batchResultDTO.getCode());
            taskInfo.setBussinessId(batchResultDTO.getId());
            taskInfo.setHappenTime(LocalDateTime.now());
            taskInfo.setStatus(taskStatus);
            taskInfo.setReason(reason);
            approveTaskInfoService.updateById(taskInfo);
        }
    }

    @Override
    public ApproveTaskInfoDTO.AddDTO buildApproveTaskInfo(JSONObject jsonObject, List<ApproveTaskDetailDTO.AddDTO> addDTOS,String bussinessKey) {
        ThirdProcessDefinitionEntity thirdProcessDefinition = thirdProcessDefinitionService.getOne(new LambdaQueryWrapper<ThirdProcessDefinitionEntity>().eq(ThirdProcessDefinitionEntity::getApprovalCode, jsonObject.getStr(FsRequestBodyAttributesEnum.APPROVALCODE.getCode())).
                eq(ThirdProcessDefinitionEntity::getIsDeleted, false).eq(ThirdProcessDefinitionEntity::getStatus, ThirdProcessDefinitionStatusEnum.ACTIVE.getCode()));
        ApproveTaskInfoDTO.AddDTO addDTO = new ApproveTaskInfoDTO.AddDTO();
        addDTO.setDetailList(addDTOS);
        addDTO.setType(thirdProcessDefinition.getType());
        addDTO.setThirdDefinniationName(thirdProcessDefinition.getName());
        addDTO.setThirdInstanceId(jsonObject.getStr(FsRequestBodyAttributesEnum.INSTANCECODE.getCode()));
        addDTO.setThirdApprovalCode(thirdProcessDefinition.getApprovalCode());
        addDTO.setSourcePlatform(thirdProcessDefinition.getSourcePlatform());
        addDTO.setBussinessKey(bussinessKey);
        return addDTO;
    }
}
