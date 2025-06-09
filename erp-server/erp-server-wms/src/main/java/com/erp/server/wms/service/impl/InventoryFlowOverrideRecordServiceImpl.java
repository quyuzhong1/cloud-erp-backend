package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.entity.InventoryFlowOverrideRecordEntity;
import com.erp.server.wms.mapper.InventoryFlowOverrideRecordMapper;
import com.erp.server.wms.service.InventoryFlowOverrideRecordService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.wms.service.OperateLogService;
import com.erp.server.wms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.InventoryFlowOverrideRecordDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
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
