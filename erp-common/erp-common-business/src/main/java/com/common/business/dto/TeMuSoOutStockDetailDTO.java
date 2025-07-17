package com.common.business.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeMuSoOutStockDetailDTO implements Serializable {

    /**
     * 平台仓库
     */
    private String platformWarehouseCode;

    /**
     * 平台sku编号
     */
    private String platformSkuNo;

    /**
     * 数量
     */
    private Integer qty;
}
