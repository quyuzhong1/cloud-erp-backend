package com.erp.server.tms.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.tms.entity.LogisticsAuthFieldEntity;
import com.erp.server.tms.mapper.LogisticsAuthFieldMapper;
import com.erp.server.tms.service.LogisticsAuthFieldService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public void removeByAuthId(String authId) {
        this.lambdaUpdate().eq(LogisticsAuthFieldEntity::getLogisticsAuthId, authId).remove();
    }


}
