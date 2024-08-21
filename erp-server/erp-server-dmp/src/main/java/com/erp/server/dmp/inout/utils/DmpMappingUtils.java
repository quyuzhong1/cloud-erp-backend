package com.erp.server.dmp.inout.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.entity.DmpCfgOutputConvertMappingEntity;
import com.erp.model.dmp.entity.DmpCfgOutputConvertValueEntity;
import com.erp.model.dmp.enums.ApiFieldTypeEnum;
import com.erp.model.dmp.enums.ApiGroupTypeEnum;
import com.erp.sdk.third.kingdee.utils.KingdeeUtils;
import com.erp.server.dmp.service.DmpCfgOutputConvertMappingService;
import com.erp.server.dmp.service.DmpCfgOutputConvertValueService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DmpMappingUtils {

    @Resource
    private DmpCfgOutputConvertMappingService dmpCfgOutputConvertMappingService;

    @Resource
    private DmpCfgOutputConvertValueService dmpCfgOutputConvertValueService;

    /**
     *
     * @param map
     * @param outputId dmp_cfg_output表id
     * @param moduleType
     * @return
     */
    public JSONObject dmpApiFieldJson(Map<String, Object> map, String outputId) {

        JSONObject json = new JSONObject(new LinkedHashMap());
        //查询配置字段
        List<DmpCfgOutputConvertMappingEntity> mapList = dmpCfgOutputConvertMappingService.lambdaQuery().eq(DmpCfgOutputConvertMappingEntity::getMainId, outputId).list();

        //未配置发送字段
        if (CollectionUtils.isEmpty(mapList)) {
            return json;
        }

        List<String> fieldMapIds = mapList.stream().map(DmpCfgOutputConvertMappingEntity::getId).collect(Collectors.toList());
        //查询配置的值映射
        List<DmpCfgOutputConvertValueEntity> dmpCfgOutputConvertValueList = dmpCfgOutputConvertValueService.lambdaQuery().in(DmpCfgOutputConvertValueEntity::getMainId, fieldMapIds).list();

        //正常级别数据
        List<DmpCfgOutputConvertMappingEntity> mainList = mapList.stream().filter(obj -> ApiGroupTypeEnum.NORMAL.getCode().equals(obj.getGroupType()) || ApiGroupTypeEnum.PARENT.getCode().equals(obj.getGroupType())).collect(Collectors.toList());


        //无值直接返回
        if (CollectionUtils.isEmpty(mainList)) {
            return json;
        }
        //给常规参数填充数据
        for (DmpCfgOutputConvertMappingEntity dmpCfgOutputConvertMappingEntity : mainList) {
            //常规参数填充数据
            if (ApiGroupTypeEnum.NORMAL.getCode().equals(dmpCfgOutputConvertMappingEntity.getGroupType())) {
                formatJsonObject(dmpCfgOutputConvertMappingEntity, json, map, dmpCfgOutputConvertValueList);
                continue;
            }
            //集合项填充数据
            if (ApiGroupTypeEnum.PARENT.getCode().equals(dmpCfgOutputConvertMappingEntity.getGroupType())) {
                handleJsonDetail(dmpCfgOutputConvertMappingEntity, mapList, map, dmpCfgOutputConvertValueList, json);
            }
        }
        return json;
    }

    /**
     * 填充数据
     */
    private void formatJsonObject(DmpCfgOutputConvertMappingEntity mappingEntity, JSONObject json, Map<String, Object> map, List<DmpCfgOutputConvertValueEntity> cfgApiFieldMapValueList) {
        //无本身字段时取默认值
        if (StringUtils.isBlank(mappingEntity.getOriginalKey())) {
            KingdeeUtils.makeFieldJson(json, mappingEntity.getConvertKey(), ".", mappingEntity.getDefaultValue());
            return;
        }
        //直接复制值
        if (ApiFieldTypeEnum.FIELD_VALUE_COPY.getCode().equals(mappingEntity.getFieldType())) {

            String[] split = mappingEntity.getOriginalKey().split("\\.");
            Object value = null;
            if (split.length > 1) {
                value = getValueFromMap(map, split);
            } else {
                value = map.get(mappingEntity.getOriginalKey());
            }

            //当传入的值是空时取默认
            if (ObjectUtils.isEmpty(value) || StringUtils.isBlank(String.valueOf(value))) {
                value = mappingEntity.getDefaultValue();
            }
            String format = "";

            //当传入的值是空时取默认
            if (ObjectUtils.isEmpty(value) || StringUtils.isBlank(String.valueOf(value))) {
                value = mappingEntity.getDefaultValue();
            }
            if (value instanceof LocalDateTime) {
                LocalDateTime value1 = (LocalDateTime) value;
                format = value1.format(DateTimeFormatter.ofPattern(DateUtil.fmt));
            }
            if (value instanceof LocalDate) {
                LocalDate value1 = (LocalDate) value;
                format = value1.format(DateTimeFormatter.ofPattern(DateUtil.fmt_day));
            }
            if (value instanceof LocalTime) {
                LocalTime value1 = (LocalTime) value;
                format = value1.format(DateTimeFormatter.ofPattern(DateUtil.fmt_hms));
            }
            KingdeeUtils.makeFieldJson(json, mappingEntity.getConvertKey(), ".", StrUtil.isNotBlank(format) ? format : value);
            return;
        }
        if (ApiFieldTypeEnum.FIELD_VALUE_MAP.getCode().equals(mappingEntity.getFieldType())) {
            //无值映射则直接返回
            if (CollectionUtils.isEmpty(cfgApiFieldMapValueList)) {
                return;
            }
            //根据值映射转换
            Map<String, Object> finalMap = map;
            String apiValue = cfgApiFieldMapValueList.stream()
                    .filter(obj -> obj.getMainId().equals(mappingEntity.getId()) && obj.getConvertBeforeValue().equals(String.valueOf(finalMap.get(mappingEntity.getOriginalKey()))))
                    .map(DmpCfgOutputConvertValueEntity::getConvertAfterValue)
                    .findFirst()
                    .orElse("");
            KingdeeUtils.makeFieldJson(json, mappingEntity.getConvertKey(), ".", apiValue);
        }
    }

    private static Object getValueFromMap(Object data, String[] keys) {
        Object value = data;
        for (int i = 0; i < keys.length; i++) {
            if (value instanceof Map) {
                value = ((Map<String, Object>) value).get(keys[i]);
            }else if (value != null) {
                try {
                    // 使用反射获取字段值
                    Field field = value.getClass().getDeclaredField(keys[i]);
                    field.setAccessible(true);
                    value = field.get(value);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    return null; // 如果字段不存在或无法访问，返回null
                }
            } else {
                return null; // 如果路径不正确，返回null
            }
        }
        return value;
    }

    /**
     * @param parentMap               1级（数据结果为集合）数据
     * @param mapList
     * @param map                     来源数据值
     * @param cfgApiFieldMapValueList 值映射数据
     * @param json                    当前级别json
     * @description:
     * @author Will
     * @date: 2023/5/30 11:35
     */
    private void handleJsonDetail(DmpCfgOutputConvertMappingEntity mappingEntity, List<DmpCfgOutputConvertMappingEntity> mapList, Map<String, Object> map, List<DmpCfgOutputConvertValueEntity> cfgApiFieldMapValueList, JSONObject json) {
        if (ObjectUtils.isEmpty(mappingEntity)) {
            return;
        }
        DmpCfgOutputConvertMappingEntity cfgApiFieldMapDTO = mapList.stream().filter(obj -> obj.getId().equals(mappingEntity.getId())).findFirst().orElse(null);

        String apiField = cfgApiFieldMapDTO.getConvertKey();
        //业务系统传参
        JSONArray JsonArray = JSONUtil.parseArray(JSONUtil.toJsonStr(map.get(cfgApiFieldMapDTO.getOriginalKey())));
        List<Map<String, Object>> listMap = JsonArray.stream().map(BeanUtil::beanToMap).collect(Collectors.toList());
        //集合子项参数配置
        List<DmpCfgOutputConvertMappingEntity> childList = mapList.stream().filter(obj -> obj.getParentId().equals(cfgApiFieldMapDTO.getId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(childList)) {
            return;
        }
        //json集合
        JSONArray jsonArray = new JSONArray();
        if (CollectionUtils.isNotEmpty(listMap)) {
            for (Map<String, Object> fieldMap : listMap) {
                JSONObject detailJson = new JSONObject(new LinkedHashMap());
                //给集合填充数据
                for (DmpCfgOutputConvertMappingEntity child : childList) {

                    //下级明细处理集合数据
                    if (ApiGroupTypeEnum.PARENT.getCode().equals(child.getGroupType())) {
                        handleJsonDetail(child, mapList, fieldMap, cfgApiFieldMapValueList, detailJson);
                    }
                    //下级明细数据填充
                    formatJsonObject(child, detailJson, fieldMap, cfgApiFieldMapValueList);
                }
                jsonArray.add(detailJson);
            }
        }
        KingdeeUtils.makeFieldJson(json, apiField, ".", jsonArray);
    }
}
