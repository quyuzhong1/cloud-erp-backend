package com.erp.server.mrp.calculation.service.impl;

import cn.hutool.core.date.DateUtil;
import com.common.core.exception.ServiceException;
import com.erp.model.mrp.entity.CfgDataArchivingEntity;
import com.erp.model.mrp.entity.CfgPlatformMappingEntity;
import com.erp.model.mrp.entity.ReplenishmentSuggestionEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.PlatformMappingTypeEnum;
import com.erp.server.mrp.calculation.service.BasicReplenishmentDataService;
import com.erp.server.mrp.calculation.service.DataArchivingService;
import com.erp.server.mrp.service.CfgDataArchivingService;
import com.erp.server.mrp.service.CfgPlatformMappingService;
import com.erp.server.mrp.service.ReplenishmentSuggestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataArchivingServiceImpl implements DataArchivingService {

    @Resource
    private CfgDataArchivingService cfgDataArchivingService;
    @Resource
    private BasicReplenishmentDataService replenishmentDataService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private CfgPlatformMappingService cfgPlatformMappingService;
    @Resource
    private ReplenishmentSuggestionService replenishmentSuggestionService;

    @Override
    public void dataArchiving(LocalDate calculationDate, Integer cleanDay) {
        String redisKey = "mrp:data:archiving";
        redisTemplate.delete(redisKey);
        if (Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(redisKey, DateUtil.now(), 7200, TimeUnit.SECONDS))) {
            try {
                calculationDate = ObjectUtils.isEmpty(calculationDate) ? LocalDate.now() : calculationDate;
                cleanDay = ObjectUtils.isEmpty(cleanDay) ? 361 : cleanDay;
                List<CfgDataArchivingEntity> effectiveData = cfgDataArchivingService.getEffectiveData();
                log.warn("开始处理归档数据,时间{}", System.currentTimeMillis());
                for (CfgDataArchivingEntity config : effectiveData) {
                    // 执行归档逻辑
                    cfgDataArchivingService.archiveData(config);
                }
                List<CfgPlatformMappingEntity> mappings = cfgPlatformMappingService.listByEffective();
                log.warn("完成处理归档数据,时间{}", System.currentTimeMillis());
                log.warn("开始增量更新建议基础数据,时间{}", System.currentTimeMillis());
                //增量更新建议基础数据
//                replenishmentDataService.initReplenishmentSku(calculationDate, mappings);
                List<String> typeList = mappings.stream()
                        .map(CfgPlatformMappingEntity::getType)
                        .distinct().collect(Collectors.toList());
                log.warn("完成增量更新建议基础数据,时间{}", System.currentTimeMillis());
                for (String type : typeList) {
                    CfgRulePlatformTypeEnum platformType = PlatformMappingTypeEnum.getEnum(type).getPlatformType();
//                    List<ReplenishmentSuggestionEntity> suggestionList = replenishmentSuggestionService.listByPlatform(platformType.getCode());
//                    //清洗历史销量和库存
//                    log.warn("开始清洗{}历史库存数据,时间{}", platformType.getName(), System.currentTimeMillis());
//                    replenishmentDataService.cleanHistoryInventory(calculationDate, suggestionList, platformType, cleanDay);
//                    log.warn("开始清洗{}历史库存数据,时间{}", platformType.getName(), System.currentTimeMillis());
//                    //清洗销售订单历史销量和库存
//                    log.warn("开始清洗{}销售订单历史销量数据,时间{}", platformType.getName(), System.currentTimeMillis());
//                    replenishmentDataService.cleanHistorySalesByOrder(calculationDate, suggestionList, platformType, cleanDay);
//                    log.warn("开始清洗{}销售订单历史销量数据,时间{}", platformType.getName(), System.currentTimeMillis());
//                    //清洗销售订单历史销量和库存
//                    log.warn("开始清洗{}销售出库单历史销量数据,时间{}", platformType.getName(), System.currentTimeMillis());
//                    replenishmentDataService.cleanHistorySalesByOutStock(calculationDate, suggestionList, platformType, cleanDay);
//                    log.warn("开始清洗{}销售出库单历史销量数据,时间{}", platformType.getName(), System.currentTimeMillis());
                    List<ReplenishmentSuggestionEntity> suggestions = replenishmentSuggestionService.listCalculationData(platformType.getCode());
                    //计算数据是否需要进行补货
                    replenishmentDataService.isReplenishment(suggestions,platformType.getCode(), calculationDate);
                    //计算明细数据
                    replenishmentDataService.calculationDetail(platformType.getCode(), calculationDate);
                }
            } catch (Exception e) {
                log.error("处理MRP归档失败", e);
                throw e;
            } finally {
                redisTemplate.delete(redisKey);
            }
        } else {
            throw new ServiceException("处理MRP归档任务正在进行中");
        }
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
