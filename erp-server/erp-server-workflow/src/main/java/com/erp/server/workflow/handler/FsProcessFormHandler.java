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
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.nacos.api.utils.StringUtils;
import com.common.business.enums.ThirdpartyPlatformEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FileUtil;
import com.erp.model.sys.entity.SysDepartmentThirdEntity;
import com.erp.model.sys.vo.ThirdUnionDTO;
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
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;
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
                            return replacement; // ✅ 改为保留新的值
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
                @SuppressWarnings("unchecked")
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
    public Map<String, Object> constructBill(JSONArray formArray,
                                             List<CfgProcessFieldMapEntity> fieldMapList,
                                             List<CfgProcessValueMapEntity> valueMapList) {
        // 1. Build lookup maps
        Map<String, List<CfgProcessFieldMapEntity>> fieldMapByThirdId = createFieldMapByThirdId(fieldMapList);
        Map<String, Map<String, String>> valueMapByField = createValueMapByField(valueMapList);
        Map<String, String> defaultValueMap = createDefaultValueMap(valueMapList);

        // —— 1. 动态构建 thirdParentId → sysParentField ——
        Map<String, String> parentFieldMap = fieldMapList.stream()
                .filter(fm -> "1".equals(fm.getGroupType()))
                .collect(Collectors.toMap(
                        CfgProcessFieldMapEntity::getThirdParentId,      // key: 飞书 fieldList 控件 id
                        CfgProcessFieldMapEntity::getSysParentId,        // value: 你的原始 Map 中 list 所在的 key
                        (existing, replacement) -> replacement            // 遇到重复，保留新的
                ));

        Map<String, Object> resultMap = new HashMap<>();
        // 不再预先 new 一个 detailList；下面每个控件独立创建

        // —— 2. 处理所有表单字段 ——
        processFormFields(formArray,
                fieldMapByThirdId,
                valueMapByField,
                defaultValueMap,
                resultMap,
                parentFieldMap);

        return resultMap;
    }

    private Map<String, List<CfgProcessFieldMapEntity>> createFieldMapByThirdId(List<CfgProcessFieldMapEntity> fieldMapList) {
        return fieldMapList.stream()
                .collect(Collectors.groupingBy(CfgProcessFieldMapEntity::getThirdFieldId));
    }

    private Map<String, Map<String, String>> createValueMapByField(List<CfgProcessValueMapEntity> valueMapList) {
        return valueMapList.stream()
                .collect(Collectors.groupingBy(
                        CfgProcessValueMapEntity::getFieldMapId,
                        Collectors.toMap(
                                CfgProcessValueMapEntity::getThirdValue,
                                CfgProcessValueMapEntity::getSysValue,
                                (v1, v2) -> v1
                        )
                ));
    }

    private Map<String, String> createDefaultValueMap(List<CfgProcessValueMapEntity> valueMapList) {
        return valueMapList.stream()
                .filter(v -> "default".equals(v.getThirdValue()))
                .collect(Collectors.toMap(
                        CfgProcessValueMapEntity::getFieldMapId,
                        CfgProcessValueMapEntity::getDefaultValue
                ));
    }

    private void processFormFields(JSONArray formArray,
                                   Map<String, List<CfgProcessFieldMapEntity>> fieldMapByThirdId,
                                   Map<String, Map<String, String>> valueMapByField,
                                   Map<String, String> defaultValueMap,
                                   Map<String, Object> resultMap,
                                   Map<String, String> parentFieldMap) {
        for (int i = 0; i < formArray.size(); i++) {
            JSONObject field = formArray.getJSONObject(i);
            String fieldId   = field.getStr("id");
            String fieldType = field.getStr("type");

            if ("fieldList".equals(fieldType)) {
                // —— 动态取出 sysParentField ——
                String sysParentField = parentFieldMap.get(fieldId);
                if (sysParentField == null) {
                    throw new ServiceException("未找到 fieldList 控件 " + fieldId + " 在 fieldMapList 中的对应 parent 映射");
                }

                // 从 JSON 里取 value 数组
                JSONArray rows = field.getJSONArray("value");
                List<Map<String,Object>> listForThisControl = new ArrayList<>();

                if (rows != null) {
                    for (int r = 0; r < rows.size(); r++) {
                        JSONArray row = rows.getJSONArray(r);
                        Map<String,Object> rowMap = new HashMap<>();

                        // 遍历每个子单元格
                        for (int c = 0; c < row.size(); c++) {
                            JSONObject cell = row.getJSONObject(c);
                            String cellId   = cell.getStr("id");
                            String cellType = cell.getStr("type");
                            // 调用你原来的 processField，把结果放到 rowMap
                            processField(cell, cellId, cellType,
                                    fieldMapByThirdId, valueMapByField, defaultValueMap,
                                    rowMap);
                        }
                        listForThisControl.add(rowMap);
                    }
                }

                // —— 以 sysParentField 作为 key 存入 resultMap ——
                resultMap.put(sysParentField, listForThisControl);
            }
            else {
                // 普通字段逻辑不变
                processField(field, field.getStr("id"), fieldType,
                        fieldMapByThirdId, valueMapByField, defaultValueMap,
                        resultMap);
            }
        }
    }

    private void processField(JSONObject field,
                              String fieldId,
                              String fieldType,
                              Map<String, List<CfgProcessFieldMapEntity>> fieldMapByThirdId,
                              Map<String, Map<String, String>> valueMapByField,
                              Map<String, String> defaultValueMap,
                              Map<String, Object> resultMap) {
        List<CfgProcessFieldMapEntity> mappings = fieldMapByThirdId.get(fieldId);
        if (mappings == null) return;

        for (CfgProcessFieldMapEntity map : mappings) {
            String sysField = map.getSysField();
            String sysFieldType = map.getSysFieldType();
            Object value = field.get("value");

            // Skip null_value type
            if ("null_value".equalsIgnoreCase(sysFieldType)) {
                continue;
            }

            // Use default value for default type
            if ("default".equalsIgnoreCase(sysFieldType)) {
                resultMap.put(sysField, map.getDefaultValue());
                continue;
            }

            // Handle radio/checkbox types
            if (isRadioType(sysFieldType, fieldType) || isCheckboxType(sysFieldType, fieldType)) {
                value = isRadioType(sysFieldType, fieldType)
                        ? handleRadioField(map, field, valueMapByField, defaultValueMap)
                        : handleCheckboxField(map, field, valueMapByField, defaultValueMap);
                resultMap.put(sysField, value);
                continue;
            }

            // Handle special field types
            processSpecialFieldTypes(map, field, fieldType, sysField, resultMap);
        }
    }

    private boolean isRadioType(String sysFieldType, String fieldType) {
        return "radioV2".equalsIgnoreCase(sysFieldType) || "radioV2".equalsIgnoreCase(fieldType);
    }

    private boolean isCheckboxType(String sysFieldType, String fieldType) {
        return "checkboxV2".equalsIgnoreCase(sysFieldType) || "checkboxV2".equalsIgnoreCase(fieldType);
    }

    private void processSpecialFieldTypes(CfgProcessFieldMapEntity map,
                                          JSONObject field,
                                          String fieldType,
                                          String sysField,
                                          Map<String, Object> resultMap) {
        if ("amount".equals(fieldType)) {
            handleAmountField(map, field, resultMap);
        } else if (isFileField(fieldType)) {
            List<String> attachmentUrlList = (List<String>)resultMap.get("attachmentUrlList");
            List<String> attachmentNameList = (List<String>)resultMap.get("attachmentNameList");
            if (CollUtil.isEmpty(attachmentUrlList)){
                resultMap.put("attachmentUrlList", handleFileFieldUrl(field));
            }else {
                List<String> urlList = handleFileFieldUrl(field);
                attachmentUrlList.addAll(urlList);
                resultMap.put("attachmentUrlList",attachmentUrlList);
            }

            if (CollUtil.isEmpty(attachmentUrlList)){
                resultMap.put("attachmentNameList", handleFileFieldName(field));
            }else {
                List<String> nameList = handleFileFieldName(field);
                attachmentNameList.addAll(nameList);
                resultMap.put("attachmentUrlList",attachmentNameList);
            }
        } else if ("department".equals(fieldType)) {
            resultMap.put(sysField, handleDepartmentField(field));
        } else if ("contact".equals(fieldType)) {
            resultMap.put(sysField, handleContactField(field));
        } else if ("date".equals(fieldType)) {
            resultMap.put(sysField, handleDateField(field));
        } else {
            resultMap.put(sysField, field.get("value"));
        }
    }

    private boolean isFileField(String fieldType) {
        return "attachmentV2".equals(fieldType) || "image".equals(fieldType) || "imageV2".equals(fieldType);
    }

    private void processDetailField(JSONObject detailField,
                                    Map<String, List<CfgProcessFieldMapEntity>> fieldMapByThirdId,
                                    Map<String, Map<String, String>> valueMapByField,
                                    Map<String, String> defaultValueMap,
                                    List<Map<String, Object>> detailList) {
        JSONArray rows = detailField.getJSONArray("value");
        for (int r = 0; r < rows.size(); r++) {
            JSONArray row = rows.getJSONArray(r);
            Map<String, Object> rowMap = new HashMap<>();

            for (int c = 0; c < row.size(); c++) {
                JSONObject cell = row.getJSONObject(c);
                String cellId = cell.getStr("id");
                String cellType = cell.getStr("type");

                processField(cell, cellId, cellType, fieldMapByThirdId, valueMapByField, defaultValueMap, rowMap);
            }
            detailList.add(rowMap);
        }
    }

    private String handleRadioField(CfgProcessFieldMapEntity map,
                                    JSONObject field,
                                    Map<String, Map<String, String>> valueMapByField,
                                    Map<String, String> defaultValueMap) {
        String fieldMapId = map.getId();
        Map<String, String> valueMap = valueMapByField.get(fieldMapId);
        String selectedText = field.getStr("value");
        String optionKey = null;

        if (field.containsKey("option")) {
            JSONObject option = field.getJSONObject("option");
            if (selectedText.equals(option.getStr("text"))) {
                optionKey = option.getStr("key");
            }
        }

        return (optionKey != null && valueMap != null)
                ? valueMap.getOrDefault(optionKey, defaultValueMap.getOrDefault(fieldMapId, selectedText))
                : selectedText;
    }

    private List<String> handleCheckboxField(CfgProcessFieldMapEntity map,
                                             JSONObject field,
                                             Map<String, Map<String, String>> valueMapByField,
                                             Map<String, String> defaultValueMap) {
        String fieldMapId = map.getId();
        Map<String, String> valueMap = valueMapByField.get(fieldMapId);
        List<String> mappedValues = new ArrayList<>();
        JSONArray selectedTexts = field.getJSONArray("value");
        Map<String, String> textToKeyMap = buildTextToKeyMap(field);

        for (int j = 0; j < selectedTexts.size(); j++) {
            String text = selectedTexts.getStr(j);
            String key = textToKeyMap.get(text);

            if (key != null && valueMap != null && valueMap.containsKey(key)) {
                mappedValues.add(valueMap.get(key));
            } else {
                mappedValues.add(defaultValueMap.getOrDefault(fieldMapId, text));
            }
        }

        return mappedValues;
    }

    private Map<String, String> buildTextToKeyMap(JSONObject field) {
        Map<String, String> textToKeyMap = new HashMap<>();
        if (field.containsKey("option")) {
            JSONArray options = field.getJSONArray("option");
            for (int j = 0; j < options.size(); j++) {
                JSONObject opt = options.getJSONObject(j);
                textToKeyMap.put(opt.getStr("text"), opt.getStr("key"));
            }
        }
        return textToKeyMap;
    }

    private void handleAmountField(CfgProcessFieldMapEntity map,
                                   JSONObject field,
                                   Map<String, Object> resultMap) {
        Integer index = map.getIndex();
        if (index == 0) {
            resultMap.put(map.getSysField(), field.get("value"));
        } else if (index == 1 && field.containsKey("ext")) {
            JSONObject ext = field.getJSONObject("ext");
            resultMap.put(map.getSysField(), ext.getStr("currency"));
        }
    }


    private Object handleDateField(JSONObject field) {
        String value = field.getStr("value");
        //从RFC 3339格式转换DateTime
        if (value != null && value.contains("T")) {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(value);

            // 转换为 LocalDateTime
            LocalDateTime localDateTime = offsetDateTime.toLocalDateTime();

            // 使用包含毫秒的格式器进行格式化
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            return localDateTime.format(formatter);
        }
        return value;
    }

    private List<String> handleFileFieldUrl(JSONObject field) {
        List<String> urlList = new ArrayList<>();
        if (field.get("value") instanceof JSONArray) {
            JSONArray urlArray = field.getJSONArray("value");
            for (int i = 0; i < urlArray.size(); i++) {
                urlList.add(urlArray.getStr(i));
            }
        }
        return urlList;
    }

    private List<String> handleFileFieldName(JSONObject field) {
        String extValue = field.getStr("ext");
        List<String> extList = extValue != null ? Arrays.asList(extValue.split(",")) : Collections.emptyList();
        return extList;
    }

    private List<String> handleDepartmentField(JSONObject field) {
        List<String> openIds = new ArrayList<>();
        if (field.containsKey("value") && field.get("value") instanceof JSONArray) {
            JSONArray deptArray = field.getJSONArray("value");
            for (int i = 0; i < deptArray.size(); i++) {
                JSONObject dept = deptArray.getJSONObject(i);
                if (dept.containsKey("open_id")) {
//                    String openId = dept.getStr("open_id");
//                    SysDepartmentThirdEntity department = sysDepartmentThirdFeign.findByDepartmentId(ThirdpartyPlatformEnum.FS.getCode(), openId);
//                    openIds.add(department.getThirdDeptId());
                    openIds.add(dept.getStr("open_id"));
                }
            }
        }
        return openIds;
    }

    private List<String> handleContactField(JSONObject field) {
        //
//        List<ThirdUnionDTO> dtoList = sysUserFeign.getThirdByUserIds(ThirdpartyPlatformEnum.FS.getCode(), extractStringArray(field.getJSONArray("value")));
//        if (CollUtil.isEmpty(dtoList)){
//            throw new ServiceException("审批可见人列表人员未关联第三方用户信息:{}",field.getStr("name"));
//        }
//        return dtoList.stream().map(ThirdUnionDTO::getUserId).collect(Collectors.toList());


        if (field.containsKey("value") && field.get("value") instanceof JSONArray) {
            return extractStringArray(field.getJSONArray("value"));
        }

        if (field.containsKey("open_ids") && field.get("open_ids") instanceof JSONArray) {
            return extractStringArray(field.getJSONArray("open_ids"));
        }

        return new ArrayList<>();
    }

    private List<String> extractStringArray(JSONArray array) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            result.add(array.getStr(i));
        }
        return result;
    }

    @Override
    public DictBasicEnum getEventType() {
        return DictBasicEnum.FSAPPROVE;
    }
}
