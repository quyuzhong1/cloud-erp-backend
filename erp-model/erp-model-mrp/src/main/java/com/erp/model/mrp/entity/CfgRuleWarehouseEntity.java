package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 仓库（规则设置）
 * </p>
 *
 * @author will
 * @since 2024-08-24
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_warehouse")
public class CfgRuleWarehouseEntity extends BaseEntity<CfgRuleWarehouseEntity> {

    /**
    * 是否启禁用虚拟仓，true启用，false禁用
    */
    @TableField("is_enable_virtual")
    private Boolean isEnableVirtual;
    /**
    * 是否启禁用海外仓，true启用，false禁用
    */
    @TableField("is_enable_overseas")
    private Boolean isEnableOverseas;
    /**
    * 平台类型(amazon Amazon、overseas 海外、internal 国内、b2b B2B)
    */
    @TableField("platform_type")
    private String platformType;


    public static final String IS_DISABLE_VIRTUAL = "is_disable_virtual";

    public static final String IS_DISABLE_OVERSEAS = "is_disable_overseas";

    public static final String PLATFORM_TYPE = "platform_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}