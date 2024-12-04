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
 * 中台第三方仓库龄信息
 * </p>
 *
 * @author Jim
 * @since 2024-12-05
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_third_inventory_age")
public class DmpThirdInventoryAgeEntity extends BaseEntity<DmpThirdInventoryAgeEntity> {

    /**
    * 任务转换ID
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 店铺ID
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 任务来源唯一加密代号
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 任务数据加密代号
    */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
    * dmp_third_inventory主表ID
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 在库库存
    */
    @TableField("inventory_qty")
    private Integer inventoryQty;
    /**
    * 库龄
    */
    @TableField("inventory_age")
    private Integer inventoryAge;


    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String MAIN_ID = "main_id";

    public static final String INVENTORY_QTY = "inventory_qty";

    public static final String INVENTORY_AGE = "inventory_age";

    @Override
    public Serializable pkVal() {
        return null;
    }

}