package com.erp.server.dmp.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ModuleOperateLogFieldTypeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.OperationLogUtil;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.erp.model.dmp.dto.OperateLogDTO;
import com.erp.model.dmp.entity.CfgOperateLogFieldEntity;
import com.erp.model.dmp.entity.OperateLogEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.dmp.mapper.OperateLogMapper;
import com.erp.server.dmp.service.CfgOperateLogFieldService;
import com.erp.server.dmp.service.DictBasicService;
import com.erp.server.dmp.service.OperateLogService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 操作日志表 服务实现类
 * </p>
 *
 * @author hyj
 * @since 2024-05-22
 */
@Slf4j
@Service
public class OperateLogServiceImpl extends SuperServiceImpl<OperateLogMapper, OperateLogEntity> implements OperateLogService {
//    @Autowired
//    private OperateLogService operateLogService;
//
//    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public BaseResultDTO.AddDTO add(OperateLogDTO.AddDTO addDTO) {
//        OperateLogEntity operateLogEntity = new OperateLogEntity();
//        BeanMapperUtils.copy(addDTO, operateLogEntity);
//
//        // 数据处理
//        handleData(operateLogEntity);
//
//        log.info("开始新增操作日志单");
//        boolean save = super.save(operateLogEntity);
//        if(!save) {
//            throw new ServiceException("操作日志单保存失败");
//        }
//
//        // 操作日志
//        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "操作日志单" , operateLogEntity.getId());
//        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLog(msg, null, operateLogEntity.getId(), "新增操作");
//        // TODO 新增明细（如果有明细的话）
//
//        return new BaseResultDTO.AddDTO(operateLogEntity.getId(), operateLogEntity.getId());
//    }
//
//    /**
//    * 修改
//    */
//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public Boolean update(OperateLogDTO.UpdateDTO updateDTO) {
//        OperateLogEntity old = super.getById(updateDTO.getId());
//        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "操作日志单"));
//        OperateLogEntity operateLogEntity =  BeanMapperUtils.map(OperateLogEntity.class, updateDTO);
//
//        // 数据处理
//        handleData(operateLogEntity);
//        log.info("编辑 开始修改操作日志单数据，id：【{}】", old.getId());
//        boolean save = super.updateById(operateLogEntity);
//        if(!save) {
//            throw new ServiceException("操作日志单保存失败");
//        }
//        // TODO 修改明细数据（包含增删改）（如果有明细的话）
//
//        // 记录主单操作日志
//            log.info("编辑 开始记录操作日志单日志数据，id：【{}】", operateLogEntity.getId());
//            String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), operateLogEntity.getId(), "操作日志单");
//        // TODO 此处的null需修改为日志模块类型，moduleType查看ModuleTypeEnum枚举类
//        operateLogService.addModuleOperateLogByObj(old, operateLogEntity, null, operateLogEntity.getId(), msg);
//        return Boolean.TRUE;
//    }
//
//
//    /**
//    * 新增修改处理数据
//    */
//    private void handleData(OperateLogEntity operateLogEntity) {
//    // TODO 验证数据 & 数据赋值
//    }

    @Resource
    private CfgOperateLogFieldService cfgOperateLogFieldService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SysDictFeign sysDictFeign;

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
        List<CfgOperateLogFieldEntity> fieldList = cfgOperateLogFieldService.listByClassPaths(classPaths);
        if (CollectionUtils.isEmpty(fieldList)) {
            return Boolean.TRUE;
        }
        List<OperateLogEntity> list = new LinkedList<>();
        for (Map.Entry<Pair<String, String>, Pair<String, String>> entry : operationLogMap.entrySet()) {
            //Pair<字段名称, 类路径>
            Pair<String, String> keyPair = entry.getKey();
            String field = keyPair.getKey();
            String fieldClass = keyPair.getValue();
            //Pair<旧值, 新值>
            Pair<String, String> valuePair = entry.getValue();
            CfgOperateLogFieldEntity fieldEntity = fieldList.stream().filter(obj -> obj.getField().equals(field) && obj.getClassPath().equals(fieldClass)).findAny().orElse(null);
            if (com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(fieldEntity)) {
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
//            //字典
//            if (ModuleOperateLogFieldTypeEnum.TYPE_DIST.getCode().equals(type)) {
//                valuePair = setDistValue(valuePair);
//            }
            //人员
            if (ModuleOperateLogFieldTypeEnum.TYPE_USER.getCode().equals(type)) {
                valuePair = setUserValue(valuePair);
            }
            //国家
            if (ModuleOperateLogFieldTypeEnum.TYPE_COUNTRY.getCode().equals(type)) {
                valuePair = setCountryValue(valuePair);
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
            OperateLogEntity entity = new OperateLogEntity();
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
    public Boolean addModuleOperateLogByObj(Object oldObj, Object newObj, String moduleType, String businessId, String msg) {
        return this.addModuleOperateLogByObj(oldObj, newObj, moduleType, businessId, null, msg);
    }

    @Override
    public Boolean addModuleOperateLog(String content, String moduleType, String businessId,String operation) {
        OperateLogEntity entity = new OperateLogEntity();
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
        List<OperateLogEntity> list = new ArrayList<>();
        for (Pair<String, String> pair : pairList) {
            OperateLogEntity entity = new OperateLogEntity();
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
        lambdaUpdate().in(OperateLogEntity::getBusinessId,businessIds).remove();
    }


    /**
     * 设置布尔值
     */
    private Pair<String,String> setBooleanValue (CfgOperateLogFieldEntity fieldEntity, Pair<String, String> valuePair) {
        String trueValue = "是";
        String falseValue = "否";
        String booleanValue = fieldEntity.getValue();
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
//    private Pair<String,String> setDistValue (Pair<String, String> valuePair) {
//        String  oldValue = "";
//        String  newValue = "";
//        List<DictBasicEntity> oldList = dictBasicService.listByIds(Arrays.asList(valuePair.getKey().split(",")));
//        if (CollectionUtils.isNotEmpty(oldList)) {
//            oldValue = oldList.stream().map(DictBasicEntity::getName).distinct().collect(Collectors.joining(","));
//        }
//        List<DictBasicEntity> newList = dictBasicService.listByIds(Arrays.asList(valuePair.getValue().split(",")));
//        if (CollectionUtils.isNotEmpty(newList)) {
//            newValue = newList.stream().map(DictBasicEntity::getName).distinct().collect(Collectors.joining(","));
//        }
//        return new Pair<>(oldValue,newValue);
//    }

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
     * 设置国家值
     */
    private Pair<String,String> setCountryValue (Pair<String, String> valuePair) {
        String  oldValue = "";
        String  newValue = "";
        List<DictCountryEntity> oldList = sysDictFeign.listCountryByIds(Arrays.asList(valuePair.getKey().split(",")));
        if (CollectionUtils.isNotEmpty(oldList)) {
            oldValue = oldList.stream().map(DictCountryEntity::getNameCn).distinct().collect(Collectors.joining(","));
        }
        List<DictCountryEntity> newList = sysDictFeign.listCountryByIds(Arrays.asList(valuePair.getValue().split(",")));
        if (CollectionUtils.isNotEmpty(newList)) {
            newValue = newList.stream().map(DictCountryEntity::getNameCn).distinct().collect(Collectors.joining(","));
        }
        return new Pair<>(oldValue,newValue);
    }
    /**
     * 设置枚举值
     */
    private Pair<String,String> setEnumValue (CfgOperateLogFieldEntity fieldEntity, Pair<String, String> valuePair) {
        if (StringUtils.isBlank(fieldEntity.getEnumClass())) {
            throw new ServiceException(ApiError.ERROR_9028);
        }
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
        String oldValue = handleEnumVale(valuePair.getKey(), aClass);
        String newValue = handleEnumVale(valuePair.getValue(), aClass);
        return new Pair<>(oldValue,newValue);
    }

    /**
     * @description: 处理枚举数据
     * @author Will
     * @date: 2023/11/24 18:44
     * @param object
     * @param aClass
     * @return String
     */
    private String handleEnumVale (String object,Class<?> aClass) {
        if (StrUtil.isBlank(object)) {
            return "";
        }
        List<String> resultList = new ArrayList<>();
        String[] split = object.split(",");
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
