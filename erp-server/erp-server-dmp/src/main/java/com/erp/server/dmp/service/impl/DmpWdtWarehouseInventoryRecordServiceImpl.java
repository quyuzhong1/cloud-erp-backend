package com.erp.server.dmp.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.dmp.entity.DmpWdtWarehouseInventoryRecordEntity;
import com.erp.server.dmp.mapper.DmpWdtWarehouseInventoryRecordMapper;
import com.erp.server.dmp.service.DmpWdtWarehouseInventoryRecordService;
import com.erp.server.dmp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 旺店通库存同步记录 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2026-01-26
 */
@Slf4j
@Service
public class DmpWdtWarehouseInventoryRecordServiceImpl extends SuperServiceImpl<DmpWdtWarehouseInventoryRecordMapper, DmpWdtWarehouseInventoryRecordEntity> implements DmpWdtWarehouseInventoryRecordService {
    @Resource
    private OperateLogService operateLogService;

    @Override
    public void updateInventoryStatus(String batchNo, String warehouseNo, List<String> skuNoList, String status, String msg) {
        this.lambdaUpdate()
                .set(DmpWdtWarehouseInventoryRecordEntity::getBillStatus, status)
                .set(CharSequenceUtil.isNotBlank(msg), DmpWdtWarehouseInventoryRecordEntity::getRemark, msg)
                .eq(DmpWdtWarehouseInventoryRecordEntity::getBatchNo, batchNo)
                .eq(DmpWdtWarehouseInventoryRecordEntity::getThirdWarehouseCode, warehouseNo)
                .in(DmpWdtWarehouseInventoryRecordEntity::getThirdSkuNo, skuNoList)
                .update();
           }
}
