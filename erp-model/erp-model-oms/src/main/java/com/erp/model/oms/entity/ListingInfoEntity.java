package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
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
@NoArgsConstructor
@TableName("listing_info")
public class ListingInfoEntity extends BaseEntity<ListingInfoEntity> {

    /**
     * 平台sku no
     */
    @TableField("platform_sku_no")
    private String platformSkuNo;

    /**
     * 平台产品Sku名称
     */
    @TableField("platform_sku_name")
    private String platformSkuName;

    /**
     * 平台产品(spu) no或id
     */
    @TableField("platform_spu_no")
    private String platformSpuNo;
    /*
     * SKU
     */
    @TableField(exist = false)
    private String productSkuNo;

    /**
     * 平台产品SPU名称
     */
    @TableField("platform_spu_name")
    private String platformSpuName;

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
    private String matchResult;

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
     * 产品条码（三方仓商品条码）
     */
    @TableField("third_barcode")
    private String thirdBarcode;

    /**
     * 平台最后修改时间
     */
    @TableField(value = "platform_update_time")
    private LocalDateTime platformUpdateTime;

    /**
     * 平台SKU额外关联的FNSKU
     */
    @TableField("platform_fn_sku")
    private String platformFnSku;

    /**
     * 平台的Listing状态
     */
    @TableField("platform_status")
    private String platformStatus;

    /**
     * 平台的SKU id
     */
    @TableField("platform_sku_id")
    private String platformSkuId;

    /**
     * erp授权id
     */
    @TableField("auth_id")
    private String authId;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    /**
     * 来源类型 selfAdd系统新增，third第三方同步
     */
    @TableField("source_type")
    private String sourceType;
    /**
     * 标签url
     */
    @TableField("label_url")
    private String labelUrl;
    /**
     * 标签文件名称
     */
    @TableField("label_file_name")
    private String labelFileName;
    /**
     * 标签来源类型
     * LabelSourceTypeEnum
     */
    @TableField("label_source_type")
    private String labelSourceType;

    /**
     * 父平台产品ID（父ASIN）
     */
    @TableField("platform_parent_sku_id")
    private String platformParentSkuId;

    /**
     * ture:父产品
     */
    @TableField("is_parent")
    private Boolean isParent;

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
                "platformSkuNo='" + platformSkuNo + '\'' +
                ", platformSpuNo='" + platformSpuNo + '\'' +
                ", productImageUrl='" + productImageUrl + '\'' +
                ", productSpec='" + productSpec + '\'' +
                ", productPacking='" + productPacking + '\'' +
                ", platformUpdateTime=" + platformUpdateTime +
                ", platformFnSku='" + platformFnSku + '\'' +
                ", platformStatus='" + platformStatus + '\'' +
                ", platformSkuId='" + platformSkuId + '\'' +
                '}';
    }
}
