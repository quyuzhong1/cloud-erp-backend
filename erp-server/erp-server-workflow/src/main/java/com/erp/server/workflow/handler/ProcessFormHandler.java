package com.erp.server.workflow.handler;

/**
 * @description:
 * @author: hcg
 * @date: 2025/5/20 09:59
 */

import cn.hutool.json.JSONArray;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.ProcessFormEvent;
import com.erp.model.workflow.dto.CfgProcessDTO;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;

import java.util.List;
import java.util.Map;

/**
 *@Author: hcg
 *@CreateTime: 2025-05-20
 *@Description:
 *@Version: 1.0
 */
public interface ProcessFormHandler {
    /**
     * 构造流程表单结构
     */
    JSONArray assemble(JSONArray formArray, Map<String,Object> variablesMap,List<CfgProcessFieldMapEntity> fieldMapList, List<CfgProcessValueMapEntity> valueMapList);

    default boolean isMatch(String event) {
        return getEvent().name().equals(event);
    }

    ProcessFormEvent getEvent();

}
