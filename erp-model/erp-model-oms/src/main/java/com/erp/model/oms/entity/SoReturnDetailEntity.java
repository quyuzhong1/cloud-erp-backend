package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 发货通知单主表明细表
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("so_return_detail")
public class SoReturnDetailEntity extends BaseEntity<SoReturnDetailEntity> {

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
     * sku编号
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 销售数量
     */
    @TableField("sales_qty")
    private Integer salesQty;

    /**
     * 退货数量
     */
    @TableField("return_qty")
    private Integer returnQty;

    /**
     * 退货类型：退货退款  退货补货
     */
    @TableField("return_type_dict")
    private String returnTypeDict;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 销售单明细表id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String SALES_QTY = "sales_qty";

    public static final String RETURN_QTY = "return_qty";

    public static final String RETURN_TYPE_DICT = "return_type_dict";

    public static final String REMARK = "remark";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
