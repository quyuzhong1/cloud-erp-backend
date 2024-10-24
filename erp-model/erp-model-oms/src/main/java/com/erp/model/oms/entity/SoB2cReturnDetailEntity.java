package com.erp.model.oms.entity;

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
 * b2c退货订单明细
 * </p>
 *
 * @author lrp
 * @since 2024-10-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2c_return_detail")
public class SoB2cReturnDetailEntity extends BaseEntity<SoB2cReturnDetailEntity> {

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
    * 产品sku编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 平台sku
    */
    @TableField("platform_sku_no")
    private String platformSkuNo;
    /**
    * 销售数量
    */
    @TableField("sale_qty")
    private Integer saleQty;
    /**
    * 退货数量
    */
    @TableField("return_qty")
    private Integer returnQty;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
     * 销售明细id
     */
    @TableField("so_detail_id")
    private String soDetailId;

    @TableField(exist = false)
    private String soId;

    @TableField(exist = false)
    private String code;

    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PLATFORM_SKU_NO = "platform_sku_no";

    public static final String SALE_QTY = "sale_qty";

    public static final String RETURN_QTY = "return_qty";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}