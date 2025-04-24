package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * <p>
 * 模具 产品
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mould_product")
public class MouldProductEntity extends BaseEntity<MouldProductEntity> {

    /**
    * 模具id
    */
    @TableField("mould_detail_id")
    private String mouldDetailId;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 图片地址
    */
    @TableField("images_url")
    private String imagesUrl;

    /**
     * 模具类型
     */
    @TableField("type_id")
    private String typeId;
    /**
     * 模具穴数
     */
    @TableField("mould_holes")
    private String mouldHoles;
    /**
     * 模具长
     */
    @TableField("length")
    private BigDecimal length;
    /**
     * 模具宽
     */
    @TableField("width")
    private BigDecimal width;
    /**
     * 模具高
     */
    @TableField("height")
    private BigDecimal height;
    /**
     * 模具材质
     */
    @TableField("material")
    private String material;


    public static final String MOULD_DETAIL_ID = "mould_detail_id";

    public static final String PRODUCT_NAME = "product_name";

    public static final String IMAGES_URL = "images_url";

    public static final String TYPE_ID = "type_id";

    public static final String MOLD_HOLES = "mold_holes";

    public static final String LENGTH = "length";

    public static final String WIDTH = "width";

    public static final String HEIGHT = "height";

    public static final String MATERIAL = "material";


    @Override
    public Serializable pkVal() {
        return null;
    }

}