package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.constant.EnumMessage;
import com.common.core.utils.EnumsUtil;
import com.common.business.dto.FindUserDTO;
import com.common.business.utils.OperationLogUtil;
import com.common.business.dto.base.PagingDTO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.SysLogSelectDTO;
import com.erp.model.plm.dto.SysLogShowDTO;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.entity.SysLogEntity;
import com.erp.model.plm.entity.SysLogFieldEntity;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.common.business.constant.IsConstant;
import com.erp.server.plm.mapper.SysLogMapper;
import com.erp.server.plm.service.BasicDictService;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.SysLogFieldService;
import com.erp.server.plm.service.SysLogService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/5 18:18
 */
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLogEntity> implements SysLogService {

    @Autowired
    private SysLogFieldService sysLogFieldService;

    @Autowired
    private CommonService commonService;

    @Autowired
    private BasicDictService basicDictService;

    @Autowired
    private SysUserFeign sysUserFeign;

    private static final String PACKAGEPATH = "com.erp.model.plm.enums";

    @Override
    public Boolean addSysLogByUpdate(Object oldObj, Object newObj, String classPath, String businessId, String pid, String msg) {

        Map<Pair<String, String>, Pair<String, String>> operationLogMap = OperationLogUtil.getOperationLogMap(oldObj, newObj);
        //判断是否为空
        if (operationLogMap.size() == 0) {
            return true;
        }
        List<String> classPaths = operationLogMap.entrySet().stream().map(obj -> obj.getKey().getValue()).distinct().collect(Collectors.toList());
        List<SysLogFieldEntity> sysLogFieldList = sysLogFieldService.listByClassPaths(classPaths);
        if (CollectionUtils.isEmpty(sysLogFieldList)) {
            return true;
        }
        LoginUser loginUser = commonService.getUserInfo();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();
        List<SysLogEntity> list = new LinkedList<>();
        for (Map.Entry<Pair<String, String>, Pair<String, String>> entry : operationLogMap.entrySet()) {
            //Pair<字段名称, 类路径>
            Pair<String, String> keyPair = entry.getKey();
            String field = keyPair.getKey();
            String fieldClass = keyPair.getValue();
            //Pair<旧值, 新值>
            Pair<String, String> valuePair = entry.getValue();
            SysLogFieldEntity sysLogFieldEntity = sysLogFieldList.stream().filter(obj -> obj.getField().equals(field) && obj.getClassPath().equals(fieldClass)).findAny().orElse(null);
            if (ObjectUtils.isEmpty(sysLogFieldEntity)) {
                continue;
            }
            String fieldName = sysLogFieldEntity.getFieldName();
            Integer type = sysLogFieldEntity.getType();
            String oldValue = String.valueOf(valuePair.getKey());
            String newValue = String.valueOf(valuePair.getValue());
            if (type == 1) {
                //是或否
                oldValue = IsConstant.YES.toString().equals(oldValue) ? "是" : "否";
                newValue = IsConstant.YES.toString().equals(newValue) ? "是" : "否";
                //值不变则不用新增操作日志
                if (oldValue.equals(newValue)) {
                    continue;
                }
            } else if (type == 2) {
                //枚举
                if (StringUtils.isBlank(sysLogFieldEntity.getEnumClass())) {
                    throw new ServiceException(ApiError.ERROR_9028);
                }
                Class<?> aClass = null;
                try {
                    aClass = Class.forName(PACKAGEPATH.concat(".").concat(sysLogFieldEntity.getEnumClass()));
                } catch (ClassNotFoundException e) {
                    throw new ServiceException(ApiError.ERROR_9028);
                }
                boolean anEnum = aClass.isEnum();
                if (!anEnum) {
                    throw new ServiceException(ApiError.ERROR_9028);
                }
                if (StringUtils.isNotBlank(oldValue)) {
                    EnumMessage enumObject = EnumsUtil.getEnumObject(Integer.valueOf(oldValue), aClass);
                    if (ObjectUtils.isNotEmpty(enumObject)) {
                        oldValue = enumObject.getName();
                    } else {
                        oldValue = "";
                    }
                }
                if (StringUtils.isNotBlank(newValue)) {
                    EnumMessage enumObject = EnumsUtil.getEnumObject(Integer.valueOf(newValue), aClass);
                    if (ObjectUtils.isNotEmpty(enumObject)) {
                        newValue = enumObject.getName();
                    } else {
                        newValue = "";
                    }
                }

            } else if (type == 3) {
                //字典
                List<BasicDictEntity> oldList = basicDictService.listByIds(Arrays.asList(oldValue.split(",")));
                if (CollectionUtils.isNotEmpty(oldList)) {
                    oldValue = oldList.stream().map(BasicDictEntity::getValue).distinct().collect(Collectors.joining(","));
                }
                List<BasicDictEntity> newList = basicDictService.listByIds(Arrays.asList(newValue.split(",")));
                if (CollectionUtils.isNotEmpty(newList)) {
                    newValue = newList.stream().map(BasicDictEntity::getValue).distinct().collect(Collectors.joining(","));
                }
            } else if (type == 4) {
                //人员
                List<FindUserDTO> oldList = sysUserFeign.getUserListByUserIds(Arrays.asList(oldValue.split(",")));
                if (CollectionUtils.isNotEmpty(oldList)) {
                    oldValue = oldList.stream().map(FindUserDTO::getUserName).distinct().collect(Collectors.joining(","));
                }
                List<FindUserDTO> newList = sysUserFeign.getUserListByUserIds(Arrays.asList(newValue.split(",")));
                if (CollectionUtils.isNotEmpty(newList)) {
                    newValue = newList.stream().map(FindUserDTO::getUserName).distinct().collect(Collectors.joining(","));
                }

            }
            if (oldValue.equals(newValue)) {
                continue;
            }
            String content = "";
            if (StringUtils.isBlank(valuePair.getKey())) {
                content = msg.concat("编辑了[").concat(fieldName).concat("]").concat("由空值变更为[").concat(newValue).concat("]");
            } else {
                content = msg.concat("编辑了[").concat(fieldName).concat("]").concat("由[").concat(oldValue).concat("]").concat("变更为[").concat(newValue).concat("]");
            }
            SysLogEntity entity = new SysLogEntity();
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
        SysLogEntity entity = new SysLogEntity();
        LoginUser loginUser = commonService.getUserInfo();
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
    public Boolean addSysLogByBatchSave(List<SysLogEntity> list) {

        LoginUser loginUser = commonService.getUserInfo();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();
        for (SysLogEntity entity : list) {
            entity.setCreateUserId(userId).setCreateUserName(userName);
        }
        return this.saveBatch(list);
    }

    @Override
    public Boolean addSysLogByOther(SysLogEntity entity) {
        LoginUser loginUser = commonService.getUserInfo();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();
        String content = entity.getContent();
        if (StringUtils.isBlank(content)) {
            if (StringUtils.isBlank(entity.getFieldName())) {
                throw new ServiceException(ApiError.ERROR_95089);
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
    public PagingVO<SysLogShowDTO> paging(PagingDTO<SysLogSelectDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        SysLogSelectDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params, IsConstant.YES);
        return new PagingVO(pageData);
    }

    @Override
    public List<SysLogShowDTO> listSysLog(SysLogSelectDTO dto) {
        return baseMapper.listSysLog(dto);
    }
}
