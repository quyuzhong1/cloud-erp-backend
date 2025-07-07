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
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.entity.SysUserThirdEntity;
import com.erp.model.workflow.dto.ApproveTaskDetailDTO;
import com.erp.model.workflow.dto.ApproveTaskInfoDTO;
import com.erp.model.workflow.dto.EndProcessDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.*;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.workflow.context.ProcessFormFactory;
import com.erp.server.workflow.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.util.logging.Slf4j;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *@Author: hcg
 *@CreateTime: 2025-05-31
 *@Description:
 *@Version: 1.0
 */
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

        ObjectMapper objectMapper = new ObjectMapper();

        if (dictBasicEnum != null && DictBasicEnum.CREATEANDUPDATE.equals(dictBasicEnum)) {
            ApproveTaskInfoEntity entity = approveTaskInfoService.getOne(new LambdaQueryWrapper<ApproveTaskInfoEntity>().eq(ApproveTaskInfoEntity::getThirdInstanceId, jsonObject.getStr(FsRequestBodyAttributesEnum.INSTANCECODE.getCode())));
            if (ObjectUtil.isNotEmpty(entity) && status.equals(FSApprovalStatusEnum.APPROVED.getCode())) {
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
                } finally {
                    entity.setStatus(ApproveTaskStatusEnum.FAIL.getCode());
                    approveTaskInfoService.updateById(entity);
                }
                return;
            }
            //解析数据
            Map<String, Object> map = constructBillHandler.constructBill(jsonObject.getJSONArray(FsRequestBodyAttributesEnum.FORM.getCode()), fieldMapList, valueMapList);
            //处理附件信息
            handleAttachment(map);
            //生成三方生成查询明细
            List<ApproveTaskDetailDTO.AddDTO> addDTOS = constructBillHandler.generatePullDetailDTO(jsonObject.getJSONArray(FsRequestBodyAttributesEnum.FORM.getCode()), map, fieldMapList);
            //值映射
            SupplierDTO.InsertDTO addDTO = objectMapper.convertValue(map, SupplierDTO.InsertDTO.class);
            //第一条账户设置成默认
            if (CollUtil.isNotEmpty(addDTO.getBankAccountList())) {
                addDTO.getBankAccountList().get(0).setIsDefault(Boolean.TRUE);
            }
            addDTO.setApprovalStatus(ApproveStatusEnum.APPROVE);

            //生成三方生成查询主表数据
            ApproveTaskInfoDTO.AddDTO taskInfo = buildApproveTaskInfo(jsonObject, addDTOS,thirdProcessEntity.getBussinessKey());

            //判断是否存在三方生成查询数据，存在则删除
            approveTaskInfoService.deleteByThird(taskInfo.getType(),taskInfo.getThirdInstanceId(),taskInfo.getThirdApprovalCode());

            taskInfo.setStatus(ApproveTaskStatusEnum.FAIL.getCode());
            //保存三方生成查询
            BaseResultDTO.AddDTO add = approveTaskInfoService.add(taskInfo);
            if (add == null || add.getId() == null) {
                throw new ServiceException("保存审批任务信息失败");
            }
            //转换
            ApproveTaskInfoEntity taskInfoEntity = BeanUtil.copyProperties(taskInfo, ApproveTaskInfoEntity.class);
            taskInfoEntity.setId(add.getId());
            try {
                // 第二步：保存供应商信息
                String id = supplierFeign.add(addDTO);
                List<SupplierEntity> list = FeignQuery.create(SupplierEntity.class).eq(SupplierEntity::getId, id).list();
                // 第三步：更新taskInfo
                if (!CollUtil.isEmpty(list)) {
                    taskInfoEntity.setBussinessKey(thirdProcessEntity.getBussinessKey());
                    taskInfoEntity.setBussinessCode(list.get(0).getCode());
                    taskInfoEntity.setBussinessId(id);
                    taskInfoEntity.setHappenTime(LocalDateTime.now());
                    taskInfoEntity.setStatus(ApproveTaskStatusEnum.SUCCESS.getCode());
                    boolean b = approveTaskInfoService.updateById(taskInfoEntity);
                    if (!b){
                        throw new ServiceException("更新三方生成查询失败");
                    }
                }
            } catch (Exception e) {
                throw new ServiceException("创建供应商失败错误信息：{}", e.getMessage());
            }
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
    private void handleAttachment(Map<String, Object> map) {
        List<Object> credentialList = (List<Object>) map.get("credentialList");
        if (CollUtil.isEmpty(credentialList)) {
            return;
        }
        // 创建新列表存储处理后的凭证
        List<Map<String, Object>> processedList = new ArrayList<>();

        for (Object object : credentialList) {
            // 将原始对象转为可修改的 Map
            Map<String, Object> credentialMap = JSONUtil.parseObj(object).toBean(Map.class);

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
            credentialMap.remove("attachment");
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

        ObjectMapper objectMapper = new ObjectMapper();
        if (dictBasicEnum != null && DictBasicEnum.UPDATEFIELDORSTATUS.equals(dictBasicEnum)) {
            return;
        }
        if (dictBasicEnum != null && DictBasicEnum.CREATEANDUPDATE.equals(dictBasicEnum)) {
            //更新单据状态为审核通过
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
            //创建单据
            SupplierDTO.InsertDTO addDTO = objectMapper.convertValue(map, SupplierDTO.InsertDTO.class);

            try {
                // 第二步：保存供应商信息
                String id = supplierFeign.add(addDTO);
                List<SupplierEntity> list = FeignQuery.create(SupplierEntity.class).eq(SupplierEntity::getId, id).list();
                // 第三步：更新taskInfo
                if (!CollUtil.isEmpty(list)) {
                    taskInfo.setBussinessKey(thirdProcessEntity.getBussinessKey());
                    taskInfo.setBussinessCode(list.get(0).getCode());
                    taskInfo.setBussinessId(id);
                    taskInfo.setHappenTime(LocalDateTime.now());
                    taskInfo.setStatus(ApproveTaskStatusEnum.SUCCESS.getCode());
                    boolean b = approveTaskInfoService.updateById(taskInfo);
                    if (!b){
                        throw new ServiceException("更新三方生成查询失败");
                    }
                }
            } catch (Exception e) {
                throw new ServiceException("创建供应商失败错误信息：{}", e.getMessage());
            }
        }
        taskInfo.setStatus(ApproveTaskStatusEnum.SUCCESS.getCode());
        approveTaskInfoService.updateById(taskInfo);
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
