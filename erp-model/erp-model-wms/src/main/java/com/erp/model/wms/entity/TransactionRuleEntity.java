package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Classname: TransactionRuleEntity
 * @Description: TODO
 * @CreateTime: 2023-04-26  10:14
 * @Author: zhangchunlin
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("transaction_rule")
public class TransactionRuleEntity extends BaseEntity<TransactionRuleEntity> implements Serializable {


    /**
     * 业务类型
     */
    @TableField("dict_biz_type")
    private String dictBizType;

    /**
     * 仓库选项
     */
    @TableField("warehouse_option")
    private String warehouseOption;

    /**
     * 库存状态（默认）；调用方可以手动指定更改的状态
     */
    @TableField("inventory_status")
    private String inventoryStatus;

    /**
     * 交易方向；1-增加；-1减少
     */
    @TableField("transaction_mode")
    private String transactionMode;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

}