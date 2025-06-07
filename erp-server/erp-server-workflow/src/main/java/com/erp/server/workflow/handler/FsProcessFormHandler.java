package com.erp.server.workflow.handler;

/**
 * @description: 飞书解析form类
 * @author: hcg
 * @date: 2025/5/20 12:04
 */

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.workflow.dto.ApproveTaskDetailDTO;
import com.erp.model.workflow.dto.ApproveTaskInfoDTO;
import com.erp.model.workflow.dto.CfgProcessFieldMapDTO;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.model.workflow.enums.CfgProcessRuleTypeEnum;
import com.erp.model.workflow.enums.CfgQueryOptionFieldTypeEnum;
import com.erp.model.workflow.enums.DictBasicEnum;
import com.erp.model.workflow.enums.FsRequestBodyAttributesEnum;
import com.erp.rpc.sys.feign.SysDepartmentThirdFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.sdk.fs.service.FsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.*;
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

    @Resource
    private FsService fsService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SysDepartmentThirdFeign sysDepartmentThirdFeign;

    @Override
    public JSONArray assembleForm(JSONArray formArray, Map<String, Object> variablesMap,
                                  List<CfgProcessFieldMapEntity> fieldMapList,
                                  List<CfgProcessValueMapEntity> valueMapList) {

        // 构建必要的映射关系
        Map<String, List<String>> tidToIdListMap = buildThirdFieldIdToMapIdMap(fieldMapList);
        Map<String, List<CfgProcessValueMapEntity>> valueMapListMap = buildValueMapGroupByFieldId(valueMapList);
        Map<String, List<CfgProcessFieldMapEntity>> fieldMapByThirdId = buildFieldMapByThirdId(fieldMapList);

        // —— 动态构建父控件映射：thirdParentId -> sysParentField ——
        Map<String, String> parentFieldMap = fieldMapList.stream()
                .filter(fm -> "1".equals(fm.getGroupType()))
                .collect(Collectors.toMap(
                        CfgProcessFieldMapEntity::getThirdParentId,
                        fm -> {
                            System.out.println("Key: " + fm.getThirdParentId() + ", Value: " + fm.getSysParentId());
                            return fm.getSysParentId();
                        },
                        (existing, replacement) -> {
                            System.err.println("Duplicate key detected: " + existing + "，Replacing with: " + replacement);
                            return replacement;
                        }
                ));
        
        // 递归处理表单
        processFormArray(formArray,
                variablesMap,
                fieldMapByThirdId,
                tidToIdListMap,
                valueMapListMap,
                parentFieldMap);

        return formArray;
    }

    // 构建映射关系的方法保持不变
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

    private void processFormArray(JSONArray formArray,
                                  Map<String, Object> variablesMap,
                                  Map<String, List<CfgProcessFieldMapEntity>> fieldMapByThirdId,
                                  Map<String, List<String>> tidToIdListMap,
                                  Map<String, List<CfgProcessValueMapEntity>> valueMapListMap,
                                  Map<String,String> parentFieldMap) {

        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formField = formArray.getJSONObject(i);
            String fieldId = formField.getStr("id");
            String type = formField.getStr("type");

            if ("fieldList".equals(type)) {
                // 1. 找到它对应的系统字段名
                String sysParentField = parentFieldMap.get(fieldId);
                // 2. 直接从 variablesMap 取原始 List
                List<Map<String, Object>> rawDetail =
                        (List<Map<String, Object>>) variablesMap.get(sysParentField);

                // 3. 直接塞进去，不解析子字段
                processDetailTable(formField, fieldMapByThirdId, tidToIdListMap, valueMapListMap, rawDetail);
            } else {
                // 普通控件走你原来的逻辑
                processSimpleField(formField,
                        variablesMap,
                        fieldMapByThirdId,
                        tidToIdListMap,
                        valueMapListMap);
            }

            // 保留三要素
            retainOnlyIdTypeValue(formField);
        }
    }

    // 提取通用的字段处理逻辑
    private JSONObject processField(String fieldId, String fieldType, Object rawValue,
                                    Map<String, List<CfgProcessFieldMapEntity>> fieldMapByThirdId,
                                    Map<String, List<String>> tidToIdListMap,
                                    Map<String, List<CfgProcessValueMapEntity>> valueMapListMap,
                                    Object additionalValue) {

        List<CfgProcessFieldMapEntity> entityList = fieldMapByThirdId.get(fieldId);
        if (entityList == null || entityList.isEmpty()) {
            return null;
        }

        // 获取映射实体
        CfgProcessFieldMapEntity amountEntity = null;
        CfgProcessFieldMapEntity entity = entityList.get(0);

        // 获取第一个匹配的映射
        if (entityList.size() > 1) {
            entityList.sort(Comparator.comparingInt(CfgProcessFieldMapEntity::getIndex).reversed());
            amountEntity = entityList.get(1);
            entity = entityList.get(0);
        }

        String sysField = entity.getSysField();
        String defaultValue = entity.getDefaultValue();

        // 创建结果对象
        JSONObject resultField = new JSONObject();
        resultField.set("id", fieldId);
        resultField.set("type", fieldType);

        // 处理默认值或空值
        if ("default".equals(sysField) || "null_Value".equals(sysField)) {
            resultField.set("value", defaultValue);
            return resultField;
        }

        // 执行值映射处理
        Object finalValue = mapFieldValue(fieldId, fieldType, rawValue, tidToIdListMap, valueMapListMap);
        if (finalValue == null) {
            throw new ServiceException("字段映射错误,未获取到{}对应的值，请检查单据数据是否有缺陷", sysField);
        }

        // 根据字段类型处理
        switch (fieldType) {
            case "attachmentV2":
            case "image":
            case "imageV2":
                resultField.set("value", processAttachment(finalValue));
                break;

            case "date":
                resultField.set("value", formatToRFC3339(finalValue));
                break;

            case "amount":
                resultField.set("value", additionalValue);
                resultField.set("currency", finalValue);
                break;

            case "department":
//                SysDepartmentThirdEntity fsDepartment = sysDepartmentThirdFeign.findByDepartmentId("FS", finalValue.toString());
//                if (StrUtil.isEmpty(fsDepartment.getThirdOpenDeptId())) {
//                    throw new ServiceException("未查询到飞书部门信息，请检查部门是否存在:{}", finalValue.toString());
//                }
                JSONObject openId = new JSONObject();
//                openId.set("open_id", Arrays.asList(fsDepartment.getThirdOpenDeptId()));
                openId.set("open_id", Arrays.asList(finalValue));
                resultField.set("value", Arrays.asList(openId));
                break;

            case "contact":
//                List<ThirdUnionDTO> fsUser = sysUserFeign.getThirdByUserIds("FS", Arrays.asList(finalValue.toString()));
//                if (CollUtil.isEmpty(fsUser) || StrUtil.isEmpty(fsUser.get(0).getThirdUserId()) && StrUtil.isEmpty(fsUser.get(0).getThirdOpenId())) {
//                    throw new ServiceException("未查询到飞书用户信息，请检查用户是否存在:{}", finalValue);
//                }
//                if (!StrUtil.isEmpty(fsUser.get(0).getThirdUserId())) {
//                    resultField.set("value", Arrays.asList(fsUser.get(0).getThirdUserId()));
//                } else {
//                    resultField.set("open_ids", Arrays.asList(fsUser.get(0).getThirdOpenId()));
//                }
                resultField.set("value", Arrays.asList(finalValue));
                break;

            default:
                resultField.set("value", finalValue);
        }

        return resultField;
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

                    // 获取系统字段名
                    CfgProcessFieldMapEntity fieldMap = fieldMapList.get(0);
                    if (fieldMapList.size() > 1) {
                        fieldMapList.sort(Comparator.comparingInt(CfgProcessFieldMapEntity::getIndex).reversed());
                        fieldMap = fieldMapList.get(0);
                    }
                    String childSysField = fieldMap.getSysField();

                    // 获取字段值
                    Object childValue = detailRow.get(childSysField);

                    // 获取额外值（用于金额字段）
                    Object additionalValue = null;
                    if ("amount".equals(childType) && fieldMapList.size() > 1) {
                        CfgProcessFieldMapEntity amountEntity = fieldMapList.get(1);
                        additionalValue = detailRow.get(amountEntity.getSysField());
                    }

                    // 使用通用处理逻辑
                    JSONObject detailItem = processField(childId, childType, childValue,
                            fieldMapByThirdId, tidToIdListMap,
                            valueMapListMap, additionalValue);
                    if (detailItem != null) {
                        row.add(detailItem);
                    }
                }
                detailValue.add(row);
            }
        }

        formField.set("value", detailValue);
        formField.remove("children"); // 移除 children 字段
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

        // 获取系统字段名
        CfgProcessFieldMapEntity entity = entityList.get(0);
        if (entityList.size() > 1) {
            entityList.sort(Comparator.comparingInt(CfgProcessFieldMapEntity::getIndex).reversed());
            entity = entityList.get(0);
        }
        String sysField = entity.getSysField();

        // 获取字段值
        Object rawValue = variablesMap.get(sysField);

        // 获取额外值（用于金额字段）
        Object additionalValue = null;
        if ("amount".equals(type) && entityList.size() > 1) {
            CfgProcessFieldMapEntity amountEntity = entityList.get(1);
            additionalValue = variablesMap.get(amountEntity.getSysField());
        }

        // 使用通用处理逻辑
        JSONObject resultField = processField(thirdFieldId, type, rawValue,
                fieldMapByThirdId, tidToIdListMap,
                valueMapListMap, additionalValue);

        if (resultField != null) {
            // 将处理结果复制到原始字段
            formField.putAll(resultField);
        }

    }

    private List<String> processAttachment(Object value) {
        if (value == null) {
            return Collections.emptyList();
        }

        Map<String, String> nameToUrlMap = (Map<String, String>) value;
        List<String> codeList = new ArrayList<>();
            for (String fileName : nameToUrlMap.keySet()) {
                String url = nameToUrlMap.get(fileName);
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
                if (CollUtil.isNotEmpty(valueMappings)) {
                    break;
                }
            }
        } else {
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
        Set<String> keepKeys = new HashSet<>(Arrays.asList("id", "type", "value", "currency", "open_ids"));
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
        dto.setThirdParentId(parentId);
        if (!field.getStr("type").toString().equals("fieldList")) {
            dto.setGroupType("0");
            return dto;
        }
        dto.setGroupType("1");
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
        currencyDto.setIndex(1);              // 币种一般排前面
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


    /**
     * 主方法：提取和处理表单数据
     */
    public Map<String, Object> constructBill(JSONArray formArray, List<CfgProcessFieldMapEntity> fieldMaps, List<CfgProcessValueMapEntity> valueMaps) {
        // 第一步：将飞书结构转为控件id和值的映射
        Map<String, Object> feishuIdValueMap = convertToIdValueMap(formArray);

        //fieldMaps、按照thirdParentId分组，thirdParentId为空的使用thirdFieldId
        Map<String, List<CfgProcessFieldMapEntity>> groupedFieldMaps = fieldMaps.stream()
                .collect(Collectors.groupingBy(entity -> {
                    String key = entity.getThirdParentId();
                    return (key == null || key.isEmpty()) ? entity.getThirdFieldId() : key;
                }));

        //fieldMaps、按照sysParentId进行分组，sysParentId为空，则使用sysFieldId进行分组，如果出现重复的则保留最新的
        Map<String, List<CfgProcessFieldMapEntity>> groupedSysMaps = fieldMaps.stream()
                .collect(Collectors.groupingBy(entity -> {
                    String key = entity.getSysParentId();
                    return (key == null || key.isEmpty()) ? entity.getSysField() : key;
                }));

        Map<String, List<CfgProcessValueMapEntity>> collect = valueMaps.stream().collect(Collectors.groupingBy(CfgProcessValueMapEntity::getFieldMapId));

        // 第二步：根据字段映射和值映射转换为系统映射
        return processMapping(feishuIdValueMap, groupedSysMaps, collect);
    }

    /**
     * 第一步：将飞书结构转为控件id和值的映射
     */
    private static Map<String, Object> convertToIdValueMap(JSONArray formFields) {
        Map<String, Object> idValueMap = new HashMap<>();

        for (int i = 0; i < formFields.size(); i++) {
            JSONObject field = formFields.getJSONObject(i);
            String fieldId = field.getStr("id");
            String fieldType = field.getStr("type");

            // 根据不同控件类型获取对应的值
            if ("fieldList".equals(fieldType)) {
                // 处理明细控件
                JSONArray valueArray = field.getJSONArray("value");
                List<Map<String, Object>> detailList = new ArrayList<>();

                // 遍历每一行明细数据
                for (int j = 0; j < valueArray.size(); j++) {
                    JSONArray rowFields = valueArray.getJSONArray(j);
                    Map<String, Object> rowMap = new HashMap<>();

                    // 处理每一行中的每个字段
                    for (int k = 0; k < rowFields.size(); k++) {
                        JSONObject subField = rowFields.getJSONObject(k);
                        String subFieldId = subField.getStr("id");
                        String subFieldType = subField.getStr("type");

                        // 获取子字段的值
                        Object subFieldValue = extractFieldValue(subField, subFieldType);
                        rowMap.put(subFieldId, subFieldValue);
                    }
                    detailList.add(rowMap);
                }
                idValueMap.put(fieldId, detailList);
            } else {
                // 处理普通控件
                Object fieldValue = extractFieldValue(field, fieldType);
                idValueMap.put(fieldId, fieldValue);
            }
        }

        return idValueMap;
    }

    /**
     * 根据控件类型提取字段值
     */
    private static Object extractFieldValue(JSONObject field, String fieldType) {
        switch (fieldType) {
            case "input":
            case "textarea":
            case "number":
                return field.get("value");
            case "date":
                return convertRFC3339ToLocalDateTime(field.get("value").toString());
            case "radioV2":
                JSONObject radioOption = field.getJSONObject("option");
                return radioOption != null ? radioOption.getStr("key") : field.getStr("text");
            case "checkboxV2":
                JSONArray checkOptions = field.getJSONArray("option");
                if (checkOptions != null && !checkOptions.isEmpty()) { //逗号分割
                    return String.join(",", checkOptions.stream()
                            .map(opt -> ((JSONObject) opt).getStr("key"))
                            .collect(Collectors.toList()));
                } else {
                    return field.getJSONArray("value");
                }
            case "attachmentV2":
            case "imageV2":
            case "image":
                LinkedHashMap<String, String> nameToUrl = new LinkedHashMap<>();
                JSONArray fileArray = field.getJSONArray("value");
                String[] names = field.getStr("ext").split(",");
                for (int i = 0; i < names.length; i++) {
                    nameToUrl.put(names[i], fileArray.get(i).toString());
                }
                return nameToUrl; //TODO 先进行上传，得到url
            case "department":
                JSONArray deptValues = field.getJSONArray("value");
                return deptValues.stream()
                        .map(dept -> ((JSONObject) dept).getStr("open_id"))
                        .collect(Collectors.toList()).get(0); //部门目前只取一个
            case "contact":
                return field.getJSONArray("value").get(0);
            case "amount":
                return field.get("value").toString() + "," + field.getJSONObject("ext").get("currency");
            default:
                return field.get("value");
        }
    }

    public Map<String, Object> processMapping(
            Map<String, Object> feishuIdValueMap,
            Map<String, List<CfgProcessFieldMapEntity>> groupedFieldMaps,
            Map<String, List<CfgProcessValueMapEntity>> valueMapsByFieldMapId) {

        Map<String, Object> finalResultMap = new HashMap<>();

        // 遍历飞书原始数据 Map
        for (Map.Entry<String, Object> entry : feishuIdValueMap.entrySet()) {
            String feishuWidgetId = entry.getKey(); // 飞书控件 ID (e.g., "customerName", "detailList")
            Object feishuOriginalValue = entry.getValue(); // 飞书原始值

            // 获取与当前飞书控件 ID 相关的字段映射配置
            List<CfgProcessFieldMapEntity> relevantFieldMaps = groupedFieldMaps.get(feishuWidgetId);

            // 如果没有找到对应的映射配置，则跳过此字段
            if (relevantFieldMaps == null || relevantFieldMaps.isEmpty()) {
                continue;
            }

            // 判断当前字段是否是明细列表的父级字段
            // 我们通过检查 relevantFieldMaps 中第一个实体的 isDetailField 和 sysParentId 来判断
            boolean isDetailListParent = relevantFieldMaps.get(0).getIsDetailField() &&
                    relevantFieldMaps.get(0).getSysParentId() != null &&
                    !relevantFieldMaps.get(0).getSysParentId().isEmpty();


            if (feishuOriginalValue instanceof List && isDetailListParent) {
                // 如果是明细列表，调用专门处理明细列表的方法
                // 注意：这里feishuOriginalValue是List，且其内部的Map的key是飞书的子控件ID
                processDetailListFeishuToSys(finalResultMap, (List<Map<String, Object>>) feishuOriginalValue, relevantFieldMaps, valueMapsByFieldMapId);
            } else {
                // 如果是普通字段（非明细列表），调用处理单个字段的方法
                processSingleField(finalResultMap, feishuOriginalValue, relevantFieldMaps, valueMapsByFieldMapId);
            }
        }
        return finalResultMap;
    }

    /**
     * 处理普通（非明细）字段类型的数据，将飞书原始值转换为系统目标值。
     *
     * @param currentResultMap    当前正在构建的结果 Map。
     * @param feishuOriginalValue 飞书原始值。
     * @param fieldMaps           与当前字段相关的映射规则列表。
     * @param valueMapsByFieldMapId 预处理过的值映射集。
     */
    private void processSingleField(Map<String, Object> currentResultMap,
                                    Object feishuOriginalValue,
                                    List<CfgProcessFieldMapEntity> fieldMaps,
                                    Map<String, List<CfgProcessValueMapEntity>> valueMapsByFieldMapId) {

        for (CfgProcessFieldMapEntity fieldMap : fieldMaps) {
            // 获取目标系统字段名
            String sysField = fieldMap.getSysField();
            if (sysField == null || sysField.isEmpty()) {
                continue; // 如果没有定义系统字段名，则跳过
            }

            // 映射值
            Object mappedValue = mapValue(feishuOriginalValue, fieldMap, valueMapsByFieldMapId);
            currentResultMap.put(sysField, mappedValue);
        }
    }

    /**
     * 核心的值映射方法，将飞书原始值转换为系统目标值。
     *
     * @param feishuOriginalValue 飞书原始值。
     * @param fieldMap            该字段的映射规则。
     * @param valueMapsByFieldMapId 预处理过的值映射集。
     * @return 映射后的值。
     */
    private Object mapValue(Object feishuOriginalValue, CfgProcessFieldMapEntity fieldMap,
                            Map<String, List<CfgProcessValueMapEntity>> valueMapsByFieldMapId) {

        // 将原始值转换为字符串，便于处理多选和值映射
        String valueStr = (feishuOriginalValue == null) ? "" : feishuOriginalValue.toString();
        // 获取与当前字段映射 ID 关联的值映射列表
        List<CfgProcessValueMapEntity> valueMappings = valueMapsByFieldMapId.get(fieldMap.getId());

        // 如果没有值映射规则，或者是非需要值映射的飞书控件类型，直接返回原始值
        if (valueMappings == null || valueMappings.isEmpty() ||
                "attachmentV2".equals(fieldMap.getThirdFieldType()) ||
                "imageV2".equals(fieldMap.getThirdFieldType()) ||
                "image".equals(fieldMap.getThirdFieldType()) ||
                "contact".equals(fieldMap.getThirdFieldType()) ||
                "department".equals(fieldMap.getThirdFieldType())) {
            // 对于金额这种复合字段，如果其类型为 "amount"
            if ("amount".equals(fieldMap.getThirdFieldType())) {
                // feishuOriginalValue 可能是 [value, currency] 形式的 List
                if (feishuOriginalValue instanceof List && ((List<?>) feishuOriginalValue).size() == 2) {
                    List<?> parts = (List<?>) feishuOriginalValue;
                    // 根据 fieldMap 中的 index 返回金额或币种。币种的值映射在下面通用逻辑处理。
                    return fieldMap.getIndex() == 0 ? parts.get(0) : parts.get(1);
                } else if (feishuOriginalValue instanceof String && ((String) feishuOriginalValue).contains(",")) {
                    // 也可能是 "100.00,CNY" 这种字符串形式
                    String[] parts = valueStr.split(",");
                    if (parts.length == 2) {
                        return fieldMap.getIndex() == 0 ? parts[0] : parts[1];
                    }
                }
            }
            return feishuOriginalValue; // 没有值映射，直接返回原始值
        }

        // 处理多选框类型（checkboxV2）
        if ("checkboxV2".equals(fieldMap.getThirdFieldType())) {
            // 将逗号分隔的原始值拆分，对每个部分进行映射，然后重新用逗号拼接
            return Arrays.stream(valueStr.split(","))
                    .map(part -> findSysValue(part, valueMappings))
                    .collect(Collectors.joining(","));
        }

        // 处理其他需要值映射的类型（如单选框 radioV2）
        return findSysValue(valueStr, valueMappings);
    }

    /**
     * 在值映射列表中查找飞书原始值对应的系统值。
     *
     * @param feishuOriginalPart 飞书原始值（或多选中的一个部分）。
     * @param valueMappings    值的映射列表。
     * @return 查找到的系统值，如果没有找到则返回默认值或原始值。
     */
    private String findSysValue(String feishuOriginalPart, List<CfgProcessValueMapEntity> valueMappings) {
        // 1. 精确匹配：查找 thirdValue 等于 feishuOriginalPart 的映射
        Optional<CfgProcessValueMapEntity> foundMap = valueMappings.stream()
                .filter(vm -> feishuOriginalPart.equals(vm.getThirdValue()))
                .findFirst();

        if (foundMap.isPresent()) {
            return foundMap.get().getSysValue(); // 返回映射到的系统值
        }

        // 2. 如果精确匹配失败，查找默认值配置
        Optional<CfgProcessValueMapEntity> defaultMap = valueMappings.stream()
                .filter(vm -> "default".equalsIgnoreCase(vm.getSysValue()) || (vm.getThirdValue() != null && vm.getThirdValue().isEmpty()))
                .findFirst();
        if (defaultMap.isPresent()) {
            return defaultMap.get().getDefaultValue(); // 返回默认值
        }

        // 3. 如果都没有找到，返回原始值
        return feishuOriginalPart;
    }

    /**
     * 处理明细列表类型的数据，将飞书明细行转换为系统明细行。
     *
     * @param finalResultMap      最终结果 Map。
     * @param feishuDetailRows    飞书明细行数据（List<Map<String, Object>>，子Map的key是飞书子控件ID）。
     * @param detailFieldMaps     针对该明细列表的所有子字段的映射配置。
     * @param valueMapsByFieldMapId 值映射配置。
     */
    private void processDetailListFeishuToSys(Map<String, Object> finalResultMap,
                                              List<Map<String, Object>> feishuDetailRows,
                                              List<CfgProcessFieldMapEntity> detailFieldMaps,
                                              Map<String, List<CfgProcessValueMapEntity>> valueMapsByFieldMapId) {
        if (feishuDetailRows == null || feishuDetailRows.isEmpty()) {
            return;
        }

        // 将明细字段配置按其第三方ID（子控件ID）分组，便于在行内查找
        // 这里的 key 是 thirdFieldId，也就是飞书明细行中子控件的 ID
        Map<String, List<CfgProcessFieldMapEntity>> detailConfigsBySubWidgetId = detailFieldMaps.stream()
                .collect(Collectors.groupingBy(CfgProcessFieldMapEntity::getThirdFieldId));

        List<Map<String, Object>> mappedRows = new ArrayList<>();

        for (Map<String, Object> feishuRow : feishuDetailRows) { // 遍历飞书的每一行明细数据
            Map<String, Object> mappedRow = new HashMap<>(); // 转换后的一行系统明细数据

            for (Map.Entry<String, Object> feishuCell : feishuRow.entrySet()) {
                String feishuSubWidgetId = feishuCell.getKey(); // 明细行中飞书子控件的 ID
                Object feishuCellValue = feishuCell.getValue(); // 明细行中飞书子控件的值

                // 找到子控件的字段映射配置
                List<CfgProcessFieldMapEntity> cellFieldMaps = detailConfigsBySubWidgetId.get(feishuSubWidgetId);
                if (cellFieldMaps != null && !cellFieldMaps.isEmpty()) {
                    // 递归调用 processSingleField 处理明细行中的每个子字段
                    // 目标 Map 是 mappedRow
                    processSingleField(mappedRow, feishuCellValue, cellFieldMaps, valueMapsByFieldMapId);
                }
            }
            if (!mappedRow.isEmpty()) {
                mappedRows.add(mappedRow);
            }
        }

        // 明细列表在目标系统的字段名 (sysParentId)
        if (!mappedRows.isEmpty() && !detailFieldMaps.isEmpty()) {
            String sysParentId = detailFieldMaps.get(0).getSysParentId();
            if (sysParentId != null && !sysParentId.isEmpty()) {
                finalResultMap.put(sysParentId, mappedRows);
            }
        }
    }

    public static LocalDateTime convertRFC3339ToLocalDateTime(String rfc3339Date) {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(rfc3339Date);
        return offsetDateTime.toLocalDateTime();
    }

    @Override
    public DictBasicEnum getEventType() {
        return DictBasicEnum.FSAPPROVE;
    }
}