package com.erp.server.workflow.handler;

/**
 * @description:
 * @author: hcg
 * @date: 2025/5/29 00:56
 */

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.workflow.dto.ApproveTaskDetailDTO;
import com.erp.model.workflow.dto.ApproveTaskInfoDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.*;
import com.erp.rpc.scm.feign.PurchaseOrderFeign;
import com.erp.server.workflow.context.ProcessFormFactory;
import com.erp.server.workflow.service.ApproveTaskInfoService;
import com.erp.server.workflow.service.CfgProcessFieldMapService;
import com.erp.server.workflow.service.ThirdProcessDefinitionService;
import com.erp.server.workflow.service.ThirdProcessManagementService;
import lombok.extern.slf4j.Slf4j;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 *@Author: hcg
 *@CreateTime: 2025-05-29
 *@Description:
 *@Version: 1.0
 */
@Component
@Slf4j
public class POUpdateBillStatusHandler implements CreateBillHandler {
    @Resource
    PurchaseOrderFeign purchaseOrderFeign;

    @Resource
    ThirdProcessManagementService thirdProcessManagementService;

    @Resource
    ProcessFormFactory processFormFactory;

    @Resource
    ApproveTaskInfoService taskInfoService;

    @Resource
    CfgProcessFieldMapService fieldMapService;

    @Resource
    private ThirdProcessDefinitionService thirdProcessDefinitionService;
    @Autowired
    private ApproveTaskInfoService approveTaskInfoService;

    @Override
    public boolean isMatch(String event) {
        return CreateBillHandler.super.isMatch(event);
    }

    @Override
    public CfgQueryOptionBussinessKeyEnum getEvent() {
        return CfgQueryOptionBussinessKeyEnum.PURCHASEORDER;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void createBill(JSONObject jsonObject, CfgThirdProcessEntity thirdProcessEntity, List<CfgProcessFieldMapEntity> fieldMapList, List<CfgProcessValueMapEntity> valueMapList) {
        DictBasicEnum dictBasicEnum = DictBasicEnum.getByCode(thirdProcessEntity.getOperateType());
        ProcessFormHandler constructBillHandler = processFormFactory.getConstructBillHandler(thirdProcessEntity.getSourcePlatform());

        if (dictBasicEnum != null && DictBasicEnum.UPDATEFIELDORSTATUS.equals(dictBasicEnum)) {
            String status = jsonObject.getStr(FsRequestBodyAttributesEnum.STATUS.getCode());
            //找到集合中unique为true的元素
            Map<String, Object> map = null;
            try {
                map = constructBillHandler.constructBill(jsonObject.getJSONArray(FsRequestBodyAttributesEnum.FORM.getCode()), fieldMapList, valueMapList);
            }catch (Exception e){
                throw new ServiceException("获取单据失败：{}", e.getMessage());
            }
            CfgProcessFieldMapEntity uniqueField = fieldMapList.stream().filter(req -> req.getIsUnique()).findFirst().orElse(null);
            String uniqueValue = (String) map.get(uniqueField.getSysField());

            List<PurchaseOrderEntity> list= FeignQuery.create(PurchaseOrderEntity.class).eq(uniqueField.getSysField(), uniqueValue).list();
            if (CollUtil.isEmpty(list)||list.size()>1){
                throw new ServiceException("三方审批生成-采购申请单合同状态更新-找到多条系统单据或为找到系统单据，请检查单据，单据唯一键{},单据唯一键值{}",  uniqueField.getSysField(), uniqueValue);
            }
            //更新单
            PurchaseOrderDTO.ContractStampStatusParamsDTO contractStampStatusParamsDTO = new PurchaseOrderDTO.ContractStampStatusParamsDTO();
            contractStampStatusParamsDTO.setIds(Arrays.asList(list.get(0).getId()));
            //根据审核状态更新合同盖章状态
            handleContractStatus(contractStampStatusParamsDTO,status);

            log.warn("采购订单数据转换完成，单据信息：{}", JSONUtil.toJsonStr(contractStampStatusParamsDTO));

            //查询三方生成查询
            ApproveTaskInfoEntity taskInfoEntity = approveTaskInfoService.getOne(new LambdaQueryWrapper<ApproveTaskInfoEntity>().eq(ApproveTaskInfoEntity::getThirdInstanceId, jsonObject.getStr(FsRequestBodyAttributesEnum.INSTANCECODE.getCode())).eq(ApproveTaskInfoEntity::getIsDeleted, false));
            if (ObjectUtil.isEmpty(taskInfoEntity)){
                //构建三方生成查询主、明细数据
                List<ApproveTaskDetailDTO.AddDTO> addDTOS = constructBillHandler.generatePullDetailDTO(jsonObject.getJSONArray(FsRequestBodyAttributesEnum.FORM.getCode()), map, fieldMapList);
                //构建三方生成查询主表数据
                ApproveTaskInfoDTO.AddDTO taskInfo = buildApproveTaskInfo(jsonObject, addDTOS,thirdProcessEntity.getBussinessKey());
                taskInfo.setStatus(ApproveTaskStatusEnum.FAIL.getCode());
                //保存三方生成查询
                BaseResultDTO.AddDTO add = taskInfoService.add(taskInfo);
                taskInfoEntity = BeanUtil.copyProperties(taskInfo, ApproveTaskInfoEntity.class);
                //主键id
                taskInfoEntity.setId(add.getId());
            }
            String reason = "";
            String taskStatus = ApproveTaskStatusEnum.SUCCESS.getCode();
            try {
                //更新盖章状态
                purchaseOrderFeign.updateContractStatusById(contractStampStatusParamsDTO);
            }catch (Exception e) {
                taskStatus = ApproveTaskStatusEnum.FAIL.getCode();
                reason = e.getMessage();
            }
            //更新三方生成查询
            taskInfoEntity.setBussinessKey(thirdProcessEntity.getBussinessKey());
            taskInfoEntity.setBussinessCode(list.get(0).getCode());
            taskInfoEntity.setBussinessId(list.get(0).getId().toString());
            taskInfoEntity.setHappenTime(LocalDateTime.now());
            taskInfoEntity.setStatus(taskStatus);
            taskInfoEntity.setReason(reason);
            boolean b = taskInfoService.updateById(taskInfoEntity);
            if (!b){
                throw new ServiceException("更新三方生成查询失败");
            }
        }
    }

    @Override
    public void afreshGenerate(Map<String, Object> map, CfgThirdProcessEntity thirdProcessEntity, ApproveTaskInfoEntity taskInfo) {
        DictBasicEnum dictBasicEnum = DictBasicEnum.getByCode(thirdProcessEntity.getOperateType());
        if (dictBasicEnum != null && DictBasicEnum.UPDATEFIELDORSTATUS.equals(dictBasicEnum)) {
            List<CfgProcessFieldMapEntity> fieldMapList = fieldMapService.list(new LambdaQueryWrapper<CfgProcessFieldMapEntity>().eq(CfgProcessFieldMapEntity::getCfgId, thirdProcessEntity.getId()).eq(CfgProcessFieldMapEntity::getIsDeleted, false));
            CfgProcessFieldMapEntity uniqueField = fieldMapList.stream().filter(req -> req.getIsUnique()).findFirst().orElse(null);
            String uniqueValue = (String) map.get(uniqueField.getSysField());
            List<PurchaseOrderEntity> list= FeignQuery.create(PurchaseOrderEntity.class).eq(uniqueField.getSysField(), uniqueValue).list();
            if (CollUtil.isEmpty(list)||list.size()>1){
                throw new ServiceException("三方审批生成-采购订单合同状态更新-找到多条系统单据或为找到系统单据，请检查单据，单据唯一键{},单据唯一键值{}",  uniqueField.getSysField(), uniqueValue);
            }
            //更新单
            PurchaseOrderDTO.ContractStampStatusParamsDTO contractStampStatusParamsDTO = new PurchaseOrderDTO.ContractStampStatusParamsDTO();
            contractStampStatusParamsDTO.setIds(Arrays.asList(list.get(0).getId()));
            handleContractStatus(contractStampStatusParamsDTO,taskInfo.getStatus());

            log.warn("采购订单数据转换完成，单据信息：{}", JSONUtil.toJsonStr(contractStampStatusParamsDTO));
            try {
                purchaseOrderFeign.updateContractStatusById(contractStampStatusParamsDTO);
                taskInfo.setBussinessCode(list.get(0).getCode());
                taskInfo.setBussinessId(Arrays.asList(list.get(0).getId()).toString());
                taskInfo.setStatus(ApproveTaskStatusEnum.ALL.getCode());
                taskInfo.setHappenTime(LocalDateTime.now());
                taskInfo.setBussinessKey(thirdProcessEntity.getBussinessKey());
                boolean b = taskInfoService.updateById(taskInfo);
                if (!b){
                    throw new ServiceException("更新三方生成查询状态失败");
                }
            }catch (Exception e){
                throw new ServiceException("更新采购订单盖章申请状态失败");
            }
        }
    }

    private void handleContractStatus(PurchaseOrderDTO.ContractStampStatusParamsDTO contractStampStatusParamsDTO,String status) {
        if (status.equals(FSApprovalStatusEnum.APPROVED.getCode())){
            contractStampStatusParamsDTO.setContractStampStatus(ApproveStatusEnum.APPROVE.getStatus());
        }else if (status.equals(FSApprovalStatusEnum.REJECTED.getCode())){
            contractStampStatusParamsDTO.setContractStampStatus(ApproveStatusEnum.REJECT.getStatus());
        }else {
            contractStampStatusParamsDTO.setContractStampStatus(ApproveStatusEnum.APPROVE_ING.getStatus());
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