package com.erp.server.workflow.handler;

/**
 * @description:
 * @author: hcg
 * @date: 2025/5/31 02:22
 */

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.PurchaseApplicationDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.PurchaseApplicationEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.model.workflow.entity.CfgThirdProcessEntity;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.model.workflow.enums.DictBasicEnum;
import com.erp.model.workflow.enums.FSApprovalStatusEnum;
import com.erp.rpc.scm.feign.PurchaseApplicationFeign;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.server.workflow.context.ProcessFormFactory;
import com.erp.server.workflow.service.CfgProcessFieldMapService;
import com.erp.server.workflow.service.CfgProcessValueMapService;
import com.erp.server.workflow.service.ThirdProcessManagementService;
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
public class PAUpdateBillStatusHandler implements UpdateBillStatusHandler {

    @Resource
    ThirdProcessManagementService thirdProcessManagementService;

    @Resource
    CfgProcessFieldMapService cfgProcessFieldMapService;

    @Resource
    ProcessFormFactory processFormFactory;

    @Resource
    CfgProcessValueMapService cfgProcessValueMapService;

    @Resource
    PurchaseApplicationFeign purchaseApplicationFeign;

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
    }

    @Override
    public void operateType(JSONObject jsonObject, CfgThirdProcessEntity thirdProcessEntity) {
        DictBasicEnum dictBasicEnum = DictBasicEnum.getByCode(thirdProcessEntity.getOperateType());
        ProcessFormHandler constructBillHandler = processFormFactory.getConstructBillHandler(thirdProcessEntity.getSourcePlatform());

        List<CfgProcessFieldMapEntity> fieldMapList = cfgProcessFieldMapService.list(new LambdaQueryWrapper<CfgProcessFieldMapEntity>().eq(CfgProcessFieldMapEntity::getCfgId, thirdProcessEntity.getId()).eq(CfgProcessFieldMapEntity::getIsDeleted, false));

        List<String> fieldIdList = fieldMapList.stream().map(e -> e.getId()).collect(Collectors.toList());
        List<CfgProcessValueMapEntity> valueMapList = cfgProcessValueMapService.list(new LambdaQueryWrapper<CfgProcessValueMapEntity>().in(CfgProcessValueMapEntity::getFieldMapId, fieldIdList).eq(CfgProcessValueMapEntity::getIsDeleted, false));
        ObjectMapper mapper = new ObjectMapper();
        if (dictBasicEnum != null && DictBasicEnum.UPDATEFIELDORSTATUS.equals(dictBasicEnum)) {
            //找到集合中unique为true的元素
            CfgProcessFieldMapEntity uniqueField = fieldMapList.stream().filter(req -> req.getIsUnique()).findFirst().orElse(null);
            //更新合同状态
            Map<String, Object> map = constructBillHandler.constructBill(jsonObject.getJSONArray("form"), fieldMapList, valueMapList);
            PurchaseApplicationDTO.updatePADTO updateDTO = mapper.convertValue(map, PurchaseApplicationDTO.updatePADTO.class);
            purchaseApplicationFeign.updatePA(updateDTO);
        }
        if (dictBasicEnum != null && DictBasicEnum.CREATE.equals(dictBasicEnum)) {
            Map<String, Object> map = constructBillHandler.constructBill(jsonObject.getJSONArray("form"), fieldMapList, valueMapList);
            PurchaseApplicationDTO.AddDTO addDTO = mapper.convertValue(map, PurchaseApplicationDTO.AddDTO.class);
            purchaseApplicationFeign.add(addDTO);
        }
        if (dictBasicEnum != null && DictBasicEnum.CREATEANDUPDATE.equals(dictBasicEnum)) {

            //更新单据状态为待审核
            if (FSApprovalStatusEnum.APPROVED.getCode().equals(jsonObject.getStr("status"))) {
                Map<String, Object> map = constructBillHandler.constructBill(jsonObject.getJSONArray("form"), fieldMapList, valueMapList);
                ObjectMapper objectMapper = new ObjectMapper();
                PurchaseApplicationDTO.AddPADTO addDTO = objectMapper.convertValue(map, PurchaseApplicationDTO.AddPADTO.class);
                addDTO.setApprovalStatus(ApproveStatusEnum.APPROVE.getCode());
                purchaseApplicationFeign.add(addDTO);
            }
        }
        //TODO
        thirdProcessManagementService.insert(jsonObject);
    }
}
