package com.erp.model.wms.entity;

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
 * 
 * </p>
 *
 * @author wtr
 * @since 2026-03-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("qc_standard_image_ref")
public class QcStandardImageRefEntity extends BaseEntity<QcStandardImageRefEntity> {

    /**
    * 质检单id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 图片类型(productPhysical=产品实物, packagingAccessories=包装配件)
    */
    @TableField("image_type")
    private String imageType;
    /**
    * 图片URL
    */
    @TableField("image_url")
    private String imageUrl;
    /**
    * 图片名称
    */
    @TableField("image_name")
    private String imageName;


    public static final String MAIN_ID = "main_id";

    public static final String IMAGE_TYPE = "image_type";

    public static final String IMAGE_URL = "image_url";

    public static final String IMAGE_NAME = "image_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}