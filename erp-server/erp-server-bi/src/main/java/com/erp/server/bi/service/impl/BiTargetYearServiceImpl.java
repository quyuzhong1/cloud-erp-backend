package com.erp.server.bi.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.erp.model.bi.entity.BiTargetYearEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.bi.enums.DataSourceCostEnum;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.server.bi.mapper.BiTargetYearMapper;
import com.erp.server.bi.mapper.SalesOrderServiceMapper;
import com.erp.server.bi.service.BiDataSourceCostDetailService;
import com.erp.server.bi.service.BiTargetYearService;
import com.erp.server.bi.service.DmpRefundInfoService;
import com.erp.server.bi.service.DmpShopInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.bi.dto.BiTargetYearDTO;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;

import javax.annotation.Resource;

/**
 * <p>
 * 年度目标表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@Service
public class BiTargetYearServiceImpl extends SuperServiceImpl<BiTargetYearMapper, BiTargetYearEntity> implements BiTargetYearService {


    @Resource
    private SalesOrderServiceMapper salesOrderServiceMapper;

    @Resource
    private DmpRefundInfoService dmpRefundInfoService;



    @Resource
    private BiDataSourceCostDetailService biDataSourceCostDetailService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetYearDTO.AddDTO addDTO) {
        BiTargetYearEntity biTargetYearEntity = new BiTargetYearEntity();
        BeanMapperUtils.copy(addDTO, biTargetYearEntity);

        // 数据处理
        handleData(biTargetYearEntity);

        log.info("开始新增年度目标单");
        boolean save = super.save(biTargetYearEntity);
        if (!save) {
            throw new ServiceException("年度目标单保存失败");
        }

        return biTargetYearEntity.getId();
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(BiTargetYearDTO.UpdateDTO updateDTO) {
        BiTargetYearEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "年度目标单"));
        BiTargetYearEntity biTargetYearEntity = BeanMapperUtils.map(BiTargetYearEntity.class, updateDTO);

        // 数据处理
        handleData(biTargetYearEntity);
        log.info("编辑 开始修改年度目标单数据，id：【{}】", old.getId());
        boolean save = super.updateById(biTargetYearEntity);
        if (!save) {
            throw new ServiceException("年度目标单保存失败");
        }
        // TODO 修改明细数据（包含增删改）（如果有明细的话）

        return Boolean.TRUE;
    }

    /**
     * 获取指标完成值
     *
     * @param dto
     * @param flagStr
     * @return java.math.BigDecimal
     * @author yl
     * @date 2023-09-18 16:51
     */
    @Override
    public BigDecimal getMetricsFinishValue(BiTargetYearDTO.SearchDTO dto, String flagStr, Integer year, Integer month, String settleRate) {
        MetricsEnum metrics = dto.getMetrics();
        String yearFlag = "year";
        String yearStr = String.valueOf(year);
        String yearMonthStr = month > 9 ? yearStr + "-" + month : yearStr + "-0" + month;
        switch (metrics) {
            case SALES_QTY:
                //年度的
                if (yearFlag.equals(flagStr)) {
                    return salesOrderServiceMapper.getYearQtyByYear(dto, yearStr);
                } else {
                    return salesOrderServiceMapper.getMonthQty(dto);
                }

            case SALES_AMOUNT:
                //年度
                if (yearFlag.equals(flagStr)) {
                    return salesOrderServiceMapper.getYearSalesAmountByYear(dto, yearStr, settleRate);
                } else {
                    return salesOrderServiceMapper.getMonthAmount(dto, settleRate);
                }

            case NET_SALES_AMOUNT:
                //净销售额
                //年度
                if (yearFlag.equals(flagStr)) {
                    //销售额
                    BigDecimal yearOrderAmount = salesOrderServiceMapper.getYearSalesAmountByYear(dto, yearStr, settleRate);
                    //年退款金额
                    BigDecimal yearRefundOrderAmount = dmpRefundInfoService.getYearRefundOrderAmount(dto, yearStr);
                    return yearOrderAmount.subtract(yearRefundOrderAmount);
                } else {
                    //退款金额
                    BigDecimal refundOrderAmount = dmpRefundInfoService.getRefundOrderAmount(dto);
                    BigDecimal monthAmount = salesOrderServiceMapper.getMonthAmount(dto, settleRate);
                    if (Objects.isNull(monthAmount)) {
                        monthAmount = BigDecimal.ZERO;
                    }
                    return monthAmount.subtract(refundOrderAmount);
                }
                //财务销售额
            case FINANCE_SALES_AMOUNT:
                //年
                if (yearFlag.equals(flagStr)) {
                    return biDataSourceCostDetailService.yearByCostType(yearStr, DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(),dto);
                } else {
                    //月
                    return biDataSourceCostDetailService.monthByCostType(yearMonthStr, DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(),dto);
                }
                //毛利额
            case GROSS_PROFIT:
                //年
                if (yearFlag.equals(flagStr)) {
                    //主营业务收入
                    BigDecimal mainBusinessIncome = biDataSourceCostDetailService.yearByCostType(yearStr, DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), dto);
                    //成本合计
                    BigDecimal costTotalCost = biDataSourceCostDetailService.yearByCostType(yearStr, DataSourceCostEnum.COST_TOTALCOST.getCode(), dto);
                    return mainBusinessIncome.subtract(costTotalCost);
                } else {
                    //月
                    BigDecimal monthMainBusinessIncome = biDataSourceCostDetailService.monthByCostType(yearMonthStr, DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), dto);
                    BigDecimal monthCostTotalCost = biDataSourceCostDetailService.monthByCostType(yearMonthStr, DataSourceCostEnum.COST_TOTALCOST.getCode(), dto);
                    return monthMainBusinessIncome.subtract(monthCostTotalCost);
                }

                //毛利率
            case GROSS_PROFIT_RATE:
                //年
                if (yearFlag.equals(flagStr)) {
                    //主营业务收入
                    BigDecimal mainBusinessIncome = biDataSourceCostDetailService.yearByCostType(yearStr, DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), dto);
                    //成本合计
                    BigDecimal costTotalCost = biDataSourceCostDetailService.yearByCostType(yearStr, DataSourceCostEnum.COST_TOTALCOST.getCode(), dto);
                    //差值
                    BigDecimal grossProfit = mainBusinessIncome.subtract(costTotalCost);
                    return MathUtil.divide(grossProfit,mainBusinessIncome,2);

                }else{
                    //月
                    BigDecimal monthMainBusinessIncome = biDataSourceCostDetailService.monthByCostType(yearMonthStr, DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), dto);
                    BigDecimal monthCostTotalCost = biDataSourceCostDetailService.monthByCostType(yearMonthStr, DataSourceCostEnum.COST_TOTALCOST.getCode(), dto);
                    //差值
                    BigDecimal monthGrossProfit = monthMainBusinessIncome.subtract(monthCostTotalCost);
                    return MathUtil.divide(monthGrossProfit,monthMainBusinessIncome,2);
                }

        }

        return BigDecimal.ZERO;
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(BiTargetYearEntity biTargetYearEntity) {
        // TODO 验证数据 & 数据赋值
    }
}
