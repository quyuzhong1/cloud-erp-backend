package com.erp.server.oms.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ModuleOperateLogFieldTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.OperationLogUtil;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.EnumsUtil;
import com.erp.model.oms.dto.CustomerCreditApplyDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.dto.OperateLogDTO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.wms.entity.OverseasTransferWarehouseEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.mapper.OperateLogMapper;
import com.erp.server.oms.service.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * <p>
 * 操作日志表 服务实现类
 * </p>
 *
 * @author will
 * @since 2023-05-08
 */
@Service
public class OperateLogServiceImpl extends SuperServiceImpl<OperateLogMapper, OperateLogEntity> implements OperateLogService {


    @Resource
    private CfgOperateLogFieldService cfgOperateLogFieldService;

    @Resource
    private BankAccountService bankAccountService;


    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private CustomerInfoService customerInfoService;


    @Override
    public PagingVO<OperateLogDTO.ListDTO> paging(PagingDTO<OperateLogDTO.SearchDTO> dto) {
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        OperateLogDTO.SearchDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }



    @Override
    public List<String> getContentByObj(Object oldObj, Object newObj,String msg) {

        List<String> resultList =  new ArrayList<>();
        Map<Pair<String, String>, Pair<String, String>> operationLogMap = OperationLogUtil.getOperationLogMap(oldObj, newObj);
        //判断是否为空
        if (CollectionUtils.isEmpty(operationLogMap)) {
            return resultList;
        }
        List<String> classPaths = operationLogMap.entrySet().stream().map(obj -> obj.getKey().getValue()).distinct().collect(Collectors.toList());
        List<CfgOperateLogFieldEntity> fieldList = cfgOperateLogFieldService.listByClassPaths(classPaths);
        if (CollectionUtils.isEmpty(fieldList)) {
            return resultList;
        }
        List<OperateLogEntity> list = new LinkedList<>();
        for (Map.Entry<Pair<String, String>, Pair<String, String>> entry : operationLogMap.entrySet()) {
            //Pair<字段名称, 类路径>
            Pair<String, String> keyPair = entry.getKey();
            //去掉属性里的数字
            String field = this.removeDigits(keyPair.getKey());
            String fieldClass = keyPair.getValue();
            //Pair<旧值, 新值>
            Pair<String, String> valuePair = entry.getValue();
            CfgOperateLogFieldEntity fieldEntity = fieldList.stream().filter(obj -> obj.getField().equals(field) && obj.getClassPath().equals(fieldClass)).findAny().orElse(null);
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
                valuePair = setDistValue(valuePair,fieldEntity.getValue());
            }
            //人员
            if (ModuleOperateLogFieldTypeEnum.TYPE_USER.getCode().equals(type)) {
                valuePair = setUserValue(valuePair);
            }
            //部门
            if (ModuleOperateLogFieldTypeEnum.TYPE_DEPT.getCode().equals(type)) {
                valuePair = setDeptValue(valuePair);
            }
            //国家
            if (ModuleOperateLogFieldTypeEnum.TYPE_COUNTRY.getCode().equals(type)) {
                valuePair = setCountryValue(valuePair);
            }
            //城市
            if (ModuleOperateLogFieldTypeEnum.TYPE_CITY.getCode().equals(type)) {
                valuePair = setCityValue(valuePair);
            }
            //客户
            if (ModuleOperateLogFieldTypeEnum.TYPE_CUSTOMER.getCode().equals(type)) {
                valuePair = setCustomerValue(valuePair);
            }
            //币别
            if (ModuleOperateLogFieldTypeEnum.TYPE_CURRENCY.getCode().equals(type)) {
                valuePair = setCurrencyValue(valuePair);
            }
            //组织
            if (ModuleOperateLogFieldTypeEnum.TYPE_ORG.getCode().equals(type)) {
                valuePair = setOrg(valuePair);
            }
            //账户
            if (ModuleOperateLogFieldTypeEnum.TYPE_ACCOUNT.getCode().equals(type)) {
                valuePair = setAccount(valuePair);
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
            resultList.add(content);
        }
        return resultList;
    }
    /**
     * 根据销售订单明细查询最新记录
     * @param soIds
     * @param operation
     * @return
     */
    @Override
    public List<OperateLogEntity> listLastLogBySoIds(List<String> soIds, String operation) {
        return baseMapper.listLastLogBySoIds(soIds, operation);
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
            //去掉属性里的数字
            String field = this.removeDigits(keyPair.getKey());
            String fieldClass = keyPair.getValue();
            //Pair<旧值, 新值>
            Pair<String, String> valuePair = entry.getValue();
            CfgOperateLogFieldEntity fieldEntity = fieldList.stream().filter(obj -> obj.getField().equals(field) && obj.getClassPath().equals(fieldClass)).findAny().orElse(null);
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
                valuePair = setDistValue(valuePair,fieldEntity.getValue());
            }
            //人员
            if (ModuleOperateLogFieldTypeEnum.TYPE_USER.getCode().equals(type)) {
                valuePair = setUserValue(valuePair);
            }
            //部门
            if (ModuleOperateLogFieldTypeEnum.TYPE_DEPT.getCode().equals(type)) {
                valuePair = setDeptValue(valuePair);
            }
            //国家
            if (ModuleOperateLogFieldTypeEnum.TYPE_COUNTRY.getCode().equals(type)) {
                valuePair = setCountryValue(valuePair);
            }
            //城市
            if (ModuleOperateLogFieldTypeEnum.TYPE_CITY.getCode().equals(type)) {
                valuePair = setCityValue(valuePair);
            }
            //客户
            if (ModuleOperateLogFieldTypeEnum.TYPE_CUSTOMER.getCode().equals(type)) {
                valuePair = setCustomerValue(valuePair);
            }
            //币别
            if (ModuleOperateLogFieldTypeEnum.TYPE_CURRENCY.getCode().equals(type)) {
                valuePair = setCurrencyValue(valuePair);
            }
            //组织
            if (ModuleOperateLogFieldTypeEnum.TYPE_ORG.getCode().equals(type)) {
                valuePair = setOrg(valuePair);
            }
            //账户
            if (ModuleOperateLogFieldTypeEnum.TYPE_ACCOUNT.getCode().equals(type)) {
                valuePair = setAccount(valuePair);
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

        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        return this.saveBatch(list);
    }

    @Override
    public Boolean addModuleOperateLogByObj(Object oldObj, Object newObj, String moduleType, String businessId, String msg) {
        return this.addModuleOperateLogByObj(oldObj, newObj, moduleType, businessId, null, msg);
    }

    @Override
    public Boolean addModuleOperateLogByObj(Object oldObj, Object newObj, String moduleType, String businessId, String pid, String msg, String operation) {

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
            //去掉属性里的数字
            String field = this.removeDigits(keyPair.getKey());
            String fieldClass = keyPair.getValue();
            //Pair<旧值, 新值>
            Pair<String, String> valuePair = entry.getValue();
            CfgOperateLogFieldEntity fieldEntity = fieldList.stream().filter(obj -> obj.getField().equals(field) && obj.getClassPath().equals(fieldClass)).findAny().orElse(null);
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
                valuePair = setDistValue(valuePair,fieldEntity.getValue());
            }
            //人员
            if (ModuleOperateLogFieldTypeEnum.TYPE_USER.getCode().equals(type)) {
                valuePair = setUserValue(valuePair);
            }
            //部门
            if (ModuleOperateLogFieldTypeEnum.TYPE_DEPT.getCode().equals(type)) {
                valuePair = setDeptValue(valuePair);
            }
            //国家
            if (ModuleOperateLogFieldTypeEnum.TYPE_COUNTRY.getCode().equals(type)) {
                valuePair = setCountryValue(valuePair);
            }
            //城市
            if (ModuleOperateLogFieldTypeEnum.TYPE_CITY.getCode().equals(type)) {
                valuePair = setCityValue(valuePair);
            }
            //客户
            if (ModuleOperateLogFieldTypeEnum.TYPE_CUSTOMER.getCode().equals(type)) {
                valuePair = setCustomerValue(valuePair);
            }
            //币别
            if (ModuleOperateLogFieldTypeEnum.TYPE_CURRENCY.getCode().equals(type)) {
                valuePair = setCurrencyValue(valuePair);
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
                    .setOperation(operation);
            list.add(entity);
        }
        return this.saveBatch(list);
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
    public Boolean addModuleOperateLogBySystem(String content, String moduleType, String businessId,String operation) {
        try {
            UserContext.setIsUserSystem(true);
            return this.addModuleOperateLog(content,moduleType,businessId,operation);
        }finally {
            UserContext.clearIsUserSystem();
        }
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
    public void batchAddModuleOperateLog(List<OperateLogDTO.AddModuleOperateLogDTO> operateLogList) {
        if (CollectionUtils.isNotEmpty(operateLogList)) {
            List<OperateLogEntity> addList = new ArrayList<>(operateLogList.size());
            for (OperateLogDTO.AddModuleOperateLogDTO item : operateLogList) {
                OperateLogEntity entity = new OperateLogEntity();
                entity.setModuleType(item.getModuleType())
                        .setBusinessId(item.getBusinessId())
                        .setContent(item.getContent())
                        .setOperation(item.getOperation());
                addList.add(entity);
            }
            this.saveBatch(addList);

        }

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
    private Pair<String,String> setDistValue (Pair<String, String> valuePair,String value) {
        String  oldValue = "";
        String  newValue = "";
        DictBasicEntity oldEntity = dictBasicService.getByTypeAndValue(value, valuePair.getKey());
        if (ObjectUtils.isNotEmpty(oldEntity)) {
            oldValue = oldEntity.getName();
        }
        DictBasicEntity newEntity = dictBasicService.getByTypeAndValue(value, valuePair.getValue());
        if (ObjectUtils.isNotEmpty(newEntity)) {
            newValue = newEntity.getName();
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
     * 设置部门值
     */
    private Pair<String,String> setDeptValue (Pair<String, String> valuePair) {
        String  oldValue = "";
        String  newValue = "";
        List<SysDepartmentEntity> deptList = sysUserFeign.listDeptByIds(Arrays.asList(valuePair.getKey(), valuePair.getValue()));
        if (CollectionUtils.isNotEmpty(deptList)) {
            oldValue = deptList.stream().filter(obj -> obj.getId().equals(valuePair.getKey())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            newValue = deptList.stream().filter(obj -> obj.getId().equals(valuePair.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        }
        return new Pair<>(oldValue,newValue);
    }

    /**
     * 设置国家值
     */
    private Pair<String,String> setCountryValue (Pair<String, String> valuePair) {
        String  oldValue = "";
        String  newValue = "";
        List<DictCountryDTO.ListDTO> countryList = sysUserFeign.countryList();
        if (CollectionUtils.isNotEmpty(countryList)) {
            oldValue = countryList.stream().filter(obj -> obj.getId().equals(valuePair.getKey())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
            newValue = countryList.stream().filter(obj -> obj.getId().equals(valuePair.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
        }
        return new Pair<>(oldValue,newValue);
    }

    /**
     * 设置城市值
     */
    private Pair<String,String> setCityValue (Pair<String, String> valuePair) {
        String  oldValue = "";
        String  newValue = "";
        List<DictCityEntity> cityList = sysUserFeign.listCityByIds(Arrays.asList(valuePair.getKey(), valuePair.getValue()));
        if (CollectionUtils.isNotEmpty(cityList)) {
            oldValue = cityList.stream().filter(obj -> obj.getId().equals(valuePair.getKey())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            newValue = cityList.stream().filter(obj -> obj.getId().equals(valuePair.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        }
        return new Pair<>(oldValue,newValue);
    }

    /**
     * 设置客户值
     */
    private Pair<String,String> setCustomerValue (Pair<String, String> valuePair) {
        String  oldValue = "";
        String  newValue = "";
        List<CustomerInfoEntity> oldList = customerInfoService.listByIds(Arrays.asList(valuePair.getKey().split(",")));
        if (CollectionUtils.isNotEmpty(oldList)) {
            oldValue = oldList.stream().map(CustomerInfoEntity::getName).distinct().collect(Collectors.joining(","));
        }
        List<CustomerInfoEntity> newList = customerInfoService.listByIds(Arrays.asList(valuePair.getValue().split(",")));
        if (CollectionUtils.isNotEmpty(newList)) {
            newValue = newList.stream().map(CustomerInfoEntity::getName).distinct().collect(Collectors.joining(","));
        }
        return new Pair<>(oldValue,newValue);
    }

    /**
     * 设置币别值
     */
    private Pair<String,String> setCurrencyValue (Pair<String, String> valuePair) {
        String  oldValue = "";
        String  newValue = "";
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Arrays.asList(valuePair.getKey(), valuePair.getValue()));
        if (CollectionUtils.isNotEmpty(currencyList)) {
            oldValue = currencyList.stream().filter(obj -> obj.getId().equals(valuePair.getKey())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            newValue = currencyList.stream().filter(obj -> obj.getId().equals(valuePair.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        }
        return new Pair<>(oldValue,newValue);
    }
    /**
     * 组织
     */
    private Pair<String,String> setOrg (Pair<String, String> valuePair) {
        String  oldValue = "";
        String  newValue = "";
        List<String> orgIds = Arrays.asList(valuePair.getKey(), valuePair.getValue());
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIds);
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            oldValue = accountingCompanyList.stream().filter(obj -> obj.getId().equals(valuePair.getKey())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            newValue = accountingCompanyList.stream().filter(obj -> obj.getId().equals(valuePair.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
        }
        return new Pair<>(oldValue,newValue);
    }

    /**
     * 账户
     */
    private Pair<String,String> setAccount (Pair<String, String> valuePair) {
        String  oldValue = "";
        String  newValue = "";
        List<String> orgIds = Arrays.asList(valuePair.getKey(), valuePair.getValue());
        List<BankAccountEntity> bankAccountEntityList = bankAccountService.listByIds(orgIds);
        if (CollectionUtils.isNotEmpty(bankAccountEntityList)) {
            oldValue = bankAccountEntityList.stream().filter(obj -> obj.getId().equals(valuePair.getKey())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getAccountName())).orElse("");
            newValue = bankAccountEntityList.stream().filter(obj -> obj.getId().equals(valuePair.getValue())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getAccountName())).orElse("");
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

    private String removeDigits(String input) {
        if(StringUtils.isBlank(input)){
            return input;
        }
        // 定义匹配数字的正则表达式
        String regex = "\\d";
        // 创建 Pattern 对象
        Pattern pattern = Pattern.compile(regex);
        // 创建 Matcher 对象
        Matcher matcher = pattern.matcher(input);
        // 使用 replaceAll 方法替换匹配的数字为空字符串
        String result = matcher.replaceAll("");
        return result;
    }
}
