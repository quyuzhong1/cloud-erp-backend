package com.erp.model.oms.entity;

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
 * 店铺渠道关联表
 * </p>
 *
 * @author lrp
 * @since 2025-02-14
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("shop_channel_ref")
public class ShopChannelRefEntity extends BaseEntity<ShopChannelRefEntity> {

    /**
    * 店铺id
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;


    public static final String SHOP_ID = "shop_id";

    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}