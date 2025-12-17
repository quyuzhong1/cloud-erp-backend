package com.erp.server.plm.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.plm.dto.SkuStdCostDTO;
import com.erp.model.plm.entity.SkuStdCostEntity;
import com.erp.server.plm.mapper.SkuStdCostMapper;
import com.erp.server.plm.service.SkuStdCostService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * sku标准成本表 服务实现类
 * </p>
 *
 * @author Jim
 * @since 2025-08-08
 */
@Slf4j
@Service
public class SkuStdCostServiceImpl extends SuperServiceImpl<SkuStdCostMapper, SkuStdCostEntity> implements SkuStdCostService {


    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class)
    public void update(SkuStdCostDTO.UpdateDTO dto) {
        if (CollectionUtils.isEmpty(dto.getSkuIds())) {
            return;
        }
        List<SkuStdCostEntity> list = this.lambdaQuery()
                .in(SkuStdCostEntity::getSkuId, dto.getSkuIds())
                .list();
        List<SkuStdCostEntity> updateList = new LinkedList<>();
        for (SkuStdCostEntity skuStdCostEntity : list) {
            if (null == skuStdCostEntity.getLastOutstockDate()
                    || dto.getBillDate().isAfter(skuStdCostEntity.getLastOutstockDate())) {
                skuStdCostEntity.setLastOutstockDate(dto.getBillDate());
                updateList.add(skuStdCostEntity);
            }
        }
        if (CollectionUtils.isNotEmpty(updateList)) {
            this.updateBatchById(updateList);
        }
    }
}
