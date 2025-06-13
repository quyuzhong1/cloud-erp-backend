package com.erp.server.workflow.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.constant.EnumMessage;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.workflow.entity.CfgQueryOptionExtEntity;
import com.erp.model.workflow.enums.CfgQueryOptionExtTypeEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.workflow.mapper.CfgQueryOptionExtMapper;
import com.erp.server.workflow.service.CfgQueryOptionExtService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.workflow.service.OperateLogService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.workflow.dto.CfgQueryOptionExtDTO;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;

import javax.annotation.Resource;

import static com.lowagie.text.xml.simpleparser.EntitiesToUnicode.map;

/**
 * <p>
 * cfg_query_option拓展表 服务实现类
 * </p>
 *
 * @author jack
 * @since 2025-06-04
 */
@Slf4j
@Service
public class CfgQueryOptionExtServiceImpl extends SuperServiceImpl<CfgQueryOptionExtMapper, CfgQueryOptionExtEntity> implements CfgQueryOptionExtService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    public Map<String,String> getRemoteValues(Map<String, String> map) {
        if(null == map || map.isEmpty() || map.size() <= 0) {
            return map;
        }
        List<String> cfgQueryOptionIds = map.entrySet().stream().map(e -> e.getKey()).collect(Collectors.toList());
        List<CfgQueryOptionExtEntity> list = lambdaQuery().in(CfgQueryOptionExtEntity::getCfgQueryOptionId, cfgQueryOptionIds).list();
        if(CollUtil.isNotEmpty(list)){
            Map<String,String> result = new HashMap<>();

            Map<String, CfgQueryOptionExtEntity> cfgQueryOptionExtMap = list.stream().collect(Collectors.toMap(CfgQueryOptionExtEntity::getCfgQueryOptionId, e -> e,(o1,o2)->o1));

            for (Map.Entry<String, String> entry : map.entrySet()) {
                CfgQueryOptionExtEntity extEntity = cfgQueryOptionExtMap.getOrDefault(entry.getKey(), null);
                if(Objects.isNull(extEntity)){
                    //设置原始值
                    result.put(entry.getKey(), Objects.isNull(entry.getValue()) ? "" : entry.getValue());
                }else {
                    String cfgQueryOptionId = extEntity.getCfgQueryOptionId();
                    //设置原始值
                    result.put(cfgQueryOptionId, map.get(cfgQueryOptionId));

                    String type = extEntity.getType();
                    String classPath = extEntity.getClassPath();
                    String dataJson = extEntity.getDataJson();
                    if(StringUtils.isBlank(type)){
                        continue;
                    }
                    //文本
                    if(CfgQueryOptionExtTypeEnum.TEXT.getCode().equals(type)){
                        continue;
                    }
                    //布尔
                    //示例list:  [{"label":"已作废","value":true},{"label":"未作废","value":false}]
                    if(CfgQueryOptionExtTypeEnum.BOOL.getCode().equals(type)){
                        setValueByBool(entry, dataJson, cfgQueryOptionId, result);
                    }
                    //人员
                    if(CfgQueryOptionExtTypeEnum.USER.getCode().equals(type)){
                        setValueByUser(entry, result, cfgQueryOptionId);
                    }
                    //部门
                    if(CfgQueryOptionExtTypeEnum.DEPT.getCode().equals(type)){
                        setValueByDept(entry, result, cfgQueryOptionId);
                    }
                    //字典
                    if(CfgQueryOptionExtTypeEnum.DICT.getCode().equals(type) && StringUtils.isNotBlank(dataJson)){
                        setValueByClass(map, dataJson, cfgQueryOptionId, classPath, result);
                    }
                    //枚举
                    if(CfgQueryOptionExtTypeEnum.ENUM.getCode().equals(type) && StringUtils.isNotBlank(dataJson)){
                        setvalueByEnum(entry, dataJson, result, cfgQueryOptionId);
                    }
                    //类
                    if(CfgQueryOptionExtTypeEnum.CLASS.getCode().equals(type) && StringUtils.isNotBlank(dataJson)){
                        setValueByClass(map, dataJson, cfgQueryOptionId, classPath, result);
                    }
                }
            }
            return result;
        }else {
            return map;
        }
    }

    private static void setValueByBool(Map.Entry<String, String> entry, String dataJson, String cfgQueryOptionId, Map<String, String> result) {
        if(StringUtils.isBlank(dataJson) || Objects.equals(dataJson,"{}")) {
            String value = entry.getValue();
            if (StringUtils.isNotBlank(value)) {
                StringBuffer sb = new StringBuffer();
                for (String str : value.split(",")) {
                    if (str.equals("true")) {
                        sb.append("是");
                        sb.append(";");
                    } else {
                        sb.append("否");
                        sb.append(";");
                    }
                }
                result.put(cfgQueryOptionId, sb.toString());
            }
        }else {
            Gson gson = new Gson();
            List<CfgQueryOptionExtDTO.BooleanDTO> dtoList = gson.fromJson(dataJson, new TypeToken<List<CfgQueryOptionExtDTO.BooleanDTO>>(){}.getType());
            Map<String, String> boolMap = dtoList.stream().collect(Collectors.toMap(CfgQueryOptionExtDTO.BooleanDTO::getValue, CfgQueryOptionExtDTO.BooleanDTO::getLabel, (o1, o2) -> o1));
            String value = entry.getValue();
            if(StringUtils.isNotBlank(value)){
                StringBuffer sb = new StringBuffer();
                for (String str : value.split(",")) {
                    if(boolMap.containsKey(str)){
                        String name = boolMap.getOrDefault(str, "");
                        sb.append(name);
                        sb.append(";");
                    }
                }
                result.put(cfgQueryOptionId, sb.toString());
            }
        }
    }

    private void setValueByUser(Map.Entry<String, String> entry, Map<String, String> result, String cfgQueryOptionId) {
        List<FindUserDTO> userList = sysUserFeign.getUserListByUserIds(Arrays.asList(entry.getValue().split(",")));
        if (CollectionUtils.isNotEmpty(userList)) {
            String userName = userList.stream().map(FindUserDTO::getUserName).distinct().collect(Collectors.joining(","));
            result.put(cfgQueryOptionId, userName);
        }
    }

    private void setValueByDept(Map.Entry<String, String> entry, Map<String, String> result, String cfgQueryOptionId) {
        List<SysDepartmentEntity> deptList = sysUserFeign.listDeptByIds(Arrays.asList(entry.getValue()));
        if (CollectionUtils.isNotEmpty(deptList)) {
            String deptName = deptList.stream().map(SysDepartmentEntity::getName).distinct().collect(Collectors.joining(","));
            result.put(cfgQueryOptionId, deptName);
        }
    }

    private static void setvalueByEnum(Map.Entry<String, String> entry, String dataJson, Map<String, String> result, String cfgQueryOptionId) {
        Gson gson = new Gson();
        CfgQueryOptionExtDTO.EnumDTO enumDTO = gson.fromJson(dataJson, CfgQueryOptionExtDTO.EnumDTO.class);
        StringBuffer sb = new StringBuffer();
        ApiResult enumSelect = FeignQuery.invoke(ApiResult.class, "com.erp.server."+enumDTO.getSysClassify()+".controller.api.CommonController", "enumSelect", Arrays.asList(enumDTO.getEnumName()));
        if(Objects.nonNull(enumSelect)){
            List<Map<String,Object>> data = (List<Map<String, Object>>) enumSelect.getData();
            if(CollUtil.isNotEmpty(data)){
                for (Map<String, Object> datum : data) {
                    String code = String.valueOf(datum.get("code"));
                    String name = String.valueOf(datum.get("value"));
                    for (String str : entry.getValue().split(",")) {
                        if(code.equals(str)){
                            sb.append(name);
                            sb.append(";");
                        }
                    }
                }
            }
        }
        result.put(cfgQueryOptionId, sb.toString());
    }

    private static void setValueByClass(Map<String, String> map, String dataJson, String cfgQueryOptionId, String classPath, Map<String, String> result) {
        Gson gson = new Gson();
        CfgQueryOptionExtDTO.ClassDTO classDTO = gson.fromJson(dataJson, CfgQueryOptionExtDTO.ClassDTO.class);
        try {
            List<String> keyList = Arrays.asList(map.get(cfgQueryOptionId).split(","));
            Class<BaseEntity> clazz = (Class<BaseEntity>) Class.forName(classPath);
            List<BaseEntity> baseEntityList;
            if(StringUtils.isNotBlank(classDTO.getType())){//字典
                baseEntityList = FeignQuery.create(clazz)
                        .in(classDTO.getCondition(), keyList)
                        .eq("type",classDTO.getType())
                        .list();
            }else{
                baseEntityList = FeignQuery.create(clazz)
                        .in(classDTO.getCondition(), keyList)
                        .list();
            }
            if(CollUtil.isNotEmpty(baseEntityList)){
                Map<String, String> baseEntityMap = baseEntityList.stream()
                        .collect(Collectors.toMap(
                                entity -> String.valueOf(ReflectUtil.getFieldValue(entity, classDTO.getCondition())), // 获取 condition 字段的值作为 key
                                entity -> String.valueOf(ReflectUtil.getFieldValue(entity, classDTO.getSelect())),   // 获取 select 字段的值作为 value
                                (existing, replacement) -> existing // 如果有重复 key，可以选择保留第一个或合并
                        ));

                StringBuffer sb = new StringBuffer();
                for (String key : keyList) {
                    String orDefault = baseEntityMap.getOrDefault(key, "");
                    if(StringUtils.isNotBlank(orDefault)){
                        sb.append(orDefault);
                        sb.append(";");
                    }
                }
                if(StringUtils.isNotBlank(sb.toString())){
                    result.put(cfgQueryOptionId,sb.toString());
                }
            }
        } catch (ClassNotFoundException e) {

        }
    }

    /**
     * 设置枚举值
     */
    private String setEnumValue(String classPath, String key) {
        Class<?> aClass;
        try {
            aClass = Class.forName(classPath);
        } catch (ClassNotFoundException e) {
            return "";
        }
        boolean anEnum = aClass.isEnum();
        if (!anEnum) {
            return "";
        }
        String value = handleEnumVale(key, aClass);
        return value;
    }

    /**
     * @description: 处理枚举数据
     * @author Will
     * @date: 2023/11/24 18:44
     * @param str
     * @param aClass
     * @return String
     */
    private String handleEnumVale (String str,Class<?> aClass) {
        if (StrUtil.isBlank(str)) {
            return "";
        }
        List<String> resultList = new ArrayList<>();
        String[] split = str.split(",");
        for (String value : split) {
            EnumMessage enumObject = EnumsUtil.getEnumObject(value, aClass);
            if (ObjectUtils.isNotEmpty(enumObject)) {
                resultList.add(enumObject.getName());
            }
        }
        if (CollectionUtils.isEmpty(resultList)) {
            return "";
        }
        return resultList.stream().collect(Collectors.joining(","));
    }
}
