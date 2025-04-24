package com.common.business.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class WdtSoOutStockDetailDTO implements Serializable {

    /**
     * 主表id
     */
    private String mainId;

    /**
     * sku id
     */
    private String skuId;

    /**
     * sku no
     */
    private String skuNo;

    /**
     * 应发数量
     */
    private Integer planQty;

    /**
     * 实发数量
     */
    private Integer actualQty;

    /**
     * 库位
     */
    private String warehouseLocation;


    /**
     * 仓库id
     */
    private String warehouseId;

    /**
     * 仓库名称
     */
    private String warehouseName;


    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 税率
     */
    private BigDecimal taxRate;

    /**
     * 销售金额
     */
    private BigDecimal amount;

    /**
     * 币种
     */
    private String currency;

    /**
     * 币种符号
     */
    private String currencySymbol;


    /**
     * 价税合计(本位币)
     */
    private BigDecimal allAmountLocalCurrency;

    /**
     * 汇率
     */
    private BigDecimal exchangeRate;

    /**
     * 销售明细id
     */
    private String soDetailId;

    /**
     * 备注
     */
    private String remark;

    /**
     * 来源明细id
     */
    private String sourceDetailId;

    /**
     * 来源明细id
     */
    private String platformCode;

    /**
     * 平台销售出库单明细ID
     */
    private String platformDetailId;

    private String approveStatus;

    private Boolean invalidStatus;

    private String soId;

    private List<PositionDetailsList> positionDetailsList;

    @Getter
    @Setter
    public static class PositionDetailsList implements Serializable {
        private String recId;
        private String stockoutDetailId;
        private String positionId;
        private String positionNo;
        private String batchNo;
        private String expireDate;
        private Integer positionGoodsCount;
    }
}
