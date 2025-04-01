package com.erp.model.mrp.entity;

import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.JdbcType;

import java.io.Serializable;

/**
 * <p>
 * 安全天数明细
 * </p>
 *
 * @author liaohui
 * @since 2025-02-17
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("cfg_rule_safe_days")
public class CfgRuleSafeDaysEntity extends BaseEntity<CfgRuleSafeDaysEntity> {

    /**
     * 备货表cfg_rule_stock_up主键id
     */
    @TableField("stock_up_id")
    private String stockUpId;

    /**
     * 店铺json
     */
    @TableField(value ="shop_id_json", jdbcType = JdbcType.OTHER)
    private JSONArray shopIdJson;

    /**
     * 入库天数（天）
     */
    @TableField("safe_days")
    private Integer safeDays;

    /**
     * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
     */
    @TableField("platform_type")
    private String platformType;


    public static final String STOCK_UP_ID = "stock_up_id";

    public static final String SHOP_ID_JSON = "shop_id_json";

    public static final String SAFE_DAYS = "safe_days";

    public static final String PLATFORM_TYPE = "platform_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
