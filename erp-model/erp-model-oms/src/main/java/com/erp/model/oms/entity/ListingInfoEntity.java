package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 对应平台sku 表
 * </p>
 *
 * @author Lambda
 * @since 2023-08-18
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("listing_info")
public class ListingInfoEntity extends BaseEntity<ListingInfoEntity> {

    /**
     * sku no
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 产品名称
     */
    @TableField("product_name")
    private String productName;

    /**
     * 平台产品(spu) no或id
     */
    @TableField("platform_product_no")
    private String platformProductNo;

    /**
     * 平台sku no
     */
    @TableField("platform_sku_no")
    private String platformSkuNo;

    /**
     * 平台产品名称
     */
    @TableField("platform_product_name")
    private String platformProductName;

    /**
     * 平台
     */
    @TableField("platform")
    private String platform;

    /**
     * 类型
     * platform=平台
     * warehouse=仓库
     * {@link com.erp.model.oms.enums.RuleTypeEnum}
     */
    @TableField("type")
    private String type;

    /**
     * 匹配结果吧true 已匹配 false 未匹配
     */
    @TableField("match_result")
    private Boolean matchResult;

    /**
     * 产品图片 url
     */
    @TableField("product_image_url")
    private String productImageUrl;

    /**
     * 产品规格信息
     */
    @TableField("product_spec")
    private String productSpec;

    /**
     * 产品包装信息
     */
    @TableField("product_packing")
    private String productPacking;

    /**
     * 平台最后修改时间
     */
    @TableField(value = "platform_update_time")
    private LocalDateTime platformUpdateTime;


    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String PLATFORM_SKU_NO = "platform_sku_no";

    public static final String PLATFORM_PRODUCT_NAME = "platform_product_name";

    public static final String TYPE = "type";

    public static final String MATCH_RESULT = "match_result";

    public static final String PRODUCT_IMAGE_URL = "product_image_url";

    public static final String PRODUCT_SPEC = "product_spec";

    public static final String PRODUCT_PACKING = "product_packing";


    @Override
    public String toString() {
        return "ListingInfoEntity{" +
                ", productName='" + productName + '\'' +
                ", platformProductName='" + platformProductName + '\'' +
                ", productImageUrl='" + productImageUrl + '\'' +
                ", productSpec='" + productSpec + '\'' +
                ", productPacking='" + productPacking + '\'' +
                ", platformUpdateTime='" + platformUpdateTime + '\'' +
                '}';
    }

}
