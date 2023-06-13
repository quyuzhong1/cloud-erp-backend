package com.erp.server.scm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ModuleOperateLogFieldTypeEnum;
import com.common.business.service.SuperServiceImpl;
import com.common.business.utils.OperationLogUtil;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.EnumsUtil;
import com.erp.model.scm.dto.OperateLogDTO;
import com.erp.model.scm.entity.CfgModuleOperateLogFieldEntity;
import com.erp.model.scm.entity.DictBasicEntity;
import com.erp.model.scm.entity.ModuleOperateLogEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.scm.mapper.ModuleOperateLogMapper;
import com.erp.server.scm.service.CfgModuleOperateLogFieldService;
import com.erp.server.scm.service.CommonService;
import com.erp.server.scm.service.DictBasicService;
import com.erp.server.scm.service.ModuleOperateLogService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 日志表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-17
 */
@Service
public class ModuleOperateLogServiceImpl extends SuperServiceImpl<ModuleOperateLogMapper, ModuleOperateLogEntity> implements ModuleOperateLogService {

    @Resource
    private CfgModuleOperateLogFieldService cfgModuleOperateLogFieldService;

    @Resource
    private CommonService commonService;
    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Override
    public PagingVO<OperateLogDTO.ListDTO> paging(PagingDTO<OperateLogDTO.SearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        OperateLogDTO.SearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }


    @Override
    public Boolean addModuleOperateLogByObj(Object oldObj, Object newObj, String moduleType, String businessId, String pid, String msg) {

        Map<Pair<String, String>, Pair<String, String>> operationLogMap = OperationLogUtil.getOperationLogMap(oldObj, newObj);
        //判断是否为空
        if (CollectionUtils.isEmpty(operationLogMap)) {
            return Boolean.TRUE;
        }
        List<String> classPaths = operationLogMap.entrySet().stream().map(obj -> obj.getKey().getValue()).distinct().collect(Collectors.toList());
        List<CfgModuleOperateLogFieldEntity> fieldList = cfgModuleOperateLogFieldService.listByClassPaths(classPaths);
        if (CollectionUtils.isEmpty(fieldList)) {
            return Boolean.TRUE;
        }
        List<ModuleOperateLogEntity> list = new LinkedList<>();
        for (Map.Entry<Pair<String, String>, Pair<String, String>> entry : operationLogMap.entrySet()) {
            //Pair<字段名称, 类路径>
            Pair<String, String> keyPair = entry.getKey();
            String field = keyPair.getKey();
            String fieldClass = keyPair.getValue();
            //Pair<旧值, 新值>
            Pair<String, String> valuePair = entry.getValue();
            CfgModuleOperateLogFieldEntity fieldEntity = fieldList.stream().filter(obj -> obj.getField().equals(field) && obj.getClassPath().equals(fieldClass)).findAny().orElse(null);
            if (ObjectUtils.isEmpty(fieldEntity)) {
                continue;
            }
            String fieldName = fieldEntity.getFieldName();
            Integer type = fieldEntity.getType();
            if (ModuleOperateLogFieldTypeEnum.TYPE_YES_NO.getCode().equals(type)) {
                valuePair = setBooleanValue(fieldEntity, valuePair);
            }
            //枚举
            if (ModuleOperateLogFieldTypeEnum.TYPE_ENUM.getCode().equals(type)) {
                valuePair = setEnumValue(fieldEntity,valuePair);
            }
            //字典
            if (ModuleOperateLogFieldTypeEnum.TYPE_DIST.getCode().equals(type)) {
                valuePair = setDistValue(valuePair);
            }
            //人员
            if (ModuleOperateLogFieldTypeEnum.TYPE_USER.getCode().equals(type)) {
                valuePair = setUserValue(valuePair);
            }
            String oldValue = String.valueOf(valuePair.getKey());
            String newValue = String.valueOf(valuePair.getValue());

            if (oldValue.equals(newValue)) {
                continue;
            }
            String content;
            String concat = msg.concat("编辑了[").concat(fieldName).concat("]");
            if (StringUtils.isBlank(valuePair.getKey())) {
                content = concat.concat("由空值变更为[").concat(newValue).concat("]");
            } else {
                content = concat.concat("由[").concat(oldValue).concat("]").concat("变更为[").concat(newValue).concat("]");
            }
            ModuleOperateLogEntity entity = new ModuleOperateLogEntity();
            entity.setModuleType(moduleType)
                    .setBusinessId(businessId)
                    .setPid(pid)
                    .setOldValue(oldValue)
                    .setNewValue(newValue)
                    .setFieldName(fieldName)
                    .setContent(content)
                    .setOperation("编辑信息");
            list.add(entity);
        }
        return this.saveBatch(list);
    }

    @Override
    public Boolean addModuleOperateLog(String content, String moduleType, String businessId,String operation) {
        ModuleOperateLogEntity entity = new ModuleOperateLogEntity();
        entity.setModuleType(moduleType)
                .setBusinessId(businessId)
                .setContent(content)
                .setOperation(operation);
        return this.save(entity);
    }


    @Override
    public Boolean batchAddModuleOperateLog(String content, String moduleType, List<Pair<String, String>> pairList, String operation) {
        if (CollectionUtils.isEmpty(pairList)) {
            return Boolean.TRUE;
        }
        List<ModuleOperateLogEntity> list = new ArrayList<>();
        for (Pair<String, String> pair : pairList) {
            ModuleOperateLogEntity entity = new ModuleOperateLogEntity();
            entity.setModuleType(moduleType)
                    .setBusinessId(pair.getKey())
                    .setContent(String.format(content,pair.getValue()))
                    .setOperation(operation);
            list.add(entity);
        }
        return this.saveBatch(list);
    }

    @Override
    public void removeByBusinessIds(List<String> businessIds) {
        lambdaUpdate().in(ModuleOperateLogEntity::getBusinessId,businessIds).remove();
    }


    /**
     * 设置布尔值
     */
    private Pair<String,String> setBooleanValue (CfgModuleOperateLogFieldEntity fieldEntity,Pair<String, String> valuePair) {
        String trueValue = "是";
        String falseValue = "否";
        String booleanValue = fieldEntity.getBooleanValue();
        if (StringUtils.isNotBlank(booleanValue)) {
            String[] booleanValues = booleanValue.split("\\|");
            trueValue = booleanValues[0];
            falseValue = booleanValues[1];
        }
        //是或否
       String oldValue = Boolean.TRUE.toString().equals(valuePair.getKey()) ? trueValue : falseValue;
       String newValue = Boolean.TRUE.toString().equals(valuePair.getValue()) ? trueValue : falseValue;

        return new Pair<>(oldValue,newValue);
    }
    /**
     * 设置字典值
     */
    private Pair<String,String> setDistValue (Pair<String, String> valuePair) {
        String  oldValue = "";
        String  newValue = "";
        List<DictBasicEntity> oldList = dictBasicService.listByIds(Arrays.asList(valuePair.getKey().split(",")));
        if (CollectionUtils.isNotEmpty(oldList)) {
            oldValue = oldList.stream().map(DictBasicEntity::getName).distinct().collect(Collectors.joining(","));
        }
        List<DictBasicEntity> newList = dictBasicService.listByIds(Arrays.asList(valuePair.getValue().split(",")));
        if (CollectionUtils.isNotEmpty(newList)) {
            newValue = newList.stream().map(DictBasicEntity::getName).distinct().collect(Collectors.joining(","));
        }
        return new Pair<>(oldValue,newValue);
    }

    /**
     * 设置人员值
     */
    private Pair<String,String> setUserValue (Pair<String, String> valuePair) {
        String  oldValue = "";
        String  newValue = "";
        List<FindUserDTO> oldList = sysUserFeign.getUserListByUserIds(Arrays.asList(valuePair.getKey().split(",")));
        if (CollectionUtils.isNotEmpty(oldList)) {
            oldValue = oldList.stream().map(FindUserDTO::getUserName).distinct().collect(Collectors.joining(","));
        }
        List<FindUserDTO> newList = sysUserFeign.getUserListByUserIds(Arrays.asList(valuePair.getValue().split(",")));
        if (CollectionUtils.isNotEmpty(newList)) {
            newValue = newList.stream().map(FindUserDTO::getUserName).distinct().collect(Collectors.joining(","));
        }
        return new Pair<>(oldValue,newValue);
    }
    /**
     * 设置枚举值
     */
    private Pair<String,String> setEnumValue (CfgModuleOperateLogFieldEntity fieldEntity,Pair<String, String> valuePair) {
        if (StringUtils.isBlank(fieldEntity.getEnumClass())) {
            throw new ServiceException(ApiError.ERROR_9028);
        }
        String  oldValue = "";
        String  newValue = "";
        Class<?> aClass ;
        try {
            aClass = Class.forName(fieldEntity.getEnumClass());
        } catch (ClassNotFoundException e) {
            throw new ServiceException(ApiError.ERROR_9028);
        }
        boolean anEnum = aClass.isEnum();
        if (!anEnum) {
            throw new ServiceException(ApiError.ERROR_9028);
        }
        if (StringUtils.isNotBlank(valuePair.getKey())) {
            EnumMessage enumObject = EnumsUtil.getEnumObject(valuePair.getKey(), aClass);
            if (ObjectUtils.isNotEmpty(enumObject)) {
                oldValue = enumObject.getName();
            } else {
                oldValue = "";
            }
        }
        if (StringUtils.isNotBlank(valuePair.getValue())) {
            EnumMessage enumObject = EnumsUtil.getEnumObject(valuePair.getValue(), aClass);
            if (ObjectUtils.isNotEmpty(enumObject)) {
                newValue = enumObject.getName();
            } else {
                newValue = "";
            }
        }
        return new Pair<>(oldValue,newValue);
    }
}
