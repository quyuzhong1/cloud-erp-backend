package com.sdk.oms.shopee.dto.product.response;

import cn.hutool.core.annotation.Alias;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @ClassName Image
 * @description: TODO
 * @date 2023年10月20日
 * @version: 1.0
 */
@Data
@ToString(callSuper = true)
public class ItemInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Alias( "item_id")
    private Long itemId;

    /**
     * shop id of the item
     */
    @Alias( "category_id")
    private Long categoryId;
    /**
     * Name of the item in local language.
     */
    @Alias( "item_name")
    private String itemName;

    /**
     * Description of the item in local language.
     */
    private String description;

    /**
     * An item SKU (stock keeping unit) is an identifier defined by a seller,
     * sometimes called parent SKU.
     */
    @Alias( "item_sku")
    private String itemSku;

    /**
     * Timestamp that indicates the date and time that the item was created.
     */
    @Alias( "create_time")
    private Long createTime;

    /**
     * Timestamp that indicates the last time that there was a change
     * in value of the item, such as price/stock change.
     */
    @Alias( "update_time")
    private Long updateTime;

    /**
     * The current price of the item in the listing currency.
     */
    private String weight;

    /**
     * Is it second-hand.
     */
    private String condition;
    /**
     * Url of size chart image.
     */
    @Alias( "size_chart")
    private String sizeChart;
    /**
     * Enumerated type that defines the current status of the item. Applicable values: NORMAL, DELETED, BANNED and UNLIST.
     */
    @Alias( "item_status")
    private String itemStatus;
    /**
     * Does it contain model.
     */
    @Alias( "has_model")
    private boolean hasModel;

    /**
     * Does it contain model.
     */
    @Alias( "promotion_id")
    private Long promotionId;

    /**
     * Does it contain model.
     */
    @Alias( "item_dangerous")
    private Long itemDangerous;


    /**
     * Type of description : values: See Data Definition- description_type (normal , extended).
     */
    @Alias( "description_type")
    private String descriptionType;

    /**
     * (only TW seller and BR Local seller available) This field will return when the item has no model and TW seller or BR local seller have uploaded the gtin_code.
     */
    @Alias( "gtin_code")
    private String gtinCode;

    /**
     * Image URLs of the item. It contains at most 9 URLs.
     */
    @Alias( "attribute_list")
    private List<Attribute> attributeList;

    /**
     * The three-digit code representing the currency unit used for the item in Shopee Listings.
     */
    @Alias( "price_info")
    private List<PriceInfo> priceInfo;



    /**
     * the net weight of this item, the unit is KG.
     */
    @Alias( "image")
    private Image image;

    /**
     * Should call shopee.item.GetCategories to get category first.
     * Related to result.categories.category_id
     */
    @Alias( "dimension")
    private Dimension dimension;

    /**
     * The original price of the item in the listing currency.
     */
    @Alias( "logistic_info")
    private List<Logistics> logisticInfo;

    /**
     * The variation of item is to list out all models of this product
     */
    @Alias( "pre_order")
    private PreOrder preOrder;

    /**
     * Attributes
     */
    @Alias( "wholesales")
    List<WholeSale> wholesales;

    /**
     * Logistics
     */
    @Alias( "video_info")
    private List<Video> videoInfo;

    /**
     * The length of package for this single item, the unit is CM
     */
    @Alias( "brand")
    private Brand brand;

    /**
     * Time for a warranty claim.Value should be in one of ONE_YEAR TWO_YEARS OVER_TWO_YEARS.
     */
    @Alias( "complaint_policy")
    private ComplaintPolicy complaintPolicy;

    /**
     * Tax information
     */
    @Alias( "tax_info")
    private TaxInfo taxInfo;

    /**
     * new stock object.
     *
     * Please check this FAQ for more detail: https://open.shopee.com/faq?top=162&sub=166&page=1&faq=230
     */
    @Alias( "stock_info_v2")
    private StockInfo stockInfoV2;

    /**
     *New description field. Only whitelist sellers can use it.
     */
    @Alias( "description_info")
    private DescriptionInfo descriptionInfo;
}
