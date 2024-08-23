package com.erp.model.mrp.entity;

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
 * 备货物流明细（规则设置）
 * </p>
 *
 * @author will
 * @since 2024-08-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_logistics_detail")
public class CfgRuleLogisticsDetailEntity extends BaseEntity<CfgRuleLogisticsDetailEntity> {

    /**
    * 主表(cfg_rule_logistics)id
    */
    @TableField("main_id")
    private String mainId;
    @TableField("area")
    private String area;
    /**
    * 店铺类型（all全部店铺，part指定店铺）
    */
    @TableField("type")
    private String type;
    /**
    * 店铺Idjson
    */
    @TableField("shop_id_json")
    private String shopIdJson;
    /**
    * 海外仓id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 物流时效（天）
    */
    @TableField("logistics_days")
    private Integer logisticsDays;


    public static final String MAIN_ID = "main_id";

    public static final String AREA = "area";

    public static final String TYPE = "type";

    public static final String SHOP_ID_JSON = "shop_id_json";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String LOGISTICS_DAYS = "logistics_days";

    @Override
    public Serializable pkVal() {
        return null;
    }

}