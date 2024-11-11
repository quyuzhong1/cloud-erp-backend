package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class EstimationResultDTO {

    /**
     * 日期
     */
    private List<LocalDate> date;

    /**
     * 库存
     */
    private List<BigDecimal> inventoryQty;

    /**
     * 试算库存
     */
    private List<BigDecimal> calcInventoryQty;

    /**
     * 预计到货日期
     */
    private List<LocalDate> planArrivalDate;

    /**
     * 断货日期
     */
    private List<LocalDate> outOfStockDate;

    /**
     * 试算预计到货日期
     */
    private List<LocalDate> calcPlanArrivalDate;

    /**
     * 试算断货日期
     */
    private List<LocalDate> calcOutOfStockDate;
}
