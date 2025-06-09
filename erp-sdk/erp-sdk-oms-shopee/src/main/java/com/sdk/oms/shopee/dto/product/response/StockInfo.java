package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName StockInfo
 * @description: TODO
 * @date 2023年10月24日
 * @version: 1.0
 */
@Data
public class StockInfo implements Serializable {

    @Alias( "summary_info")
    private SummaryInfo summaryInfo;

    @Alias( "seller_stock")
    private List<SellerStock> sellerStock;

    @Alias( "shopee_stock")
    private List<ShopeeStock> shopeeStock;
}
