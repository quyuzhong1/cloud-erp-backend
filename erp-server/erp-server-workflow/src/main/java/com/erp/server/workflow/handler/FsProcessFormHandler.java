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
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FileUtil;
import com.erp.model.sys.entity.SysDepartmentThirdEntity;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.workflow.dto.ApproveTaskDetailDTO;
import com.erp.model.workflow.dto.CfgProcessFieldMapDTO;
import com.erp.model.workflow.dto.CfgSystemFieldMappingDTO;
import com.erp.model.workflow.entity.CfgProcessFieldMapEntity;
import com.erp.model.workflow.entity.CfgProcessValueMapEntity;
import com.erp.model.workflow.entity.CfgSystemFieldMappingEntity;
import com.erp.model.workflow.enums.*;
import com.erp.rpc.sys.feign.SysDepartmentThirdFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.sdk.fs.service.FsService;
import com.erp.server.workflow.service.CfgQueryOptionService;
import com.erp.server.workflow.service.CfgSystemFieldMappingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
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
    @Autowired
    private CfgQueryOptionService cfgQueryOptionService;

    @Resource
    private CfgSystemFieldMappingService cfgSystemFieldMappingService;

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
                .filter(fm -> CharSequenceUtil.isNotBlank(fm.getSysParentId()))
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
        if (CollUtil.isEmpty(valueMapList)) {
            return new HashMap<>();
        }
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
                List<Map<String, Object>> rawDetail = new ArrayList<>();
                Object object = variablesMap.get(sysParentField);
                if (object instanceof List) {
                    rawDetail = (List<Map<String, Object>>) variablesMap.get(sysParentField);
                } else if (object instanceof Map) {
                    // 如果是 Map，可能是单条明细数据，转换为 List
                    rawDetail.add((Map<String, Object>) object);
                } else {
                    log.warn("数据无需处理，sysParentField = {},object = {}", sysParentField,object);
                   continue;
                }

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
        if (finalValue == null && entity.getThirdFieldRequired()) {
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
            case "datetime":
                resultField.set("value", formatToRFC3339(finalValue));
                break;

            case "amount":
                resultField.set("value", additionalValue);
                resultField.set("currency", finalValue);
                break;

            case "department":
                SysDepartmentThirdEntity fsDepartment = sysDepartmentThirdFeign.findByDepartmentId("FS", finalValue.toString());
                if (StrUtil.isEmpty(fsDepartment.getThirdOpenDeptId())) {
                    throw new ServiceException("未查询到飞书部门信息，请检查部门是否存在:{}", finalValue.toString());
                }
                JSONObject openId = new JSONObject();
                openId.set("open_id", Arrays.asList(fsDepartment.getThirdOpenDeptId()));
                openId.set("open_id", Arrays.asList(finalValue));
                resultField.set("value", Arrays.asList(openId));
                break;

            case "contact":
                List<ThirdUnionDTO> fsUser = sysUserFeign.getThirdByUserIds("FS", Arrays.asList(finalValue.toString()));
                if (CollUtil.isEmpty(fsUser) || StrUtil.isEmpty(fsUser.get(0).getThirdUserId()) && StrUtil.isEmpty(fsUser.get(0).getThirdOpenId())) {
                    throw new ServiceException("未查询到飞书用户信息，请检查用户是否存在:{}", finalValue);
                }
                if (!StrUtil.isEmpty(fsUser.get(0).getThirdUserId())) {
                    resultField.set("value", Arrays.asList(fsUser.get(0).getThirdUserId()));
                } else {
                    resultField.set("open_ids", Arrays.asList(fsUser.get(0).getThirdOpenId()));
                }
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
                        boolean delete = tempFile.delete();
                        if (!delete) {
                            log.error("附件清理失败" );
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
            DateTime hutoolDate = DateUtil.parse(dateValue.toString());

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
        //无值映射直接返回
        if (ObjUtil.isEmpty(valueMapListMap)) {
            return rawValue;
        }

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
            // 顶层控件调用，父级 ID 和父级 name 均为 null
            result.addAll(parseField(field, false, null, null));
        }
        return result;
    }

    /**
     * 递归解析单个字段
     * @param field 当前字段的 JSON 对象
     * @param isDetail 是否为明细子项
     * @param parentId 父级 ID
     * @param parentName 父级控件的 name（用于构造 thirdField）
     * @return ViewDTO 列表
     */
    private List<CfgProcessFieldMapDTO.ViewDTO> parseField(
            JSONObject field, boolean isDetail, String parentId, String parentName
    ) {
        List<CfgProcessFieldMapDTO.ViewDTO> list = new ArrayList<>();

        // 构造基础 DTO，传入 parentName
        CfgProcessFieldMapDTO.ViewDTO dto = buildBaseDTO(field, isDetail, parentId, parentName);

        String type = dto.getThirdFieldType();
        // 如果是明细列表（FIELDLIST），则递归解析其 children
        if (CfgQueryOptionFieldTypeEnum.FIELDLIST.getCode().equals(type)) {
            JSONArray children = field.getJSONArray(FsRequestBodyAttributesEnum.CHILDREN.getCode());
            // 获取当前明细控件的 name，作为所有子控件的前缀
            String currentFieldName = field.getStr(FsRequestBodyAttributesEnum.NAME.getCode());
            for (JSONObject child : children.jsonIter()) {
                // 递归调用，将当前控件的 ID 和 name 作为父级信息传入
                list.addAll(parseField(child, true, dto.getThirdFieldId(), currentFieldName));
            }
            return list;
        }
        // 非 FIELDLIST 的正常项先添加自身
        list.add(dto);

        // 如果是金额类型，再生成一个“币种”子项
        /*if (CfgQueryOptionFieldTypeEnum.AMOUNT.getCode().equals(type)) {
            list.add(createAmountField(dto));
        }*/

        return list;
    }

    /**
     * 构造一个最基础的 ViewDTO（不包含“币种”那条）
     */
    private CfgProcessFieldMapDTO.ViewDTO buildBaseDTO(
            JSONObject field, boolean isDetail, String parentId, String parentName
    ) {
        CfgProcessFieldMapDTO.ViewDTO dto = new CfgProcessFieldMapDTO.ViewDTO();

        String currentFieldName = field.getStr(FsRequestBodyAttributesEnum.NAME.getCode());
        String prefix;

        // ✨ 核心改动：根据 parentName 是否为空来决定前缀
        if (parentName != null && !parentName.isEmpty()) {
            // 如果 parentName 存在，说明是明细子控件，前缀 = "父控件name-"
            prefix = parentName + "-";
        } else {
            // 如果 parentName 不存在，说明是顶层控件，前缀 = "单据头-"
            prefix = "基础信息-";
        }
        dto.setThirdField(prefix + currentFieldName);

        dto.setThirdFieldType(field.getStr(FsRequestBodyAttributesEnum.TYPE.getCode()));
        dto.setThirdFieldRequired(field.getBool(FsRequestBodyAttributesEnum.REQUIRED.getCode(), false));
        dto.setThirdFieldId(field.getStr(FsRequestBodyAttributesEnum.ID.getCode()));
        dto.setIsDetailField(isDetail);
        dto.setThirdParentId(parentId);
        if (!"fieldList".equals(field.getStr("type"))) {
            dto.setGroupType("0");
        } else {
            dto.setGroupType("1");
        }
        // 默认 index 可不设，或由调用方根据业务设定
        return dto;
    }

    /**
     * 由一个“金额”类型的 DTO 克隆并生产对应的“币种”子项 (此方法无需改动)
     */
    private CfgProcessFieldMapDTO.ViewDTO createAmountField(
            CfgProcessFieldMapDTO.ViewDTO amountDto
    ) {
        CfgProcessFieldMapDTO.ViewDTO currencyDto = new CfgProcessFieldMapDTO.ViewDTO();
        // 复制除了 type、prefix 之外的其它公共属性
        BeanUtil.copyProperties(amountDto, currencyDto);

        // 调整子项显示文案、类型、index、cfgType
        // 因为 amountDto.getThirdField() 已经被正确设置，这里会自动拼接出正确结果
        currencyDto.setThirdField(amountDto.getThirdField() + "币种");
        currencyDto.setThirdFieldType(CfgQueryOptionFieldTypeEnum.INPUT.getCode());
        currencyDto.setIndex(1);           // 币种一般排前面
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
     * 根据映射关系生成数据MAP
     * @author will
     * @date 2025/10/16 19:12
     * @param formArray
     * @param fieldMaps
     * @param valueMaps
     * @return Map<String,Object>
     */
    public Map<String, Object> constructBill(JSONArray formArray, List<CfgProcessFieldMapEntity> fieldMaps, List<CfgProcessValueMapEntity> valueMaps,String sourceType) {
        // 第一步：将飞书结构转为控件id和值的映射
        Map<String, Object> feishuIdValueMap = convertToIdValueMap(formArray);

        //fieldMaps、按照thirdParentId分组，thirdParentId为空的使用thirdFieldId
        Map<String, List<CfgProcessFieldMapEntity>> groupedFieldMaps = fieldMaps.stream()
                .collect(Collectors.groupingBy(entity -> {
                    String key = entity.getThirdParentId();
                    return (key == null || key.isEmpty()) ? entity.getThirdFieldId() : key;
                }));

        Map<String, List<CfgProcessValueMapEntity>> collect = valueMaps.stream().collect(Collectors.groupingBy(CfgProcessValueMapEntity::getFieldMapId));

        //查询需要转换字段值的配置
        List<CfgSystemFieldMappingDTO.FieldMappingParamDTO> paramList = fieldMaps.stream().map(obj -> new CfgSystemFieldMappingDTO.FieldMappingParamDTO(CfgQueryOptionUseTypeEnum.ALL_DATA.getCode(),sourceType,obj.getSysField(), obj.getSysParentId())).collect(Collectors.toList());
        List<CfgSystemFieldMappingEntity> cfgSystemFieldMappingList = cfgSystemFieldMappingService.listSystemFieldMapping(paramList);
        Map<String, CfgSystemFieldMappingEntity> mappingMap = CollUtil.isEmpty(cfgSystemFieldMappingList) ? new HashMap<>() : cfgSystemFieldMappingList.stream().collect(Collectors.toMap(obj -> CharSequenceUtil.format("{}-{}", obj.getSysParentId(), obj.getBusinessField()), Function.identity()));

        // 第二步：根据字段映射和值映射转换为系统映射
        return processMapping(feishuIdValueMap, groupedFieldMaps, collect,mappingMap);
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
                String fieldName = field.getStr("name");
                String[] names = field.getStr("ext").split(",");
                for (int i = 0; i < names.length; i++) {
                    String fileUrl = fileArray.get(i).toString();
                    String fileName = names[i];
                    try {
                        // 1.下载第三方文件
                        byte[] fileByte = FileUtil.downloadFile(fileUrl);
                        //fileByte转为file
                        File file = new File(fileName);
                        FileUtils.writeByteArrayToFile(file, fileByte);
                        // 2.将文件上传到文件服务器
                        String uploadUrl = FastDFSClientUtil.uploadFile(file, fileName);
                        // 3.更新url
                        nameToUrl.put(CharSequenceUtil.format("{},{}",fieldName,fileName) , uploadUrl);
                    } catch (Exception e) {
                        throw new RuntimeException("文件下载处理失败"+e);
                    }
                }
                return nameToUrl;
            case "department":
                JSONArray deptValues = field.getJSONArray("value");
                //TODO 部门映射
                return deptValues.stream()
                        .map(dept -> ((JSONObject) dept).getStr("open_id"))
                        .collect(Collectors.toList()).get(0);
            case "contact":
                //TODO 用户映射
                return field.getJSONArray("value").get(0);
            case "amount":
                return field.getBigDecimal("value").toString() + "," + field.getJSONObject("ext").get("currency");
            default:
                return field.get("value");
        }
    }


    /**
     * 字段映射
     * @author will
     * @date 2025/10/16 18:28
     * @param feishuIdValueMap 飞书字段MAP
     * @param groupedFieldMaps ERP配置MAP
     * @param valueMapsByFieldMapId 选项值映射MAP
     * @return Map<String,Object>
     */
    public Map<String, Object> processMapping(
            Map<String, Object> feishuIdValueMap,
            Map<String, List<CfgProcessFieldMapEntity>> groupedFieldMaps,
            Map<String, List<CfgProcessValueMapEntity>> valueMapsByFieldMapId,
            Map<String, CfgSystemFieldMappingEntity> mappingMap) {

        Map<String, Object> finalResultMap = new HashMap<>();
        //feign接口查询结果集
        Map<String,List<JSONObject>> feginResultMap = new HashMap<>();

        //排序
        Map<String, List<CfgProcessFieldMapEntity>> readListMap = reorderMap(groupedFieldMaps);
        for (Map.Entry<String, List<CfgProcessFieldMapEntity>> entry : readListMap.entrySet()) {
            String feishuWidgetId = entry.getKey();
            List<CfgProcessFieldMapEntity> relevantFieldMaps = entry.getValue();
            //如果key值是默认值则需要循环所有的映射
            if ("default".equals(feishuWidgetId)) {
                for (CfgProcessFieldMapEntity fieldMap : relevantFieldMaps) {
                    Object object = finalResultMap.get(fieldMap.getSysParentId());
                    if (ObjUtil.isEmpty(object)) {
                        finalResultMap.put(fieldMap.getSysField(),fieldMap.getDefaultValue());
                        continue;
                    }
                    if (object instanceof Map) {
                        //将默认值加入object中
                        ((Map<String, Object>) object).put(fieldMap.getSysField(),fieldMap.getDefaultValue());
                    } else if ( object instanceof List) {
                        //将默认值加入object中
                        ((List<Map<String, Object>>) object).forEach(map -> map.put(fieldMap.getSysField(),fieldMap.getDefaultValue()));
                    } else {
                        finalResultMap.put(fieldMap.getSysField(),fieldMap.getDefaultValue());
                    }
                    //字段远程接口值转换
                    putMappingResultMap(feginResultMap,finalResultMap,mappingMap,fieldMap.getSysParentId(),fieldMap.getSysField(),Collections.singletonList(fieldMap.getDefaultValue()));
                }
                continue;
            }
            Object feiShuOriginalValue = feishuIdValueMap.get(feishuWidgetId);
            //非默认配置处理
            processDetailListFeiShuToSys(feginResultMap,finalResultMap, feiShuOriginalValue, relevantFieldMaps, valueMapsByFieldMapId,mappingMap);
        }
        return finalResultMap;
    }

    /**
     * 字段远程接口值转换
     * @author will
     * @date 2025/10/20 11:53
     * @param finalResultMap
     * @param mappingMap
     * @param sysParentId
     * @param sysField
     * @param value
     * @return void
     */
    private void putMappingResultMap(Map<String,List<JSONObject>> feginResultMap,Map<String, Object> finalResultMap,
                                     Map<String, CfgSystemFieldMappingEntity> mappingMap,String sysParentId,String sysField,List<Object> value) {
        CfgSystemFieldMappingEntity mappingEntity = mappingMap.get(CharSequenceUtil.format("{}-{}", sysParentId, sysField));
        if (ObjUtil.isEmpty(mappingEntity)) {
            return;
        }
        List<JSONObject> feignData = feginResultMap.get(CharSequenceUtil.format("{}-{}-{}", mappingEntity.getFeignPath(), mappingEntity.getFeignMethod(), mappingEntity.getFeignParam()));
        if (ObjUtil.isEmpty(feignData)) {
            feignData = cfgSystemFieldMappingService.listFeignQueryData(mappingEntity);
            if (ObjUtil.isEmpty(feignData)) {
                return;
            }
            //接口查询结果添加到fegin查询结果集中
            feginResultMap.put(CharSequenceUtil.format("{}-{}-{}", mappingEntity.getFeignPath(), mappingEntity.getFeignMethod(), mappingEntity.getFeignParam()),feignData);
        }
        String targetFieldValue = feignData.stream().filter(obj -> value.contains(ObjUtil.defaultIfNull(obj.get(mappingEntity.getSourceDisplayField()),"")))
                .map(obj -> ObjUtil.defaultIfNull(obj.get(mappingEntity.getSourceField()),"").toString())
                .collect(Collectors.joining(","));
        finalResultMap.put(mappingEntity.getTargetField(),targetFieldValue);
    }

    /**
     * 排序MAP
     * @author will
     * @date 2025/9/30 10:46
     * @param groupedFieldMaps
     * @return Map<String,List<CfgProcessFieldMapEntity>>
     */
    private Map<String, List<CfgProcessFieldMapEntity>> reorderMap(Map<String, List<CfgProcessFieldMapEntity>> groupedFieldMaps) {
        return groupedFieldMaps.entrySet().stream()
                .sorted((e1, e2) -> {
                    if ("default".equals(e1.getKey())) return 1;
                    if ("default".equals(e2.getKey())) return -1;
                    return 0;
                })
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        LinkedHashMap::putAll);
    }

    /**
     * 处理普通（非明细）字段类型的数据，将飞书原始值转换为系统目标值。
     *
     * @param currentResultMap    当前正在构建的结果 Map。
     * @param feishuOriginalValue 飞书原始值。
     * @param fieldMaps           与当前字段相关的映射规则列表。
     * @param valueMapsByFieldMapId 预处理过的值映射集。
     */
    private void processSingleField(Map<String,List<JSONObject>> feginResultMap,Map<String, Object> currentResultMap,
                                    Object feishuOriginalValue,
                                    List<CfgProcessFieldMapEntity> fieldMaps,
                                    Map<String, List<CfgProcessValueMapEntity>> valueMapsByFieldMapId,
                                    Map<String, CfgSystemFieldMappingEntity> mappingMap) {

        for (CfgProcessFieldMapEntity fieldMap : fieldMaps) {
            // 获取目标系统字段名
            String sysField = fieldMap.getSysField();
            if (sysField == null || sysField.isEmpty()) {
                continue; // 如果没有定义系统字段名，则跳过
            }

            // 映射值
            Object mappedValue = mapValue(feishuOriginalValue, fieldMap, valueMapsByFieldMapId);

            //如果已存在目标系统字段则将值取出来设置为list
            List<Object> valueList = new ArrayList<>();
            if (currentResultMap.containsKey(sysField)) {
                Object existingValue = currentResultMap.get(sysField);
                if (existingValue instanceof List) {
                    valueList = (List<Object>) existingValue;
                } else {
                    valueList.add(existingValue);
                }
                valueList.add(mappedValue);
                currentResultMap.put(sysField, valueList);
            } else {
                currentResultMap.put(sysField, mappedValue);
                valueList.add(mappedValue);
            }

            //字段远程接口值转换
            putMappingResultMap(feginResultMap,currentResultMap,mappingMap,fieldMap.getSysParentId(),fieldMap.getSysField(),valueList);
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

        //默认值直接取
        if ("default".equals(fieldMap.getThirdFieldId())) {
            return fieldMap.getDefaultValue();
        }

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
                "department".equals(fieldMap.getThirdFieldType())||
                "amount".equals(fieldMap.getSysFieldType())) {
            // 对于金额这种复合字段，如果其类型为 "amount"
            if ("amount".equals(fieldMap.getThirdFieldType()) || "amount".equals(fieldMap.getSysFieldType())) {
                // feishuOriginalValue 可能是 [value, currency] 形式的 List
                if (feishuOriginalValue instanceof List && ((List<?>) feishuOriginalValue).size() == 2) {
                    List<?> parts = (List<?>) feishuOriginalValue;
                    // 根据 fieldMap 中的 index 返回金额或币种。币种的值映射在下面通用逻辑处理。
                    return fieldMap.getIndex() == 0 ? parts.get(0) : parts.get(1);
                } else if (feishuOriginalValue instanceof String && ((String) feishuOriginalValue).contains(",")) {
                    // 也可能是 "100.00,CNY" 这种字符串形式
                    String[] parts = valueStr.split(",");
                    if (parts.length == 2) {
                        return fieldMap.getIndex() == 1 ? parts[0] : findSysValue(parts[1], valueMappings);
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
     * @param feiShuOriginalValue    飞书明细行数据（List<Map<String, Object>>，子Map的key是飞书子控件ID）。
     * @param detailFieldMaps     针对该明细列表的所有子字段的映射配置。
     * @param valueMapsByFieldMapId 值映射配置。
     */
    private void processDetailListFeiShuToSys(Map<String,List<JSONObject>> feginResultMap,Map<String, Object> finalResultMap,
                                              Object feiShuOriginalValue,
                                              List<CfgProcessFieldMapEntity> detailFieldMaps,
                                              Map<String, List<CfgProcessValueMapEntity>> valueMapsByFieldMapId,
                                              Map<String, CfgSystemFieldMappingEntity> mappingMap) {
        //1、feiShuOriginalValue为空时返回
        if (ObjUtil.isEmpty(feiShuOriginalValue)) {
            return;
        }
        // 我们通过检查 relevantFieldMaps 中第一个实体的 isDetailField 和 sysParentId 来判断
        boolean isDetailListParent = detailFieldMaps.get(0).getIsDetailField() &&
                CharSequenceUtil.isNotBlank(detailFieldMaps.get(0).getSysParentId());

        //2、当本次处理类型为集合时
        if (feiShuOriginalValue instanceof List && isDetailListParent) {
            List<Map<String, Object>> feiShuOriginalValueList = (List<Map<String, Object>>)feiShuOriginalValue;
            // 这里的 key 是 thirdFieldId，也就是飞书明细行中子控件的 ID
            Map<String, List<CfgProcessFieldMapEntity>> detailConfigsBySubWidgetId = detailFieldMaps.stream()
                    .collect(Collectors.groupingBy(CfgProcessFieldMapEntity::getThirdFieldId));

            List<Map<String, Object>> mappedRows = new ArrayList<>();

            for (Map<String, Object> feishuRow : feiShuOriginalValueList) { // 遍历飞书的每一行明细数据
                Map<String, Object> mappedRow = new HashMap<>(); // 转换后的一行系统明细数据

                for (Map.Entry<String, Object> feishuCell : feishuRow.entrySet()) {
                    String feishuSubWidgetId = feishuCell.getKey(); // 明细行中飞书子控件的 ID
                    Object feishuCellValue = feishuCell.getValue(); // 明细行中飞书子控件的值

                    // 找到子控件的字段映射配置
                    List<CfgProcessFieldMapEntity> cellFieldMaps = detailConfigsBySubWidgetId.get(feishuSubWidgetId);
                    if (cellFieldMaps != null && !cellFieldMaps.isEmpty()) {
                        // 递归调用 processSingleField 处理明细行中的每个子字段
                        // 目标 Map 是 mappedRow
                        processSingleField(feginResultMap,mappedRow, feishuCellValue, cellFieldMaps, valueMapsByFieldMapId,mappingMap);
                    }
                }
                if (!mappedRow.isEmpty()) {
                    mappedRows.add(mappedRow);
                }
            }
            // 明细列表在目标系统的字段名 (sysParentId)
            if (!mappedRows.isEmpty()) {
                String sysParentId = detailFieldMaps.get(0).getSysParentId();
                finalResultMap.put(sysParentId, mappedRows);
            }
            return;
        }
        //3、当feiShuOriginalValue为单个对象时
        processSingleField(feginResultMap,finalResultMap, feiShuOriginalValue, detailFieldMaps, valueMapsByFieldMapId,mappingMap);
    }

    public static LocalDateTime convertRFC3339ToLocalDateTime(String rfc3339Date) {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(rfc3339Date);
        return offsetDateTime.toLocalDateTime();
    }



    @Override
    public List<ApproveTaskDetailDTO.AddDTO> generatePushDetailDTO(JSONArray formArray, List<CfgProcessFieldMapEntity> fieldMapList, Map<String, Object> variablesMap,Map<String,String> optionMap) {
        // 按照 thirdParentId 或 thirdFieldId 分组字段映射
        Map<String, List<CfgProcessFieldMapEntity>> groupedFieldMaps = fieldMapList.stream()
                .collect(Collectors.groupingBy(entity -> {
                    String key = entity.getThirdParentId();
                    return (key == null || key.isEmpty()) ? entity.getThirdFieldId() : key;
                }));

        List<ApproveTaskDetailDTO.AddDTO> list = new ArrayList<>();

        for (int i = 0; i < formArray.size(); i++) {
            JSONObject jsonObject = formArray.getJSONObject(i);
            String id = jsonObject.getStr("id");
            String type = jsonObject.getStr("type");


            // 处理明细表格
            if (type.equals(CfgQueryOptionFieldTypeEnum.FIELDLIST.getCode())) {
                String name = optionMap.get(id);
                // 获取明细表格数据
                JSONArray detailValue = jsonObject.getJSONArray("value");
                if (detailValue == null || detailValue.isEmpty()) {
                    continue;
                }

                // 获取明细表格对应的系统字段
                List<CfgProcessFieldMapEntity> parentFieldMaps = groupedFieldMaps.get(id);
                if (parentFieldMaps == null || parentFieldMaps.isEmpty()) {
                    continue;
                }

                CfgProcessFieldMapEntity parentFieldMap = parentFieldMaps.get(0);
                String sysParentId = parentFieldMap.getSysParentId();
                // 获取系统明细数据
                // 2. 直接从 variablesMap 取原始 List
                List<Map<String, Object>> sysDetailList = new ArrayList<>();
                Object object = variablesMap.get(sysParentId);
                if (object instanceof List) {
                    sysDetailList = (List<Map<String, Object>>) variablesMap.get(sysParentId);
                } else if (object instanceof Map) {
                    // 如果是 Map，可能是单条明细数据，转换为 List
                    sysDetailList.add((Map<String, Object>) object);
                } else {
                    // 如果不是 List 或 Map，抛出异常或处理错误
                    log.warn("数据无需处理，sysParentId = {},object = {}", sysParentId,object);
                    continue;
                }

                // 处理每一行明细
                for (int j = 0; j < detailValue.size(); j++) {
                    JSONArray rowArray = detailValue.getJSONArray(j);

                    // 获取当前行的系统数据
                    Map<String, Object> rowSysData = (sysDetailList != null && j < sysDetailList.size()) ?
                            sysDetailList.get(j) : new HashMap<>();

                    // 处理行中的每个字段
                    for (int k = 0; k < rowArray.size(); k++) {
                        JSONObject fieldObj = rowArray.getJSONObject(k);
                        String fieldId = fieldObj.getStr("id");
                        String fieldType = fieldObj.getStr("type");

                        // 获取字段映射
                        List<CfgProcessFieldMapEntity> fieldMapEntities = fieldMapList.stream()
                                .filter(entity -> fieldId.equals(entity.getThirdFieldId()))
                                .collect(Collectors.toList());

                        if (fieldMapEntities.isEmpty()) {
                            continue;
                        }

                        // 处理字段并添加到列表
                        processDetailFieldForDTO(fieldObj, fieldType, fieldMapEntities, rowSysData, list, j , name);
                    }
                }
            } else {
                // 处理普通字段
                List<CfgProcessFieldMapEntity> cfgProcessFieldMapEntityList = groupedFieldMaps.get(id);
                if (cfgProcessFieldMapEntityList == null || cfgProcessFieldMapEntityList.isEmpty()) {
                    continue;
                }

                // 处理字段并添加到列表
                processFieldForDTO(jsonObject, type, cfgProcessFieldMapEntityList, variablesMap, list, null);
            }
        }

        return list;
    }

    /**
     * 处理明细表格中的字段并生成DTO
     */
    private void processDetailFieldForDTO(JSONObject jsonObject, String type, List<CfgProcessFieldMapEntity> fieldMapList,
                                          Map<String, Object> rowSysData, List<ApproveTaskDetailDTO.AddDTO> list, Integer rowIndex,String name) {

        String id = jsonObject.getStr("id");

        // 处理金额字段
        if (type.equals(CfgQueryOptionFieldTypeEnum.AMOUNT.getCode())) {
            String thirdCurrency = jsonObject.getStr("currency");
            BigDecimal thirdValue = jsonObject.getBigDecimal("value", BigDecimal.ZERO);

            fieldMapList.sort(Comparator.comparingInt(CfgProcessFieldMapEntity::getIndex));
            CfgProcessFieldMapEntity currencyObj = fieldMapList.get(1);
            CfgProcessFieldMapEntity thirdValueObj = fieldMapList.get(0);

            String currencyField = currencyObj.getSysField();
            String thirdValueField = thirdValueObj.getSysField();

            // 从行数据中获取系统值
            Object sysCurrencyObj = rowSysData.get(currencyField);
            Object sysValueObj = rowSysData.get(thirdValueField);

            String sysCurrency = sysCurrencyObj != null ? sysCurrencyObj.toString() : "";
            BigDecimal sysValue = (sysValueObj instanceof BigDecimal) ?
                    (BigDecimal) sysValueObj : new BigDecimal(sysValueObj != null ? sysValueObj.toString() : "0");

            // 创建货币单位DTO
            ApproveTaskDetailDTO.AddDTO currencyAddDTO = BeanUtil.copyProperties(currencyObj, ApproveTaskDetailDTO.AddDTO.class);
            currencyAddDTO.setSysFieldValue(sysCurrency);
            currencyAddDTO.setThirdFieldValue(thirdCurrency);
            currencyAddDTO.setEntityName(name);
            currencyAddDTO.setEntityCode(currencyObj.getSysParentId());
            currencyAddDTO.setIndex(rowIndex);

            // 创建金额值DTO
            ApproveTaskDetailDTO.AddDTO thirdValueAddDTO = BeanUtil.copyProperties(thirdValueObj, ApproveTaskDetailDTO.AddDTO.class);
            thirdValueAddDTO.setSysFieldValue(sysValue.toString());
            thirdValueAddDTO.setThirdFieldValue(thirdValue.toString());
            thirdValueAddDTO.setEntityName(name);
            thirdValueAddDTO.setEntityCode(thirdValueObj.getSysParentId());
            thirdValueAddDTO.setIndex(rowIndex);

            list.add(currencyAddDTO);
            list.add(thirdValueAddDTO);
            return;
        }

        // 处理部门字段
        if (type.equals(CfgQueryOptionFieldTypeEnum.DEPARTMENT.getCode())) {
            CfgProcessFieldMapEntity fieldMap = fieldMapList.get(0);
            Object value = jsonObject.getObj("value");

            // 从行数据中获取系统值
            String sysField = fieldMap.getSysField();
            Object sysValue = rowSysData.get(sysField);

            if (value == null) {
                // 创建空值DTO
                ApproveTaskDetailDTO.AddDTO emptyDTO = BeanUtil.copyProperties(fieldMap, ApproveTaskDetailDTO.AddDTO.class);
                emptyDTO.setThirdFieldValue("");
                emptyDTO.setSysFieldValue(sysValue != null ? sysValue.toString() : "");
                emptyDTO.setIndex(rowIndex);
                list.add(emptyDTO);
                return;
            }

            List<JSONObject> options = (List<JSONObject>) value;

            // 获取部门ID
            List<String> openId = new ArrayList<>();
            for (JSONObject option : options) {
                List<String> ids = (List<String>) option.getObj("open_id");
                if (ids != null && !ids.isEmpty()) {
                    openId.addAll(ids);
                }
            }

            ApproveTaskDetailDTO.AddDTO addDTO = BeanUtil.copyProperties(fieldMap, ApproveTaskDetailDTO.AddDTO.class);
            addDTO.setThirdFieldValue(openId.isEmpty() ? "" : openId.get(0));
            addDTO.setSysFieldValue(sysValue != null ? sysValue.toString() : "");
            addDTO.setEntityName(name);
            addDTO.setEntityCode(fieldMap.getSysParentId());
            addDTO.setIndex(rowIndex);

            list.add(addDTO);
            return;
        }

        // 处理联系人、附件、图片、多选等列表类型字段
        if (type.equals(CfgQueryOptionFieldTypeEnum.CONTACT.getCode()) ||
                type.equals(CfgQueryOptionFieldTypeEnum.ATTACHMENTV2.getCode()) ||
                type.equals(CfgQueryOptionFieldTypeEnum.IMAGEV2.getCode()) ||
                type.equals(CfgQueryOptionFieldTypeEnum.IMAGE.getCode()) ||
                type.equals(CfgQueryOptionFieldTypeEnum.CHECKBOXV2.getCode())) {

            CfgProcessFieldMapEntity fieldMap = fieldMapList.get(0);
            Object valueObj = jsonObject.get("value");
            List<String> values;

            if (valueObj instanceof List) {
                values = new ArrayList<>();
                for (Object item : (List<?>) valueObj) {
                    values.add(item != null ? item.toString() : "");
                }
            } else {
                values = Collections.singletonList(valueObj != null ? valueObj.toString() : "");
            }

            // 从行数据中获取系统值
            String sysField = fieldMap.getSysField();
            Object sysValue = rowSysData.get(sysField);

            ApproveTaskDetailDTO.AddDTO addDTO = BeanUtil.copyProperties(fieldMap, ApproveTaskDetailDTO.AddDTO.class);
            addDTO.setSysFieldValue(sysValue != null ? sysValue.toString() : "");
            addDTO.setThirdFieldValue(String.join(",", values));
            addDTO.setEntityName(name);
            addDTO.setEntityCode(fieldMap.getSysParentId());
            addDTO.setIndex(rowIndex);

            list.add(addDTO);
            return;
        }

        // 处理其他普通字段
        CfgProcessFieldMapEntity fieldMap = fieldMapList.get(0);
        Object thirdValue = jsonObject.get("value");

        // 从行数据中获取系统值
        String sysField = fieldMap.getSysField();
        Object sysValue = rowSysData.get(sysField);

        ApproveTaskDetailDTO.AddDTO addDTO = BeanUtil.copyProperties(fieldMap, ApproveTaskDetailDTO.AddDTO.class);
        addDTO.setSysFieldValue(sysValue != null ? sysValue.toString() : "");
        addDTO.setThirdFieldValue(thirdValue != null ? thirdValue.toString() : "");
        addDTO.setEntityName(name);
        addDTO.setEntityCode(fieldMap.getSysParentId());
        addDTO.setIndex(rowIndex);

        list.add(addDTO);
    }

    /**
     * 处理普通字段并生成DTO
     */
    private void processFieldForDTO(JSONObject jsonObject, String type, List<CfgProcessFieldMapEntity> fieldMapList,
                                    Map<String, Object> variablesMap, List<ApproveTaskDetailDTO.AddDTO> list, Integer rowIndex) {

        String id = jsonObject.getStr("id");

        // 处理金额字段
        if (type.equals(CfgQueryOptionFieldTypeEnum.AMOUNT.getCode())) {
            String thirdCurrency = jsonObject.getStr("currency");
            BigDecimal thirdValue = jsonObject.getBigDecimal("value", BigDecimal.ZERO);

            fieldMapList.sort(Comparator.comparingInt(CfgProcessFieldMapEntity::getIndex));
            CfgProcessFieldMapEntity currencyObj = fieldMapList.get(1);
            CfgProcessFieldMapEntity thirdValueObj = fieldMapList.get(0);

            String currencyField = currencyObj.getSysField();
            String thirdValueField = thirdValueObj.getSysField();

            String sysCurrency = variablesMap.get(currencyField) != null ?
                    variablesMap.get(currencyField).toString() : "";

            Object sysValueObj = variablesMap.get(thirdValueField);
            BigDecimal sysValue = (sysValueObj instanceof BigDecimal) ?
                    (BigDecimal) sysValueObj : new BigDecimal(sysValueObj != null ? sysValueObj.toString() : "0");

            // 创建货币单位DTO
            ApproveTaskDetailDTO.AddDTO currencyAddDTO = BeanUtil.copyProperties(currencyObj, ApproveTaskDetailDTO.AddDTO.class);
            currencyAddDTO.setSysFieldValue(sysCurrency);
            currencyAddDTO.setThirdFieldValue(thirdCurrency);

            // 创建金额值DTO
            ApproveTaskDetailDTO.AddDTO thirdValueAddDTO = BeanUtil.copyProperties(thirdValueObj, ApproveTaskDetailDTO.AddDTO.class);
            thirdValueAddDTO.setSysFieldValue(sysValue.toString());
            thirdValueAddDTO.setThirdFieldValue(thirdValue.toString());

            list.add(currencyAddDTO);
            list.add(thirdValueAddDTO);
            return;
        }

        // 处理部门字段
        if (type.equals(CfgQueryOptionFieldTypeEnum.DEPARTMENT.getCode())) {
            CfgProcessFieldMapEntity fieldMap = fieldMapList.get(0);
            Object value = jsonObject.getObj("value");

            // 获取系统值
            String sysField = fieldMap.getSysField();
            Object sysValue = variablesMap.get(sysField);

            if (value == null) {
                // 创建空值DTO
                ApproveTaskDetailDTO.AddDTO emptyDTO = BeanUtil.copyProperties(fieldMap, ApproveTaskDetailDTO.AddDTO.class);
                emptyDTO.setThirdFieldValue("");
                emptyDTO.setSysFieldValue(sysValue != null ? sysValue.toString() : "");
                list.add(emptyDTO);
                return;
            }

            List<JSONObject> options = (List<JSONObject>) value;

            // 获取部门ID
            List<String> openId = new ArrayList<>();
            for (JSONObject option : options) {
                List<String> ids = (List<String>) option.getObj("open_id");
                if (ids != null && !ids.isEmpty()) {
                    openId.addAll(ids);
                }
            }

            ApproveTaskDetailDTO.AddDTO addDTO = BeanUtil.copyProperties(fieldMap, ApproveTaskDetailDTO.AddDTO.class);
            addDTO.setThirdFieldValue(openId.isEmpty() ? "" : openId.get(0));
            addDTO.setSysFieldValue(sysValue != null ? sysValue.toString() : "");

            list.add(addDTO);
            return;
        }

        // 处理联系人、附件、图片、多选等列表类型字段
        if (type.equals(CfgQueryOptionFieldTypeEnum.CONTACT.getCode()) ||
                type.equals(CfgQueryOptionFieldTypeEnum.ATTACHMENTV2.getCode()) ||
                type.equals(CfgQueryOptionFieldTypeEnum.IMAGEV2.getCode()) ||
                type.equals(CfgQueryOptionFieldTypeEnum.IMAGE.getCode()) ||
                type.equals(CfgQueryOptionFieldTypeEnum.CHECKBOXV2.getCode())) {

            CfgProcessFieldMapEntity fieldMap = fieldMapList.get(0);
            Object valueObj = jsonObject.get("value");
            List<String> values;

            if (valueObj instanceof List) {
                values = new ArrayList<>();
                for (Object item : (List<?>) valueObj) {
                    values.add(item != null ? item.toString() : "");
                }
            } else {
                values = Collections.singletonList(valueObj != null ? valueObj.toString() : "");
            }

            // 获取系统值
            String sysField = fieldMap.getSysField();
            Object sysValue = variablesMap.get(sysField);

            ApproveTaskDetailDTO.AddDTO addDTO = BeanUtil.copyProperties(fieldMap, ApproveTaskDetailDTO.AddDTO.class);
            addDTO.setSysFieldValue(sysValue != null ? sysValue.toString() : "");
            addDTO.setThirdFieldValue(String.join(",", values));

            list.add(addDTO);
            return;
        }

        // 处理其他普通字段
        CfgProcessFieldMapEntity fieldMap = fieldMapList.get(0);
        Object thirdValue = jsonObject.get("value");

        // 获取系统值
        String sysField = fieldMap.getSysField();
        Object sysValue = variablesMap.get(sysField);

        ApproveTaskDetailDTO.AddDTO addDTO = BeanUtil.copyProperties(fieldMap, ApproveTaskDetailDTO.AddDTO.class);
        addDTO.setSysFieldValue(sysValue != null ? sysValue.toString() : "");
        addDTO.setThirdFieldValue(thirdValue != null ? thirdValue.toString() : "");

        list.add(addDTO);
    }

    /**
     * 拉取产生的三方生成查询记录
     * @param formArray
     * @param variablesMap
     * @param fieldMapList
     * @return
     *//*
    public List<ApproveTaskDetailDTO.AddDTO> generatePullDetailDTO(
            JSONArray formArray,
            Map<String, Object> variablesMap,
            List<CfgProcessFieldMapEntity> fieldMapList) {

        // 创建结果列表
        List<ApproveTaskDetailDTO.AddDTO> detailList = new ArrayList<>();

        // 按照 thirdFieldId 分组字段映射
        Map<String, List<CfgProcessFieldMapEntity>> fieldMapByThirdFieldId = fieldMapList.stream()
                .collect(Collectors.groupingBy(CfgProcessFieldMapEntity::getThirdFieldId));

        // 打印调试信息
        System.out.println("表单字段数量: " + formArray.size());
        System.out.println("字段映射数量: " + fieldMapList.size());

        // 处理表单中的每个字段
        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formField = formArray.getJSONObject(i);
            String fieldId = formField.getStr("id");
            String fieldType = formField.getStr("type");
            String fieldName = formField.getStr("name");

            System.out.println("处理字段: " + fieldId + " (" + fieldName + "), 类型: " + fieldType);

            // 处理明细表格
            if ("fieldList".equals(fieldType)) {
                // 获取明细表格数据
                JSONArray detailRows = formField.getJSONArray("value");
                if (detailRows == null || detailRows.isEmpty()) {
                    System.out.println("明细表格数据为空: " + fieldId);
                    continue;
                }

                // 获取子控件对应的字段映射
                Map<String, List<CfgProcessFieldMapEntity>> childFieldMaps = new HashMap<>();
                for (CfgProcessFieldMapEntity fieldMap : fieldMapList) {
                    if (fieldId.equals(fieldMap.getThirdParentId())) {
                        String childId = fieldMap.getThirdFieldId();
                        if (!childFieldMaps.containsKey(childId)) {
                            childFieldMaps.put(childId, new ArrayList<>());
                        }
                        childFieldMaps.get(childId).add(fieldMap);
                    }
                }

                if (childFieldMaps.isEmpty()) {
                    System.out.println("未找到明细表格子控件映射: " + fieldId);
                    continue;
                }

                // 获取父级ID（用于entityCode）
                String sysParentId = null;
                for (List<CfgProcessFieldMapEntity> childMaps : childFieldMaps.values()) {
                    if (!childMaps.isEmpty()) {
                        sysParentId = childMaps.get(0).getSysParentId();
                        break;
                    }
                }

                // 处理每一行明细
                for (int j = 0; j < detailRows.size(); j++) {
                    JSONArray rowFields = detailRows.getJSONArray(j);

                    // 处理行中的每个字段
                    for (int k = 0; k < rowFields.size(); k++) {
                        JSONObject detailField = rowFields.getJSONObject(k);
                        String detailFieldId = detailField.getStr("id");
                        String detailFieldType = detailField.getStr("type");

                        // 获取字段映射
                        List<CfgProcessFieldMapEntity> detailFieldMapEntities = childFieldMaps.get(detailFieldId);
                        if (detailFieldMapEntities == null || detailFieldMapEntities.isEmpty()) {
                            System.out.println("未找到子字段映射: " + detailFieldId);
                            continue;
                        }

                        // 获取系统明细数据
                        Map<String, Object> sysRowData = new HashMap<>();
                        if (CharSequenceUtil.isNotBlank(sysParentId)) {
                            Object sysDetailObj = variablesMap.get(sysParentId);
                            if (sysDetailObj instanceof List) {
                                List<Map<String, Object>> sysDetailList = (List<Map<String, Object>>) sysDetailObj;
                                if (j < sysDetailList.size()) {
                                    sysRowData = sysDetailList.get(j);
                                }
                            }
                        }

                        // 处理字段并添加到列表
                        buildFieldForDTO(detailField, detailFieldType, detailFieldMapEntities, sysRowData, detailList, j, fieldName, sysParentId);
                    }
                }
            } else {
                // 处理普通字段
                List<CfgProcessFieldMapEntity> fieldMapEntities = fieldMapByThirdFieldId.get(fieldId);
                if (fieldMapEntities == null || fieldMapEntities.isEmpty()) {
                    System.out.println("未找到字段映射: " + fieldId);
                    continue;
                }

                buildFieldForDTO(formField, fieldType, fieldMapEntities, variablesMap, detailList, null, null, null);
            }
        }

        //添加默认值
        List<CfgProcessFieldMapEntity> defaultList = fieldMapByThirdFieldId.get("default");
        if (CollUtil.isNotEmpty(defaultList)) {
            List<ApproveTaskDetailDTO.AddDTO> addDefaultList = new ArrayList<>();
            for  (CfgProcessFieldMapEntity fieldMapEntity : defaultList) {
                if (CharSequenceUtil.equals(fieldMapEntity.getSysParentId(),"main")) {
                    ApproveTaskDetailDTO.AddDTO addDTO = BeanUtil.toBean(fieldMapEntity, ApproveTaskDetailDTO.AddDTO.class);
                    addDTO.setSysFieldValue(fieldMapEntity.getDefaultValue());
                    addDTO.setEntityCode(fieldMapEntity.getSysParentId());
                    addDefaultList.add(addDTO);
                } else {
                    List<Integer> indexList = detailList.stream().filter(obj ->CharSequenceUtil.equals(obj.getEntityCode(), fieldMapEntity.getSysParentId())).map(ApproveTaskDetailDTO.AddDTO::getIndex).distinct().collect(Collectors.toList());
                    for (Integer index : indexList) {
                        ApproveTaskDetailDTO.AddDTO addDTO = BeanUtil.toBean(fieldMapEntity, ApproveTaskDetailDTO.AddDTO.class);
                        addDTO.setSysFieldValue(fieldMapEntity.getDefaultValue());
                        addDTO.setEntityCode(fieldMapEntity.getSysParentId());
                        addDTO.setIndex(index);
                        addDefaultList.add(addDTO);
                    }
                }
            }
            detailList.addAll(addDefaultList);
        }
        System.out.println("生成的DTO数量: " + detailList.size());
        return detailList;
    }

    *//**
     * 处理字段并生成DTO
     *//*
    private void buildFieldForDTO(
            JSONObject formField,
            String fieldType,
            List<CfgProcessFieldMapEntity> fieldMapEntities,
            Map<String, Object> dataMap,
            List<ApproveTaskDetailDTO.AddDTO> detailList,
            Integer rowIndex,
            String entityName,
            String entityCode) {

        String fieldId = formField.getStr("id");
        String fieldName = formField.getStr("name");

        try {

            // 处理附件和图片字段
            if ("attachmentV2".equals(fieldType) || "image".equals(fieldType)) {
                CfgProcessFieldMapEntity fieldMap = fieldMapEntities.get(0);

                // 获取系统值
                String sysField = fieldMap.getSysField();
                Object sysValue = dataMap.get(sysField);

                // 提取文件URL
                String thirdValue = "";
                try {
                    JSONArray fileValues = formField.getJSONArray("value");
                    if (fileValues != null && !fileValues.isEmpty()) {
                        thirdValue = fileValues.getStr(0);
                    }
                } catch (Exception e) {
                    System.err.println("获取附件字段值失败: " + e.getMessage());
                }

                // 创建DTO
                ApproveTaskDetailDTO.AddDTO dto = BeanUtil.copyProperties(fieldMap, ApproveTaskDetailDTO.AddDTO.class);
                dto.setSysFieldValue(sysValue != null ? sysValue.toString() : "");
                dto.setThirdFieldValue(thirdValue);
                if (rowIndex != null) {
                    dto.setIndex(rowIndex);
                    dto.setEntityName(entityName);
                    dto.setEntityCode(entityCode);
                }

                detailList.add(dto);
                System.out.println("添加附件字段DTO: " + thirdValue);
                return;
            }

            // 处理多选框字段
            if ("checkboxV2".equals(fieldType)) {
                CfgProcessFieldMapEntity fieldMap = fieldMapEntities.get(0);

                // 获取系统值
                String sysField = fieldMap.getSysField();
                Object sysValue = dataMap.get(sysField);

                // 提取选项值
                List<String> values = new ArrayList<>();
                try {
                    JSONArray checkboxValues = formField.getJSONArray("value");
                    if (checkboxValues != null) {
                        for (int i = 0; i < checkboxValues.size(); i++) {
                            values.add(checkboxValues.getStr(i));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("获取多选框字段值失败: " + e.getMessage());
                }

                // 创建DTO
                ApproveTaskDetailDTO.AddDTO dto = BeanUtil.copyProperties(fieldMap, ApproveTaskDetailDTO.AddDTO.class);
                dto.setSysFieldValue(sysValue != null ? sysValue.toString() : "");
                dto.setThirdFieldValue(String.join(",", values));
                if (rowIndex != null) {
                    dto.setIndex(rowIndex);
                    dto.setEntityName(entityName);
                    dto.setEntityCode(entityCode);
                }

                detailList.add(dto);
                System.out.println("添加多选框字段DTO: " + String.join(",", values));
                return;
            }

            // 处理单选框字段
            if ("radioV2".equals(fieldType)) {
                CfgProcessFieldMapEntity fieldMap = fieldMapEntities.get(0);
                Object radioValue = formField.get("value");

                // 获取系统值
                String sysField = fieldMap.getSysField();
                Object sysValue = dataMap.get(sysField);

                // 创建DTO
                ApproveTaskDetailDTO.AddDTO dto = BeanUtil.copyProperties(fieldMap, ApproveTaskDetailDTO.AddDTO.class);
                dto.setSysFieldValue(sysValue != null ? sysValue.toString() : "");
                dto.setThirdFieldValue(radioValue != null ? radioValue.toString() : "");
                if (rowIndex != null) {
                    dto.setIndex(rowIndex);
                    dto.setEntityName(entityName);
                    dto.setEntityCode(entityCode);
                }

                detailList.add(dto);
                System.out.println("添加单选框字段DTO: " + radioValue);
                return;
            }

            // 处理其他普通字段
            CfgProcessFieldMapEntity fieldMap = fieldMapEntities.get(0);
            Object thirdValue = formField.get("value");

            // 获取系统值
            String sysField = fieldMap.getSysField();
            Object sysValue = dataMap.get(sysField);

            // 创建DTO
            ApproveTaskDetailDTO.AddDTO dto = BeanUtil.copyProperties(fieldMap, ApproveTaskDetailDTO.AddDTO.class);
            dto.setSysFieldValue(sysValue != null ? sysValue.toString() : "");
            dto.setThirdFieldValue(thirdValue != null ? thirdValue.toString() : "");
            if (rowIndex != null) {
                dto.setIndex(rowIndex);
                dto.setEntityName(entityName);
                dto.setEntityCode(entityCode);
            }

            detailList.add(dto);
            System.out.println("添加普通字段DTO: " + thirdValue);
        } catch (Exception e) {
            System.err.println("处理字段时出错: " + fieldId + " (" + fieldName + "), 类型: " + fieldType);
            e.printStackTrace();
        }
    }*/

    @Override
    public ProcessSourcePlatformEnum getEventType() {
        return ProcessSourcePlatformEnum.FS;
    }

    /**
     * 拉取产生的三方生成查询记录
     * @param formArray 飞书表单数据
     * @param variablesMap ERP变量映射
     * @param fieldMapList 字段映射配置
     * @return 三方生成查询明细列表
     */
    public List<ApproveTaskDetailDTO.AddDTO> generatePullDetailDTO(
            JSONArray formArray,
            Map<String, Object> variablesMap,
            List<CfgProcessFieldMapEntity> fieldMapList) {

        // 创建结果列表
        List<ApproveTaskDetailDTO.AddDTO> detailList = new ArrayList<>();

        // 按照不同维度分组字段映射
        Map<String, List<CfgProcessFieldMapEntity>> fieldMapByThirdFieldId = fieldMapList.stream()
                .collect(Collectors.groupingBy(CfgProcessFieldMapEntity::getThirdFieldId));

        // 按系统字段分组，用于查找ERP未映射字段
        Map<String, List<CfgProcessFieldMapEntity>> fieldMapBySysField = fieldMapList.stream()
                .collect(Collectors.groupingBy(CfgProcessFieldMapEntity::getSysField));

        // 按父级ID分组明细字段映射
        Map<String, List<CfgProcessFieldMapEntity>> detailFieldMapByParentId = fieldMapList.stream()
                .filter(map -> map.getIsDetailField() != null && map.getIsDetailField())
                .collect(Collectors.groupingBy(CfgProcessFieldMapEntity::getThirdParentId));

        // 打印调试信息
        System.out.println("表单字段数量: " + formArray.size());
        System.out.println("字段映射数量: " + fieldMapList.size());

        // 1. 处理飞书表头字段（有映射和无映射的）
        processHeaderFields(formArray, fieldMapByThirdFieldId, variablesMap, detailList);

        // 2. 处理飞书明细字段（有映射和无映射的）
        processDetailFields(formArray, detailFieldMapByParentId, fieldMapByThirdFieldId, variablesMap, detailList);

        // 3. 处理ERP未映射到飞书的表头字段
        processUnmappedERPHeaderFields(fieldMapList, fieldMapByThirdFieldId, variablesMap, detailList);

        // 4. 处理ERP未映射到飞书的明细字段
        processUnmappedERPDetailFields(fieldMapList, formArray, variablesMap, detailList);

        // 5. 添加默认值字段
        addDefaultValueFields(fieldMapByThirdFieldId, detailList);

        // 6. 处理相同sysField的多值情况（合并为数组）
        detailList = mergeDuplicateSysFields(detailList);

        //7.对结果进行排序:主表->明细->未映射字段
        detailList = sortDetailList(detailList);
        System.out.println("最终生成的DTO数量: " + detailList.size());
        return detailList;
    }

    /**
     * 对明细列表进行排序：主表 -> 明细 -> 未映射字段
     */
    private List<ApproveTaskDetailDTO.AddDTO> sortDetailList(List<ApproveTaskDetailDTO.AddDTO> detailList) {
        return detailList.stream()
                .sorted((dto1, dto2) -> {
                    // 1. 按类型排序：主表 -> 明细 -> 未映射
                    int typeOrder1 = getTypeOrder(dto1);
                    int typeOrder2 = getTypeOrder(dto2);

                    if (typeOrder1 != typeOrder2) {
                        return Integer.compare(typeOrder1, typeOrder2);
                    }

                    // 2. 同类型内按entityCode排序
                    if (!Objects.equals(dto1.getEntityCode(), dto2.getEntityCode())) {
                        if (dto1.getEntityCode() == null) return -1;
                        if (dto2.getEntityCode() == null) return 1;
                        return dto1.getEntityCode().compareTo(dto2.getEntityCode());
                    }

                    // 3. 同entityCode内按index排序
                    Integer index1 = dto1.getIndex() != null ? dto1.getIndex() : 0;
                    Integer index2 = dto2.getIndex() != null ? dto2.getIndex() : 0;
                    if (!index1.equals(index2)) {
                        return Integer.compare(index1, index2);
                    }

                    // 4. 同index内按字段名称排序
                    String field1 = dto1.getThirdField() != null ? dto1.getThirdField() : dto1.getSysField();
                    String field2 = dto2.getThirdField() != null ? dto2.getThirdField() : dto2.getSysField();

                    if (field1 == null) return -1;
                    if (field2 == null) return 1;
                    return field1.compareTo(field2);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取DTO的类型排序值
     * 排序规则：主表(1) -> 明细(2) -> 未映射字段(3)
     */
    private int getTypeOrder(ApproveTaskDetailDTO.AddDTO dto) {
        // 判断是否为主表字段
        if ("main".equals(dto.getEntityCode())) {
            return 1;
        }

        // 判断是否为未映射字段
        boolean isUnmapped = (dto.getThirdField() != null && dto.getSysField() != null &&
                dto.getSysField().isEmpty()) ||
                (dto.getThirdField() != null && dto.getThirdField().isEmpty() &&
                        dto.getSysField() != null);

        if (isUnmapped) {
            return 3;
        }

        // 其他情况为明细字段
        return 2;
    }

    /**
     * 处理飞书表头字段（有映射和无映射的）
     */
    private void processHeaderFields(JSONArray formArray,
                                     Map<String, List<CfgProcessFieldMapEntity>> fieldMapByThirdFieldId,
                                     Map<String, Object> variablesMap,
                                     List<ApproveTaskDetailDTO.AddDTO> detailList) {

        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formField = formArray.getJSONObject(i);
            String fieldType = formField.getStr("type");

            // 跳过明细字段，后面单独处理
            if ("fieldList".equals(fieldType)) {
                continue;
            }

            String fieldId = formField.getStr("id");
            String fieldName = formField.getStr("name");

            System.out.println("处理表头字段: " + fieldId + " (" + fieldName + "), 类型: " + fieldType);

            // 获取字段映射配置
            List<CfgProcessFieldMapEntity> fieldMapEntities = fieldMapByThirdFieldId.get(fieldId);

            if (fieldMapEntities != null && !fieldMapEntities.isEmpty()) {
                // 有映射的字段 - 飞书映射到ERP的表头字段
                for (CfgProcessFieldMapEntity fieldMap : fieldMapEntities) {
                    if (fieldMap.getIsDetailField() != null && fieldMap.getIsDetailField()) {
                        continue; // 跳过明细字段映射
                    }
                    buildMappedHeaderFieldDTO(formField, fieldMap, variablesMap, detailList);
                }
            } else {
                // 无映射的字段 - 飞书未映射到ERP的表头字段
                buildUnmappedHeaderFieldDTO(formField, detailList);
            }
        }
    }

    /**
     * 处理飞书明细字段（有映射和无映射的）
     */
    private void processDetailFields(JSONArray formArray,
                                     Map<String, List<CfgProcessFieldMapEntity>> detailFieldMapByParentId,
                                     Map<String, List<CfgProcessFieldMapEntity>> fieldMapByThirdFieldId,
                                     Map<String, Object> variablesMap,
                                     List<ApproveTaskDetailDTO.AddDTO> detailList) {

        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formField = formArray.getJSONObject(i);
            String fieldType = formField.getStr("type");

            if (!"fieldList".equals(fieldType)) {
                continue;
            }

            String fieldId = formField.getStr("id");
            String fieldName = formField.getStr("name");

            System.out.println("处理明细字段: " + fieldId + " (" + fieldName + ")");

            // 获取明细数据
            JSONArray detailRows = formField.getJSONArray("value");

            // 获取该明细控件对应的字段映射
            List<CfgProcessFieldMapEntity> parentMappings = detailFieldMapByParentId.get(fieldId);
            String sysParentId = parentMappings != null && !parentMappings.isEmpty() ?
                    parentMappings.get(0).getSysParentId() : fieldId;

            // 获取ERP系统明细数据
            List<Map<String, Object>> erpDetailData = getERPDetailData(sysParentId, variablesMap);

            if (detailRows == null || detailRows.isEmpty()) {
                System.out.println("明细表格数据为空: " + fieldId);
                // 处理空明细结构
                processEmptyDetailStructure(fieldId, fieldName, parentMappings, erpDetailData, detailList, sysParentId);
                continue;
            }

            // 处理每一行明细数据 - 为每一行生成相同的index
            for (int rowIndex = 0; rowIndex < detailRows.size(); rowIndex++) {
                JSONArray rowFields = detailRows.getJSONArray(rowIndex);

                // 获取对应行的ERP数据
                Map<String, Object> erpRowData = rowIndex < erpDetailData.size() ? erpDetailData.get(rowIndex) : new HashMap<>();

                // 统一处理明细行中的所有字段，使用相同的rowIndex
                processDetailRow(rowFields, parentMappings, erpRowData,
                        detailList, rowIndex, fieldName, sysParentId);
            }

            // 处理ERP未映射到飞书的明细字段（仅处理ERP有但飞书没有的字段）
            processERPUnmappedDetailFields(parentMappings, detailRows.size(),
                    erpDetailData, detailList, fieldName, sysParentId);
        }
    }


    /**
     * 获取ERP系统明细数据
     */
    private List<Map<String, Object>> getERPDetailData(String sysParentId, Map<String, Object> variablesMap) {
        if (variablesMap == null || sysParentId == null) {
            return new ArrayList<>();
        }

        Object detailData = variablesMap.get(sysParentId);
        if (detailData instanceof List) {
            return (List<Map<String, Object>>) detailData;
        }

        return new ArrayList<>();
    }

    /**
     * 统一处理明细行中的所有字段
     */
    private void processDetailRow(JSONArray rowFields,
                                  List<CfgProcessFieldMapEntity> parentMappings,
                                  Map<String, Object> erpRowData,
                                  List<ApproveTaskDetailDTO.AddDTO> detailList,
                                  int rowIndex, String entityName, String sysParentId) {

        // 获取已映射的字段ID集合
        Set<String> mappedFieldIds = new HashSet<>();
        Map<String, List<CfgProcessFieldMapEntity>> childMappingsByFieldId = new HashMap<>();

        if (parentMappings != null && !parentMappings.isEmpty()) {
            childMappingsByFieldId = parentMappings.stream()
                    .collect(Collectors.groupingBy(CfgProcessFieldMapEntity::getThirdFieldId));
            mappedFieldIds = childMappingsByFieldId.keySet();
        }

        // 处理行中的每个字段
        for (int k = 0; k < rowFields.size(); k++) {
            JSONObject detailField = rowFields.getJSONObject(k);
            String detailFieldId = detailField.getStr("id");

            List<CfgProcessFieldMapEntity> fieldMappings = childMappingsByFieldId.get(detailFieldId);
            if (fieldMappings != null && !fieldMappings.isEmpty()) {
                // 有映射的字段 - 飞书映射到ERP的明细字段
                for (CfgProcessFieldMapEntity fieldMap : fieldMappings) {
                    buildMappedDetailFieldDTO(detailField, fieldMap, erpRowData,
                            detailList, rowIndex, entityName, sysParentId);
                }
            } else {
                // 无映射的字段 - 飞书未映射到ERP的明细字段
                buildUnmappedDetailFieldDTO(detailField, detailList, rowIndex, entityName, sysParentId);
            }
        }
    }

    /**
     * 处理ERP未映射到飞书的明细字段（ERP有但飞书没有的字段）
     */
    private void processERPUnmappedDetailFields(List<CfgProcessFieldMapEntity> parentMappings,
                                                int rowCount,
                                                List<Map<String, Object>> erpDetailData,
                                                List<ApproveTaskDetailDTO.AddDTO> detailList,
                                                String entityName, String sysParentId) {

        if (parentMappings == null || parentMappings.isEmpty()) {
            return;
        }

        // 收集飞书中实际存在的字段ID（按行分组）
        Map<Integer, Set<String>> existingFeishuFieldIdsByRow = new HashMap<>();
        for (ApproveTaskDetailDTO.AddDTO dto : detailList) {
            if (sysParentId.equals(dto.getEntityCode())) {
                Integer index = dto.getIndex();
                if (!existingFeishuFieldIdsByRow.containsKey(index)) {
                    existingFeishuFieldIdsByRow.put(index, new HashSet<>());
                }
                existingFeishuFieldIdsByRow.get(index).add(dto.getThirdField());
            }
        }

        // 为每一行生成ERP有但飞书没有的字段
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            // 获取对应行的ERP数据
            Map<String, Object> erpRowData = rowIndex < erpDetailData.size() ?
                    erpDetailData.get(rowIndex) : new HashMap<>();

            Set<String> existingFields = existingFeishuFieldIdsByRow.getOrDefault(rowIndex, new HashSet<>());

            for (CfgProcessFieldMapEntity fieldMap : parentMappings) {
                // 如果这个ERP字段在当前行的飞书中没有对应的字段，则生成未映射的DTO
                if (!existingFields.contains(fieldMap.getThirdField())) {
                    buildERPUnmappedDetailFieldDTO(fieldMap, erpRowData, detailList,
                            rowIndex, entityName, sysParentId);
                }
            }
        }
    }


    /**
     * 处理空的明细结构
     */
    private void processEmptyDetailStructure(String fieldId, String fieldName,
                                             List<CfgProcessFieldMapEntity> parentMappings,
                                             List<Map<String, Object>> erpDetailData,
                                             List<ApproveTaskDetailDTO.AddDTO> detailList,
                                             String sysParentId) {

        if (parentMappings != null && !parentMappings.isEmpty()) {
            // 为ERP映射的明细生成空行
            int rowCount = Math.max(1, erpDetailData.size());
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                Map<String, Object> erpRowData = rowIndex < erpDetailData.size() ?
                        erpDetailData.get(rowIndex) : new HashMap<>();

                for (CfgProcessFieldMapEntity fieldMap : parentMappings) {
                    buildMappedDetailFieldDTO(null, fieldMap, erpRowData, detailList,
                            rowIndex, fieldName, sysParentId);
                }
            }
        } else {
            // 如果没有映射配置，也要记录这个空的明细结构
            ApproveTaskDetailDTO.AddDTO dto = new ApproveTaskDetailDTO.AddDTO();
            dto.setThirdField(fieldName);
            dto.setThirdFieldType("fieldList");
            dto.setThirdFieldValue("");
            dto.setThirdFieldRequired(false);
            dto.setSysField("");
            dto.setSysFieldType("");
            dto.setSysFieldValue("");
            dto.setSysFieldRequired(false);
            dto.setIndex(0);
            dto.setEntityName(fieldName);
            dto.setEntityCode(sysParentId);
            detailList.add(dto);
        }
    }

    /**
     * 合并相同sysField的多值情况
     */
    private List<ApproveTaskDetailDTO.AddDTO> mergeDuplicateSysFields(List<ApproveTaskDetailDTO.AddDTO> detailList) {
        // 按entityCode + index + sysField 分组
        Map<String, List<ApproveTaskDetailDTO.AddDTO>> groupedByKey = new HashMap<>();

        for (ApproveTaskDetailDTO.AddDTO dto : detailList) {
            String key = buildGroupKey(dto);
            if (!groupedByKey.containsKey(key)) {
                groupedByKey.put(key, new ArrayList<>());
            }
            groupedByKey.get(key).add(dto);
        }

        // 合并相同key的DTO
        List<ApproveTaskDetailDTO.AddDTO> mergedList = new ArrayList<>();
        for (Map.Entry<String, List<ApproveTaskDetailDTO.AddDTO>> entry : groupedByKey.entrySet()) {
            List<ApproveTaskDetailDTO.AddDTO> dtos = entry.getValue();

            if (dtos.size() == 1) {
                // 只有一个DTO，直接添加
                mergedList.add(dtos.get(0));
            } else {
                // 多个DTO，需要合并thirdFieldValue
                ApproveTaskDetailDTO.AddDTO baseDto = dtos.get(0);

                // 收集所有的thirdFieldValue
                List<String> thirdFieldValues = new ArrayList<>();
                List<String> thirdFields = new ArrayList<>();
                List<String> thirdFieldTypes = new ArrayList<>();

                for (ApproveTaskDetailDTO.AddDTO dto : dtos) {
                    if (CharSequenceUtil.isNotBlank(dto.getThirdFieldValue())) {
                        thirdFieldValues.add(dto.getThirdFieldValue());
                        thirdFields.add(dto.getThirdField());
                        thirdFieldTypes.add(dto.getThirdFieldType());
                    }
                }

                // 合并值（使用JSON数组格式）
                if (!thirdFieldValues.isEmpty()) {
                    baseDto.setThirdFieldValue("[" + String.join(",", thirdFieldValues) + "]");
                    baseDto.setThirdField(String.join(",", thirdFields));
                    baseDto.setThirdFieldType(String.join(",", thirdFieldTypes));
                }

                mergedList.add(baseDto);
            }
        }

        return mergedList;
    }

    /**
     * 构建分组key：entityCode + index + sysField
     */
    private String buildGroupKey(ApproveTaskDetailDTO.AddDTO dto) {
        String entityCode = dto.getEntityCode() != null ? dto.getEntityCode() : "main";
        String index = dto.getIndex() != null ? String.valueOf(dto.getIndex()) : "0";
        String sysField = dto.getSysField() != null ? dto.getSysField() : "";

        // 只有非空的sysField才需要合并
        if (CharSequenceUtil.isBlank(sysField)) {
            return entityCode + "_" + index + "_" + System.identityHashCode(dto);
        }

        return entityCode + "_" + index + "_" + sysField;
    }


    /**
     * 构建有映射的明细字段DTO
     */
    private void buildMappedDetailFieldDTO(JSONObject detailField,
                                           CfgProcessFieldMapEntity fieldMap,
                                           Map<String, Object> erpRowData,
                                           List<ApproveTaskDetailDTO.AddDTO> detailList,
                                           int rowIndex, String entityName, String sysParentId) {

        ApproveTaskDetailDTO.AddDTO dto = createBaseDTO(detailField, fieldMap, erpRowData);
        dto.setIndex(rowIndex);
        dto.setEntityName(entityName);
        dto.setEntityCode(sysParentId);
        detailList.add(dto);

        if (detailField != null) {
            System.out.println("添加有映射明细字段DTO: " + detailField.getStr("name") +
                    ", 行号: " + rowIndex + ", sysField: " + fieldMap.getSysField());
        } else {
            System.out.println("添加空明细字段DTO: " + fieldMap.getSysField() + ", 行号: " + rowIndex);
        }
    }

    /**
     * 构建ERP未映射的明细字段DTO
     */
    private void buildERPUnmappedDetailFieldDTO(CfgProcessFieldMapEntity fieldMap,
                                                Map<String, Object> erpRowData,
                                                List<ApproveTaskDetailDTO.AddDTO> detailList,
                                                int rowIndex, String entityName, String sysParentId) {

        ApproveTaskDetailDTO.AddDTO dto = BeanUtil.copyProperties(fieldMap, ApproveTaskDetailDTO.AddDTO.class);
        dto.setThirdField("");
        dto.setThirdFieldType("");
        dto.setThirdFieldValue("");
        dto.setThirdFieldRequired(false);

        String sysFieldValue = getSysFieldValue(fieldMap.getSysField(), erpRowData, fieldMap.getDefaultValue());
        dto.setSysFieldValue(sysFieldValue);
        dto.setIndex(rowIndex);
        dto.setEntityName(entityName);
        dto.setEntityCode(sysParentId);

        detailList.add(dto);
        System.out.println("添加ERP未映射明细字段DTO: " + fieldMap.getSysField() + ", 行号: " + rowIndex);
    }
    /**
     * 创建基础DTO（处理字段值提取）- 明细版本
     */
    private ApproveTaskDetailDTO.AddDTO createBaseDTO(JSONObject formField,
                                                      CfgProcessFieldMapEntity fieldMap,
                                                      Map<String, Object> erpRowData) {

        ApproveTaskDetailDTO.AddDTO dto = BeanUtil.copyProperties(fieldMap, ApproveTaskDetailDTO.AddDTO.class);

        // 设置飞书字段值
        if (formField != null) {
            dto.setThirdFieldValue(extractFieldValue(formField));
        } else {
            dto.setThirdFieldValue("");
        }

        // 设置系统字段值 - 从明细行数据中获取
        String sysFieldValue = getSysFieldValue(fieldMap.getSysField(), erpRowData, fieldMap.getDefaultValue());
        dto.setSysFieldValue(sysFieldValue);

        return dto;
    }

    /**
     * 获取系统字段值 - 支持明细行数据
     */
    private String getSysFieldValue(String sysField, Map<String, Object> dataMap, String defaultValue) {
        if (dataMap != null && dataMap.containsKey(sysField)) {
            Object value = dataMap.get(sysField);
            return value != null ? value.toString() : "";
        }
        return defaultValue != null ? defaultValue : "";
    }

    /**
     * 构建无映射的飞书明细字段DTO
     */
    private void buildUnmappedDetailFieldDTO(JSONObject detailField,
                                             List<ApproveTaskDetailDTO.AddDTO> detailList,
                                             int rowIndex, String entityName, String sysParentId) {

        if (detailField == null) return;

        ApproveTaskDetailDTO.AddDTO dto = new ApproveTaskDetailDTO.AddDTO();
        dto.setThirdField(detailField.getStr("name"));
        dto.setThirdFieldType(detailField.getStr("type"));
        dto.setThirdFieldValue(extractFieldValue(detailField));
        dto.setThirdFieldRequired(false);
        dto.setSysField("");
        dto.setSysFieldType("");
        dto.setSysFieldValue("");
        dto.setSysFieldRequired(false);
        dto.setIndex(rowIndex);
        dto.setEntityName(entityName);
        dto.setEntityCode(sysParentId);

        detailList.add(dto);
        System.out.println("添加无映射明细字段DTO: " + detailField.getStr("name") + ", 行号: " + rowIndex);
    }


    /**
     * 处理ERP未映射到飞书的表头字段
     */
    private void processUnmappedERPHeaderFields(List<CfgProcessFieldMapEntity> fieldMapList,
                                                Map<String, List<CfgProcessFieldMapEntity>> fieldMapByThirdFieldId,
                                                Map<String, Object> variablesMap,
                                                List<ApproveTaskDetailDTO.AddDTO> detailList) {

        // 收集所有飞书字段ID
        Set<String> allThirdFieldIds = fieldMapByThirdFieldId.keySet();

        // 找出ERP有但飞书没有的表头字段
        for (CfgProcessFieldMapEntity fieldMap : fieldMapList) {
            // 跳过明细字段和默认值字段
            if ((fieldMap.getIsDetailField() != null && fieldMap.getIsDetailField()) ||
                    "default".equals(fieldMap.getThirdFieldId())) {
                continue;
            }

            // 如果这个系统字段没有对应的飞书字段映射
            if (!allThirdFieldIds.contains(fieldMap.getThirdFieldId())) {
                buildERPUnmappedHeaderFieldDTO(fieldMap, variablesMap, detailList);
            }
        }
    }

    /**
     * 处理ERP未映射到飞书的明细字段
     */
    private void processUnmappedERPDetailFields(List<CfgProcessFieldMapEntity> fieldMapList,
                                                JSONArray formArray,
                                                Map<String, Object> variablesMap,
                                                List<ApproveTaskDetailDTO.AddDTO> detailList) {

        // 收集所有飞书明细父级ID
        Set<String> allDetailParentIds = new HashSet<>();
        for (int i = 0; i < formArray.size(); i++) {
            JSONObject formField = formArray.getJSONObject(i);
            if ("fieldList".equals(formField.getStr("type"))) {
                allDetailParentIds.add(formField.getStr("id"));
            }
        }

        // 按系统父级ID分组ERP明细字段
        Map<String, List<CfgProcessFieldMapEntity>> erpDetailFieldsByParent = fieldMapList.stream()
                .filter(map -> map.getIsDetailField() != null && map.getIsDetailField() &&
                        map.getSysParentId() != null && !"main".equals(map.getSysParentId()))
                .collect(Collectors.groupingBy(CfgProcessFieldMapEntity::getSysParentId));

        // 处理每个ERP明细表
        for (Map.Entry<String, List<CfgProcessFieldMapEntity>> entry : erpDetailFieldsByParent.entrySet()) {
            String sysParentId = entry.getKey();
            List<CfgProcessFieldMapEntity> detailMappings = entry.getValue();

            // 检查这个ERP明细表是否有对应的飞书明细映射
            boolean hasFeishuMapping = detailMappings.stream()
                    .anyMatch(map -> map.getThirdParentId() != null &&
                            allDetailParentIds.contains(map.getThirdParentId()));

            if (!hasFeishuMapping) {
                // ERP未映射到飞书的整个明细
                buildERPUnmappedCompleteDetail(detailMappings, variablesMap, detailList, sysParentId);
            } else {
                // ERP映射到飞书的明细但部分字段未映射
                buildERPUnmappedPartialDetail(detailMappings, variablesMap, detailList, sysParentId, allDetailParentIds);
            }
        }
    }
// ========== DTO构建方法 ==========

    /**
     * 构建有映射的表头字段DTO
     */
    private void buildMappedHeaderFieldDTO(JSONObject formField,
                                           CfgProcessFieldMapEntity fieldMap,
                                           Map<String, Object> variablesMap,
                                           List<ApproveTaskDetailDTO.AddDTO> detailList) {

        ApproveTaskDetailDTO.AddDTO dto = createBaseDTO(formField, fieldMap, variablesMap);
        dto.setEntityCode("main");
        detailList.add(dto);
    }

    /**
     * 构建无映射的飞书表头字段DTO
     */
    private void buildUnmappedHeaderFieldDTO(JSONObject formField,
                                             List<ApproveTaskDetailDTO.AddDTO> detailList) {

        ApproveTaskDetailDTO.AddDTO dto = new ApproveTaskDetailDTO.AddDTO();
        dto.setThirdField(formField.getStr("name"));
        dto.setThirdFieldType(formField.getStr("type"));
        dto.setThirdFieldValue(extractFieldValue(formField));
        dto.setThirdFieldRequired(false); // 无映射时设为false
        dto.setSysField("");
        dto.setSysFieldType("");
        dto.setSysFieldValue("");
        dto.setSysFieldRequired(false);
        dto.setEntityCode("main");

        detailList.add(dto);
        System.out.println("添加无映射表头字段DTO: " + formField.getStr("name"));
    }

    /**
     * 构建ERP未映射的表头字段DTO
     */
    private void buildERPUnmappedHeaderFieldDTO(CfgProcessFieldMapEntity fieldMap,
                                                Map<String, Object> variablesMap,
                                                List<ApproveTaskDetailDTO.AddDTO> detailList) {

        ApproveTaskDetailDTO.AddDTO dto = BeanUtil.copyProperties(fieldMap, ApproveTaskDetailDTO.AddDTO.class);
        dto.setThirdField("");
        dto.setThirdFieldType("");
        dto.setThirdFieldValue("");
        dto.setThirdFieldRequired(false);

        // 设置系统字段值
        String sysFieldValue = getSysFieldValue(fieldMap.getSysField(), variablesMap, fieldMap.getDefaultValue());
        dto.setSysFieldValue(sysFieldValue);
        dto.setEntityCode("main");

        detailList.add(dto);
        System.out.println("添加ERP未映射表头字段DTO: " + fieldMap.getSysField());
    }

    /**
     * 构建ERP未映射的完整明细
     */
    private void buildERPUnmappedCompleteDetail(List<CfgProcessFieldMapEntity> detailMappings,
                                                Map<String, Object> variablesMap,
                                                List<ApproveTaskDetailDTO.AddDTO> detailList,
                                                String sysParentId) {

        // 生成一行空数据
        for (CfgProcessFieldMapEntity fieldMap : detailMappings) {
            ApproveTaskDetailDTO.AddDTO dto = BeanUtil.copyProperties(fieldMap, ApproveTaskDetailDTO.AddDTO.class);
            dto.setThirdField("");
            dto.setThirdFieldType("");
            dto.setThirdFieldValue("");
            dto.setThirdFieldRequired(false);

            String sysFieldValue = getSysFieldValue(fieldMap.getSysField(), variablesMap, fieldMap.getDefaultValue());
            dto.setSysFieldValue(sysFieldValue);
            dto.setIndex(0);
            dto.setEntityCode(sysParentId);
            dto.setEntityName("未映射明细");

            detailList.add(dto);
        }
        System.out.println("添加ERP未映射完整明细: " + sysParentId);
    }

    /**
     * 构建ERP未映射的部分明细字段
     */
    private void buildERPUnmappedPartialDetail(List<CfgProcessFieldMapEntity> detailMappings,
                                               Map<String, Object> variablesMap,
                                               List<ApproveTaskDetailDTO.AddDTO> detailList,
                                               String sysParentId,
                                               Set<String> allDetailParentIds) {

        // 这里需要根据实际情况确定行号，简化处理为第0行
        for (CfgProcessFieldMapEntity fieldMap : detailMappings) {
            // 如果这个字段没有对应的飞书父级ID映射
            if (fieldMap.getThirdParentId() == null ||
                    !allDetailParentIds.contains(fieldMap.getThirdParentId())) {

                ApproveTaskDetailDTO.AddDTO dto = BeanUtil.copyProperties(fieldMap, ApproveTaskDetailDTO.AddDTO.class);
                dto.setThirdField("");
                dto.setThirdFieldType("");
                dto.setThirdFieldValue("");
                dto.setThirdFieldRequired(false);

                String sysFieldValue = getSysFieldValue(fieldMap.getSysField(), variablesMap, fieldMap.getDefaultValue());
                dto.setSysFieldValue(sysFieldValue);
                dto.setIndex(0);
                dto.setEntityCode(sysParentId);
                dto.setEntityName("部分未映射明细");

                detailList.add(dto);
            }
        }
    }

    /**
     * 提取字段值（处理不同类型字段）
     */
    private String extractFieldValue(JSONObject formField) {
        if (formField == null) return "";

        String fieldType = formField.getStr("type");
        Object value = formField.get("value");

        if (value == null) return "";

        try {
            if ("attachmentV2".equals(fieldType) || "image".equals(fieldType)) {
                // 处理附件字段
                JSONArray fileValues = formField.getJSONArray("value");
                if (fileValues != null && !fileValues.isEmpty()) {
                    return fileValues.getStr(0);
                }
            } else if ("checkboxV2".equals(fieldType)) {
                // 处理多选框字段
                JSONArray checkboxValues = formField.getJSONArray("value");
                if (checkboxValues != null) {
                    List<String> values = new ArrayList<>();
                    for (int i = 0; i < checkboxValues.size(); i++) {
                        values.add(checkboxValues.getStr(i));
                    }
                    return String.join(",", values);
                }
            } else {
                // 处理其他字段
                return value.toString();
            }
        } catch (Exception e) {
            System.err.println("提取字段值失败: " + formField.getStr("name") + ", 类型: " + fieldType);
            e.printStackTrace();
        }

        return value.toString();
    }

    /**
     * 添加默认值字段
     */
    private void addDefaultValueFields(Map<String, List<CfgProcessFieldMapEntity>> fieldMapByThirdFieldId,
                                       List<ApproveTaskDetailDTO.AddDTO> detailList) {

        List<CfgProcessFieldMapEntity> defaultList = fieldMapByThirdFieldId.get("default");
        if (CollUtil.isNotEmpty(defaultList)) {
            List<ApproveTaskDetailDTO.AddDTO> addDefaultList = new ArrayList<>();
            for (CfgProcessFieldMapEntity fieldMapEntity : defaultList) {
                if (CharSequenceUtil.equals(fieldMapEntity.getSysParentId(), "main")) {
                    ApproveTaskDetailDTO.AddDTO addDTO = BeanUtil.toBean(fieldMapEntity, ApproveTaskDetailDTO.AddDTO.class);
                    addDTO.setSysFieldValue(fieldMapEntity.getDefaultValue());
                    addDTO.setEntityCode(fieldMapEntity.getSysParentId());
                    addDefaultList.add(addDTO);
                } else {
                    List<Integer> indexList = detailList.stream()
                            .filter(obj -> CharSequenceUtil.equals(obj.getEntityCode(), fieldMapEntity.getSysParentId()))
                            .map(ApproveTaskDetailDTO.AddDTO::getIndex)
                            .distinct()
                            .collect(Collectors.toList());
                    for (Integer index : indexList) {
                        ApproveTaskDetailDTO.AddDTO addDTO = BeanUtil.toBean(fieldMapEntity, ApproveTaskDetailDTO.AddDTO.class);
                        addDTO.setSysFieldValue(fieldMapEntity.getDefaultValue());
                        addDTO.setEntityCode(fieldMapEntity.getSysParentId());
                        addDTO.setIndex(index);
                        addDefaultList.add(addDTO);
                    }
                }
            }
            detailList.addAll(addDefaultList);
        }
    }

}