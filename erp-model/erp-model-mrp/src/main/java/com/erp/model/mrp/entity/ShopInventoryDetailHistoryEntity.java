package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 店铺库存明细
 * </p>
 *
 * @author liaohui
 * @since 2024-09-25
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("shop_inventory_detail_history")
public class ShopInventoryDetailHistoryEntity extends BaseEntity<ShopInventoryDetailHistoryEntity> {

    /**
     * 补货建议库存明细id
     */
    @TableField("main_id")
    private String mainId;

    /**
     * 店铺id
     */
    @TableField("shop_id")
    private String shopId;

    /**
     * 数量
     */
    @TableField("qty")
    private BigDecimal qty;

    /**
     * 计算版本  所有子表加   根据单号生成规则
     */
    @TableField("calc_version")
    private String calcVersion;


    public static final String MAIN_ID = "main_id";

    public static final String SHOP_ID = "shop_id";

    public static final String QTY = "qty";

    public static final String CALC_VERSION = "calc_version";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
