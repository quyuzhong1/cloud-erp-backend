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
 * 仓库（规则设置）
 * </p>
 *
 * @author will
 * @since 2024-08-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_warehouse")
public class CfgRuleWarehouseEntity extends BaseEntity<CfgRuleWarehouseEntity> {

    /**
    * 是否启禁用虚拟仓，false启用，true禁用
    */
    @TableField("is_disable_virtual")
    private Boolean isDisableVirtual;
    /**
    * 是否启禁用海外仓，false启用，true禁用
    */
    @TableField("is_disable_overseas")
    private Boolean isDisableOverseas;
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