package com.erp.server.tms.service.impl;

import com.erp.model.tms.entity.TransferLogisticsAuthFieldEntity;
import com.erp.server.tms.mapper.TransferLogisticsAuthFieldMapper;
import com.erp.server.tms.service.TransferLogisticsAuthFieldService;
import com.common.business.service.impl.SuperServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 中转报关服务商授权字段值表 服务实现类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Slf4j
@Service
public class TransferLogisticsAuthFieldServiceImpl extends SuperServiceImpl<TransferLogisticsAuthFieldMapper, TransferLogisticsAuthFieldEntity> implements TransferLogisticsAuthFieldService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateAuthField(String authId, Map<String, String> fieldMap) {
        List<TransferLogisticsAuthFieldEntity> authFieldList = this.listByLogisticsAuthId(authId);
        List<TransferLogisticsAuthFieldEntity> saveOrUpdateList = new ArrayList<>(authFieldList.size());
        List<String> updateIdList = new ArrayList<>(authFieldList.size());
        for (Map.Entry<String, String> item : fieldMap.entrySet()) {
            String fieldCode = item.getKey();
            String fieldValue = item.getValue();
            TransferLogisticsAuthFieldEntity entity = authFieldList.stream().filter(a -> a.getFieldCode().equals(fieldCode)).findFirst().
                    orElse(new TransferLogisticsAuthFieldEntity());
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
                map(TransferLogisticsAuthFieldEntity::getId).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deleteIdList)) {
            this.removeByIds(deleteIdList);
        }
        this.saveOrUpdateBatch(saveOrUpdateList);

    }

    @Override
    public List<TransferLogisticsAuthFieldEntity> listByLogisticsAuthId(String authId) {
        return this.lambdaQuery().eq(TransferLogisticsAuthFieldEntity::getLogisticsAuthId, authId).list();
    }

    @Override
    public void removeByAuthId(String authId) {
        this.lambdaUpdate().eq(TransferLogisticsAuthFieldEntity::getLogisticsAuthId, authId).remove();
    }

}
