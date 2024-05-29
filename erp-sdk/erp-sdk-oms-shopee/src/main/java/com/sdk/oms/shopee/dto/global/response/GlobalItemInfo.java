package com.sdk.oms.shopee.dto.global.response;

import cn.hutool.core.annotation.Alias;
import com.sdk.oms.shopee.dto.product.response.Attribute;
import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName GlobalProductBase
 * @description: TODO
 * @date 2023年10月19日
 * @version: 1.0
 */
@Data
public class GlobalItemInfo implements Serializable {
    public static final long serialVersionUID = 1L;


    /**
     * global_item_id
     */
    @Alias("global_item_id")
    private Long id;

    /**
     * global_item_name
     */
    @Alias("global_item_name")
    private String globalItemName;

    private String description;

    @Alias("global_item_sku")
    private String globalItemSku;

    @Alias("global_item_status")
    private String globalItemStatus;

    @Alias("create_time")
    private Long createTime;

    @Alias("update_time")
    private Long updateTime;
    @Alias("stock_info")
    private List<StockInfo> stockInfos;
    @Alias("price_info")
    private List<PriceInfo> priceInfos;
    @Alias("image")
    private Image image;
    @Alias("weight")
    private float weight;
    @Alias("dimension")
    private Dimension dimension;
    @Alias("pre_order")
    private PreOrder preOrder;
    @Alias("size_chart")
    private String sizeChart;
    @Alias("condition")
    private String condition;
    @Alias("has_model")
    private Boolean hasModel;
    @Alias("video")
    private List<Video> videos;
    @Alias("brand")
    private Brand brand;
    /**
     * Image URLs of the item. It contains at most 9 URLs.
     */
    @Alias( "attribute_list")
    private List<Attribute> attributes;
    @Alias("description_info")
    private DescriptionInfo descriptionInfo;
    private String description_type;
    /**
     * messages for Delete
     */
    private String msg;
}
