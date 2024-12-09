package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;


/**
 * <p>
 * 海外仓库存库龄明细表
 * </p>
 *
 * @author jack
 * @since 2024-12-06
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("overseas_inventory_age_detail")
public class OverseasInventoryAgeDetailEntity extends BaseEntity<OverseasInventoryAgeDetailEntity> {

    /**
    * overseas_inventory主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 拉取日期
    */
    @TableField("pull_date")
    private LocalDate pullDate;
    /**
    * 上架日期
    */
    @TableField("put_away_date")
    private LocalDate putAwayDate;
    /**
    * 在库库存
    */
    @TableField("inventory_qty")
    private Integer inventoryQty;
    /**
    * 库龄 = 拉取日期 - 上架日期
    */
    @TableField("inventory_age")
    private Integer inventoryAge;


    public static final String MAIN_ID = "main_id";

    public static final String PULL_DATE = "pull_date";

    public static final String PUT_AWAY_DATE = "put_away_date";

    public static final String INVENTORY_QTY = "inventory_qty";

    public static final String INVENTORY_AGE = "inventory_age";

    @Override
    public Serializable pkVal() {
        return null;
    }

}