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
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.dto.PurchaseOrderSupplierDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.workflow.dto.ApproveTaskDetailDTO;
import com.erp.model.workflow.dto.ApproveTaskInfoDTO;
import com.erp.model.workflow.entity.*;
import com.erp.model.workflow.enums.*;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.server.workflow.context.ProcessFormFactory;
import com.erp.server.workflow.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Override
    public boolean isMatch(String event) {
        return CreateBillHandler.super.isMatch(event);
    }

    @Override
    public CfgQueryOptionBussinessKeyEnum getEvent() {
        return CfgQueryOptionBussinessKeyEnum.SUPPLIER;
    }

    @Override
    public void createBill(JSONObject jsonObject, CfgThirdProcessEntity thirdProcessEntity, List<CfgProcessFieldMapEntity> fieldMapList, List<CfgProcessValueMapEntity> valueMapList) {
        DictBasicEnum dictBasicEnum = DictBasicEnum.getByCode(thirdProcessEntity.getOperateType());
        ProcessFormHandler constructBillHandler = processFormFactory.getConstructBillHandler(thirdProcessEntity.getSourcePlatform());

        if (dictBasicEnum != null && DictBasicEnum.UPDATEFIELDORSTATUS.equals(dictBasicEnum)) {
            //找到集合中unique为true的元素

        }
        if (dictBasicEnum != null && DictBasicEnum.CREATE.equals(dictBasicEnum)) {
            Map<String, Object> map = constructBillHandler.constructBill(jsonObject.getJSONArray("form"), fieldMapList, valueMapList);
            ObjectMapper objectMapper = new ObjectMapper();
            SupplierDTO.InsertDTO addDTO = objectMapper.convertValue(map, SupplierDTO.InsertDTO.class);
            addDTO.setApprovalStatus(ApproveStatusEnum.APPROVE_ING);
            try {
                List<ApproveTaskDetailDTO.AddDTO> addDTOS = constructBillHandler.generatePullDetailDTO(jsonObject.getJSONArray("form"), map, fieldMapList);
                ApproveTaskInfoDTO.AddDTO taskInfo = buildApproveTaskInfo(thirdProcessEntity, addDTOS);
                taskInfo.setThirdInstanceId(jsonObject.getStr("instance_code"));
                BaseResultDTO.AddDTO add = approveTaskInfoService.add(taskInfo);
                //添加
                String id = supplierFeign.add(addDTO);

                List<SupplierEntity> list = FeignQuery.create(SupplierEntity.class).eq(SupplierEntity::getId, id).list();
                taskInfo.setBussinessCode(list.get(0).getCode());
                taskInfo.setBussinessId(id);
                ApproveTaskInfoEntity taskInfoEntity = BeanUtil.copyProperties(taskInfo, ApproveTaskInfoEntity.class);
                taskInfoEntity.setStatus(ApproveTaskStatusEnum.SUCCESS.getCode());
                taskInfoEntity.setId(add.getId());
                approveTaskInfoService.updateById(taskInfoEntity);
            }catch (Exception e){
                throw new RuntimeException("创建供应商失败错误信息：",e);
            }
        }
        if (dictBasicEnum != null && DictBasicEnum.CREATEANDUPDATE.equals(dictBasicEnum)) {
            //更新单据状态为待审核
            if (FSApprovalStatusEnum.APPROVED.getCode().equals(jsonObject.getStr("status"))) {
                Map<String, Object> map = constructBillHandler.constructBill(jsonObject.getJSONArray("form"), fieldMapList, valueMapList);
                ObjectMapper objectMapper = new ObjectMapper();
                SupplierDTO.InsertDTO addDTO = objectMapper.convertValue(map, SupplierDTO.InsertDTO.class);
                addDTO.setApprovalStatus(ApproveStatusEnum.APPROVE);
                supplierFeign.add(addDTO);
            }
        }
        //TODO
        thirdProcessManagementService.insert(jsonObject,thirdProcessEntity.getSourcePlatform());
    }

    @Override
    public void afreshGenerate(Map<String, Object> map, CfgThirdProcessEntity thirdProcessEntity, ApproveTaskInfoEntity taskInfo) {
        ThirdProcessInstanceEntity one = thirdProcessInstanceService.getOne(new LambdaQueryWrapper<ThirdProcessInstanceEntity>().eq(ThirdProcessInstanceEntity::getInstanceCode, taskInfo.getThirdInstanceId()));
        DictBasicEnum dictBasicEnum = DictBasicEnum.getByCode(thirdProcessEntity.getOperateType());

        if (dictBasicEnum != null && DictBasicEnum.UPDATEFIELDORSTATUS.equals(dictBasicEnum)) {
            //找到集合中unique为true的元素

        }
        if (dictBasicEnum != null && DictBasicEnum.CREATE.equals(dictBasicEnum)) {
            ObjectMapper objectMapper = new ObjectMapper();
            SupplierDTO.InsertDTO addDTO = objectMapper.convertValue(map, SupplierDTO.InsertDTO.class);
            addDTO.setApprovalStatus(ApproveStatusEnum.APPROVE_ING);
            try {
                String id = supplierFeign.add(addDTO);
                List<SupplierEntity> list = FeignQuery.create(SupplierEntity.class).eq(SupplierEntity::getId, id).list();
                taskInfo.setBussinessCode(list.get(0).getCode());
                taskInfo.setBussinessId(id);
            }catch (Exception e){
                throw new RuntimeException("创建供应商失败错误信息：",e);
            }
        }
        if (dictBasicEnum != null && DictBasicEnum.CREATEANDUPDATE.equals(dictBasicEnum)) {
            //更新单据状态为待审核
            if (FSApprovalStatusEnum.APPROVED.getCode().equals(one.getStatus())) {
                ObjectMapper objectMapper = new ObjectMapper();
                SupplierDTO.InsertDTO addDTO = objectMapper.convertValue(map, SupplierDTO.InsertDTO.class);
                addDTO.setApprovalStatus(ApproveStatusEnum.APPROVE);
                String id = supplierFeign.add(addDTO);
                List<SupplierEntity> list = FeignQuery.create(SupplierEntity.class).eq(SupplierEntity::getId, id).list();
                taskInfo.setBussinessCode(list.get(0).getCode());
                taskInfo.setBussinessId(id);
            }
        }
        taskInfo.setStatus(ApproveTaskStatusEnum.SUCCESS.getCode());
        approveTaskInfoService.updateById(taskInfo);

    }

    @Override
    public ApproveTaskInfoDTO.AddDTO buildApproveTaskInfo(CfgThirdProcessEntity thirdProcessEntity,List<ApproveTaskDetailDTO.AddDTO> addDTOS){
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
