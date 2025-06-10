package com.erp.server.workflow.handler;

/**
 * @description: 飞书获取审批实例后更新原单据状态
 * @author: hcg
 * @date: 2025/5/29 00:54
 */

import cn.hutool.json.JSONObject;
import com.erp.model.workflow.dto.ApproveTaskDetailDTO;
import com.erp.model.workflow.dto.ApproveTaskInfoDTO;
import com.erp.model.workflow.entity.ApproveTaskInfoEntity;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.model.workflow.entity.CfgThirdProcessEntity;
import com.erp.model.workflow.enums.ApproveTaskTypeEnum;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;

import java.util.List;
import java.util.Map;

/**
 * @Author: hcg
 * @CreateTime: 2025-05-29
 * @Description:
 * @Version: 1.0
 */

public interface CreateBillHandler {
    default boolean isMatch(String event) {
        return getEvent().name().equals(event);
    }

    CfgQueryOptionBussinessKeyEnum getEvent();


    void createBill(JSONObject jsonObject, CfgThirdProcessEntity thirdProcessEntity, List<CfgProcessFieldMapEntity> fieldMapList, List<CfgProcessValueMapEntity> valueMapList);

    void afreshGenerate(Map<String,Object> map, CfgThirdProcessEntity thirdProcessEntity, ApproveTaskInfoEntity entity);

    ApproveTaskInfoDTO.AddDTO buildApproveTaskInfo(CfgThirdProcessEntity thirdProcessEntity, List<ApproveTaskDetailDTO.AddDTO> addDTOS);
}
