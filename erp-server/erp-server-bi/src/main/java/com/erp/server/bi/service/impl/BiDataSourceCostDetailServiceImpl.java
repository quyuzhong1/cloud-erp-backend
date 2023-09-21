package com.erp.server.bi.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.model.bi.dto.BiFilterDTO;
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
                .in(BiDataSourceCostDetailEntity::getCostId, costIds)
                .in(BiDataSourceCostDetailEntity::getCostType, dictValues)
                .list();

        if(CollectionUtil.isEmpty(detailEntities)){
            return new HashMap<>(0);
        }
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


    /**
     * 获取月度值
     *
     * @param yearMonthStr
     * @param costType
     * @param dto
     * @return
     */
    @Override
    public BigDecimal monthByCostType(String yearMonthStr, String costType, BiFilterDTO dto) {
        return baseMapper.monthByCostType(yearMonthStr,costType,dto);
    }

    /**
     * 获取成本根据类型
     * @param costType
     * @return
     */
    @Override
    public BigDecimal yearByCostType(String year,String costType,BiFilterDTO dto) {
        return baseMapper.yearByCostType(year,costType,dto);
    }
}
