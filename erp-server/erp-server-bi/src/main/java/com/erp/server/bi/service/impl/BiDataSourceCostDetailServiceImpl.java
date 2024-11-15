package com.erp.server.bi.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.MathUtil;
import com.erp.model.bi.dto.BiDataSourceCostDTO;
import com.erp.model.bi.dto.BiFilterDTO;
import com.erp.model.bi.entity.BiDataSourceCostDetailEntity;
import com.erp.model.bi.enums.DataSourceCostEnum;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.model.dmp.entity.BiShopInfoEntity;
import com.erp.server.bi.mapper.BiDataSourceCostDetailMapper;
import com.erp.server.bi.service.BiDataSourceCostDetailService;
import com.erp.server.bi.service.BiShopInfoService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
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

    @Resource
    private BiShopInfoService shopInfoService;

    @Override
    public HashMap<String, Map<String, BigDecimal>> convertListByCostIds(List<String> costIds, List<String> dictValues) {
        if (CollectionUtils.isEmpty(costIds) || CollectionUtils.isEmpty(dictValues)) {
            return new HashMap<>(0);
        }
        List<BiDataSourceCostDetailEntity> detailEntities = lambdaQuery()
                .in(BiDataSourceCostDetailEntity::getCostId, costIds)
                .in(BiDataSourceCostDetailEntity::getCostType, dictValues)
                .list();

        if (CollUtil.isEmpty(detailEntities)) {
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
        queryWrapper.in(BiDataSourceCostDetailEntity::getCostId, costIds);
        return this.list(queryWrapper);
    }

    @Override
    public void removeByCostId(String costId) {
        LambdaUpdateWrapper<BiDataSourceCostDetailEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(BiDataSourceCostDetailEntity::getCostId, costId);
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
        List<String> shopNameList = dto.getShopName();
        List<BiShopInfoEntity> shopInfoList = shopInfoService.listByNames(shopNameList);
        List<String> shopIdList = shopInfoList.stream().map(BiShopInfoEntity::getId).collect(Collectors.toList());
        return baseMapper.monthByCostType(yearMonthStr, costType, dto,shopIdList);
    }


    /**
     * 获取月份的目标值
     *
     * @param dto
     * @return
     */
    @Override
    public List<BiDataSourceCostDTO.DataValueDTO> listGrossMonth(BiFilterDTO dto) {
        //主营业务收入
        String mainBusinessIncome = DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode();
        List<BiDataSourceCostDTO.DataValueDTO> mainIncomeList = baseMapper.listMonthByCostType(mainBusinessIncome, dto, 12);
        //成本合计
        String totalcost = DataSourceCostEnum.COST_TOTALCOST.getCode();
        //成本的
        List<BiDataSourceCostDTO.DataValueDTO> costList = baseMapper.listMonthByCostType(totalcost, dto, 12);

        List<BiDataSourceCostDTO.DataValueDTO> resultList = new ArrayList<>(mainIncomeList.size());
        //毛利额
        String grossProfit = MetricsEnum.GROSS_PROFIT.getCode();
        //毛利率
        String grossProfitRate = MetricsEnum.GROSS_PROFIT_RATE.getCode();
        for (BiDataSourceCostDTO.DataValueDTO item : mainIncomeList) {
            //毛利额
            BiDataSourceCostDTO.DataValueDTO profitResult = new BiDataSourceCostDTO.DataValueDTO();
            String dataStr = item.getDateStr();
            profitResult.setType(grossProfit);
            profitResult.setDateStr(item.getDateStr());
            BigDecimal mainIncome = item.getValue();
            BigDecimal cost = costList.stream().filter(c -> c.getDateStr().equals(dataStr)).
                    findFirst().map(BiDataSourceCostDTO.DataValueDTO::getValue).orElse(BigDecimal.ZERO);
            BigDecimal grossProfitValue = MathUtil.subtract(mainIncome, cost);
            profitResult.setValue(grossProfitValue);
            resultList.add(profitResult);
            //毛利率
            BiDataSourceCostDTO.DataValueDTO profitRateResult = new BiDataSourceCostDTO.DataValueDTO();
            profitRateResult.setType(grossProfitRate);
            profitRateResult.setDateStr(item.getDateStr());
            BigDecimal grossProfitRateValue = MathUtil.divide(grossProfitValue, mainIncome, 4);
            grossProfitRateValue = MathUtil.multiply(grossProfitRateValue, MathUtil.BigDecimal_100);
            profitRateResult.setValue(grossProfitRateValue);
            resultList.add(profitRateResult);
        }
        return resultList;
    }

    /**
     * 获取年度毛利值
     *
     * @param dto
     * @return java.util.List<com.erp.model.bi.dto.BiDataSourceCostDTO.DataValueDTO>
     * @author yl
     * @date 2023-09-22 14:39
     */
    @Override
    public List<BiDataSourceCostDTO.DataValueDTO> listGrossYear(BiDataSourceCostDTO.GrossProfitDTO dto) {
        //主营业务收入
        String mainBusinessIncome = DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode();
        List<BiDataSourceCostDTO.DataValueDTO> mainIncomeList = baseMapper.listYearByCostType(mainBusinessIncome, dto, 12);
        //成本合计
        String totalcost = DataSourceCostEnum.COST_TOTALCOST.getCode();
        //成本的
        List<BiDataSourceCostDTO.DataValueDTO> costList = baseMapper.listYearByCostType(totalcost, dto, 12);

        List<BiDataSourceCostDTO.DataValueDTO> resultList = new ArrayList<>(mainIncomeList.size());
        //毛利额
        String grossProfit = MetricsEnum.GROSS_PROFIT.getCode();
        //毛利率
        String grossProfitRate = MetricsEnum.GROSS_PROFIT_RATE.getCode();
        for (BiDataSourceCostDTO.DataValueDTO item : mainIncomeList) {
            //毛利额
            BiDataSourceCostDTO.DataValueDTO profitResult = new BiDataSourceCostDTO.DataValueDTO();
            String dataStr = item.getDateStr();
            profitResult.setType(grossProfit);
            profitResult.setDateStr(item.getDateStr());
            BigDecimal mainIncome = item.getValue();
            BigDecimal cost = costList.stream().filter(c -> c.getDateStr().equals(dataStr)).
                    findFirst().map(BiDataSourceCostDTO.DataValueDTO::getValue).orElse(BigDecimal.ZERO);
            BigDecimal grossProfitValue = MathUtil.subtract(mainIncome, cost);
            profitResult.setValue(grossProfitValue);
            resultList.add(profitResult);

            //毛利率
            BiDataSourceCostDTO.DataValueDTO profitRateResult = new BiDataSourceCostDTO.DataValueDTO();
            profitRateResult.setType(grossProfitRate);
            profitRateResult.setDateStr(item.getDateStr());
            BigDecimal grossProfitRateValue = MathUtil.divide(grossProfitValue, mainIncome, 4);
            grossProfitRateValue = MathUtil.multiply(grossProfitRateValue, MathUtil.BigDecimal_100);
            profitRateResult.setValue(grossProfitRateValue);
            resultList.add(profitRateResult);
        }
        return resultList;
    }


    /**
     * 获取季度值
     * @param dto
     * @return
     */
    @Override
    public List<BiDataSourceCostDTO.DataValueDTO> listGrossQuarter(BiDataSourceCostDTO.GrossProfitDTO dto) {
        //主营业务收入
        String mainBusinessIncome = DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode();
        List<BiDataSourceCostDTO.DataValueDTO> mainIncomeList = baseMapper.listQuarterByCostType(mainBusinessIncome, dto, 12);
        //成本合计
        String totalcost = DataSourceCostEnum.COST_TOTALCOST.getCode();
        //成本的
        List<BiDataSourceCostDTO.DataValueDTO> costList = baseMapper.listQuarterByCostType(totalcost, dto, 12);

        List<BiDataSourceCostDTO.DataValueDTO> resultList = new ArrayList<>(mainIncomeList.size());
        //毛利额
        String grossProfit = MetricsEnum.GROSS_PROFIT.getCode();
        //毛利率
        String grossProfitRate = MetricsEnum.GROSS_PROFIT_RATE.getCode();
        for (BiDataSourceCostDTO.DataValueDTO item : mainIncomeList) {
            //毛利额
            BiDataSourceCostDTO.DataValueDTO profitResult = new BiDataSourceCostDTO.DataValueDTO();
            String dataStr = item.getDateStr();
            profitResult.setType(grossProfit);
            profitResult.setDateStr(item.getDateStr());
            BigDecimal mainIncome = item.getValue();
            BigDecimal cost = costList.stream().filter(c -> c.getDateStr().equals(dataStr)).
                    findFirst().map(BiDataSourceCostDTO.DataValueDTO::getValue).orElse(BigDecimal.ZERO);
            BigDecimal grossProfitValue = MathUtil.subtract(mainIncome, cost);
            profitResult.setValue(grossProfitValue);
            resultList.add(profitResult);

            //毛利率
            BiDataSourceCostDTO.DataValueDTO profitRateResult = new BiDataSourceCostDTO.DataValueDTO();
            profitRateResult.setType(grossProfitRate);
            profitRateResult.setDateStr(item.getDateStr());
            BigDecimal grossProfitRateValue = MathUtil.divide(grossProfitValue, mainIncome, 4);
            grossProfitRateValue = MathUtil.multiply(grossProfitRateValue, MathUtil.BigDecimal_100);
            profitRateResult.setValue(grossProfitRateValue);
            resultList.add(profitRateResult);
        }
        return resultList;
    }

    /**
     * 获取成本根据类型
     *
     * @param costType
     * @return
     */
    @Override
    public BigDecimal yearByCostType(String year, String costType, BiFilterDTO dto) {
        List<String> shopNameList = dto.getShopName();
        List<BiShopInfoEntity> shopInfoList = shopInfoService.listByNames(shopNameList);
        List<String> shopIdList = shopInfoList.stream().map(BiShopInfoEntity::getId).collect(Collectors.toList());
        return baseMapper.yearByCostType(year, costType, dto,shopIdList);
    }
}
