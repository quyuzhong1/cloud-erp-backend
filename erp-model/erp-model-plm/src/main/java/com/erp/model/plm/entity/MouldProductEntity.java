package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


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


    public static final String MOULD_DETAIL_ID = "mould_detail_id";

    public static final String PRODUCT_NAME = "product_name";

    public static final String IMAGES_URL = "images_url";

    @Override
    public Serializable pkVal() {
        return null;
    }

}