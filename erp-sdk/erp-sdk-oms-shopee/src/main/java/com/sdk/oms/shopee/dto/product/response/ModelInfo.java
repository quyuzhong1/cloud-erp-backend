package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName ModelInfo
 * @description: TODO
 * @date 2024年10月15日
 * @version: 1.0
 */
@Data
public class ModelInfo implements Serializable {
    public static final long serialVersionUID = 1L;

    @Alias( "price_info")
    private List<PriceInfo> priceInfo;
    @Alias( "model_id")
    private Long modelId;
    @Alias( "model_name")
    private String modelName;
    @Alias( "tier_index")
    private List<Integer> tierIndex;
    @Alias( "promotion_id")
    private Long promotionId;
    @Alias( "model_sku")
    private String modelSku;
    @Alias( "model_status")
    private String modelStatus;
    @Alias( "pre_order")
    private PreOrder preOrder;
    @Alias( "stock_info_v2")
    private StockInfo stockInfo;
    @Alias( "gtin_code")
    private String gtinCode;
    @Alias( "weight")
    private String weight;
    @Alias( "dimension")
    private Dimension dimension;
    /**
     * 产品缩略图
     */
    private String imageUrl;
}
