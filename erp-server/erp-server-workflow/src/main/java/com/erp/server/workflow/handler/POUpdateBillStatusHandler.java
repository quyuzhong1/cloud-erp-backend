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
import com.common.business.wrapper.FeignQuery;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.workflow.dto.ThirdProcessManagementDTO;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.model.workflow.entity.CfgThirdProcessEntity;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.model.workflow.enums.DictBasicEnum;
import com.erp.model.workflow.enums.FSApprovalStatusEnum;
import com.erp.rpc.scm.feign.PurchaseOrderFeign;
import com.erp.server.workflow.context.ProcessFormFactory;
import com.erp.server.workflow.service.CfgProcessFieldMapService;
import com.erp.server.workflow.service.CfgProcessValueMapService;
import com.erp.server.workflow.service.ThirdProcessManagementService;
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
public class POUpdateBillStatusHandler implements UpdateBillStatusHandler {
    @Resource
    PurchaseOrderFeign purchaseOrderFeign;

    @Resource
    ThirdProcessManagementService thirdProcessManagementService;

    @Resource
    ProcessFormFactory processFormFactory;

    @Override
    public boolean isMatch(String event) {
        return UpdateBillStatusHandler.super.isMatch(event);
    }

    @Override
    public CfgQueryOptionBussinessKeyEnum getEvent() {
        return CfgQueryOptionBussinessKeyEnum.PURCHASEORDER;
    }

    @Override
    @GlobalTransactional
    public void operateType(JSONObject jsonObject, CfgThirdProcessEntity thirdProcessEntity, List<CfgProcessFieldMapEntity> fieldMapList, List<CfgProcessValueMapEntity> valueMapList) {
        DictBasicEnum dictBasicEnum = DictBasicEnum.getByCode(thirdProcessEntity.getOperateType());
        ProcessFormHandler constructBillHandler = processFormFactory.getConstructBillHandler(thirdProcessEntity.getSourcePlatform());

        if (dictBasicEnum != null && DictBasicEnum.UPDATEFIELDORSTATUS.equals(dictBasicEnum)) {
            //找到集合中unique为true的元素
            CfgProcessFieldMapEntity uniqueField = fieldMapList.stream().filter(req -> req.getIsUnique()).findFirst().orElse(null);

            //更新单
            PurchaseOrderEntity purchaseOrderEntity = new PurchaseOrderEntity();
            purchaseOrderEntity.setApproveStatus(jsonObject.getStr("status"));

//            Map<String, Object> map = constructBillHandler.constructBill(jsonObject.getJSONArray("form"), fieldMapList, valueMapList);
//            String uniqueValue = (String) map.get(uniqueField.getSysField());
//            purchaseOrderFeign.updatePurchaseOrderByUnique(uniqueField.getSysField(),  uniqueValue,purchaseOrderEntity);
        }
        if (dictBasicEnum != null && DictBasicEnum.CREATE.equals(dictBasicEnum)) {
//            Map<String, Object> map = constructBillHandler.constructBill(jsonObject.getJSONArray("form"), fieldMapList, valueMapList);
            //创建 TODO 转entiy
            PurchaseOrderEntity purchaseOrderEntity = new PurchaseOrderEntity();
            purchaseOrderFeign.save(purchaseOrderEntity);
        }
        if (dictBasicEnum != null && DictBasicEnum.CREATEANDUPDATE.equals(dictBasicEnum)) {
            //更新单据状态为待审核
            if (FSApprovalStatusEnum.APPROVED.getCode().equals(jsonObject.getStr("status"))) {
//                Map<String, Object> map = constructBillHandler.constructBill(jsonObject.getJSONArray("form"), fieldMapList, valueMapList);
                //创建 TODO 转entiy
                PurchaseOrderEntity purchaseOrderEntity = new PurchaseOrderEntity();
                purchaseOrderFeign.saveAndUpdate(purchaseOrderEntity);
            }
        }
        //TODO
        thirdProcessManagementService.insert(jsonObject);
    }
}
