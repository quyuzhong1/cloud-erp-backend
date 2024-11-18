package com.erp.server.wms.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.RedisService;
import com.common.core.utils.StrUtils;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.WmsRedisKeyEnum;
import com.erp.server.wms.service.WarehouseService;
import com.google.common.collect.Lists;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * 基础数据定时器
 * @CreateTime: 2023-07-18  21:16
 * @Author: zhangchunlin
 */
@Component
@Slf4j
public class BaseDataJob {

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private RedisService redisService;

    /**
     * 仓库缓存清除
     */
    @XxlJob("warehouseCacheClean")
    public ReturnT<String> warehouseCacheClean() {
        String jobParam = XxlJobHelper.getJobParam();
        String warehouseName = StrUtils.null2EmptyWithTrim(jobParam);
        if(CharSequenceUtil.isNotEmpty(warehouseName)) {
            log.warn("定时器传入的需要清除缓存的仓库名称：{}", warehouseName);
            WarehouseEntity warehouseEntity = warehouseService.lambdaQuery().eq(WarehouseEntity::getName, warehouseName).last("LIMIT 1").one();
            if(Objects.nonNull(warehouseEntity)) {
                redisService.deleteObject(WmsRedisKeyEnum.WMS_WAREHOUSE_DETAIL_ID.keyBuilder(warehouseEntity.getId()));
                log.warn("清除仓库：{}缓存成功", warehouseName);
            }
            return ReturnT.SUCCESS;
        }

        List<WarehouseEntity> list = warehouseService.lambdaQuery().list();
        if (CollUtil.isEmpty(list)) {
            log.info("没有启用的仓库");
            return ReturnT.SUCCESS;
        }

        List<String> removeRedisKeys = Lists.newArrayList();
        list.forEach(warehouse->{
            try {
                String redisKey = WmsRedisKeyEnum.WMS_WAREHOUSE_DETAIL_ID.keyBuilder(warehouse.getId());
                Object obj = redisService.getCacheObject(redisKey);
                if (Objects.nonNull(obj) ) {
                    removeRedisKeys.add(redisKey);
                }
            } catch (Exception e) {
                XxlJobHelper.log("仓库【{}】缓存清除失败",warehouse.getName());
                log.error("仓库【{}】缓存清除失败",warehouse.getName());
            }
        });
        if(CollUtil.isNotEmpty(removeRedisKeys)) {
            redisService.deleteObject(removeRedisKeys);
        }
        return ReturnT.SUCCESS;
    }

}