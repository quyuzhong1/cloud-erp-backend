package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 波次规则
 * </p>
 *
 * @author will
 * @since 2024-06-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_wave")
public class CfgRuleWaveEntity extends BaseEntity<CfgRuleWaveEntity> {

    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 波次类型((waveType类型)，枚举PickingWaveTypeEnum
    */
    @TableField("wave_type")
    private String waveType;
    /**
    * 优先级
    */
    @TableField("priority")
    private Integer priority;
    /**
    * 拣货车类型id
    */
    @TableField("picking_cart_type_json")
    private String pickingCartTypeJson;
    /**
    * 最小单数
    */
    @TableField("min_order_qty")
    private Integer minOrderQty;
    /**
    * 最大单数
    */
    @TableField("max_order_qty")
    private Integer maxOrderQty;
    /**
    * 最少商品数量
    */
    @TableField("min_qty")
    private Integer minQty;
    /**
    * 最多商品数量
    */
    @TableField("max_qty")
    private Integer maxQty;
    /**
    * 状态,true禁用，false启用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 执行时间JSON
    */
    @TableField(value = "execution_time_json")
    private String executionTimeJson;
    /**
    * 执行类型（自动执行，手动执行）
    */
    @TableField("execution_type")
    private String executionType;
    /**
    * 分拣方式（边拣边分，先拣后分）
    */
    @TableField("picking_type")
    private String pickingType;
    /**
    * 规则描述
    */
    @TableField("remark")
    private String remark;


    /**
     * 拣货车类型名称
     */
    @TableField(exist = false)
    private String pickingCartTypeJsonName;
    @Override
    public Serializable pkVal() {
        return null;
    }

}