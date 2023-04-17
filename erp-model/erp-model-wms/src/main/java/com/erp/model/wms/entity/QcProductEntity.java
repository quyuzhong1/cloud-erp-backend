package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("qc_product")
public class QcProductEntity extends BaseEntity<QcProductEntity> {

    /**
     * 质检单id
     */
    @TableField("mian_id")
    private String mianId;

    /**
     * sku id
     */
    @TableField("sku_id")
    private String skuId;

    /**
     * sku no
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 产品等级信息
     */
    @TableField("product_grade")
    private String productGrade;

    /**
     * 产品长度
     */
    @TableField("product_length")
    private BigDecimal productLength;

    /**
     * 产品宽
     */
    @TableField("product_width")
    private BigDecimal productWidth;

    /**
     * 产品高
     */
    @TableField("product_height")
    private BigDecimal productHeight;

    /**
     * 外箱长
     */
    @TableField("box_length")
    private BigDecimal boxLength;

    /**
     * 外箱宽
     */
    @TableField("box_width")
    private BigDecimal boxWidth;

    /**
     * 外箱高
     */
    @TableField("box_height")
    private BigDecimal boxHeight;

    /**
     * 产品净重
     */
    @TableField("product_net_weight")
    private BigDecimal productNetWeight;

    /**
     * 外箱重量
     */
    @TableField("box_weight")
    private BigDecimal boxWeight;

    /**
     * 变体信息
     */
    @TableField("variant_property")
    private String variantProperty;


    public static final String MIAN_ID = "mian_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_GRADE_KEY = "product_grade_key";

    public static final String PRODUCT_LENGTH = "product_length";

    public static final String PRODUCT_WIDTH = "product_width";

    public static final String PRODUCT_HEIGHT = "product_height";

    public static final String BOX_LENGTH = "box_length";

    public static final String BOX_WIDTH = "box_width";

    public static final String BOX_HEIGHT = "box_height";

    public static final String PRODUCT_NET_WEIGHT = "product_net_weight";

    public static final String BOX_WEIGHT = "box_weight";

    public static final String VARIANT_PROPERTY = "variant_property";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
