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
        return getEvent().getCode().equals(event);
    }

    CfgQueryOptionBussinessKeyEnum getEvent();

    /**
     * 根据三方审批生成的类型，更新单据状态 OR 生成单据
     * @param jsonObject
     * @param thirdProcessEntity
     * @param fieldMapList
     * @param valueMapList
     */
    void createBill(JSONObject jsonObject, CfgThirdProcessEntity thirdProcessEntity, List<CfgProcessFieldMapEntity> fieldMapList, List<CfgProcessValueMapEntity> valueMapList);

    /**
     * 三方生成查询，重新生成功能
     * @param map
     * @param thirdProcessEntity
     * @param entity
     */
    void afreshGenerate(Map<String,Object> map, CfgThirdProcessEntity thirdProcessEntity, ApproveTaskInfoEntity entity);

    /**
     * 构建taskInfo
     * @param jsonObject
     * @param addDTOS
     * @return
     */
    ApproveTaskInfoDTO.AddDTO buildApproveTaskInfo(JSONObject jsonObject, List<ApproveTaskDetailDTO.AddDTO> addDTOS,String bussinessKey);
}
