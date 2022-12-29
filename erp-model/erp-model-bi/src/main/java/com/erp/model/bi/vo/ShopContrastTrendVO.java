package com.erp.model.bi.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ShopContrastTrendVO {
    /**
     * 店铺名称
     */
    public String shopName;

    /**
     * 销售信息
     */
    public ContrastTrendVO contrastTrendVO;
}
