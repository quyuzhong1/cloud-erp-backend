package com.erp.server.workflow.handler;

/**
 * @description: 飞书解析form类
 *
 * @author: hcg
 * @date: 2025/5/20 12:04
 */

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.workflow.dto.CfgProcessFieldMapDTO;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.model.workflow.enums.CfgProcessRuleTypeEnum;
import com.erp.model.workflow.enums.CfgQueryOptionFieldTypeEnum;
import com.erp.model.workflow.enums.DictBasicEnum;
import com.erp.model.workflow.enums.FsRequestBodyAttributesEnum;
import com.erp.sdk.fs.service.FsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


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
    public JSONArray assembleForm(JSONArray formArray, Map<String, Object> variablesMap,
                              List<CfgProcessFieldMapEntity> fieldMapList,
                              List<CfgProcessValueMapEntity> valueMapList) {

        // 构建必要的映射关系
        Map<String, List<String>> tidToIdListMap = buildThirdFieldIdToMapIdMap(fieldMapList);
        Map<String, List<CfgProcessValueMapEntity>> valueMapListMap = buildValueMapGroupByFieldId(valueMapList);
        Map<String, List<CfgProcessFieldMapEntity>> fieldMapByThirdId = buildFieldMapByThirdId(fieldMapList);

        // 获取明细数据
        List<Map<String, Object>> detailList = (List<Map<String, Object>>) variablesMap.get("detailList");

        // 递归处理表单
        processFormArray(formArray, variablesMap, fieldMapByThirdId, tidToIdListMap, valueMapListMap, detailList);

        return formArray;
    }

    private Map<String, List<String>> buildThirdFieldIdToMapIdMap(List<CfgProcessFieldMapEntity> fieldMapList) {
        return fieldMapList.stream().collect(Collectors.groupingBy(CfgProcessFieldMapEntity::getThirdFieldId, Collectors.mapping(CfgProcessFieldMapEntity::getId, Collectors.toList())));
    }

    private Map<String, List<CfgProcessValueMapEntity>> buildValueMapGroupByFieldId(List<CfgProcessValueMapEntity> valueMapList) {
        return valueMapList.stream().collect(Collectors.groupingBy(
                CfgProcessValueMapEntity::getFieldMapId
        ));
    }

    private Map<String, List<CfgProcessFieldMapEntity>> buildFieldMapByThirdId(List<CfgProcessFieldMapEntity> fieldMapList) {
        return fieldMapList.stream()
                .collect(Collectors.groupingBy(CfgProcessFieldMapEntity::getThirdFieldId));
    }

    private void processFormArray(JSONArray formArray, Map<String, Object> variablesMap,
                                  Map<String, List<CfgProcessFieldMapEntity>> fieldMapByThirdId,
                                  Map<String, List<String>> tidToIdListMap,
                                  Map<String, List<CfgProcessValueMapEntity>> valueMapListMap,
                                  List<Map<String, Object>> detailList) {

        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formField = formArray.getJSONObject(i);
            String type = formField.getStr("type");

            // 根据字段类型分别处理
            if ("fieldList".equals(type)) {
                processDetailTable(formField, fieldMapByThirdId, tidToIdListMap, valueMapListMap, detailList);
            } else {
                processSimpleField(formField, variablesMap, fieldMapByThirdId, tidToIdListMap, valueMapListMap);
            }

            // 只保留 id、type、value 字段
            retainOnlyIdTypeValue(formField);
        }
    }

    private void processDetailTable(JSONObject formField, Map<String, List<CfgProcessFieldMapEntity>> fieldMapByThirdId,
                                    Map<String, List<String>> tidToIdListMap, Map<String, List<CfgProcessValueMapEntity>> valueMapListMap,
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
                    List<CfgProcessFieldMapEntity> fieldMapList = fieldMapByThirdId.get(childId);
                    if (fieldMapList == null || fieldMapList.isEmpty()) {
                        continue;
                    }

                    // 获取第一个匹配的映射
                    CfgProcessFieldMapEntity amountEntity = null;
                    CfgProcessFieldMapEntity fieldMap = fieldMap= fieldMapList.get(0);
                    // 获取第一个匹配的映射
                    if (fieldMapList.size()>1){
                        fieldMapList.sort(Comparator.comparingInt(CfgProcessFieldMapEntity::getIndex).reversed());
                        amountEntity= fieldMapList.get(1);
                        fieldMap= fieldMapList.get(0);
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
                    childValue = mapFieldValue(childId, childType, childValue, tidToIdListMap, valueMapListMap);

                    // 特殊类型处理
                    if ("attachmentV2".equals(childType)) {
                        List<String> fileCodeList = processAttachment(childValue);
                        JSONObject detailItem = createDetailItem(childId, childType, fileCodeList);
                        row.add(detailItem);
                    } else if ("date".equals(childType)) {
                        String formattedDate = formatToRFC3339(childValue);
                        JSONObject detailItem = createDetailItem(childId, childType, formattedDate);
                        row.add(detailItem);
                    } else if ("amount".equals(childType)) {
                        JSONObject detailItem = new JSONObject();
                        detailItem.set("id", childId);
                        detailItem.set("type", childType);
                        detailItem.set("value", detailRow.get(amountEntity.getSysField()));
                        detailItem.set("currency", childValue);
                        row.add(detailItem);
                    } else if ("department".equals(childType)) {
                        //查询用户关系表->飞书id TODO未实现,再次查询做值映射
                        JSONObject detailItem = new JSONObject();
                        detailItem.set("id", childId);
                        detailItem.set("type", childType);
                        JSONObject openId = new JSONObject();
                        openId.set("open_id", childValue);
                        detailItem.set("value", Arrays.asList(openId));
                        row.add(detailItem);
                    } else if ("contact".equals(childType)) {
                        //查询部门关系表->飞书id TODO未实现,再次查询做值映射
                        JSONObject detailItem = new JSONObject();
                        detailItem.set("id", childId);
                        detailItem.set("type", childType);
                        detailItem.set("value", Arrays.asList(childValue));
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
                                    Map<String, List<CfgProcessFieldMapEntity>> fieldMapByThirdId,
                                    Map<String, List<String>> tidToIdListMap, Map<String, List<CfgProcessValueMapEntity>> valueMapListMap) {

        String thirdFieldId = formField.getStr("id");
        String type = formField.getStr("type");

        List<CfgProcessFieldMapEntity> entityList = fieldMapByThirdId.get(thirdFieldId);
        if (entityList == null || entityList.isEmpty()) {
            return;
        }
        CfgProcessFieldMapEntity amountEntity = null;
        CfgProcessFieldMapEntity entity = entityList.get(0);
        // 获取第一个匹配的映射
        if (entityList.size()>1){
            entityList.sort(Comparator.comparingInt(CfgProcessFieldMapEntity::getIndex).reversed());
            amountEntity= entityList.get(1);
        }

        String sysField = entity.getSysField();
        String defaultValue = entity.getDefaultValue();

        if ("default".equals(sysField) || "null_Value".equals(sysField)) {
            formField.set("value", defaultValue);
            return;
        }

        Object rawValue = variablesMap.get(sysField);
        // 执行值映射处理
        Object finalValue = mapFieldValue(thirdFieldId, type, rawValue, tidToIdListMap, valueMapListMap);
        if (finalValue == null){
            throw new ServiceException("字段映射错误,未获取到{}对应的值，请检查单据数据是否有缺陷",entity.getSysField());
        }
        // 特殊类型处理,附件、图片类型上传使用同一个接口
        if ("attachmentV2".equals(type)||"image".equals(type)||"imageV2".equals(type)) {
            List<String> fileCodeList = processAttachment(finalValue);
            formField.set("value", fileCodeList);
        } else if ("date".equals(type)) {
            String formattedDate = formatToRFC3339(finalValue);
            formField.set("value", formattedDate);
        } else if ("amount".equals(type)) {
            formField.set("value", variablesMap.get(amountEntity.getSysField()));
            formField.set("currency", finalValue);
        } else if ("department".equals(type)) {
            //查询部门关系表->飞书id
        } else if ("contact".equals(type)) {
            //查询用户关系表->飞书id
            formField.set("value", Arrays.asList(finalValue));
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
                                 Map<String, List<String>> tidToIdListMap, Map<String, List<CfgProcessValueMapEntity>> valueMapListMap) {

        List<String> fieldMapId = tidToIdListMap.get(fieldId);
        List<CfgProcessValueMapEntity> valueMappings = Collections.emptyList();
        //只有金额控件类型需要遍历获取单位的值映射
        if (CfgQueryOptionFieldTypeEnum.AMOUNT.getCode().equals(tidToIdListMap)) {
            //金额类型对应的fieldMaId
            for (String fieldMaId : fieldMapId) {
                valueMappings = valueMapListMap.get(fieldMaId);
                if (CollUtil.isNotEmpty(valueMappings)){
                    break;
                }
            }
        }else {
            valueMappings = valueMapListMap.get(fieldMapId.get(0));
        }

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
            List<CfgProcessValueMapEntity> finalValueMappings = valueMappings;
            return Arrays.stream(rawValue.toString().split(","))
                    .map(String::trim)
                    .map(val -> finalValueMappings.stream()
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
        Set<String> keepKeys = new HashSet<>(Arrays.asList("id", "type", "value","currency"));
        formField.keySet().stream()
                .filter(key -> !keepKeys.contains(key))
                .collect(Collectors.toList())
                .forEach(formField::remove);
    }


    /**
     * 解析整个表单 JSON，返回平铺的 ViewDTO 列表
     */
    public List<CfgProcessFieldMapDTO.ViewDTO> parseForm(String formString) {
        JSONObject body = JSONUtil.parseObj(formString);
        String formJson = body.getStr(FsRequestBodyAttributesEnum.FORM.getCode());
        JSONArray fields = JSONUtil.parseArray(formJson);

        List<CfgProcessFieldMapDTO.ViewDTO> result = new ArrayList<>();
        for (JSONObject field : fields.jsonIter()) {
            result.addAll(parseField(field, false, null));
        }
        return result;
    }
    /**
     * 递归解析单个字段，isDetail=是否明细子项，parentId=父级 ID
     * 如果是 FIELDLIST，会继续对子节点调用本方法。
     * 如果是 AMOUNT，会产生额外的“币种”子项。
     */
    private List<CfgProcessFieldMapDTO.ViewDTO> parseField(
            JSONObject field, boolean isDetail, String parentId
    ) {
        List<CfgProcessFieldMapDTO.ViewDTO> list = new ArrayList<>();

        // 构造基础 DTO
        CfgProcessFieldMapDTO.ViewDTO dto = buildBaseDTO(field, isDetail, parentId);

        String type = dto.getThirdFieldType();
        // 如果是明细列表（FIELDLIST），则递归解析其 children 并直接返回汇总列表
        if (CfgQueryOptionFieldTypeEnum.FIELDLIST.getCode().equals(type)) {
            JSONArray children = field.getJSONArray(FsRequestBodyAttributesEnum.CHILDREN.getCode());
            for (JSONObject child : children.jsonIter()) {
                list.addAll(parseField(child, true, dto.getThirdFieldId()));
            }
            return list;
        }
        // 非 FIELDLIST 的正常项先添加自身
        list.add(dto);

        // 如果是金额类型，再生成一个“币种”子项
        if (CfgQueryOptionFieldTypeEnum.AMOUNT.getCode().equals(type)) {
            list.add(createAmountField(dto));
        }

        return list;
    }

    /**
     * 构造一个最基础的 ViewDTO（不包含“币种”那条）
     */
    private CfgProcessFieldMapDTO.ViewDTO buildBaseDTO(
            JSONObject field, boolean isDetail, String parentId
    ) {
        CfgProcessFieldMapDTO.ViewDTO dto = new CfgProcessFieldMapDTO.ViewDTO();

        // 设置前缀：单据头 or 单据明细
        String prefix = isDetail ? "单据明细-" : "单据头-";
        dto.setThirdField(prefix + field.getStr(FsRequestBodyAttributesEnum.NAME.getCode()));

        dto.setThirdFieldType(field.getStr(FsRequestBodyAttributesEnum.TYPE.getCode()));
        dto.setThirdFieldRequired(field.getBool(FsRequestBodyAttributesEnum.REQUIRED.getCode(), false));
        dto.setThirdFieldId(field.getStr(FsRequestBodyAttributesEnum.ID.getCode()));
        dto.setIsDetailField(isDetail);
        dto.setParentId(parentId);
        // 默认 index 可不设，或由调用方根据业务设定
        return dto;
    }

    /**
     * 由一个“金额”类型的 DTO 克隆并生产对应的“币种”子项
     */
    private CfgProcessFieldMapDTO.ViewDTO createAmountField(
            CfgProcessFieldMapDTO.ViewDTO amountDto
    ) {
        CfgProcessFieldMapDTO.ViewDTO currencyDto = new CfgProcessFieldMapDTO.ViewDTO();
        // 复制除了 type、prefix 之外的其它公共属性
        BeanUtil.copyProperties(amountDto, currencyDto);

        // 调整子项显示文案、类型、index、cfgType
        currencyDto.setThirdField(amountDto.getThirdField() + "币种");
        currencyDto.setThirdFieldType(CfgQueryOptionFieldTypeEnum.RADIOV2.getCode());
        currencyDto.setIndex(0);              // 币种一般排前面
        currencyDto.setCfgType("sysCfg");
        return currencyDto;
    }

    /**
     * 解析表单值，提取下拉选项和其他特定字段类型的值映射
     *
     * @param formString 表单JSON字符串
     * @return 字段ID到值映射的Map
     */
    public Map<String, Map<String, String>> parseFormValue(String formString) {
        JSONObject root = JSONUtil.parseObj(formString);
        String formStringValue = root.getStr(FsRequestBodyAttributesEnum.FORM.getCode());
        JSONArray formArray = JSONUtil.parseArray(formStringValue);

        Map<String, Map<String, String>> parsedValues = new HashMap<>();

        // 遍历表单字段
        for (JSONObject field : formArray.jsonIter()) {
            String fieldId = field.getStr(FsRequestBodyAttributesEnum.ID.getCode());
            String type = field.getStr(FsRequestBodyAttributesEnum.TYPE.getCode());

            if (FsRequestBodyAttributesEnum.FIELDLIST.getCode().equals(type)) {
                // 处理明细表字段
                processDetailFields(field, parsedValues);
            } else {
                // 处理普通字段
                processField(field, fieldId, type, parsedValues);
            }
        }

        return parsedValues;
    }

    /**
     * 处理普通字段
     */
    private static void processField(JSONObject field, String fieldId, String type, Map<String, Map<String, String>> parsedValues) {
        // 处理金额类型
        if (CfgQueryOptionFieldTypeEnum.AMOUNT.getCode().equals(type)) {
            processAmountField(field, fieldId, parsedValues);
            return;
        }

        // 处理包含选项的字段
        if (field.containsKey(FsRequestBodyAttributesEnum.OPTION.getCode())) {
            Object valueObj = field.get(FsRequestBodyAttributesEnum.OPTION.getCode());
            if (valueObj instanceof JSONArray) {
                processOptionArray((JSONArray) valueObj, fieldId, parsedValues);
            } else if (valueObj != null && !field.get(FsRequestBodyAttributesEnum.TYPE.getCode()).equals(CfgQueryOptionFieldTypeEnum.DEPARTMENT.getCode())) {
                // 处理单个值选项
                Map<String, String> valueMap = new HashMap<>();
                valueMap.put(FsRequestBodyAttributesEnum.VALUE.getCode(), valueObj.toString());
                parsedValues.put(fieldId, valueMap);
            }
        }
    }

    /**
     * 处理明细表字段
     */
    private static void processDetailFields(JSONObject field, Map<String, Map<String, String>> parsedValues) {
        JSONArray detailFields = field.getJSONArray(FsRequestBodyAttributesEnum.CHILDREN.getCode());
        if (detailFields == null) {
            return;
        }

        for (JSONObject detail : detailFields.jsonIter()) {
            String detailId = detail.getStr("id");
            String detailType = detail.getStr(FsRequestBodyAttributesEnum.TYPE.getCode());

            if (!detail.containsKey(FsRequestBodyAttributesEnum.OPTION.getCode())) {
                continue;
            }

            // 处理明细表中的字段
            processField(detail, detailId, detailType, parsedValues);
        }
    }

    /**
     * 处理金额类型字段
     */
    private static void processAmountField(JSONObject field, String fieldId, Map<String, Map<String, String>> parsedValues) {
        Object obj = field.get(FsRequestBodyAttributesEnum.OPTION.getCode());
        if (obj == null) {
            return;
        }

        Map<String, String> valueMap = new HashMap<>();
        JSONObject option = (JSONObject) obj;
        JSONArray currencyRange = option.getJSONArray("currencyRange");

        if (currencyRange != null) {
            for (Object currency : currencyRange) {
                valueMap.put(currency.toString(), currency.toString());
            }
            parsedValues.put(fieldId, valueMap);
        }
    }

    /**
     * 处理选项数组
     */
    private static void processOptionArray(JSONArray valueList, String fieldId, Map<String, Map<String, String>> parsedValues) {
        if (valueList == null) {
            return;
        }

        Map<String, String> valueMap = new HashMap<>();
        for (JSONObject value : valueList.jsonIter()) {
            String valueCode = value.getStr(FsRequestBodyAttributesEnum.VALUE.getCode());
            String valueText = value.getStr(FsRequestBodyAttributesEnum.TEXT.getCode());
            if (valueCode != null && valueText != null) {
                valueMap.put(valueCode, valueText);
            }
        }

        if (!valueMap.isEmpty()) {
            parsedValues.put(fieldId, valueMap);
        }
    }

    /**
     * 处理单个选项对象
     */
    private static void processSingleOption(JSONObject option, String fieldId, Map<String, Map<String, String>> parsedValues) {
        if (option == null) {
            return;
        }

        String valueCode = option.getStr(FsRequestBodyAttributesEnum.VALUE.getCode());
        String valueText = option.getStr(FsRequestBodyAttributesEnum.TEXT.getCode());

        if (valueCode != null && valueText != null) {
            Map<String, String> valueMap = new HashMap<>();
            valueMap.put(valueCode, valueText);
            parsedValues.put(fieldId, valueMap);
        }
    }


    @Override
    public boolean isMatch(String event) {
        return ProcessFormHandler.super.isMatch(event);
    }

    @Override
    public CfgProcessRuleTypeEnum getEvent() {
        return CfgProcessRuleTypeEnum.FSPROCESS;
    }


    @Override
    public Map<String, Object> constructBill(JSONArray formArray, List<CfgProcessFieldMapEntity> fieldMapList, List<CfgProcessValueMapEntity> valueMapList) {

        return Collections.emptyMap();
    }

    @Override
    public DictBasicEnum getEventType() {
        return DictBasicEnum.FSAPPROVE;
    }
}
