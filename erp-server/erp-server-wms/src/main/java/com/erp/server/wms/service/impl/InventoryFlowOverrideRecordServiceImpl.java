package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import com.erp.model.wms.entity.InventoryFlowOverrideRecordEntity;
import com.erp.server.wms.mapper.InventoryFlowOverrideRecordMapper;
import com.erp.server.wms.service.InventoryFlowOverrideRecordService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 库存流水重算时间范围记录 服务实现类
 * </p>
 *
 * @author cloud
 * @since 2024-08-09
 */
@Slf4j
@Service
public class InventoryFlowOverrideRecordServiceImpl extends SuperServiceImpl<InventoryFlowOverrideRecordMapper, InventoryFlowOverrideRecordEntity> implements InventoryFlowOverrideRecordService {

    @Override
    public List<InventoryFlowOverrideRecordEntity> listMaxEndTimeGroupByOrgId(String inventoryOrgId) {
        return baseMapper.listMaxEndTimeGroupByOrgId(inventoryOrgId);
    }

    @Override
    public Map<String, LocalDateTime> mapMaxEndTimeGroupByOrgId(String inventoryOrgId) {
        List<InventoryFlowOverrideRecordEntity> entityList = listMaxEndTimeGroupByOrgId(inventoryOrgId);
        if (CollUtil.isEmpty(entityList)){
            return Collections.emptyMap();
        }
        Map<String, LocalDateTime> map = new HashMap<>(entityList.size());
        for (InventoryFlowOverrideRecordEntity entity : entityList) {
            map.put(entity.getInventoryOrgId(), entity.getEndTime());
        }
        return map;
    }

}
