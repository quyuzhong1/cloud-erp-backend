package com.erp.server.workflow.handler;

/**
 * @description:
 * @author: hcg
 * @date: 2025/5/31 02:22
 */

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.PilotApplicationDTO;
import com.erp.model.plm.entity.PilotApplicationEntity;
import com.erp.model.wms.dto.TransferInfoDTO;
import com.erp.model.wms.entity.TransferInfoEntity;
import com.erp.model.workflow.entity.CfgThirdProcessEntity;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;
import com.erp.rpc.plm.feign.PilotApplicationFeign;
import com.erp.rpc.wms.feign.TransferInfoFeign;
import com.erp.server.workflow.service.ThirdProcessManagementService;
import groovy.util.logging.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 试产量产单
 *@Author: hcg
 *@CreateTime: 2025-05-31
 *@Description:
 *@Version: 1.0
 */
@Component
@Slf4j
public class PilotApplicationUpdateBillStatusHandler implements UpdateBillStatusHandler {

    @Resource
    ThirdProcessManagementService thirdProcessManagementService;

    @Resource
    PilotApplicationFeign pilotApplicationFeign;

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
        List<PilotApplicationEntity> pilotApplicationEntityList = pilotApplicationFeign.listByCodes(Arrays.asList(billId));
        if (CollUtil.isEmpty(pilotApplicationEntityList)) {
            throw new ServiceException("未找到直接调拨单，请检查三方生成查询的单据编号是否正确，{}", billId);
        }
        PilotApplicationEntity entity = pilotApplicationEntityList.get(0);
        //更新审核状态
        if (jsonObject.getStr("status").equalsIgnoreCase("pending")) {
            //更新单据状态和审核人的信息
            entity.setApproveStatus(ApproveStatusEnum.APPROVE_ING);
            JSONArray taskList = jsonObject.getJSONArray("taskList");
            for (Object task : taskList) {
                JSONObject taskJson = (JSONObject) task;
                if (taskJson.getStr("status").equalsIgnoreCase("pending")) {
                    entity.setApproveUserId(taskJson.getStr("userId"));
                    break;
                }
            }
        } else if (jsonObject.getStr("status").equalsIgnoreCase("rejected")) {
            entity.setApproveStatus(ApproveStatusEnum.REJECT);
            thirdProcessManagementService.insert(jsonObject);
        } else if (jsonObject.getStr("status").equalsIgnoreCase("approved")) {
            entity.setApproveStatus(ApproveStatusEnum.APPROVE);
            thirdProcessManagementService.insert(jsonObject);
        } else if (jsonObject.getStr("status").equalsIgnoreCase("canceled")||jsonObject.getStr("status").equalsIgnoreCase("deleted")|| (jsonObject.getStr("status").equalsIgnoreCase("OVERTIME_RECOVER") || jsonObject.getStr("status").equalsIgnoreCase("OVERTIME_CLOSE"))) {
            //更新单据状态为待提交
            entity.setApproveStatus(ApproveStatusEnum.WAIT_SUBMIT);
        }
        PilotApplicationDTO.UpdateApprovalStatusDTO updateApprovalStatusDTO = new PilotApplicationDTO.UpdateApprovalStatusDTO(entity, entity.getApproveStatus());
        pilotApplicationFeign.updateApproveStatus(updateApprovalStatusDTO);
    }

    @Override
    public void operateType(JSONObject jsonObject, CfgThirdProcessEntity thirdProcessEntity) {
    }
}
