package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.erp.model.wms.entity.InventoryClosedRecordEntity;
import com.erp.server.wms.mapper.InventoryClosedRecordMapper;
import com.erp.server.wms.service.InventoryClosedRecordService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.exception.ServiceException;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.wms.dto.inventory.InventoryClosedRecordDTO;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.util.CollectionUtils;

/**
 * <p>
 * 库存关账记录表 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2023-10-12
 */
@Slf4j
@Service
public class InventoryClosedRecordServiceImpl extends SuperServiceImpl<InventoryClosedRecordMapper, InventoryClosedRecordEntity> implements InventoryClosedRecordService {

    @Override
    public Map<String, LocalDate> mapByOrgId() {
        return lambdaQuery()
                .list()
                .stream()
                .collect(Collectors.toMap(InventoryClosedRecordEntity::getInventoryOrgId, InventoryClosedRecordEntity::getClosedDate));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void actionBatch(List<InventoryClosedRecordEntity> newEntityList, List<InventoryClosedRecordEntity> oldEntityList) {
        // 1: 新增列表
        List<InventoryClosedRecordEntity> saveEntityList = newEntityList.stream()
                .filter(e -> oldEntityList.stream().noneMatch(o -> o.getInventoryOrgId().equalsIgnoreCase(e.getInventoryOrgId())))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(saveEntityList)) {
            boolean result = this.saveBatch(saveEntityList);
            if (!result) {
                throw new ServiceException("[InventoryClosedRecordEntity]批量保存失败");
            }
        }

        // 2: 删除不存在的列表
        List<InventoryClosedRecordEntity> deleteEntityList = oldEntityList.stream()
                .filter(e -> newEntityList.stream().noneMatch(o -> o.getInventoryOrgId().equalsIgnoreCase(e.getInventoryOrgId())))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(deleteEntityList)) {
            List<String> entityIds = deleteEntityList.stream().map(InventoryClosedRecordEntity::getId).collect(Collectors.toList());
            boolean result = this.removeByIds(entityIds);
            if (!result) {
                throw new ServiceException("[InventoryClosedRecordEntity]批量删除失败");
            }
        }

        // 3: 更新列表
        // 需要更新的map
        Map<String, LocalDate> newClosedDateMap = newEntityList
                .stream()
                .collect(Collectors.toMap(InventoryClosedRecordEntity::getInventoryOrgId, InventoryClosedRecordEntity::getClosedDate));
        // 过滤得到需要更新的列表
        List<InventoryClosedRecordEntity> updateEntityList = oldEntityList.stream()
                .filter(e -> e.isUpdateClosedDate(newClosedDateMap))
                .map(e -> e.setClosedDateByMap(newClosedDateMap))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(updateEntityList)) {
            boolean result = this.updateBatchById(updateEntityList);
            if (!result) {
                throw new ServiceException("[InventoryClosedRecordEntity]批量更新失败");
            }
        }
    }


}
