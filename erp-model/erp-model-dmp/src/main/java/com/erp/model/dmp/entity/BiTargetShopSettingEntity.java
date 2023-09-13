package com.erp.model.dmp.entity;

import java.math.BigDecimal;
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
 * 店铺目标设置表
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("bi_target_shop_setting")
public class BiTargetShopSettingEntity extends BaseEntity<BiTargetShopSettingEntity> {

    /**
    * 主表id 对应 target_year 表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 店铺id
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 店铺名
    */
    @TableField("shop_name")
    private String shopName;
    /**
    * 月
    */
    @TableField("month")
    private Integer month;
    /**
    * 对应值
    */
    @TableField("value")
    private BigDecimal value;
    /**
    * 指标维度
    */
    @TableField("metrics")
    private String metrics;


    public static final String MAIN_ID = "main_id";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String MONTH = "month";

    public static final String VALUE = "value";

    public static final String METRICS = "metrics";

    @Override
    public Serializable pkVal() {
        return null;
    }

}