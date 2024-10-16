package com.erp.server.mrp.calculation.service.impl;

import com.erp.model.mrp.entity.CfgDataArchivingEntity;
import com.erp.server.mrp.calculation.service.BasicReplenishmentDataService;
import com.erp.server.mrp.calculation.service.DataArchivingService;
import com.erp.server.mrp.service.CfgDataArchivingService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

@Service
public class DataArchivingServiceImpl implements DataArchivingService {

    @Resource
    private CfgDataArchivingService cfgDataArchivingService;
    @Resource
    private BasicReplenishmentDataService replenishmentDataService;
    @Override
    public void dataArchiving(LocalDate calculationDate) {
        calculationDate = ObjectUtils.isEmpty(calculationDate) ? LocalDate.now() : calculationDate;
        List<CfgDataArchivingEntity> effectiveData = cfgDataArchivingService.getEffectiveData();
        for (CfgDataArchivingEntity config : effectiveData) {
            // 执行归档逻辑
            cfgDataArchivingService.archiveData(config);
        }
        //增量更新建议基础数据
        replenishmentDataService.initReplenishmentSku(calculationDate);
        //清洗历史销量和库存
        replenishmentDataService.cleanHistorySalesAndInventory(calculationDate);
    }

    @Override
    public void dataArchiving(String detailId) {
        List<CfgDataArchivingEntity> effectiveData = cfgDataArchivingService.getEffectiveData();
        for (CfgDataArchivingEntity config : effectiveData) {
            // 执行归档逻辑
            cfgDataArchivingService.archiveData(config, detailId);
        }
    }
}
