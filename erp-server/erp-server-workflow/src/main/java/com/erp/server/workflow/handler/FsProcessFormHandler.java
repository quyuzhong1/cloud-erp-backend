package com.erp.server.workflow.handler;

/**
 * @description: 飞书解析form类
 * TODO：未支持金额、图片、部门、联系人控件类型
 * @author: hcg
 * @date: 2025/5/20 12:04
 */

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.common.business.enums.ProcessFormEvent;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.model.workflow.enums.FsRequestBodyAttributesEnum;
import com.erp.sdk.fs.service.FsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
    private final FsService fsService;

    public FsProcessFormHandler(FsService fsService) {
        this.fsService = fsService;
    }

    @Override
    public JSONArray assemble(JSONArray formArray, Map<String, Object> variablesMap,
                              List<CfgProcessFieldMapEntity> fieldMapList,
                              List<CfgProcessValueMapEntity> valueMapList) {

        // 构建必要的映射关系
        Map<String, String> tidToIdMap = buildThirdFieldIdToMapIdMap(fieldMapList);
        Map<String, List<CfgProcessValueMapEntity>> valueMapListMap = buildValueMapGroupByFieldId(valueMapList);
        Map<String, CfgProcessFieldMapEntity> fieldMapByThirdId = buildFieldMapByThirdId(fieldMapList);

        // 获取明细数据
        List<Map<String, Object>> detailList = (List<Map<String, Object>>) variablesMap.get("detailList");

        // 递归处理表单
        processFormArray(formArray, variablesMap, fieldMapByThirdId, tidToIdMap, valueMapListMap, detailList);

        return formArray;
    }

    private Map<String, String> buildThirdFieldIdToMapIdMap(List<CfgProcessFieldMapEntity> fieldMapList) {
        return fieldMapList.stream().collect(Collectors.toMap(
                CfgProcessFieldMapEntity::getThirdFieldId,
                CfgProcessFieldMapEntity::getId
        ));
    }

    private Map<String, List<CfgProcessValueMapEntity>> buildValueMapGroupByFieldId(List<CfgProcessValueMapEntity> valueMapList) {
        return valueMapList.stream().collect(Collectors.groupingBy(
                CfgProcessValueMapEntity::getFieldMapId
        ));
    }

    private Map<String, CfgProcessFieldMapEntity> buildFieldMapByThirdId(List<CfgProcessFieldMapEntity> fieldMapList) {
        return fieldMapList.stream().collect(Collectors.toMap(
                CfgProcessFieldMapEntity::getThirdFieldId,
                Function.identity()
        ));
    }

    private void processFormArray(JSONArray formArray, Map<String, Object> variablesMap,
                                  Map<String, CfgProcessFieldMapEntity> fieldMapByThirdId,
                                  Map<String, String> tidToIdMap,
                                  Map<String, List<CfgProcessValueMapEntity>> valueMapListMap,
                                  List<Map<String, Object>> detailList) {

        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formField = formArray.getJSONObject(i);
            String type = formField.getStr("type");

            // 根据字段类型分别处理
            if ("fieldList".equals(type)) {
                processDetailTable(formField, fieldMapByThirdId, tidToIdMap, valueMapListMap, detailList);
            } else {
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
        if (detailList != null && !detailList.isEmpty()) {
            for (Map<String, Object> detailRow : detailList) {
                JSONArray row = new JSONArray();
                for (Object childObj : children) {
                    JSONObject child = (JSONObject) childObj;
                    String childId = child.getStr("id");
                    String childType = child.getStr("type");

                    // 获取字段映射信息
                    CfgProcessFieldMapEntity fieldMap = fieldMapByThirdId.get(childId);
                    if (fieldMap == null) {
                        continue;
                    }

                    String childSysField = fieldMap.getSysField();
                    String childDefault = fieldMap.getDefaultValue();

                    if ("default".equals(childSysField) || "null_Value".equals(childSysField)) {
                        JSONObject detailItem = createDetailItem(childId, childType, childDefault);
                        row.add(detailItem);
                        continue;
                    }

                    // 获取字段值
                    Object childValue = detailRow.get(childSysField);

                    // 执行值映射处理
                    childValue = mapFieldValue(childId, childType, childValue, tidToIdMap, valueMapListMap);

                    // 特殊类型处理
                    if ("attachmentV2".equals(childType)) {
                        List<String> fileCodeList = processAttachment(childValue);
                        JSONObject detailItem = createDetailItem(childId, childType, fileCodeList);
                        row.add(detailItem);
                    } else if ("date".equals(childType)) {
                        String formattedDate = formatToRFC3339(childValue);
                        JSONObject detailItem = createDetailItem(childId, childType, formattedDate);
                        row.add(detailItem);
                    } else {
                        JSONObject detailItem = createDetailItem(childId, childType, childValue);
                        row.add(detailItem);
                    }
                }
                detailValue.add(row);
            }
        }

        formField.set("value", detailValue);
        formField.remove("children"); // 移除 children 字段
    }

    private JSONObject createDetailItem(String id, String type, Object value) {
        JSONObject detailItem = new JSONObject();
        detailItem.set("id", id);
        detailItem.set("type", type);
        detailItem.set("value", value);
        return detailItem;
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
        String defaultValue = entity.getDefaultValue();

        if ("default".equals(sysField) || "null_Value".equals(sysField)) {
            formField.set("value", defaultValue);
            return;
        }

        Object rawValue = variablesMap.get(sysField);
        // 执行值映射处理
        Object finalValue = mapFieldValue(thirdFieldId, type, rawValue, tidToIdMap, valueMapListMap);
        if (finalValue == null){
            throw new ServiceException("字段映射错误,未获取到{}对应的值，请检查单据数据是否有缺陷",entity.getSysField());
        }
        // 特殊类型处理
        if ("attachmentV2".equals(type)) {
            List<String> fileCodeList = processAttachment(finalValue);
            formField.set("value", fileCodeList);
        } else if ("date".equals(type)) {
            String formattedDate = formatToRFC3339(finalValue);
            formField.set("value", formattedDate);
        } else {
            formField.set("value", finalValue);
        }
    }

    private List<String> processAttachment(Object value) {
        if (value == null) {
            return Collections.emptyList();
        }

        List<Map<String, String>> nameToUrlMap = (List<Map<String, String>>) value;
        List<String> codeList = new ArrayList<>();

        for (Map<String, String> map : nameToUrlMap) {
            for (String fileName : map.keySet()) {
                String url = map.get(fileName);
                File tempFile = null;
                try {
                    // 获取文件内容
                    byte[] fileByte = FastDFSClientUtil.getFileByte(url);

                    // 创建临时文件
                    tempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + fileName);
                    try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                        fos.write(fileByte);
                        fos.flush();
                    }

                    // 上传到飞书
                    String fileCode = fsService.uploadApprovalFile(tempFile, fileName);
                    codeList.add(fileCode);
                } catch (Exception e) {
                    log.error("处理附件失败: {}", fileName, e);
                } finally {
                    // 清理临时文件
                    if (tempFile != null && tempFile.exists()) {
                        tempFile.delete();
                    }
                }
            }
        }

        return codeList;
    }

    private String formatToRFC3339(Object dateValue) {
        if (dateValue == null) {
            return null;
        }

        try {
            // 解析原始时间字符串
            DateTime hutoolDate = DateUtil.parse(dateValue.toString(), "yyyy-MM-dd HH:mm:ss.SSS");

            // 转换为带时区的时间格式
            ZonedDateTime zonedDateTime = hutoolDate.toInstant().atZone(ZoneId.systemDefault());
            OffsetDateTime offsetDateTime = zonedDateTime.toOffsetDateTime();

            // 格式化为 RFC3339 格式
            return offsetDateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e) {
            log.error("日期格式转换失败: {}", dateValue, e);
            return dateValue.toString();
        }
    }

    private Object mapFieldValue(String fieldId, String fieldType, Object rawValue,
                                 Map<String, String> tidToIdMap, Map<String, List<CfgProcessValueMapEntity>> valueMapListMap) {

        String fieldMapId = tidToIdMap.get(fieldId);
        List<CfgProcessValueMapEntity> valueMappings = valueMapListMap.get(fieldMapId);

        // 当rawValue为空时，获取对应值映射中sysValue为default记录的default_value字段作为rawValue
        if (rawValue == null || StrUtil.isBlank(rawValue.toString())) {
            if (valueMappings != null) {
                Optional<String> mappedDefault = valueMappings.stream()
                        .filter(vm -> "default".equals(vm.getSysValue()))
                        .map(CfgProcessValueMapEntity::getDefaultValue)
                        .findFirst();
                if (mappedDefault.isPresent()) {
                    rawValue = mappedDefault.get();
                }
            }
        }

        // 如果没有映射配置，直接返回rawValue
        if (valueMappings == null || valueMappings.isEmpty()) {
            return rawValue;
        }

        // 根据字段类型进行不同处理
        if ("checkboxV2".equals(fieldType)) {
            // 多选处理
            return Arrays.stream(rawValue.toString().split(","))
                    .map(String::trim)
                    .map(val -> valueMappings.stream()
                            .filter(vm -> vm.getSysValue().equals(val))
                            .map(CfgProcessValueMapEntity::getThirdValue)
                            .findFirst().orElse(val))
                    .collect(Collectors.toList());
        } else if ("radioV2".equals(fieldType)) {
            // 单选处理
            final String rawValueStr = rawValue.toString();
            return valueMappings.stream()
                    .filter(vm -> vm.getSysValue().equals(rawValueStr))
                    .map(CfgProcessValueMapEntity::getThirdValue)
                    .findFirst().orElse(rawValueStr);
        }

        // 非映射类型直接返回原始值
        return rawValue;
    }

    /**
     * 只保留字段 id、type、value，其余移除
     */
    private void retainOnlyIdTypeValue(JSONObject formField) {
        Set<String> keepKeys = new HashSet<>(Arrays.asList("id", "type", "value"));
        formField.keySet().stream()
                .filter(key -> !keepKeys.contains(key))
                .collect(Collectors.toList())
                .forEach(formField::remove);
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
