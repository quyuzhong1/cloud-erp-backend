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
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.model.scm.entity.PurchasePriceEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.model.workflow.entity.CfgThirdProcessEntity;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.model.workflow.enums.DictBasicEnum;
import com.erp.model.workflow.enums.FSApprovalStatusEnum;
import com.erp.rpc.scm.feign.PurchasePriceFeign;
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
 * 采购价目表
 *@Author: hcg
 *@CreateTime: 2025-05-31
 *@Description:
 *@Version: 1.0
 */
@Component
@Slf4j
public class PurchasePriceUpdateBillStatusHandler implements UpdateBillStatusHandler {

    @Resource
    ThirdProcessManagementService thirdProcessManagementService;

    @Resource
    CfgProcessFieldMapService cfgProcessFieldMapService;

    @Resource
    ProcessFormFactory processFormFactory;

    @Resource
    CfgProcessValueMapService cfgProcessValueMapService;

    @Resource
    PurchasePriceFeign purchasePriceFeign;

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
        List<PurchasePriceEntity> purchasePriceEntityList = purchasePriceFeign.listByCodes(Arrays.asList(billId));
        if (CollUtil.isEmpty(purchasePriceEntityList)) {
            throw new ServiceException("未找到采购价目信息，请检查三方生成查询的单据编号是否正确，{}", billId);
        }
        PurchasePriceEntity purchasePriceEntity = purchasePriceEntityList.get(0);
        //更新审核状态
        if (jsonObject.getStr("status").equalsIgnoreCase("pending")) {
            //更新单据状态和审核人的信息
            purchasePriceEntity.setApproveStatus(ApproveStatusEnum.APPROVE_ING);
            JSONArray taskList = jsonObject.getJSONArray("taskList");
            //遍历taskList，找到元素status为pending，将该元素的userId设置到purchaseOrderEntity中
            for (Object task : taskList) {
                JSONObject taskJson = (JSONObject) task;
                if (taskJson.getStr("status").equalsIgnoreCase("pending")) {
                    purchasePriceEntity.setApproveUserId(taskJson.getStr("userId"));
                    break;
                }
            }
        } else if (jsonObject.getStr("status").equalsIgnoreCase("rejected")) {
            purchasePriceEntity.setApproveStatus(ApproveStatusEnum.REJECT);
            thirdProcessManagementService.insert(jsonObject);
        } else if (jsonObject.getStr("status").equalsIgnoreCase("approved")) {
            purchasePriceEntity.setApproveStatus(ApproveStatusEnum.APPROVE);
            thirdProcessManagementService.insert(jsonObject);
        } else if (jsonObject.getStr("status").equalsIgnoreCase("canceled")||jsonObject.getStr("status").equalsIgnoreCase("deleted")|| (jsonObject.getStr("status").equalsIgnoreCase("OVERTIME_RECOVER") || jsonObject.getStr("status").equalsIgnoreCase("OVERTIME_CLOSE"))) {
            //更新单据状态为待提交
            purchasePriceEntity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
        }
        PurchasePriceDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO = new PurchasePriceDTO.UpdateApprovalStatusDTO(purchasePriceEntity, purchasePriceEntity.getApproveStatus());
        purchasePriceFeign.updateApproveStatus(updateApprovalStatusDTO);
    }

    @Override
    public void operateType(JSONObject jsonObject, CfgThirdProcessEntity thirdProcessEntity) {
    }
}
