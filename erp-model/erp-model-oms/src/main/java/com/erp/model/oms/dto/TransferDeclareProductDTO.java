package com.erp.model.oms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author zdy
 * @ClassName TransferDeclareProductDTO
 * @description: TODO
 * @date 2024年01月30日
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferDeclareProductDTO implements Serializable {

    private String soId;
    private String soCode;
    /**
     * 销售订单明细id
     */
    private String soDetailId;

    /**
     * 来源id[中转报关单据]
     */
    private String declareId;

    /**
     * 来源明细id[中转报关单据]
     */
    private String declareDetailId;

    /**
     * 数量
     */
    private Integer qty;
    /**
     * 产品sku编号
     */
    private String skuId;
    /**
     * 产品sku编号
     */
    private String skuNo;

    /**
     * 中文报关名称
     */
    private String declareChineseName;

    /**
     * 英文报关名称
     */
    private String declareEnglishName;

    /**
     * 申报价
     */
    private BigDecimal declarePrice;

    /**
     * 申报币种
     */
    private String currency;
}
