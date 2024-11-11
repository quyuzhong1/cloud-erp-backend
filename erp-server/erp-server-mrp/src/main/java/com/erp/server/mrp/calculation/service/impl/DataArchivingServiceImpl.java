package com.erp.server.mrp.calculation.service.impl;

import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.mrp.entity.CfgDataArchivingEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.mrp.calculation.service.BasicReplenishmentDataService;
import com.erp.server.mrp.calculation.service.DataArchivingService;
import com.erp.server.mrp.service.CfgDataArchivingService;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DataArchivingServiceImpl implements DataArchivingService {

    @Resource
    private CfgDataArchivingService cfgDataArchivingService;
    @Resource
    private BasicReplenishmentDataService replenishmentDataService;

    @Resource
    private ShopInfoFeign shopInfoFeign;
    
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Override
    public void dataArchiving(LocalDate calculationDate) {
    	String redisKey = "mrp:data:archiving";
		if(redisTemplate.opsForValue().setIfAbsent(redisKey, DateUtil.now(), 7200, TimeUnit.SECONDS)) {
			try {
				calculationDate = ObjectUtils.isEmpty(calculationDate) ? LocalDate.now() : calculationDate;
				List<CfgDataArchivingEntity> effectiveData = cfgDataArchivingService.getEffectiveData();
				log.warn("开始处理归档数据,时间{}", System.currentTimeMillis());
				for (CfgDataArchivingEntity config : effectiveData) {
				    // 执行归档逻辑
				    cfgDataArchivingService.archiveData(config);
				}
				log.warn("完成处理归档数据,时间{}", System.currentTimeMillis());
				log.warn("开始增量更新建议基础数据,时间{}", System.currentTimeMillis());
				//获取所有店铺
				ApiResult<List<ShopInfoEntity>> allShopResult = shopInfoFeign.list();
				if (!allShopResult.isSuccess()) {
				    throw new ServiceException(allShopResult.getMsg());
				}
				//增量更新建议基础数据
				replenishmentDataService.initReplenishmentSku(calculationDate, allShopResult.getData());
				log.warn("完成增量更新建议基础数据,时间{}", System.currentTimeMillis());
				//清洗历史销量和库存
				log.warn("开始清洗历史销量和库存数据,时间{}", System.currentTimeMillis());
				replenishmentDataService.cleanHistorySalesAndInventory(calculationDate, allShopResult.getData());
				log.warn("开始清洗历史销量和库存数据,时间{}", System.currentTimeMillis());
			} catch (Exception e) {
				log.error("处理MRP归档失败" , e);
				throw e;
			} finally {
				redisTemplate.delete(redisKey);
			}
		}else {
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
