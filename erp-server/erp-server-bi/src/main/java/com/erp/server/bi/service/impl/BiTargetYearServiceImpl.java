package com.erp.server.bi.service.impl;


import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.bi.entity.BiTargetYearEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.bi.enums.DataSourceCostEnum;
import com.erp.model.bi.enums.MetricsEnum;
import com.erp.server.bi.enums.DateTypeEnum;
import com.erp.server.bi.mapper.BiTargetYearMapper;
import com.erp.server.bi.mapper.SalesOrderServiceMapper;
import com.erp.server.bi.service.BiDataSourceCostDetailService;
import com.erp.server.bi.service.BiTargetYearService;
import com.erp.server.bi.service.BiRefundInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.bi.dto.BiTargetYearDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.*;

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
    private BiRefundInfoService biRefundInfoService;


    @Resource
    private BiDataSourceCostDetailService biDataSourceCostDetailService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String add(BiTargetYearDTO.AddDTO addDTO) {
        BiTargetYearEntity biTargetYearEntity = new BiTargetYearEntity();
        BeanMapperUtils.copy(addDTO, biTargetYearEntity);

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

        log.info("编辑 开始修改年度目标单数据，id：【{}】", old.getId());
        boolean save = super.updateById(biTargetYearEntity);
        if (!save) {
            throw new ServiceException("年度目标单保存失败");
        }
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
    public BigDecimal getMetricsFinishValue(BiTargetYearDTO.SearchDTO dto, String flagStr, String yearMonth, String settleRate) {
        MetricsEnum metrics = dto.getMetrics();
        String yearFlag = "year";
        switch (metrics) {
            case SALES_QTY:
                //年度的
                if (yearFlag.equals(flagStr)) {
                    setYearDate(dto, yearMonth);
                    dto.setDateType(DateTypeEnum.YEAR.getCode());
                } else {
                    setMonthDate(dto, yearMonth);
                    dto.setDateType(DateTypeEnum.MONTH.getCode());
                }
                return salesOrderServiceMapper.getQty(dto);
            case SALES_AMOUNT:
                //年度
                if (yearFlag.equals(flagStr)) {
                    setYearDate(dto, yearMonth);
                    dto.setDateType(DateTypeEnum.YEAR.getCode());
                } else {
                    setMonthDate(dto, yearMonth);
                    dto.setDateType(DateTypeEnum.MONTH.getCode());
                }
                return salesOrderServiceMapper.getAmount(dto, settleRate);
            case NET_SALES_AMOUNT:
                //净销售额
                //年度
                if (yearFlag.equals(flagStr)) {
                    setYearDate(dto, yearMonth);
                    //销售额
                    BigDecimal yearOrderAmount = salesOrderServiceMapper.netSalesAmount(dto, settleRate);
                    //年退款金额
                    BigDecimal yearRefundOrderAmount = biRefundInfoService.getRefundOrderAmount(dto);
                    return MathUtil.subtract(yearOrderAmount, yearRefundOrderAmount);
                } else {
                    setMonthDate(dto, yearMonth);
                    //退款金额
                    BigDecimal refundOrderAmount = biRefundInfoService.getRefundOrderAmount(dto);
                    BigDecimal monthAmount = salesOrderServiceMapper.netSalesAmount(dto, settleRate);
                    if (Objects.isNull(monthAmount)) {
                        monthAmount = BigDecimal.ZERO;
                    }
                    return MathUtil.subtract(monthAmount, refundOrderAmount);
                }
                //财务销售额
            case FINANCE_SALES_AMOUNT:
                LocalDate date = setMonthDate(dto, yearMonth);
                String yearStr = String.valueOf(date.getYear());
                DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                        .appendPattern(DateUtil.fmt_month)
                        .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                        .toFormatter();
                String yearMonthStr = date.format(fmt);
                //年
                if (yearFlag.equals(flagStr)) {
                    return biDataSourceCostDetailService.yearByCostType(yearStr, DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), dto);
                } else {
                    //月
                    return biDataSourceCostDetailService.monthByCostType(yearMonthStr, DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), dto);
                }
                //毛利额
            case GROSS_PROFIT:
                LocalDate grossProfitDate = setMonthDate(dto, yearMonth);
                String grossProfitYearStr = String.valueOf(grossProfitDate.getYear());
                DateTimeFormatter grossProfitFmt = new DateTimeFormatterBuilder()
                        .appendPattern(DateUtil.fmt_month)
                        .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                        .toFormatter();
                String grossProfitYearMonthStr = grossProfitDate.format(grossProfitFmt);
                //年
                if (yearFlag.equals(flagStr)) {
                    //主营业务收入
                    BigDecimal mainBusinessIncome = biDataSourceCostDetailService.yearByCostType(grossProfitYearStr, DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), dto);
                    //成本合计
                    BigDecimal costTotalCost = biDataSourceCostDetailService.yearByCostType(grossProfitYearStr, DataSourceCostEnum.COST_TOTALCOST.getCode(), dto);
                    return MathUtil.subtract(mainBusinessIncome, costTotalCost);
                } else {
                    //月
                    BigDecimal monthMainBusinessIncome = biDataSourceCostDetailService.monthByCostType(grossProfitYearMonthStr, DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), dto);
                    BigDecimal monthCostTotalCost = biDataSourceCostDetailService.monthByCostType(grossProfitYearMonthStr, DataSourceCostEnum.COST_TOTALCOST.getCode(), dto);
                    return MathUtil.subtract(monthMainBusinessIncome, monthCostTotalCost);
                }

                //毛利率
            case GROSS_PROFIT_RATE:
                BigDecimal multiplyFlag = MathUtil.BigDecimal_100;
                LocalDate grossProfitRateDate = setMonthDate(dto, yearMonth);
                String grossProfitRateYearStr = String.valueOf(grossProfitRateDate.getYear());
                DateTimeFormatter grossProfitRateFmt = new DateTimeFormatterBuilder()
                        .appendPattern(DateUtil.fmt_month)
                        .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                        .toFormatter();
                String grossProfitRateYearMonthStr = grossProfitRateDate.format(grossProfitRateFmt);

                //年
                if (yearFlag.equals(flagStr)) {
                    //主营业务收入
                    BigDecimal mainBusinessIncome = biDataSourceCostDetailService.yearByCostType(grossProfitRateYearStr, DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), dto);
                    //成本合计
                    BigDecimal costTotalCost = biDataSourceCostDetailService.yearByCostType(grossProfitRateYearStr, DataSourceCostEnum.COST_TOTALCOST.getCode(), dto);
                    //差值
                    BigDecimal grossProfit = mainBusinessIncome.subtract(costTotalCost);
                    BigDecimal yearGrossProfitRate = MathUtil.divide(grossProfit, mainBusinessIncome, 2);
                    yearGrossProfitRate = MathUtil.multiply(yearGrossProfitRate, multiplyFlag, 2);

                    return yearGrossProfitRate;

                } else {
                    //月
                    BigDecimal monthMainBusinessIncome = biDataSourceCostDetailService.monthByCostType(grossProfitRateYearMonthStr, DataSourceCostEnum.COST_MAINBUSINESSINCOME.getCode(), dto);
                    BigDecimal monthCostTotalCost = biDataSourceCostDetailService.monthByCostType(grossProfitRateYearMonthStr, DataSourceCostEnum.COST_TOTALCOST.getCode(), dto);
                    //差值
                    BigDecimal monthGrossProfit = monthMainBusinessIncome.subtract(monthCostTotalCost);
                    BigDecimal monthGrossProfitRate = MathUtil.divide(monthGrossProfit, monthMainBusinessIncome, 2);
                    monthGrossProfitRate = MathUtil.multiply(monthGrossProfitRate, multiplyFlag, 2);
                    return monthGrossProfitRate;
                }

            default:
                return BigDecimal.ZERO;
        }

    }

    /**
     * 设置年度日期
     *
     * @param dto
     */
    private LocalDate setYearDate(BiTargetYearDTO.SearchDTO dto, String yearMonth) {
        if (StringUtils.isNotBlank(yearMonth) && yearMonth.length() >= 7) {
            DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                    .appendPattern(DateUtil.fmt_month)
                    .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                    .toFormatter();
            LocalDate yearMonthDate = LocalDate.parse(yearMonth, fmt);
            LocalDateTime yearStart = LocalDateUtil.getThisYearStart(yearMonthDate);
            dto.setStartTime(yearStart);
            dto.setEndTime(yearStart.plusYears(1));
            return yearMonthDate;
        } else {
            LocalDate now = LocalDate.now();
            LocalDateTime localYearStart = LocalDateUtil.getThisYearStart(now);
            dto.setStartTime(localYearStart);
            dto.setEndTime(localYearStart.plusYears(1));
            return now;
        }
    }


    /**
     * 设置年度日期
     *
     * @param dto
     */
    private LocalDate setMonthDate(BiTargetYearDTO.SearchDTO dto, String yearMonth) {
        if (StringUtils.isNotBlank(yearMonth) && yearMonth.length() >= 7) {
            DateTimeFormatter fmt = new DateTimeFormatterBuilder()
                    .appendPattern(DateUtil.fmt_month)
                    .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                    .toFormatter();
            LocalDate yearMonthDate = LocalDate.parse(yearMonth, fmt);
            LocalDateTime localDateTime = LocalDate.parse(dto.getYearMonth(), fmt).atStartOfDay();
            dto.setStartTime(localDateTime);
            dto.setEndTime(localDateTime.plusMonths(1));
            return yearMonthDate;
        } else {
            LocalDate now = LocalDate.now();
            LocalDateTime localDateTime = LocalDate.of(now.getYear(), now.getMonth(), 1).atStartOfDay();
            dto.setStartTime(localDateTime);
            dto.setEndTime(localDateTime.plusMonths(1));
            return now;
        }
    }


}
