package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 模具返还数量计算
 * </p>
 *
 * @author liaohui
 * @since 2024-12-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("mould_ref_calc_qty")
public class MouldRefCalcQtyEntity extends BaseEntity<MouldRefCalcQtyEntity> {

    /**
     * 模具id
     */
    @TableField("mould_detail_id")
    private String mouldDetailId;

    /**
     * 采购数量
     */
    @TableField("purchase_qty")
    private Integer purchaseQty;

    /**
     * 收获数量
     */
    @TableField("receive_qty")
    private Integer receiveQty;

    /**
     * 入库数量
     */
    @TableField("stock_in_qty")
    private Integer stockInQty;

    /**
     * 计算数量
     */
    @TableField("calc_qty")
    private Integer calcQty;


    public static final String MOULD_DETAIL_ID = "mould_detail_id";

    public static final String PURCHASE_QTY = "purchase_qty";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String STOCK_IN_QTY = "stock_in_qty";

    public static final String CALC_QTY = "calc_qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
