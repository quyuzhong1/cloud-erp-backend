package com.erp.model.tms.entity;

import java.math.BigDecimal;
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
 * 中转费用分摊
 * </p>
 *
 * @author shukai
 * @since 2024-12-06
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("transfer_declare_cost_allocation")
public class TransferDeclareCostAllocationEntity extends BaseEntity<TransferDeclareCostAllocationEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 销售出库明细id
    */
    @TableField("outstock_detail_id")
    private String outstockDetailId;
    /**
    * 发货数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
    * 单SKU计费重
    */
    @TableField("sku_weight")
    private BigDecimal skuWeight;
    
    /**
     * 单SKU计费重
     */
    @TableField("unit_cost")
    private BigDecimal unitCost;
    
    /**
     * 成本币别
     */
    @TableField("unit_currency")
    private String unitCurrency;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String OUTSTOCK_DETAIL_ID = "outstock_detail_id";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String SKU_WEIGHT = "sku_weight";

    @Override
    public Serializable pkVal() {
        return null;
    }

}