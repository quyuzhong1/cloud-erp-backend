package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.constant.IsConstant;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.ModuleOperateLogFieldTypeEnum;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.OperationLogUtil;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.EnumsUtil;
import com.erp.model.plm.dto.OperateLogShowDTO;
import com.erp.model.plm.dto.OperateLogSelectDTO;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.entity.OperateLogEntity;
import com.erp.model.plm.entity.CfgOperateLogFieldEntity;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.DictCountryDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.mapper.OperateLogMapper;
import com.erp.server.plm.service.BasicDictService;
import com.erp.server.plm.service.CfgOperateLogFieldService;
import com.erp.server.plm.service.OperateLogService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0

 * @date 2022/12/5 18:18
 */
@Service
public class OperateLogServiceImpl extends ServiceImpl<OperateLogMapper, OperateLogEntity> implements OperateLogService {

    @Autowired
    private CfgOperateLogFieldService cfgOperateLogFieldService;

    @Autowired
    private BasicDictService basicDictService;


    @Autowired
    private SysUserFeign sysUserFeign;

    private static final String PACKAGEPATH = "com.erp.model.plm.enums";

    /**
     * 临时脱敏：操作日志变更明细中需隐藏的字段名，新增时仅在此列表追加字段名。
     */
    private static final List<String> SENSITIVE_OPERATE_LOG_FIELD_NAMES = Collections.unmodifiableList(
            Arrays.asList("实际不含税成本"));

    private static final List<OperateLogContentMaskRule> SENSITIVE_OPERATE_LOG_CONTENT_MASK_RULES =
            SENSITIVE_OPERATE_LOG_FIELD_NAMES.stream()
                    .map(OperateLogContentMaskRule::new)
                    .collect(Collectors.toList());

    @Override
    public Boolean addSysLogByUpdate(Object oldObj, Object newObj, String classPath, String businessId, String pid, String msg) {

        Map<Pair<String, String>, Pair<String, String>> operationLogMap = OperationLogUtil.getOperationLogMap(oldObj, newObj);
        //判断是否为空
        if (operationLogMap.size() == 0) {
            return true;
        }
        List<String> classPaths = operationLogMap.entrySet().stream().map(obj -> obj.getKey().getValue()).distinct().collect(Collectors.toList());
        List<CfgOperateLogFieldEntity> sysLogFieldList = cfgOperateLogFieldService.listByClassPaths(classPaths);
        if (CollectionUtils.isEmpty(sysLogFieldList)) {
            return true;
        }
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();
        List<OperateLogEntity> list = new LinkedList<>();
        for (Map.Entry<Pair<String, String>, Pair<String, String>> entry : operationLogMap.entrySet()) {
            //Pair<字段名称, 类路径>
            Pair<String, String> keyPair = entry.getKey();
            String field = keyPair.getKey();
            String fieldClass = keyPair.getValue();
            //Pair<旧值, 新值>
            Pair<String, String> valuePair = entry.getValue();
            CfgOperateLogFieldEntity cfgOperateLogFieldEntity = sysLogFieldList.stream().filter(obj -> obj.getField().equals(field) && obj.getClassPath().equals(fieldClass)).findAny().orElse(null);
            if (ObjectUtils.isEmpty(cfgOperateLogFieldEntity)) {
                continue;
            }
            String fieldName = cfgOperateLogFieldEntity.getFieldName();
            Integer type = cfgOperateLogFieldEntity.getType();
            if (ModuleOperateLogFieldTypeEnum.TYPE_YES_NO.getCode().equals(type)) {
                valuePair = setBooleanValue(cfgOperateLogFieldEntity, valuePair);
            }
            //枚举
            if (ModuleOperateLogFieldTypeEnum.TYPE_ENUM.getCode().equals(type)) {
                valuePair = setEnumValue(cfgOperateLogFieldEntity,valuePair);
            }
            //字典
            if (ModuleOperateLogFieldTypeEnum.TYPE_DIST.getCode().equals(type)) {
                valuePair = setDistValue(valuePair,cfgOperateLogFieldEntity.getValue());
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
            //币别
            if (ModuleOperateLogFieldTypeEnum.TYPE_CURRENCY.getCode().equals(type)) {
                valuePair = setCurrencyValue(valuePair);
            }
            //组织
            if (ModuleOperateLogFieldTypeEnum.TYPE_ORG.getCode().equals(type)) {
                valuePair = setOrg(valuePair);
            }
            String oldValue = String.valueOf(valuePair.getKey());
            String newValue = String.valueOf(valuePair.getValue());

            if (oldValue.equals(newValue)) {
                continue;
            }
            String content = "";
            if (StringUtils.isBlank(valuePair.getKey())) {
                content = msg.concat("编辑了[").concat(fieldName).concat("]").concat("由空值变更为[").concat(newValue).concat("]");
            } else {
                content = msg.concat("编辑了[").concat(fieldName).concat("]").concat("由[").concat(oldValue).concat("]").concat("变更为[").concat(newValue).concat("]");
            }
            OperateLogEntity entity = new OperateLogEntity();
            entity.setClassPath(classPath)
                    .setBusinessId(businessId)
                    .setPid(pid)
                    .setOldValue(oldValue)
                    .setNewValue(newValue)
                    .setFieldName(fieldName)
                    .setContent(content)
                    .setOperation("编辑信息")
                    .setCreateUserId(userId)
                    .setCreateUserName(userName);
            list.add(entity);
        }
        return this.saveBatch(list);
    }

    @Override
    public List<String> listSysLogField(Object oldObj, Object newObj) {
        List<String> contentList = new ArrayList<>();
        Map<Pair<String, String>, Pair<String, String>> operationLogMap = OperationLogUtil.getOperationLogMap(oldObj, newObj);
        //判断是否为空
        if (operationLogMap.size() == 0) {
            return contentList;
        }
        for (Map.Entry<Pair<String, String>, Pair<String, String>> entry : operationLogMap.entrySet()) {
            //Pair<字段名称, 类路径>
            Pair<String, String> keyPair = entry.getKey();
            String field = keyPair.getKey();
            contentList.add(field);
        }
        return contentList;
    }

    @Override
    public Boolean addSysLogBySave(String content, String classPath, String businessId, String pid) {
        OperateLogEntity entity = new OperateLogEntity();
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();
        entity.setClassPath(classPath)
                .setBusinessId(businessId)
                .setPid(pid)
                .setContent(content)
                .setOperation("新增信息")
                .setCreateUserId(userId)
                .setCreateUserName(userName);
        return this.save(entity);
    }

    @Override
    public Boolean addSysLogBySave(String content, String classPath, String businessId, String pid, String operation) {
        OperateLogEntity entity = new OperateLogEntity();
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();
        entity.setClassPath(classPath)
                .setBusinessId(businessId)
                .setPid(pid)
                .setContent(content)
                .setOperation(operation)
                .setCreateUserId(userId)
                .setCreateUserName(userName);
        return this.save(entity);
    }

    @Override
    public Boolean addSysLogByBatchSave(List<OperateLogEntity> list) {

        LoginUser loginUser = UserContext.getDefaultLoginUser();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();
        for (OperateLogEntity entity : list) {
            entity.setCreateUserId(userId).setCreateUserName(userName);
        }
        return this.saveBatch(list);
    }

    @Override
    public Boolean addSysLogByOther(OperateLogEntity entity) {
        LoginUser loginUser = UserContext.getDefaultLoginUser();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();
        String content = entity.getContent();
        if (StringUtils.isBlank(content)) {
            if (StringUtils.isBlank(entity.getFieldName())) {
                throw new ServiceException(ApiError.BILL_SUBMIT_APPROVAL_STATUS_INVALID);
            }
            if (StringUtils.isBlank(entity.getOldValue())) {
                content = "编辑了[".concat(entity.getFieldName()).concat("]").concat("由空值变更为[").concat(entity.getNewValue()).concat("]");
            } else {
                content = "编辑了[".concat(entity.getFieldName()).concat("]").concat("由[").concat(entity.getOldValue()).concat("]").concat("变更为[").concat(entity.getNewValue()).concat("]");
            }
        }
        entity.setContent(content)
                .setCreateUserId(userId)
                .setCreateUserName(userName);
        return this.save(entity);
    }

    @Override
    public PagingVO<OperateLogShowDTO> paging(PagingDTO<OperateLogSelectDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        OperateLogSelectDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params, IsConstant.YES);
        if (CollectionUtils.isNotEmpty(pageData.getRecords())) {
            for (Object record : pageData.getRecords()) {
                OperateLogShowDTO showDTO = (OperateLogShowDTO) record;
                if (showDTO != null && StringUtils.isNotBlank(showDTO.getContent())) {
                    showDTO.setContent(maskSensitiveOperateLogContent(showDTO.getContent()));
                }
            }
        }
        return new PagingVO(pageData);
    }

    private static String maskSensitiveOperateLogContent(String content) {
        String masked = content;
        for (OperateLogContentMaskRule rule : SENSITIVE_OPERATE_LOG_CONTENT_MASK_RULES) {
            if (!masked.contains(rule.fieldName)) {
                continue;
            }
            masked = rule.pattern.matcher(masked).replaceAll(rule.replacement);
        }
        return masked;
    }

    /**
     * 操作日志 content 脱敏规则：由字段名生成「编辑了[字段]由***变更为***」模式。
     */
    private static final class OperateLogContentMaskRule {
        private final String fieldName;
        private final Pattern pattern;
        private final String replacement;

        private OperateLogContentMaskRule(String fieldName) {
            this.fieldName = fieldName;
            this.pattern = Pattern.compile(
                    "编辑了\\[" + Pattern.quote(fieldName) + "\\](?:由\\[[^\\]]*\\]变更为\\[[^\\]]*\\]|由空值变更为\\[[^\\]]*\\])");
            this.replacement = "编辑了[" + fieldName + "]由***变更为***";
        }
    }

    @Override
    public List<OperateLogShowDTO> listSysLog(OperateLogSelectDTO dto) {
        return baseMapper.listSysLog(dto);
    }

    @Override
    public PagingVO<OperateLogShowDTO.HistoryDTO> getBomChangeHistory(PagingDTO<OperateLogShowDTO.PagingParamDTO> dto) {
        Page<OperateLogShowDTO.HistoryDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        OperateLogShowDTO.PagingParamDTO params = dto.getParams();
        IPage<OperateLogShowDTO.HistoryDTO> pageData = baseMapper.getProductChangeHistory(query, params);
        return new PagingVO(pageData);
    }
    /**
     * 设置布尔值
     */
    private Pair<String,String> setBooleanValue (CfgOperateLogFieldEntity fieldEntity, Pair<String, String> valuePair) {
        String trueValue = "是";
        String falseValue = "否";

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
        BasicDictEntity oldEntity = basicDictService.getByTypeAndValue(value, valuePair.getKey());
        if (ObjectUtils.isNotEmpty(oldEntity)) {
            oldValue = oldEntity.getName();
        }
        BasicDictEntity newEntity = basicDictService.getByTypeAndValue(value, valuePair.getValue());
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
     * 设置枚举值
     */
    private Pair<String,String> setEnumValue (CfgOperateLogFieldEntity fieldEntity, Pair<String, String> valuePair) {
        if (StringUtils.isBlank(fieldEntity.getEnumClass())) {
            throw new ServiceException(ApiError.COMMON_ENUM_CONVERT_FAILED);
        }
        String  oldValue = "";
        String  newValue = "";
        Class<?> aClass ;
        try {
            aClass = Class.forName(fieldEntity.getEnumClass());
        } catch (ClassNotFoundException e) {
            throw new ServiceException(ApiError.COMMON_ENUM_CONVERT_FAILED);
        }
        boolean anEnum = aClass.isEnum();
        if (!anEnum) {
            throw new ServiceException(ApiError.COMMON_ENUM_CONVERT_FAILED);
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addModuleOperateLog(String content, String moduleType, String businessId, String operation) {
        OperateLogEntity entity = new OperateLogEntity();
        entity.setModuleType(moduleType)
                .setBusinessId(businessId)
                .setContent(content)
                .setOperation(operation);
        return this.save(entity);
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

}
