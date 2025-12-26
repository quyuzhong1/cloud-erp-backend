package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @Author: wtr
 * @Date: 2025/12/25 11:00
 * @Param:
 * @Return:
 * @Description:
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("dmp_awd_inventory")
public class DmpAwdInventoryEntity extends BaseEntity<DmpAwdInventoryEntity> {


    /**
     * 卖家sku
     */
    @TableField("msku")
    private String msku;
    /**
     * 输入任务ID
     */
    @TableField("input_task_id")
    private String inputTaskId;
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
     * AWD在库
     */
    @TableField("total_onhand_qty")
    private Integer totalOnhandQty;
    /**
     * AWD可用
     */
    @TableField("available_distributable_qty")
    private Integer availableDistributableQty;
    /**
     * AWD发FBA在途
     */
    @TableField("replenishment_qty")
    private Integer replenishmentQty;
    /**
     * 发AWD在途
     */
    @TableField("reserved_distributable_qty")
    private Integer reservedDistributableQty;

    /**
     * 发AWD在途
     */
    @TableField("total_inbound_qty")
    private Integer totalInboundQty;
}
