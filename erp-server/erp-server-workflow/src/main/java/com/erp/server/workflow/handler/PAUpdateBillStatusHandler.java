package com.erp.server.workflow.handler;

/**
 * @description:
 * @author: hcg
 * @date: 2025/5/31 02:22
 */

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.entity.SysUserThirdEntity;
import com.erp.model.workflow.dto.ApproveTaskDetailDTO;
import com.erp.model.workflow.dto.ApproveTaskInfoDTO;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.*;
import com.erp.rpc.scm.feign.PurchaseApplicationFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.workflow.context.ProcessFormFactory;
import com.erp.server.workflow.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 采购申请单
 *
 * @Author: hcg
 * @CreateTime: 2025-05-31
 * @Description:
 * @Version: 1.0
 */
@Component
@Slf4j
public class PAUpdateBillStatusHandler implements CreateBillHandler {

    @Resource
    ThirdProcessManagementService thirdProcessManagementService;

    @Resource
    ProcessFormFactory processFormFactory;

    @Resource
    PurchaseApplicationFeign purchaseApplicationFeign;

    @Resource
    ApproveTaskInfoService taskInfoService;

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

    @Override
    public boolean isMatch(String event) {
        return CreateBillHandler.super.isMatch(event);
    }

    @Override
    public CfgQueryOptionBussinessKeyEnum getEvent() {
        return CfgQueryOptionBussinessKeyEnum.TRANSFEROUT;
    }

    @Override
    public void createBill(JSONObject jsonObject, CfgThirdProcessEntity thirdProcessEntity, List<CfgProcessFieldMapEntity> fieldMapList, List<CfgProcessValueMapEntity> valueMapList) {
        DictBasicEnum dictBasicEnum = DictBasicEnum.getByCode(thirdProcessEntity.getOperateType());
        ProcessFormHandler constructBillHandler = processFormFactory.getConstructBillHandler(thirdProcessEntity.getSourcePlatform());

        String status = jsonObject.getStr(FsRequestBodyAttributesEnum.STATUS.getCode());

        //值映射
        ObjectMapper mapper = new ObjectMapper();
        //先创建，审批通过后修改单据审核状态
        if (dictBasicEnum != null && DictBasicEnum.CREATEANDUPDATE.equals(dictBasicEnum)) {
            //解析数据
            Map<String, Object> map = constructBillHandler.constructBill(jsonObject.getJSONArray(FsRequestBodyAttributesEnum.FORM.getCode()), fieldMapList, valueMapList);
            //值映射
            PurchaseApplicationDTO.AddDTO addDTO = mapper.convertValue(map, PurchaseApplicationDTO.AddDTO.class);
            //查询三方生成查询
            ApproveTaskInfoEntity taskInfoEntity = approveTaskInfoService.getOne(new LambdaQueryWrapper<ApproveTaskInfoEntity>().eq(ApproveTaskInfoEntity::getThirdInstanceId, jsonObject.getStr(FsRequestBodyAttributesEnum.INSTANCECODE.getCode())).eq(ApproveTaskInfoEntity::getIsDeleted, false));
            if (ObjectUtil.isEmpty(taskInfoEntity)){
                //构建三方生成查询主、明细数据
                List<ApproveTaskDetailDTO.AddDTO> addDTOS = constructBillHandler.generatePullDetailDTO(jsonObject.getJSONArray(FsRequestBodyAttributesEnum.FORM.getCode()), map, fieldMapList);
                //构建三方生成查询主表数据
                ApproveTaskInfoDTO.AddDTO taskInfo = buildApproveTaskInfo(jsonObject, addDTOS);
                taskInfo.setStatus(ApproveTaskStatusEnum.FAIL.getCode());
                //保存三方生成查询
                BaseResultDTO.AddDTO add = taskInfoService.add(taskInfo);
                taskInfoEntity = BeanUtil.copyProperties(taskInfo, ApproveTaskInfoEntity.class);
                //主键id
                taskInfoEntity.setId(add.getId());
            }
            try {
                //添加供应商
                BatchResultDTO batchResultDTO = purchaseApplicationFeign.add(addDTO);
                //回调通过
                handleCallback(taskInfoEntity, jsonObject);
                //更新三方生成查询
                taskInfoEntity.setBussinessKey(thirdProcessEntity.getBussinessKey());
                taskInfoEntity.setBussinessCode(batchResultDTO.getCode());
                taskInfoEntity.setBussinessId(batchResultDTO.getId());
                taskInfoEntity.setHappenTime(LocalDateTime.now());
                taskInfoEntity.setStatus(ApproveTaskStatusEnum.ALL.getCode());
                boolean b = approveTaskInfoService.updateById(taskInfoEntity);
                if (!b) {
                    throw new ServiceException("更新三方生成查询失败");
                }
            } catch (Exception e) {
                throw new ServiceException("创建采购申请单失败错误信息：{}", e.getMessage());
            }
        }
        thirdProcessManagementService.addOrUpdate(jsonObject, thirdProcessEntity.getSourcePlatform());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void afreshGenerate(Map<String, Object> map, CfgThirdProcessEntity thirdProcessEntity, ApproveTaskInfoEntity taskInfo) {
        ThirdProcessInstanceEntity instanceEntity = thirdProcessInstanceService.getOne(new LambdaQueryWrapper<ThirdProcessInstanceEntity>().eq(ThirdProcessInstanceEntity::getInstanceCode, taskInfo.getThirdInstanceId()));
        DictBasicEnum dictBasicEnum = DictBasicEnum.getByCode(thirdProcessEntity.getOperateType());

        ObjectMapper mapper = new ObjectMapper();
        if (dictBasicEnum != null && DictBasicEnum.UPDATEFIELDORSTATUS.equals(dictBasicEnum)) {

        }
        if (dictBasicEnum != null && DictBasicEnum.CREATE.equals(dictBasicEnum)) {
            PurchaseApplicationDTO.AddDTO addDTO = mapper.convertValue(map, PurchaseApplicationDTO.AddDTO.class);
            try {
                //添加供应商
                BatchResultDTO batchResultDTO = purchaseApplicationFeign.add(addDTO);
                //更新三方生成查询
                taskInfo.setBussinessKey(thirdProcessEntity.getBussinessKey());
                taskInfo.setBussinessCode(batchResultDTO.getCode());
                taskInfo.setBussinessId(batchResultDTO.getId());
                taskInfo.setHappenTime(LocalDateTime.now());
                taskInfo.setStatus(ApproveTaskStatusEnum.SUCCESS.getCode());
                boolean b = approveTaskInfoService.updateById(taskInfo);
                if (!b) {
                    throw new ServiceException("更新三方生成查询失败");
                }
            } catch (Exception e) {
                throw new ServiceException("创建采购申请单失败错误信息：{}", e.getMessage());
            }
        }
        if (dictBasicEnum != null && DictBasicEnum.CREATEANDUPDATE.equals(dictBasicEnum)) {
            if (instanceEntity.getStatus().equals(FSApprovalStatusEnum.APPROVED.getCode())) {
                JSONArray taskList = JSONUtil.parseArray(instanceEntity.getTaskList());
                JSONObject lastTask = taskList.getJSONObject(taskList.size() - 1);
                String lastUserId = lastTask.getStr(FsRequestBodyAttributesEnum.USERID.getCode());
                Long endTime = lastTask.getLong(FsRequestBodyAttributesEnum.ENDTIME.getCode());
                LocalDateTime approveTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault());
                EndProcessDTO processDTO = new EndProcessDTO();
                processDTO.setBusinessKey(taskInfo.getBussinessKey());
                processDTO.setBusinessId(taskInfo.getBussinessId());
                processDTO.setApproveStatus(ApproveTypeEnum.getByCode(ApproveTypeEnum.PASS.getStatus()));
                // 来自第三方系统的用户ID可能需要转换为您系统内部的用户ID
                SysUserThirdEntity user = sysUserFeign.getUserByThird(ProcessSourcePlatformEnum.FS.getCode().toUpperCase(), lastUserId);
                processDTO.setApproveUserId(user.getUserId());
                processDTO.setApproveTime(approveTime);
                try {
                    processManagementService.callFeign(taskInfo.getBussinessKey(), processDTO);
                } catch (Exception e) {
                    throw new ServiceException("回调审核通过失败错误信息：{}", e.getMessage());
                } finally {
                    taskInfo.setStatus(ApproveTaskStatusEnum.FAIL.getCode());
                    approveTaskInfoService.updateById(taskInfo);
                }
                return;
            }
            //更新单据状态为待审核
            PurchaseApplicationDTO.AddPADTO addDTO = mapper.convertValue(map, PurchaseApplicationDTO.AddPADTO.class);
            try {
                //添加供应商
                BatchResultDTO batchResultDTO = purchaseApplicationFeign.add(addDTO);
                //更新三方生成查询
                taskInfo.setBussinessKey(thirdProcessEntity.getBussinessKey());
                taskInfo.setBussinessCode(batchResultDTO.getCode());
                taskInfo.setBussinessId(batchResultDTO.getId());
                taskInfo.setHappenTime(LocalDateTime.now());
                taskInfo.setStatus(ApproveTaskStatusEnum.SUCCESS.getCode());
                boolean b = approveTaskInfoService.updateById(taskInfo);
                if (!b) {
                    throw new ServiceException("更新三方生成查询失败");
                }
            } catch (Exception e) {
                throw new ServiceException("创建采购申请单失败错误信息：{}", e.getMessage());
            }
        }
        approveTaskInfoService.updateById(taskInfo);
    }

    @Override
    public ApproveTaskInfoDTO.AddDTO buildApproveTaskInfo(JSONObject jsonObject, List<ApproveTaskDetailDTO.AddDTO> addDTOS) {
        ThirdProcessDefinitionEntity thirdProcessDefinition = thirdProcessDefinitionService.getOne(new LambdaQueryWrapper<ThirdProcessDefinitionEntity>().eq(ThirdProcessDefinitionEntity::getApprovalCode, jsonObject.getStr(FsRequestBodyAttributesEnum.APPROVAL_CODE.getCode())).
                eq(ThirdProcessDefinitionEntity::getIsDeleted, false).eq(ThirdProcessDefinitionEntity::getStatus, ThirdProcessDefinitionStatusEnum.ACTIVE.getCode()));
        ApproveTaskInfoDTO.AddDTO addDTO = new ApproveTaskInfoDTO.AddDTO();
        addDTO.setDetailList(addDTOS);
        addDTO.setType(thirdProcessDefinition.getType());
        addDTO.setThirdDefinniationName(thirdProcessDefinition.getName());
        addDTO.setThirdInstanceId(jsonObject.getStr(FsRequestBodyAttributesEnum.INSTANCE_CODE.getCode()));
        addDTO.setThirdApprovalCode(thirdProcessDefinition.getCode());
        addDTO.setSourcePlatform(thirdProcessDefinition.getSourcePlatform());
        return addDTO;
    }

    public void handleCallback(ApproveTaskInfoEntity entity, JSONObject jsonObject) {
        JSONArray taskList = jsonObject.getJSONArray(FsRequestBodyAttributesEnum.TASKLIST.getCode());
        JSONObject lastTask = taskList.getJSONObject(taskList.size() - 1);
        String lastUserId = lastTask.getStr(FsRequestBodyAttributesEnum.USERID.getCode());
        Long endTime = lastTask.getLong(FsRequestBodyAttributesEnum.ENDTIME.getCode());
        LocalDateTime approveTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTime), ZoneId.systemDefault());
        EndProcessDTO processDTO = new EndProcessDTO();
        processDTO.setBusinessKey(entity.getBussinessKey());
        processDTO.setBusinessId(entity.getBussinessId());
        processDTO.setApproveStatus(ApproveTypeEnum.getByCode(ApproveTypeEnum.PASS.getStatus()));
        // 来自第三方系统的用户ID可能需要转换为您系统内部的用户ID
        SysUserThirdEntity user = sysUserFeign.getUserByThird(ProcessSourcePlatformEnum.FS.getCode().toUpperCase(), lastUserId);
        processDTO.setApproveUserId(user.getUserId());
        processDTO.setApproveTime(approveTime);
        try {
            processManagementService.callFeign(entity.getBussinessKey(), processDTO);

        } catch (Exception e) {
            throw new ServiceException("回调审核通过失败错误信息：{}", e.getMessage());
        }
    }
}