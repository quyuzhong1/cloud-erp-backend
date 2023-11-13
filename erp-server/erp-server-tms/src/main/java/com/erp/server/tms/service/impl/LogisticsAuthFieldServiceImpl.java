package com.erp.server.tms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.plm.dto.ProductTaskViewDTO;
import com.erp.model.tms.entity.LogisticsAuthFieldEntity;
import com.erp.server.tms.mapper.LogisticsAuthFieldMapper;
import com.erp.server.tms.service.LogisticsAuthFieldService;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.server.tms.service.OperateLogService;
import com.erp.server.tms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsAuthFieldDTO;

import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

/**
 * <p>
 * 物流授权字段值表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
@Slf4j
@Service
public class LogisticsAuthFieldServiceImpl extends SuperServiceImpl<LogisticsAuthFieldMapper, LogisticsAuthFieldEntity> implements LogisticsAuthFieldService {


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateAuthField(String authId, Map<String, String> fieldMap) {
        List<LogisticsAuthFieldEntity> authFieldList = this.listByLogisticsAuthId(authId);
        List<LogisticsAuthFieldEntity> saveOrUpdateList = new ArrayList<>(authFieldList.size());
        List<String> updateIdList = new ArrayList<>(authFieldList.size());
        for (Map.Entry<String, String> item : fieldMap.entrySet()) {
            String fieldCode = item.getKey();
            String fieldValue = item.getValue();
            LogisticsAuthFieldEntity entity = authFieldList.stream().filter(a -> a.getFieldCode().equals(fieldCode)).findFirst().
                    orElse(new LogisticsAuthFieldEntity());
            entity.setFieldCode(fieldCode);
            entity.setFieldValue(fieldValue);
            entity.setLogisticsAuthId(authId);
            saveOrUpdateList.add(entity);
            String id = entity.getId();
            if (StringUtils.isNotBlank(id)) {
                updateIdList.add(id);
            }
        }

        List<String> deleteIdList = authFieldList.stream().filter(a -> !updateIdList.contains(a.getId())).
                map(LogisticsAuthFieldEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        this.saveOrUpdateBatch(saveOrUpdateList);

    }

    @Override
    public List<LogisticsAuthFieldEntity> listByLogisticsAuthId(String authId) {
        return this.lambdaQuery().eq(LogisticsAuthFieldEntity::getLogisticsAuthId, authId).list();
    }




}
