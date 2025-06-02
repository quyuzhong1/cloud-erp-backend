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
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.dto.PurchaseOrderSupplierDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.model.workflow.entity.CfgThirdProcessEntity;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.model.workflow.enums.DictBasicEnum;
import com.erp.model.workflow.enums.FSApprovalStatusEnum;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.server.workflow.context.ProcessFormFactory;
import com.erp.server.workflow.service.CfgProcessFieldMapService;
import com.erp.server.workflow.service.CfgProcessValueMapService;
import com.erp.server.workflow.service.ThirdProcessManagementService;
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
public class SupplierUpdateBillStatusHandler implements UpdateBillStatusHandler {

    @Resource
    ThirdProcessManagementService thirdProcessManagementService;

    @Resource
    CfgProcessFieldMapService cfgProcessFieldMapService;

    @Resource
    ProcessFormFactory processFormFactory;

    @Resource
    CfgProcessValueMapService cfgProcessValueMapService;

    @Resource
    SupplierFeign supplierFeign;

    @Override
    public boolean isMatch(String event) {
        return UpdateBillStatusHandler.super.isMatch(event);
    }

    @Override
    public CfgQueryOptionBussinessKeyEnum getEvent() {
        return CfgQueryOptionBussinessKeyEnum.SUPPLIER;
    }

    @Override
    public void updateBillStatus(JSONObject jsonObject, String billId) throws Exception {
        List<SupplierEntity> supplierEntityList = supplierFeign.listByCodes(Arrays.asList(billId));
        if (CollUtil.isEmpty(supplierEntityList)) {
            throw new ServiceException("未找到供应商信息，请检查三方生成查询的单据编号是否正确，{}", billId);
        }
        SupplierEntity supplierEntity = supplierEntityList.get(0);
        //更新审核状态
        if (jsonObject.getStr("status").equalsIgnoreCase("pending")) {
            //更新单据状态和审核人的信息
            supplierEntity.setApproveStatus(ApproveStatusEnum.APPROVE_ING);
            JSONArray taskList = jsonObject.getJSONArray("taskList");
            //遍历taskList，找到元素status为pending，将该元素的userId设置到purchaseOrderEntity中
            for (Object task : taskList) {
                JSONObject taskJson = (JSONObject) task;
                if (taskJson.getStr("status").equalsIgnoreCase("pending")) {
                    supplierEntity.setApproveUserId(taskJson.getStr("userId"));
                    break;
                }
            }
        } else if (jsonObject.getStr("status").equalsIgnoreCase("rejected")) {
            supplierEntity.setApproveStatus(ApproveStatusEnum.REJECT);
            thirdProcessManagementService.insert(jsonObject);
        } else if (jsonObject.getStr("status").equalsIgnoreCase("approved")) {
            supplierEntity.setApproveStatus(ApproveStatusEnum.APPROVE);
            thirdProcessManagementService.insert(jsonObject);
        } else if (jsonObject.getStr("status").equalsIgnoreCase("canceled")||jsonObject.getStr("status").equalsIgnoreCase("deleted")|| (jsonObject.getStr("status").equalsIgnoreCase("OVERTIME_RECOVER") || jsonObject.getStr("status").equalsIgnoreCase("OVERTIME_CLOSE"))) {
            //更新单据状态为待提交
            supplierEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
        }
        SupplierDTO.UpdateApproveStatusDTO updateApproveStatusDTO = new SupplierDTO.UpdateApproveStatusDTO(supplierEntity, supplierEntity.getApproveStatus());
        supplierFeign.updateApproveStatus(updateApproveStatusDTO);
    }

    @Override
    public void operateType(JSONObject jsonObject, CfgThirdProcessEntity thirdProcessEntity) {
        DictBasicEnum dictBasicEnum = DictBasicEnum.getByCode(thirdProcessEntity.getOperateType());
        ProcessFormHandler constructBillHandler = processFormFactory.getConstructBillHandler(thirdProcessEntity.getSourcePlatform());

        List<CfgProcessFieldMapEntity> fieldMapList = cfgProcessFieldMapService.list(new LambdaQueryWrapper<CfgProcessFieldMapEntity>().eq(CfgProcessFieldMapEntity::getCfgId, thirdProcessEntity.getId()).eq(CfgProcessFieldMapEntity::getIsDeleted, false));

        List<String> fieldIdList = fieldMapList.stream().map(e -> e.getId()).collect(Collectors.toList());
        List<CfgProcessValueMapEntity> valueMapList = cfgProcessValueMapService.list(new LambdaQueryWrapper<CfgProcessValueMapEntity>().in(CfgProcessValueMapEntity::getFieldMapId, fieldIdList).eq(CfgProcessValueMapEntity::getIsDeleted, false));

        if (dictBasicEnum != null && DictBasicEnum.UPDATEFIELDORSTATUS.equals(dictBasicEnum)) {
            //找到集合中unique为true的元素
            CfgProcessFieldMapEntity uniqueField = fieldMapList.stream().filter(req -> req.getIsUnique()).findFirst().orElse(null);
            //更新合同状态
            Map<String, Object> map = constructBillHandler.constructBill(jsonObject.getJSONArray("form"), fieldMapList, valueMapList);
        }
        if (dictBasicEnum != null && DictBasicEnum.CREATE.equals(dictBasicEnum)) {
            Map<String, Object> map = constructBillHandler.constructBill(jsonObject.getJSONArray("form"), fieldMapList, valueMapList);
            ObjectMapper objectMapper = new ObjectMapper();
            SupplierDTO.InsertDTO addDTO = objectMapper.convertValue(map, SupplierDTO.InsertDTO.class);
            addDTO.setApprovalStatus(ApproveStatusEnum.APPROVE_ING);
            supplierFeign.add(addDTO);
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
        thirdProcessManagementService.insert(jsonObject);
    }
}
