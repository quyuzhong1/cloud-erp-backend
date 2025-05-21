package com.erp.server.workflow.handler;

/**
 * @description: 飞书解析form类
 * @author: hcg
 * @date: 2025/5/20 12:04
 */

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.common.business.enums.ProcessFormEvent;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.model.workflow.enums.FsRequestBodyAttributesEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.ProcessFormEvent.FS_PROCESS_FORM;

/**
 * @Author: hcg
 * @CreateTime: 2025-05-20
 * @Description:
 * @Version: 1.0
 */
@Component
@Slf4j
public class FsProcessFormHandler implements ProcessFormHandler {
    @Override
    public JSONArray assemble(JSONArray formArray, Map<String, Object> variablesMap,
                              List<CfgProcessFieldMapEntity> fieldMapList,
                              List<CfgProcessValueMapEntity> valueMapList) {

        // Map<thirdFieldId,FieldMapId>
        Map<String, String> tidToIdMap = fieldMapList.stream().collect(Collectors.toMap(
                CfgProcessFieldMapEntity::getThirdFieldId,
                CfgProcessFieldMapEntity::getId
        ));

        // Map<FieldMapId,List<valueMap>>
        Map<String, List<CfgProcessValueMapEntity>> valueMapListMap = valueMapList.stream().collect(Collectors.groupingBy(
                CfgProcessValueMapEntity::getFieldMapId
        ));

        // Map<thirdFieldId,CfgProcessFieldMapEntity>
        Map<String, CfgProcessFieldMapEntity> fieldMapByThirdId = fieldMapList.stream().collect(Collectors.toMap(
                CfgProcessFieldMapEntity::getThirdFieldId,
                Function.identity()
        ));

        // 明细数据（变量名 detailList）
        List<Map<String, Object>> detailList = (List<Map<String, Object>>) variablesMap.get("detailList");

        // 递归处理表单
        processFormArray(formArray, variablesMap, fieldMapByThirdId, tidToIdMap, valueMapListMap, detailList);

        return formArray;
    }

    private void processFormArray(JSONArray formArray, Map<String, Object> variablesMap,
                                  Map<String, CfgProcessFieldMapEntity> fieldMapByThirdId,
                                  Map<String, String> tidToIdMap,
                                  Map<String, List<CfgProcessValueMapEntity>> valueMapListMap,
                                  List<Map<String, Object>> detailList) {

        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formField = formArray.getJSONObject(i);
            String type = formField.getStr("type");

            // 如果是 FieldList 类型，处理明细表
            if ("fieldList".equals(type)) {
                processDetailTable(formField, fieldMapByThirdId, tidToIdMap, valueMapListMap, detailList);
            } else {
                // 处理普通字段
                processSimpleField(formField, variablesMap, fieldMapByThirdId, tidToIdMap, valueMapListMap);
            }

            // 只保留 id、type、value 字段
            retainOnlyIdTypeValue(formField);
        }
    }

    private void processDetailTable(JSONObject formField, Map<String, CfgProcessFieldMapEntity> fieldMapByThirdId,
                                    Map<String, String> tidToIdMap, Map<String, List<CfgProcessValueMapEntity>> valueMapListMap,
                                    List<Map<String, Object>> detailList) {

        JSONArray children = formField.getJSONArray("children");
        if (children == null) {
            return;
        }

        JSONArray detailValue = new JSONArray();
        if (detailList != null) {
            for (Map<String, Object> detailRow : detailList) {
                JSONArray row = new JSONArray();
                for (Object childObj : children) {
                    JSONObject child = (JSONObject) childObj;
                    String childId = child.getStr("id");
                    String childType = child.getStr("type");

                    CfgProcessFieldMapEntity fieldMap = fieldMapByThirdId.get(childId);
                    String childSysField = fieldMap != null ? fieldMap.getSysField() : null;
                    String childDefault = fieldMap != null ? fieldMap.getDefaultValue() : "";

                    Object childValue = (childSysField == null || detailRow.get(childSysField) == null) ?
                            childDefault : detailRow.get(childSysField);

                    // 执行值映射处理
                    childValue = mapFieldValue(childId, childType, childValue, childDefault, tidToIdMap, valueMapListMap);

                    JSONObject detailItem = new JSONObject();
                    detailItem.set("id", childId);
                    detailItem.set("type", childType);
                    detailItem.set("value", childValue);
                    row.add(detailItem);
                }
                detailValue.add(row);
            }
        }

        formField.set("value", detailValue);
        formField.remove("children"); // 移除 children 字段
    }

    private void processSimpleField(JSONObject formField, Map<String, Object> variablesMap,
                                    Map<String, CfgProcessFieldMapEntity> fieldMapByThirdId,
                                    Map<String, String> tidToIdMap, Map<String, List<CfgProcessValueMapEntity>> valueMapListMap) {

        String thirdFieldId = formField.getStr("id");
        String type = formField.getStr("type");

        CfgProcessFieldMapEntity entity = fieldMapByThirdId.get(thirdFieldId);
        if (entity == null) {
            return;
        }

        String sysField = entity.getSysField();
        if (sysField == ""){
            //TODO:无字段对应，默认值，就不进行下面的映射了，用defalut_value字段
        }
        String defaultValue = entity.getDefaultValue();
        Object rawValue = variablesMap.get(sysField);

        // 执行值映射处理
        Object finalValue = mapFieldValue(thirdFieldId, type, rawValue, defaultValue, tidToIdMap, valueMapListMap);
        formField.set("value", finalValue);
    }

    private Object mapFieldValue(String fieldId, String fieldType, Object rawValue, String defaultValue,
                                 Map<String, String> tidToIdMap, Map<String, List<CfgProcessValueMapEntity>> valueMapListMap) {
        // 仅针对 radioV2 和 checkboxV2 类型需要值映射
        String fieldMapId = tidToIdMap.get(fieldId);
        List<CfgProcessValueMapEntity> valueMappings = valueMapListMap.get(fieldMapId);
        //取value中的default_value
        if (rawValue == null || StrUtil.isBlank(rawValue.toString())) {
            String mappedDefault = null;
            if (valueMappings != null) {
                for (CfgProcessValueMapEntity vm : valueMappings) {
                    if ("defalut".equals(vm.getSysValue())) {
                        mappedDefault = vm.getDefalutValue();
                        break;
                    }
                    mappedDefault = vm.getThirdValue();
                }
            }
            rawValue = mappedDefault;
        }
        //开始映射，两种结构不一样，所以需要进行判断type
        if (valueMappings != null && !valueMappings.isEmpty()) {
            if ("checkboxV2".equals(fieldType)) {
                return Arrays.stream(rawValue.toString().split(","))
                        .map(String::trim)
                        .map(val -> valueMappings.stream()
                                .filter(vm -> vm.getSysValue().equals(val))
                                .map(CfgProcessValueMapEntity::getThirdValue)
                                .findFirst().orElse(val))
                        .collect(Collectors.toList());
            } else if ("radioV2".equals(fieldType)) {
                final Object fRawValue = rawValue;
                return valueMappings.stream()
                        .filter(vm -> vm.getSysValue().equals(fRawValue))
                        .map(CfgProcessValueMapEntity::getThirdValue)
                        .findFirst().orElse(rawValue.toString());
            }
        }
        // 非映射类型或无映射结果时直接返回原始值（已处理空情况）
        return rawValue;
    }

    /**
     * 只保留字段 id、type、value，其余移除
     */
    private void retainOnlyIdTypeValue(JSONObject formField) {
        Set<String> keepKeys = new HashSet<>(Arrays.asList("id", "type", "value"));
        List<String> keysToRemove = new ArrayList<>();
        for (String key : formField.keySet()) {
            if (!keepKeys.contains(key)) {
                keysToRemove.add(key);
            }
        }
        for (String key : keysToRemove) {
            formField.remove(key);
        }
    }

    @Override
    public boolean isMatch(String event) {
        return ProcessFormHandler.super.isMatch(event);
    }

    @Override
    public ProcessFormEvent getEvent() {
        return FS_PROCESS_FORM;
    }
}
