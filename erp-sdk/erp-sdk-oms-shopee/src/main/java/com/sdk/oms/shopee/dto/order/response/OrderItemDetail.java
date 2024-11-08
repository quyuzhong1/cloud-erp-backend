package com.sdk.oms.shopee.dto.order.response;

import cn.hutool.core.annotation.Alias;
import com.sdk.oms.shopee.dto.product.response.ImageInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
public class OrderItemDetail implements Serializable {

	private static final long serialVersionUID = 1L;

    @Alias( "item_id")
    private Long itemId;

    /**
     * Name of the item in local language.
     */
    @Alias( "item_name")
    private String itemName;

    /**
     * A item SKU (stock keeping unit) is an identifier defined by a seller,
     */
    @Alias( "item_sku")
    private String itemSku;

    @Alias( "model_id")
    private Long modelId;
    @Alias( "model_name")
    private String modelName;
    @Alias( "model_sku")
    private String modelSku;
    @Alias( "model_quantity_purchased")
    private Integer modelQuantityPurchased;
    @Alias( "model_original_price")
    private float modelOriginalPrice;
    @Alias( "model_discounted_price")
    private float modelDiscountedPrice;
    /**
     * TThis value indicates whether buyer buy the order item in wholesale price.
     */
    @Alias( "is_wholesale")
    private boolean isWholesale;
    @Alias( "weight")
    private float weight;
    @Alias( "add_on_deal")
    private boolean addOnDeal;
    @Alias( "main_item")
    private boolean mainItem;
    @Alias( "add_on_deal_id")
    private Long addOnDealId;
    @Alias( "promotion_type")
    private String promotionType;
    @Alias( "promotion_id")
    private Long promotionId;
    @Alias( "order_item_id")
    private Long orderItemId;

    /**
     * The identify of product promotion.
     */
    @Alias( "promotion_group_id")
    private Long promotionGroupId;
    @Alias( "image_info")
    private ImageInfo imageInfo;

    @Alias( "product_location_id")
    private List<String> productLocationId;

    @Alias( "is_prescription_item")
    private boolean isPrescriptionItem;

    @Alias( "is_b2c_owned_item")
    private boolean isB2cOwnedItem;
}
