package com.erp.server.mrp.calculation.service.impl;

import com.erp.model.mrp.entity.CfgDataArchivingEntity;
import com.erp.server.mrp.calculation.service.BasicReplenishmentDataService;
import com.erp.server.mrp.calculation.service.DataArchivingService;
import com.erp.server.mrp.service.CfgDataArchivingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class DataArchivingServiceImpl implements DataArchivingService {

    @Resource
    private CfgDataArchivingService cfgDataArchivingService;
    @Resource
    private BasicReplenishmentDataService replenishmentDataService;
    @Override
    public void dataArchiving(LocalDate calculationDate) {
        calculationDate = ObjectUtils.isEmpty(calculationDate) ? LocalDate.now() : calculationDate;
        List<CfgDataArchivingEntity> effectiveData = cfgDataArchivingService.getEffectiveData();
        log.warn("开始处理归档数据,时间{}", System.currentTimeMillis());
        for (CfgDataArchivingEntity config : effectiveData) {
            // 执行归档逻辑
            cfgDataArchivingService.archiveData(config);
        }
        log.warn("完成处理归档数据,时间{}", System.currentTimeMillis());
        log.warn("开始增量更新建议基础数据,时间{}", System.currentTimeMillis());
        //增量更新建议基础数据
        replenishmentDataService.initReplenishmentSku(calculationDate);
        log.warn("完成增量更新建议基础数据,时间{}", System.currentTimeMillis());
        //清洗历史销量和库存
        log.warn("开始清洗历史销量和库存数据,时间{}", System.currentTimeMillis());
        replenishmentDataService.cleanHistorySalesAndInventory(calculationDate);
        log.warn("开始清洗历史销量和库存数据,时间{}", System.currentTimeMillis());
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
