package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.bi.entity.BiDataSourceCostDetailEntity;
import com.erp.server.bi.mapper.BiDataSourceCostDetailMapper;
import com.erp.server.bi.service.BiDataSourceCostDetailService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/15 18:14
 */
@Service
public class BiDataSourceCostDetailServiceImpl extends ServiceImpl<BiDataSourceCostDetailMapper, BiDataSourceCostDetailEntity>
        implements BiDataSourceCostDetailService {
    @Override
    public HashMap<String, Map<String, BigDecimal>> convertListByCostIds(List<String> costIds, List<String> dictValues) {
        if (CollectionUtils.isEmpty(costIds) || CollectionUtils.isEmpty(dictValues) ){
            return new HashMap<>(0);
        }
        List<BiDataSourceCostDetailEntity> detailEntities = lambdaQuery()
                .in(BiDataSourceCostDetailEntity::getId, costIds)
                .eq(BiDataSourceCostDetailEntity::getCostType, dictValues)
                .list();

        Map<String, List<BiDataSourceCostDetailEntity>> detailMap = detailEntities.stream()
                .collect(Collectors.groupingBy(BiDataSourceCostDetailEntity::getCostId));
        HashMap<String, Map<String, BigDecimal>> entityMap = new HashMap<>(detailMap.keySet().size());
        detailMap.keySet().stream().forEach(x -> {
            List<BiDataSourceCostDetailEntity> detailList = detailMap.get(x);
            HashMap<String, BigDecimal> tempMap = new HashMap<>(detailList.size());
            detailList.stream().forEach(m -> {
                tempMap.put(m.getCostType(), m.getCostValue());
            });
            entityMap.put(x, tempMap);
        });


        return entityMap;
    }

    @Override
    public List<BiDataSourceCostDetailEntity> listByCostIds(List<String> costIds) {
        LambdaQueryWrapper<BiDataSourceCostDetailEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(BiDataSourceCostDetailEntity::getCostId,costIds);
        return this.list(queryWrapper);
    }

    @Override
    public void removeByCostId(String costId) {
        LambdaUpdateWrapper<BiDataSourceCostDetailEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(BiDataSourceCostDetailEntity::getCostId,costId);
        this.remove(updateWrapper);
    }
}
