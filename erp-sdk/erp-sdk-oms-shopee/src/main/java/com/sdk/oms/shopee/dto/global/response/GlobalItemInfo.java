package com.sdk.oms.shopee.dto.global.response;

import com.alibaba.fastjson.annotation.JSONField;
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
    @JSONField(name ="global_item_id")
    private Long id;

    /**
     * global_item_name
     */
    @JSONField(name ="global_item_name")
    private String globalItemName;

    private String description;

    @JSONField(name ="global_item_sku")
    private String globalItemSku;

    @JSONField(name ="global_item_status")
    private String globalItemStatus;

    @JSONField(name ="create_time")
    private Long createTime;

    @JSONField(name ="update_time")
    private Long updateTime;
    @JSONField(name ="stock_info")
    private List<StockInfo> stockInfos;
    @JSONField(name ="price_info")
    private List<PriceInfo> priceInfos;
    @JSONField(name ="image")
    private Image image;
    @JSONField(name ="weight")
    private float weight;
    @JSONField(name ="dimension")
    private Dimension dimension;
    @JSONField(name ="pre_order")
    private PreOrder preOrder;
    @JSONField(name ="size_chart")
    private String sizeChart;
    @JSONField(name ="condition")
    private String condition;
    @JSONField(name ="has_model")
    private Boolean hasModel;
    @JSONField(name ="video")
    private List<Video> videos;
    @JSONField(name ="brand")
    private Brand brand;
    /**
     * Image URLs of the item. It contains at most 9 URLs.
     */
    @JSONField(name = "attribute_list")
    private List<Attribute> attributes;
    @JSONField(name ="description_info")
    private DescriptionInfo descriptionInfo;
    private String description_type;
    /**
     * messages for Delete
     */
    private String msg;
}
