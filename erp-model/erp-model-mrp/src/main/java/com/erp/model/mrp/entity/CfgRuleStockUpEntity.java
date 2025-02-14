package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * <p>
 * 备货（规则设置）
 * </p>
 *
 * @author will
 * @since 2024-08-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_stock_up")
public class CfgRuleStockUpEntity extends BaseEntity<CfgRuleStockUpEntity> {


    /**
    * 平台安全天数（天）
    */
    @TableField("platform_safe_days")
    private Integer platformSafeDays;
    /**
     * 海外仓安全天数（天）
     */
    @TableField("overseas_safe_days")
    private Integer overseasSafeDays;
    /**
    * 常规品备货系数
    */
    @TableField("stocking_ratio")
    private BigDecimal stockingRatio;
    /**
    * 新品备货系数
    */
    @TableField("new_stocking_ratio")
    private BigDecimal newStockingRatio;
    /**
    * 平台类型
    */
    @TableField("platform")
    private String platform;
    /**
    * 关联id
    */
    @TableField("ref_id")
    private String refId;
    /**
    * 关联类型
    */
    @TableField("ref_type")
    private String refType;
    /**
     * 是否同常规品配置一致,true是，false否
     */
    @TableField("is_cfg_same")
    private Boolean isCfgSame;



    public static final String STOCKING_RATIO = "stocking_ratio";

    public static final String NEW_STOCKING_RATIO = "new_stocking_ratio";

    public static final String PLATFORM = "platform";

    public static final String REF_ID = "ref_id";

    public static final String REF_TYPE = "ref_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}