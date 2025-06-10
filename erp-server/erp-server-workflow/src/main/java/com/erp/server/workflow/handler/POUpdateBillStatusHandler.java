package com.erp.server.workflow.handler;

/**
 * @description:
 * @author: hcg
 * @date: 2025/5/29 00:56
 */

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.wrapper.FeignBuilder;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.workflow.dto.ApproveTaskDetailDTO;
import com.erp.model.workflow.dto.ApproveTaskInfoDTO;
import com.erp.model.workflow.dto.ThirdProcessManagementDTO;
import com.erp.model.workflow.entity.ApproveTaskInfoEntity;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.model.workflow.entity.CfgThirdProcessEntity;
import com.erp.model.workflow.enums.*;
import com.erp.rpc.scm.feign.PurchaseOrderFeign;
import com.erp.server.workflow.context.ProcessFormFactory;
import com.erp.server.workflow.service.ApproveTaskInfoService;
import com.erp.server.workflow.service.CfgProcessFieldMapService;
import com.erp.server.workflow.service.CfgProcessValueMapService;
import com.erp.server.workflow.service.ThirdProcessManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.util.logging.Slf4j;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.collections.SetUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public boolean isMatch(String event) {
        return CreateBillHandler.super.isMatch(event);
    }

    @Override
    public CfgQueryOptionBussinessKeyEnum getEvent() {
        return CfgQueryOptionBussinessKeyEnum.PURCHASEORDER;
    }

    @Override
    @GlobalTransactional
    public void createBill(JSONObject jsonObject, CfgThirdProcessEntity thirdProcessEntity, List<CfgProcessFieldMapEntity> fieldMapList, List<CfgProcessValueMapEntity> valueMapList) {
        DictBasicEnum dictBasicEnum = DictBasicEnum.getByCode(thirdProcessEntity.getOperateType());
        ProcessFormHandler constructBillHandler = processFormFactory.getConstructBillHandler(thirdProcessEntity.getSourcePlatform());

        if (dictBasicEnum != null && DictBasicEnum.UPDATEFIELDORSTATUS.equals(dictBasicEnum)) {
            //找到集合中unique为true的元素
            Map<String, Object> map = constructBillHandler.constructBill(jsonObject.getJSONArray("form"), fieldMapList, valueMapList);

            CfgProcessFieldMapEntity uniqueField = fieldMapList.stream().filter(req -> req.getIsUnique()).findFirst().orElse(null);
            String uniqueValue = (String) map.get(uniqueField.getSysField());

            List<PurchaseOrderEntity> list= FeignQuery.create(PurchaseOrderEntity.class).eq(uniqueField.getSysField(), uniqueValue).list();
            if (CollUtil.isEmpty(list)||list.size()>1){
                throw new ServiceException("三方审批生成-采购申请单合同状态更新-找到多条系统单据或为找到系统单据，请检查单据，单据唯一键{},单据唯一键值{}",  uniqueField.getSysField(), uniqueValue);
            }
            //更新单
            PurchaseOrderDTO.ContractStampStatusParamsDTO contractStampStatusParamsDTO = new PurchaseOrderDTO.ContractStampStatusParamsDTO();
            contractStampStatusParamsDTO.setIds(Arrays.asList(list.get(0).getId()));
            contractStampStatusParamsDTO.setContractStampStatus(map.get("contractStampStatus").toString());

            List<ApproveTaskDetailDTO.AddDTO> addDTOS = constructBillHandler.generatePullDetailDTO(jsonObject.getJSONArray("form"), map, fieldMapList);
            ApproveTaskInfoDTO.AddDTO taskInfo = buildApproveTaskInfo(thirdProcessEntity, addDTOS);
            taskInfo.setThirdInstanceId(jsonObject.getStr("instance_code"));
            BaseResultDTO.AddDTO add = taskInfoService.add(taskInfo);
            taskInfo.setBussinessCode(list.get(0).getCode());
            taskInfo.setBussinessId(Arrays.asList(list.get(0).getId()).toString());
            ApproveTaskInfoEntity taskInfoEntity = BeanUtil.copyProperties(taskInfo, ApproveTaskInfoEntity.class);
            taskInfoEntity.setId(add.getId());
            try {
                //成功则更新状态
                purchaseOrderFeign.updateContractStatusById(contractStampStatusParamsDTO);
                taskInfoEntity.setStatus(ApproveTaskStatusEnum.SUCCESS.getCode());
                taskInfoService.updateById(taskInfoEntity);
            }catch (Exception e) {
                throw new ServiceException("更新合同状态失败");
            }finally {
                //失败
                taskInfoEntity.setStatus(ApproveTaskStatusEnum.FAIL.getCode());
                taskInfoService.updateById(taskInfoEntity);
            }
        }
        //TODO 更新thirdTask
        thirdProcessManagementService.insert(jsonObject,thirdProcessEntity.getSourcePlatform());
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
            contractStampStatusParamsDTO.setContractStampStatus(map.get("contractStampStatus").toString());
            try {
                purchaseOrderFeign.updateContractStatusById(contractStampStatusParamsDTO);
                taskInfo.setBussinessCode(list.get(0).getCode());
                taskInfo.setBussinessId(Arrays.asList(list.get(0).getId()).toString());
                taskInfo.setStatus(ApproveTaskStatusEnum.SUCCESS.getCode());
            }catch (Exception e){
                throw new ServiceException("更新合同状态失败");
            }finally {
                taskInfo.setBussinessCode(list.get(0).getCode());
                taskInfo.setBussinessId(Arrays.asList(list.get(0).getId()).toString());
                taskInfo.setStatus(ApproveTaskStatusEnum.SUCCESS.getCode());
            }

        }
    }

    @Override
    public ApproveTaskInfoDTO.AddDTO buildApproveTaskInfo(CfgThirdProcessEntity thirdProcessEntity, List<ApproveTaskDetailDTO.AddDTO> addDTOS) {
        ApproveTaskInfoDTO.AddDTO addDTO = new ApproveTaskInfoDTO.AddDTO();
        addDTO.setDetailList(addDTOS);
        addDTO.setType(ApproveTaskTypeEnum.PULL.getCode());
        addDTO.setThirdDefinniationName(thirdProcessEntity.getName());
        addDTO.setBussinessKey(thirdProcessEntity.getBussinessKey());
        addDTO.setThirdApprovalCode(thirdProcessEntity.getCode());
        addDTO.setSourcePlatform(thirdProcessEntity.getSourcePlatform());
        return addDTO;
    }

}