package com.erp.server.workflow.handler;

/**
 * @description:
 * @author: hcg
 * @date: 2025/5/20 09:59
 */

import cn.hutool.json.JSONArray;
import com.erp.model.workflow.dto.ApproveTaskDetailDTO;
import com.erp.model.workflow.dto.CfgProcessFieldMapDTO;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.model.workflow.enums.CfgProcessRuleTypeEnum;
import com.erp.model.workflow.enums.ProcessSourcePlatformEnum;

import java.util.List;
import java.util.Map;

/**
 * @Author: hcg
 * @CreateTime: 2025-05-20
 * @Description:
 * @Version: 1.0
 */
public interface ProcessFormHandler {
    /**
     * 构造流程表单结构
     */
    JSONArray assembleForm(JSONArray formArray, Map<String, Object> variablesMap,
                           List<CfgProcessFieldMapEntity> fieldMapList,
                           List<CfgProcessValueMapEntity> valueMapList);

    default boolean isMatch(String event) {
        return getEvent().name().equals(event);
    }

    CfgProcessRuleTypeEnum getEvent();


    /**
     * 解析表单结构
     */
    List<CfgProcessFieldMapDTO.ViewDTO> parseForm(String formString);

    /**
     * 解析表单选项值
     */
    Map<String, Map<String, String>> parseFormValue(String formString);

    /**
     * 构造单据
     * Map<String, Object> variablesMap
     *
     * JSONArray formArray
     */
    Map<String, Object> constructBill(JSONArray formArray, List<CfgProcessFieldMapEntity> fieldMapList, List<CfgProcessValueMapEntity> valueMapList,String sourceType);

    /**
     * 批量生成taskdetailDto push
     * @param formArray
     * @param fieldMapList
     * @param variablesMap
     * @return
     */
    List<ApproveTaskDetailDTO.AddDTO> generatePushDetailDTO(JSONArray formArray, List<CfgProcessFieldMapEntity> fieldMapList, Map<String, Object> variablesMap,Map<String,String> optionMap);

    /**
     * 批量生成taskdetailDto Pull
     * @param formArray
     * @param variablesMap
     * @param fieldMapList
     * @return
     */
    List<ApproveTaskDetailDTO.AddDTO> generatePullDetailDTO(
            JSONArray formArray,
            Map<String, Object> variablesMap,
            List<CfgProcessFieldMapEntity> fieldMapList);


    ProcessSourcePlatformEnum getEventType();
}
