package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.EnumsUtil;
import com.common.core.utils.OperationLogUtil;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.LoginUser;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.SysLogSelectDTO;
import com.erp.model.plm.dto.SysLogShowDTO;
import com.erp.model.plm.entity.SysDocsEntity;
import com.erp.model.plm.entity.SysLogEntity;
import com.erp.model.plm.entity.SysLogFieldEntity;
import com.erp.server.plm.constant.IsConstant;
import com.erp.server.plm.mapper.SysDocsMapper;
import com.erp.server.plm.mapper.SysLogMapper;
import com.erp.server.plm.service.CommonService;
import com.erp.server.plm.service.SysDocsService;
import com.erp.server.plm.service.SysLogFieldService;
import com.erp.server.plm.service.SysLogService;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/5 18:18
 */
@Service
public class SysLogServiceImpl  extends ServiceImpl<SysLogMapper, SysLogEntity> implements SysLogService {

    @Autowired
    private SysLogFieldService sysLogFieldService;

    @Autowired
    private CommonService commonService;

    private static final  String PACKAGEPATH = "com.erp.server.plm.enums";

    @Override
    public Boolean addSysLogByUpdate(Object oldObj,Object newObj,String classPath ,String businessId) {

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
        List<SysLogEntity>  list = new LinkedList<>();
        for (Map.Entry<Pair<String, String>, Pair<String, String>> entry : operationLogMap.entrySet()) {
            //Pair<字段名称, 类路径>
            Pair<String, String> keyPair = entry.getKey();
            String field = keyPair.getKey();
            String fieldClass = keyPair.getValue();
            //Pair<旧值, 新值>
            Pair<String, String> valuePair = entry.getValue();
            SysLogFieldEntity sysLogFieldEntity = sysLogFieldList.stream().filter(obj -> obj.getField().equals(field) && obj.getClassPath().equals(fieldClass)).findAny().orElse(null);
            if (ObjectUtils.isEmpty(sysLogFieldEntity)){
                continue;
            }
            String fieldName = sysLogFieldEntity.getFieldName();
            Integer type = sysLogFieldEntity.getType();
            String oldValue = valuePair.getKey();
            String newValue = valuePair.getValue();
            if (type == 1) {
                oldValue = IsConstant.YES.equals(oldValue) ? "是" : "否";
                newValue = IsConstant.YES.equals(newValue) ? "是" : "否";
            } else if (type == 2) {
                Class<?> aClass = null;
                try {
                     aClass = Class.forName(PACKAGEPATH.concat(sysLogFieldEntity.getEnumClass()));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                boolean anEnum = aClass.isEnum();
                if (!anEnum) {
                    throw new ServiceException(ApiError.Default);
                }
                oldValue =   EnumsUtil.getEnumObject(Integer.valueOf(oldValue), aClass).getMsg();
                newValue =   EnumsUtil.getEnumObject(Integer.valueOf(newValue), aClass).getMsg();
            }
            String content = "";
            if (StringUtils.isBlank(valuePair.getKey())) {
                content = "字段：".concat(fieldName).concat("由空值变更为").concat(newValue);
            } else {
                content = "字段：".concat(fieldName).concat("由").concat(oldValue).concat("变更为").concat(newValue);
            }
            SysLogEntity entity = new SysLogEntity();
            entity.setClassPath(classPath)
                    .setBusinessId(businessId)
                    .setOldValue(oldValue)
                    .setNewValue(newValue)
                    .setFieldName(fieldName)
                    .setContent(content)
                    .setOperation("编辑")
                    .setCreateUserId(userId)
                    .setCreateUserName(userName);
            list.add(entity);
        }
        return this.saveBatch(list);
    }

    @Override
    public Boolean addSysLogBySave(String content,String classPath,String businessId) {
        SysLogEntity entity = new SysLogEntity();
        LoginUser loginUser = commonService.getUserInfo();
        String userName = loginUser.getUserName();
        String userId = loginUser.getUid();
        entity.setClassPath(classPath)
              .setBusinessId(businessId)
              .setContent(content)
              .setOperation("新增")
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
        String content = "";
        if (StringUtils.isBlank(entity.getOldValue())) {
            content = "字段：".concat(entity.getFieldName()).concat("由空值变更为").concat(entity.getNewValue());
        } else {
            content = "字段：".concat(entity.getFieldName()).concat("由").concat(entity.getOldValue()).concat("变更为").concat(entity.getNewValue());
        }
        entity.setContent(StringUtils.isBlank(entity.getContent())? content : entity.getContent())
                .setCreateUserId(userId)
                .setCreateUserName(userName);
        return this.save(entity);
    }

    @Override
    public PagingVO<SysLogShowDTO> paging(PagingDTO<SysLogSelectDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        SysLogSelectDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params, IsConstant.YES);
        List<SysLogShowDTO> records = pageData.getRecords();
        return new PagingVO(pageData);
    }
}
