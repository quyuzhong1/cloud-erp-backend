package com.erp.model.tms.entity;

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
 * 产品备案表
 * </p>
 *
 * @author lambda
 * @since 2024-01-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("product_registration")
public class ProductRegistrationEntity extends BaseEntity<ProductRegistrationEntity> {

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
    * 报关平台
    */
    @TableField("declare_platform")
    private String declarePlatform;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 是否已备案  true 已备案 fasle 未备案
    */
    @TableField("is_registration")
    private Boolean isRegistration;


    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String DECLARE_PLATFORM = "declare_platform";

    public static final String REMARK = "remark";

    public static final String IS_REGISTRATION = "is_registration";

    @Override
    public Serializable pkVal() {
        return null;
    }

}