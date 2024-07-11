package com.erp.server.dmp.push.service;

import cn.hutool.core.text.CharSequenceUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.entity.CfgApiFieldMapValueEntity;
import com.erp.model.dmp.enums.ApiFieldTypeEnum;
import com.erp.model.dmp.enums.ApiGroupTypeEnum;
import com.erp.sdk.oms.amz.spapi.client.JSON;
import com.erp.server.dmp.service.CfgApiFieldMapService;
import com.erp.server.dmp.service.CfgApiFieldMapValueService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommonServiceImpl implements CommonService {
    @Resource
    private CfgApiFieldMapValueService cfgApiFieldMapValueService;
    @Resource
    private CfgApiFieldMapService cfgApiFieldMapService;

    @Override
    public Map<String, Object> makeApiFieldMap(Map<String, Object> map, String apiPlatformId, Integer moduleType) {
        Map<String, Object> resultMap = new HashMap<>();
        //查询配置字段
        CfgApiFieldMapDTO dto = new CfgApiFieldMapDTO();
        dto.setApiPlatformId(apiPlatformId);
        dto.setModuleType(moduleType);
        List<CfgApiFieldMapDTO> mapList = cfgApiFieldMapService.getByParams(dto);
        //未配置发送字段
        if (CollectionUtils.isEmpty(mapList)) {
            return resultMap;
        }
        List<String> fieldMapIds = mapList.stream().map(CfgApiFieldMapDTO::getId).collect(Collectors.toList());
        //查询配置的值映射
        List<CfgApiFieldMapValueEntity> cfgApiFieldMapValueList = cfgApiFieldMapValueService.listByFieldMapIds(fieldMapIds);
        //正常级别数据
        List<CfgApiFieldMapDTO> mainList = mapList.stream().filter(obj -> ApiGroupTypeEnum.NORMAL.getCode().equals(obj.getGroupType()) || ApiGroupTypeEnum.PARENT.getCode().equals(obj.getGroupType())).collect(Collectors.toList());
        //无值直接返回
        if (CollectionUtils.isEmpty(mainList)) {
            return resultMap;
        }
        //给常规参数填充数据
        resultMap = getResultMap(map, mainList, cfgApiFieldMapValueList, mapList);
        return resultMap;
    }

    private static Map<String, Object> getResultMap(Map<String, Object> map, List<CfgApiFieldMapDTO> mainList, List<CfgApiFieldMapValueEntity> cfgApiFieldMapValueList, List<CfgApiFieldMapDTO> mapList) {
        Map<String, Object> resultMap = new HashMap<>();
        for (CfgApiFieldMapDTO cfgApiFieldMapDTO : mainList) {
            //常规参数填充数据
            if (ApiGroupTypeEnum.NORMAL.getCode().equals(cfgApiFieldMapDTO.getGroupType())) {
                formatObject(cfgApiFieldMapDTO, resultMap, map, cfgApiFieldMapValueList);
                continue;
            }
            //集合项填充数据
            if (ApiGroupTypeEnum.PARENT.getCode().equals(cfgApiFieldMapDTO.getGroupType())) {
                handleDetail(cfgApiFieldMapDTO, mapList, map, cfgApiFieldMapValueList, resultMap);
            }
        }
        return resultMap;
    }

    private static void handleDetail(CfgApiFieldMapDTO parentMap, List<CfgApiFieldMapDTO> mapList, Map<String, Object> map, List<CfgApiFieldMapValueEntity> cfgApiFieldMapValueList, Map<String, Object> resultMap) {
        CfgApiFieldMapDTO cfgApiFieldMapDTO = mapList.stream().filter(obj -> obj.getId().equals(parentMap.getId())).findFirst().orElse(null);
        if (ObjectUtils.isEmpty(cfgApiFieldMapDTO)) {
            return;
        }
        String apiField = cfgApiFieldMapDTO.getApiField();
        List<Map<String, Object>> list = JSONObject.parseObject(JSON.toJsonStr(map.get(cfgApiFieldMapDTO.getSelfField())), new TypeReference<List<Map<String, Object>>>() {
        });
        //集合子项参数配置
        List<CfgApiFieldMapDTO> childList = mapList.stream().filter(obj -> obj.getParentId().equals(cfgApiFieldMapDTO.getId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(childList)) {
            return;
        }
        List<Map<String, Object>> dataList = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            Map<String, Object> result = new HashMap<>();
            for (CfgApiFieldMapDTO child : childList) {
                //下级明细处理集合数据
                if (ApiGroupTypeEnum.PARENT.getCode().equals(child.getGroupType())) {
                    handleDetail(child, mapList, new HashMap<>(), cfgApiFieldMapValueList, result);
                }
                //下级明细数据填充
                formatObject(child, result, new HashMap<>(), cfgApiFieldMapValueList);
            }
            dataList.add(result);
        }else {
            for (Map<String, Object> result : list) {
                Map<String, Object> resultData = new HashMap<>();
                for (CfgApiFieldMapDTO child : childList) {
                    //下级明细处理集合数据
                    if (ApiGroupTypeEnum.PARENT.getCode().equals(child.getGroupType())) {
                        handleDetail(child, mapList, result, cfgApiFieldMapValueList, resultData);
                    }
                    //下级明细数据填充
                    formatObject(child, resultData, result, cfgApiFieldMapValueList);
                }
                dataList.add(resultData);
            }
        }
        //给集合填充数据
        resultMap.put(apiField, dataList);
    }

    private static void formatObject(CfgApiFieldMapDTO cfgApiFieldMapDTO, Map<String, Object> resultMap, Map<String, Object> map, List<CfgApiFieldMapValueEntity> cfgApiFieldMapValueList) {
        //无本身字段时取默认值
        if (StringUtils.isBlank(cfgApiFieldMapDTO.getSelfField())) {
            resultMap.put(cfgApiFieldMapDTO.getApiField(), cfgApiFieldMapDTO.getDefaultValue());
            return;
        }
        //直接复制值
        if (ApiFieldTypeEnum.FIELD_VALUE_COPY.getCode().equals(cfgApiFieldMapDTO.getFieldType())) {
            Object value = map.get(cfgApiFieldMapDTO.getSelfField());
            String format = "";
            //当传入的值是空时取默认
            if (ObjectUtils.isEmpty(value) || StringUtils.isBlank(String.valueOf(value))) {
                value = cfgApiFieldMapDTO.getDefaultValue();
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
            if (cfgApiFieldMapDTO.getApiField().contains(".")) {
                String[] split = cfgApiFieldMapDTO.getApiField().split("\\.");
                Map<String, Object> o = (Map<String, Object>) map.get(split[0]);
                buildObject(resultMap, cfgApiFieldMapDTO.getApiField(), o.get(cfgApiFieldMapDTO.getSelfField()));
            }else {
                resultMap.put(cfgApiFieldMapDTO.getApiField(), CharSequenceUtil.isNotBlank(format) ? format : value);
            }
            return;
        }
        if (ApiFieldTypeEnum.FIELD_VALUE_MAP.getCode().equals(cfgApiFieldMapDTO.getFieldType())) {
            //无值映射则直接返回
            if (CollectionUtils.isEmpty(cfgApiFieldMapValueList)) {
                return;
            }
            if (cfgApiFieldMapDTO.getApiField().contains(".")) {
                String[] split = cfgApiFieldMapDTO.getApiField().split("\\.");
                Map<String, Object> o = (Map<String, Object>) map.get(split[0]);
                String apiValue = cfgApiFieldMapValueList.stream()
                        .filter(obj -> obj.getFieldMapId().equals(cfgApiFieldMapDTO.getId()) && obj.getSelfValue().equals(String.valueOf(o.get(cfgApiFieldMapDTO.getSelfField()))))
                        .map(CfgApiFieldMapValueEntity::getApiValue)
                        .findFirst()
                        .orElse("");
                buildObject(resultMap, cfgApiFieldMapDTO.getApiField(), apiValue);
            }else {
                //根据值映射转换
                String apiValue = cfgApiFieldMapValueList.stream()
                        .filter(obj -> obj.getFieldMapId().equals(cfgApiFieldMapDTO.getId()) && obj.getSelfValue().equals(String.valueOf(map.get(cfgApiFieldMapDTO.getSelfField()))))
                        .map(CfgApiFieldMapValueEntity::getApiValue)
                        .findFirst()
                        .orElse("");
                resultMap.put(cfgApiFieldMapDTO.getApiField(), apiValue);
            }

        }
    }

    private static void buildObject(Map<String, Object> resultMap, String apiField, Object apiValue) {
        String[] split = apiField.split("\\.");
        if (split.length > 1) {
            Map<String, Object> map = new HashMap<>();
            if (ObjectUtils.isNotEmpty(resultMap.get(split[0]))) {
                map = (Map<String, Object>) resultMap.get(split[0]);
            }
            map.put(split[1], apiValue);
            resultMap.put(split[0], map);
        }
    }

    @Override
    public List<Map<String, Object>> makeApiFieldList(List<Map<String, Object>> list, String apiPlatformId, Integer moduleType) {
        List<Map<String, Object>> resultList = new ArrayList<>();
        //查询配置字段
        CfgApiFieldMapDTO dto = new CfgApiFieldMapDTO();
        dto.setApiPlatformId(apiPlatformId);
        dto.setModuleType(moduleType);
        List<CfgApiFieldMapDTO> mapList = cfgApiFieldMapService.getByParams(dto);
        //未配置发送字段
        if (CollectionUtils.isEmpty(mapList)) {
            return resultList;
        }
        List<String> fieldMapIds = mapList.stream().map(CfgApiFieldMapDTO::getId).collect(Collectors.toList());
        //查询配置的值映射
        List<CfgApiFieldMapValueEntity> cfgApiFieldMapValueList = cfgApiFieldMapValueService.listByFieldMapIds(fieldMapIds);
        //正常级别数据
        List<CfgApiFieldMapDTO> mainList = mapList.stream().filter(obj -> ApiGroupTypeEnum.NORMAL.getCode().equals(obj.getGroupType()) || ApiGroupTypeEnum.PARENT.getCode().equals(obj.getGroupType())).collect(Collectors.toList());
        //无值直接返回
        if (CollectionUtils.isEmpty(mainList)) {
            return resultList;
        }
        for (Map<String, Object> map : list) {
            Map<String, Object> resultMap = getResultMap(map, mainList, cfgApiFieldMapValueList, mapList);
            list.add(resultMap);
        }
        return list;
    }

}
