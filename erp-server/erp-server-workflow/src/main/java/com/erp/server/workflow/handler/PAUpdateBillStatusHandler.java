package com.erp.server.workflow.handler;

/**
 * @description:
 * @author: hcg
 * @date: 2025/5/31 02:22
 */

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.workflow.dto.ApproveTaskDetailDTO;
import com.erp.model.workflow.dto.ApproveTaskInfoDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.*;
import com.erp.rpc.scm.feign.PurchaseApplicationFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.server.workflow.context.ProcessFormFactory;
import com.erp.server.workflow.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 采购申请单
 *@Author: hcg
 *@CreateTime: 2025-05-31
 *@Description:
 *@Version: 1.0
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
    CfgProcessFieldMapService fieldMapService;

    @Resource
    ApproveTaskInfoService approveTaskInfoService;

    @Resource
    ThirdProcessInstanceService thirdProcessInstanceService;

    @Override
    public boolean isMatch(String event) {
        return CreateBillHandler.super.isMatch(event);
    }

    @Override
    public CfgQueryOptionBussinessKeyEnum getEvent() {
        return CfgQueryOptionBussinessKeyEnum.SUPPLIER;
    }

    @Override
    public void createBill(JSONObject jsonObject, CfgThirdProcessEntity thirdProcessEntity,List<CfgProcessFieldMapEntity> fieldMapList,List<CfgProcessValueMapEntity> valueMapList) {
        DictBasicEnum dictBasicEnum = DictBasicEnum.getByCode(thirdProcessEntity.getOperateType());
        ProcessFormHandler constructBillHandler = processFormFactory.getConstructBillHandler(thirdProcessEntity.getSourcePlatform());

        ObjectMapper mapper = new ObjectMapper();
        if (dictBasicEnum != null && DictBasicEnum.UPDATEFIELDORSTATUS.equals(dictBasicEnum)) {
            //找到集合中unique为true的元素
            CfgProcessFieldMapEntity uniqueField = fieldMapList.stream().filter(req -> req.getIsUnique()).findFirst().orElse(null);
            //更新合同状态
            Map<String, Object> map = constructBillHandler.constructBill(jsonObject.getJSONArray("form"), fieldMapList, valueMapList);
            PurchaseApplicationDTO.updatePADTO updateDTO = mapper.convertValue(map, PurchaseApplicationDTO.updatePADTO.class);
//            purchaseApplicationFeign.updatePA(updateDTO);
        }
        if (dictBasicEnum != null && DictBasicEnum.CREATE.equals(dictBasicEnum)) {
            Map<String, Object> map = constructBillHandler.constructBill(jsonObject.getJSONArray("form"), fieldMapList, valueMapList);
            List<ApproveTaskDetailDTO.AddDTO> addDTOS = constructBillHandler.generatePullDetailDTO(jsonObject.getJSONArray("form"), map, fieldMapList);
            PurchaseApplicationDTO.AddDTO addDTO = mapper.convertValue(map, PurchaseApplicationDTO.AddDTO.class);
            BatchResultDTO batchResultDTO = purchaseApplicationFeign.add(addDTO);


            ApproveTaskInfoDTO.AddDTO taskInfo = buildApproveTaskInfo(thirdProcessEntity, addDTOS);
            taskInfo.setThirdInstanceId(jsonObject.getStr("instance_code"));
            BaseResultDTO.AddDTO add = approveTaskInfoService.add(taskInfo);

            taskInfo.setBussinessCode(batchResultDTO.getCode());
            taskInfo.setBussinessId(batchResultDTO.getId());
            ApproveTaskInfoEntity taskInfoEntity = BeanUtil.copyProperties(taskInfo, ApproveTaskInfoEntity.class);
            taskInfoEntity.setStatus(ApproveTaskStatusEnum.SUCCESS.getCode());
            taskInfoEntity.setId(add.getId());
            approveTaskInfoService.updateById(taskInfoEntity);
        }
        if (dictBasicEnum != null && DictBasicEnum.CREATEANDUPDATE.equals(dictBasicEnum)) {
            //更新单据状态为待审核
            if (FSApprovalStatusEnum.APPROVED.getCode().equals(jsonObject.getStr("status"))) {
                Map<String, Object> map = constructBillHandler.constructBill(jsonObject.getJSONArray("form"), fieldMapList, valueMapList);
                List<ApproveTaskDetailDTO.AddDTO> addDTOS = constructBillHandler.generatePullDetailDTO(jsonObject.getJSONArray("form"), map, fieldMapList);

                ObjectMapper objectMapper = new ObjectMapper();
                PurchaseApplicationDTO.AddPADTO addDTO = objectMapper.convertValue(map, PurchaseApplicationDTO.AddPADTO.class);
                addDTO.setApprovalStatus(ApproveStatusEnum.APPROVE.getCode());

                ApproveTaskInfoDTO.AddDTO taskInfo = buildApproveTaskInfo(thirdProcessEntity, addDTOS);
                taskInfo.setThirdInstanceId(jsonObject.getStr("instance_code"));
                BaseResultDTO.AddDTO add = approveTaskInfoService.add(taskInfo);
                BatchResultDTO batchResultDTO = purchaseApplicationFeign.add(addDTO);

                taskInfo.setBussinessCode(batchResultDTO.getCode());
                taskInfo.setBussinessId(batchResultDTO.getId());
                ApproveTaskInfoEntity taskInfoEntity = BeanUtil.copyProperties(taskInfo, ApproveTaskInfoEntity.class);
                taskInfoEntity.setStatus(ApproveTaskStatusEnum.SUCCESS.getCode());
                taskInfoEntity.setId(add.getId());
                approveTaskInfoService.updateById(taskInfoEntity);
            }
        }

        thirdProcessManagementService.addOrUpdate(jsonObject,thirdProcessEntity.getSourcePlatform());
    }

    @Override
    public void afreshGenerate(Map<String, Object> map, CfgThirdProcessEntity thirdProcessEntity, ApproveTaskInfoEntity taskInfo) {
        ThirdProcessInstanceEntity one = thirdProcessInstanceService.getOne(new LambdaQueryWrapper<ThirdProcessInstanceEntity>().eq(ThirdProcessInstanceEntity::getInstanceCode, taskInfo.getThirdInstanceId()));
        DictBasicEnum dictBasicEnum = DictBasicEnum.getByCode(thirdProcessEntity.getOperateType());

        ObjectMapper mapper = new ObjectMapper();
        if (dictBasicEnum != null && DictBasicEnum.UPDATEFIELDORSTATUS.equals(dictBasicEnum)) {
            List<CfgProcessFieldMapEntity> fieldMapList = fieldMapService.list(new LambdaQueryWrapper<CfgProcessFieldMapEntity>().eq(CfgProcessFieldMapEntity::getCfgId, thirdProcessEntity.getId()).eq(CfgProcessFieldMapEntity::getIsDeleted, false));
            //找到集合中unique为true的元素
            CfgProcessFieldMapEntity uniqueField = fieldMapList.stream().filter(req -> req.getIsUnique()).findFirst().orElse(null);
            //更新合同状态
            PurchaseApplicationDTO.updatePADTO updateDTO = mapper.convertValue(map, PurchaseApplicationDTO.updatePADTO.class);
            purchaseApplicationFeign.updatePA(updateDTO);
            taskInfo.setStatus(ApproveTaskStatusEnum.SUCCESS.getCode());
        }
        if (dictBasicEnum != null && DictBasicEnum.CREATE.equals(dictBasicEnum)) {
            PurchaseApplicationDTO.AddDTO addDTO = mapper.convertValue(map, PurchaseApplicationDTO.AddDTO.class);
            BatchResultDTO batchResultDTO = purchaseApplicationFeign.add(addDTO);
            taskInfo.setBussinessCode(batchResultDTO.getCode());
            taskInfo.setBussinessId(batchResultDTO.getId());
            taskInfo.setStatus(ApproveTaskStatusEnum.SUCCESS.getCode());
        }
        if (dictBasicEnum != null && DictBasicEnum.CREATEANDUPDATE.equals(dictBasicEnum)) {
            //更新单据状态为待审核
            if (FSApprovalStatusEnum.APPROVED.getCode().equals(taskInfo.getStatus())) {
                ObjectMapper objectMapper = new ObjectMapper();
                PurchaseApplicationDTO.AddPADTO addDTO = objectMapper.convertValue(map, PurchaseApplicationDTO.AddPADTO.class);
                addDTO.setApprovalStatus(ApproveStatusEnum.APPROVE.getCode());
                BatchResultDTO batchResultDTO = purchaseApplicationFeign.add(addDTO);
                taskInfo.setStatus(ApproveTaskStatusEnum.SUCCESS.getCode());
                taskInfo.setBussinessCode((batchResultDTO.getCode()));
                taskInfo.setBussinessId(batchResultDTO.getId());
            }
        }
        approveTaskInfoService.updateById(taskInfo);
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
