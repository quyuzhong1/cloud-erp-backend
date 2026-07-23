package com.erp.model.plm.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * PDA扫码查询SKU返回：继承 {@link SkuVO} 全部字段，额外携带客户SKU。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PdaSearchSkuVO extends SkuVO {

    /**
     * 客户SKU（sku对照表 platform_sku_no）
     */
    private String platformSkuNo;
}
