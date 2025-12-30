package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 图片分类附件关联表
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("ref_product_img_attachment")
public class RefProductImgAttachmentEntity extends BaseEntity<RefProductImgAttachmentEntity> {

    /**
    * 分类ID
    */
    @TableField("category_id")
    private String categoryId;
    /**
    * 产品明细ID
    */
    @TableField("product_detail_id")
    private String productDetailId;
    /**
    * SKU编码
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 附件ID
    */
    @TableField("attachment_id")
    private String attachmentId;


    public static final String CATEGORY_ID = "category_id";

    public static final String PRODUCT_DETAIL_ID = "product_detail_id";

    public static final String SKU_NO = "sku_no";

    public static final String ATTACHMENT_ID = "attachment_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}