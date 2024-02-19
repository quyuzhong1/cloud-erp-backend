package com.erp.model.dmp.entity;

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
 * 店铺与第三方平台对照表
 * </p>
 *
 * @author Jim
 * @since 2024-02-19
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("shop_info_mapping")
public class ShopInfoMappingEntity extends BaseEntity<ShopInfoMappingEntity> {

    /**
    * 店铺表id
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 同步第三方平台类型
    */
    @TableField("third_platform_type")
    private String thirdPlatformType;
    /**
     * 同步第三方的店铺ID
     */
    @TableField("third_platform_shop_id")
    private String thirdPlatformShopId;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String SHOP_ID = "shop_id";

    public static final String THIRD_PLATFORM_TYPE = "third_platform_type";

    public static final String THIRD_PLATFORM_SHOP_ID = "third_platform_shop_id";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}