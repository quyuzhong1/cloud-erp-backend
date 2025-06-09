package com.erp.server.workflow.handler;

/**
 * @description:
 * @author: hcg
 * @date: 2025/5/29 00:56
 */

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.wrapper.FeignBuilder;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.workflow.dto.ThirdProcessManagementDTO;
import com.erp.model.workflow.entity.ApproveTaskInfoEntity;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.model.workflow.entity.CfgThirdProcessEntity;
import com.erp.model.workflow.enums.ApproveTaskStatusEnum;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.model.workflow.enums.DictBasicEnum;
import com.erp.model.workflow.enums.FSApprovalStatusEnum;
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
                throw new ServiceException("三方审批生成-采购订单合同状态更新-找到多条系统单据或为找到系统单据，请检查单据，单据唯一键{},单据唯一键值{}",  uniqueField.getSysField(), uniqueValue);
            }
            //更新单
            PurchaseOrderDTO.ContractStampStatusParamsDTO contractStampStatusParamsDTO = new PurchaseOrderDTO.ContractStampStatusParamsDTO();
            contractStampStatusParamsDTO.setIds(Arrays.asList(list.get(0).getId()));
            contractStampStatusParamsDTO.setContractStampStatus(map.get("contractStampStatus").toString());
            ApproveTaskInfoEntity one = taskInfoService.getOne(new LambdaQueryWrapper<ApproveTaskInfoEntity>().eq(ApproveTaskInfoEntity::getThirdInstanceId, jsonObject.getStr("thirdInstanceId")).eq(ApproveTaskInfoEntity::getStatus, ApproveTaskStatusEnum.SUCCESS));
            try {
                purchaseOrderFeign.updateContractStatusById(contractStampStatusParamsDTO);
                one.setStatus(ApproveTaskStatusEnum.SUCCESS.getCode());
            }catch (Exception e){
                one.setStatus(ApproveTaskStatusEnum.FAIL.getCode());
                throw new ServiceException("更新合同状态失败");
            }finally {
                taskInfoService.updateById(one);
                return;
            }
        }
        //TODO 更新thirdTask
        thirdProcessManagementService.insert(jsonObject);
    }

    
}