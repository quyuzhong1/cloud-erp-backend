package com.erp.server.workflow.handler;

/**
 * @description: 飞书获取审批实例后更新原单据状态
 * @author: hcg
 * @date: 2025/5/29 00:54
 */

import cn.hutool.json.JSONObject;
import com.erp.model.workflow.entity.CfgThirdProcessEntity;
import com.erp.model.workflow.enums.CfgQueryOptionBussinessKeyEnum;

/**
 * @Author: hcg
 * @CreateTime: 2025-05-29
 * @Description:
 * @Version: 1.0
 */

public interface UpdateBillStatusHandler {
    default boolean isMatch(String event) {
        return getEvent().name().equals(event);
    }

    CfgQueryOptionBussinessKeyEnum getEvent();

    void updateBillStatus(JSONObject jsonObject,String billId) throws Exception;

    void operateType(JSONObject jsonObject, CfgThirdProcessEntity thirdProcessEntity);
}
