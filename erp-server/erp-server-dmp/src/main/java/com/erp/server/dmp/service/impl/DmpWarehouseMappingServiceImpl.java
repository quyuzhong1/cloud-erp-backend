package com.erp.server.dmp.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.model.dmp.entity.DmpWarehouseMappingEntity;
import com.erp.server.dmp.mapper.DmpWarehouseMappingMapper;
import com.erp.server.dmp.service.DmpWarehouseMappingService;
import com.common.business.service.SuperServiceImpl;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 平台仓库映射表 服务实现类
 * </p>
 * @author zhangchunlin
 * @since 2023-06-27
 */
@Slf4j
@Service
public class DmpWarehouseMappingServiceImpl extends SuperServiceImpl<DmpWarehouseMappingMapper, DmpWarehouseMappingEntity> implements DmpWarehouseMappingService {

    @Override
    public DmpWarehouseMappingEntity getByWarehouseCode(String warehouseCode) {
        LambdaQueryWrapper<DmpWarehouseMappingEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DmpWarehouseMappingEntity::getWarehouseCode, warehouseCode).last("limit 1");
        return this.baseMapper.selectOne(queryWrapper);
    }

    @Override
    public Map<String, DmpWarehouseMappingEntity> getByWarehouseCodes(List<String> warehouseCodes) {
        LambdaQueryWrapper<DmpWarehouseMappingEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(DmpWarehouseMappingEntity::getWarehouseCode, warehouseCodes);
        List<DmpWarehouseMappingEntity> warehouseList = this.baseMapper.selectList(queryWrapper);
        if(CollUtil.isNotEmpty(warehouseList)) {
            return warehouseList.stream().collect(Collectors.toMap(DmpWarehouseMappingEntity::getWarehouseCode, Function.identity(), (o1, o2) -> o1));
        }
        return Maps.newHashMap();
    }

    @Override
    public DmpWarehouseMappingEntity getSourceWarehouseId(String sourceId, String platform) {
        LambdaQueryWrapper<DmpWarehouseMappingEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DmpWarehouseMappingEntity::getSourceId, sourceId)
                .eq(DmpWarehouseMappingEntity::getPlatformSign, platform).last("limit 1");
        return this.baseMapper.selectOne(queryWrapper);
    }

}
