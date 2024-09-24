package com.erp.sdk.oms.amz.spapi.dto;

import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.ReflectUtils;
import com.erp.sdk.oms.amz.spapi.enums.AmazonIdentifiersTypeEnum;
import com.erp.sdk.oms.amz.spapi.enums.AmazonMarketplaceEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;


/**
 * 商品报告实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ReportListingMongoDTO extends ReportSuperMongoDTO  {

    @Panno(findType = PannoEnum.EQ, field = "item-name")
    private String itemName;

    @Panno(findType = PannoEnum.EQ, field = "item-description")
    private String itemDescription;

    @Panno(findType = PannoEnum.EQ, field = "listing-id")
    private String listingId;

    @Panno(findType = PannoEnum.EQ, field = "seller-sku")
    private String sellerSku;

    @Panno(findType = PannoEnum.EQ, field = "price")
    private String price;

    @Panno(findType = PannoEnum.EQ, field = "quantity")
    private String quantity;

    @Panno(findType = PannoEnum.EQ, field = "open-date")
    private String openDate;

    @Panno(findType = PannoEnum.EQ, field = "image-url")
    private String imageUrl;

    @Panno(findType = PannoEnum.EQ, field = "item-is-marketplace")
    private String itemIsMarketplace;

    @Panno(findType = PannoEnum.EQ, field = "product-id-type")
    private String productIdType;

    @Panno(findType = PannoEnum.EQ, field = "zshop-shipping-fee")
    private String zshopShippingFee;

    @Panno(findType = PannoEnum.EQ, field = "item-note")
    private String itemNote;

    @Panno(findType = PannoEnum.EQ, field = "item-condition")
    private String itemCondition;

    @Panno(findType = PannoEnum.EQ, field = "zshop-category1")
    private String zshopCategory1;

    @Panno(findType = PannoEnum.EQ, field = "zshop-browse-path")
    private String zshopBrowsePath;

    @Panno(findType = PannoEnum.EQ, field = "zshop-storefront-feature")
    private String zshopStorefrontFeature;

    @Panno(findType = PannoEnum.EQ, field = "asin1")
    private String asin1;

    @Panno(findType = PannoEnum.EQ, field = "asin2")
    private String asin2;

    @Panno(findType = PannoEnum.EQ, field = "asin3")
    private String asin3;

    @Panno(findType = PannoEnum.EQ, field = "will-ship-internationally")
    private String willShipInternationally;

    @Panno(findType = PannoEnum.EQ, field = "expedited-shipping")
    private String expeditedShipping;

    @Panno(findType = PannoEnum.EQ, field = "zshop-boldface")
    private String zshopBoldface;

    @Panno(findType = PannoEnum.EQ, field = "product-id")
    private String productId;

    @Panno(findType = PannoEnum.EQ, field = "bid-for-featured-placement")
    private String bidForFeaturedPlacement;

    @Panno(findType = PannoEnum.EQ, field = "add-delete")
    private String addDelete;

    @Panno(findType = PannoEnum.EQ, field = "pending-quantity")
    private String pendingQuantity;

    @Panno(findType = PannoEnum.EQ, field = "fulfillment-channel")
    private String fulfillmentChannel;

    @Panno(findType = PannoEnum.EQ, field = "merchant-shipping-group")
    private String merchantShippingGroup;

    @Panno(findType = PannoEnum.EQ, field = "status")
    private String status;

    @Panno(findType = PannoEnum.EQ, field = "supportsDetailDownload")
    private Boolean supportsDetailDownload = true;


    /**
     * 转换IdentifiersType
     */
    public AmazonIdentifiersTypeEnum convertIdentifiersType() {
        if ("3".equals(this.productIdType)){
            return AmazonIdentifiersTypeEnum.UPC;
        } else {
            return AmazonIdentifiersTypeEnum.ASIN;
        }
    }

    /**
     * 检查IdentifiersType
     */
    public String checkAndGetIdentifier() {
        if (("4".equals(this.productIdType) || "2".equals(this.productIdType))&& StringUtils.isNotBlank(this.getAsin1())){
            return this.getAsin1();
        }
        return this.getProductId();
    }

    @Override
    public String convertBusinessUniqueKey() {
        Class<? extends ReportListingMongoDTO> subClass = this.getClass();
        StringBuilder sb = new StringBuilder();
        sb.append(subClass.getSimpleName()).append("{");
        Arrays.stream(ReflectUtil.getFields(subClass)).forEach(field -> {
            String fieldName = field.getName();
            // 指定字段字段不在忽略列表中，则将其添加到字符串表示形式中
            if (businessUniqueKeyFields().contains(fieldName)) {
                Object value = ReflectUtils.getFieldValue(this, fieldName);
                sb.append(fieldName).append("=").append(value).append(", ");
            }
        });
        // 删除最后一个逗号和空格
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("}");
        return DigestUtil.md5Hex(sb.toString());
    }

    private List<String> businessUniqueKeyFields(){
        return Arrays.asList(
                "requestShopId",
                "itemName",
                "itemDescription",
                "listingId",
                "sellerSku",
                "price",
                "quantity",
                "openDate",
                "imageUrl",
                "itemIsMarketplace",
                "productIdType",
                "zshopShippingFee",
                "itemNote",
                "itemCondition",
                "zshopCategory1",
                "zshopBrowsePath",
                "zshopStorefrontFeature",
                "asin1",
                "asin2",
                "asin3",
                "willShipInternationally",
                "expeditedShipping",
                "zshopBoldface",
                "productId",
                "bidForFeaturedPlacement",
                "addDelete",
                "pendingQuantity",
                "fulfillmentChannel",
                "merchantShippingGroup",
                "status"
        );
    }
}
