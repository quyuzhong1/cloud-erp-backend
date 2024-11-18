package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 虚拟库存交易规则表
 * </p>
 *
 * @author will
 * @since 2024-06-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_virtual_trans_rules")
public class CfgVirtualTransRulesEntity extends BaseEntity<CfgVirtualTransRulesEntity> {

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
    * 库存状态
    */
    @TableField("inventory_status")
    private String inventoryStatus;
    /**
    * 交易类型 1表示+，-1表示-
    */
    @TableField("transaction_mode")
    private Integer transactionMode;
    /**
    * 交易类型描述信息
    */
    @TableField("remark")
    private String remark;


    public static final String DICT_BIZ_TYPE = "dict_biz_type";

    public static final String WAREHOUSE_OPTION = "warehouse_option";

    public static final String INVENTORY_STATUS = "inventory_status";

    public static final String TRANSACTION_MODE = "transaction_mode";

    @Override
    public Serializable pkVal() {
        return null;
    }

}