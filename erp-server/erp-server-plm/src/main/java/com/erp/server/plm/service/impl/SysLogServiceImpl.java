package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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


    @Override
    public Boolean addSysLog(Object oldObj,Object newObj,String classPath ,String businessId) {

        Map<Pair<String, String>, Pair<String, String>> operationLogMap = OperationLogUtil.getOperationLogMap(oldObj, newObj);
        //判断是否为空
        if (CollectionUtils.isNotEmpty(operationLogMap)) {
            return true;
        }
        List<String> classPaths = operationLogMap.entrySet().stream().map(obj -> obj.getKey().getKey()).distinct().collect(Collectors.toList());
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
            SysLogEntity entity = new SysLogEntity();
            entity.setClassPath(classPath);
            entity.setBusinessId(businessId);
            entity.setOldValue(valuePair.getKey());
            entity.setNewValue(valuePair.getValue());
            entity.setCreateUserId(userId);
            entity.setCreateUserName(userName);
            String fieldName = sysLogFieldList.stream().filter(obj -> obj.getField().equals(field) && obj.getClassPath().equals(fieldClass)).map(SysLogFieldEntity::getFieldName).findAny().orElse(null);
            entity.setFieldName(fieldName);
            String content ="字段：".concat(fieldName).concat("由").concat(entity.getOldValue()).concat("变更为").concat(entity.getNewValue());
            entity.setContent(content);
            list.add(entity);
        }
        return this.saveBatch(list);
    }

    @Override
    public PagingVO<SysLogShowDTO> paging(PagingDTO<SysLogSelectDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        SysLogSelectDTO params = dto.getParams();
        IPage pageData = baseMapper.paging(query, params, IsConstant.YES);
        return new PagingVO(pageData);
    }

}
