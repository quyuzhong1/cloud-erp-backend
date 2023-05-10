package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 销售退货入库单明细表
 * </p>
 *
 * @author LUO_WG
 * @since 2023-05-10
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("so_return_instock_detail")
public class SoReturnInstockDetailEntity extends BaseEntity<SoReturnInstockDetailEntity> {

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
     * 退货数量
     */
    @TableField("must_qty")
    private Integer mustQty;

    /**
     * 退货数量
     */
    @TableField("receive_qty")
    private Integer receiveQty;

    /**
     * 实退数量
     */
    @TableField("real_qty")
    private Integer realQty;

    /**
     * 退货类型：dict_basic表type = returnType  退货退款  退货补货
     */
    @TableField("return_type_dict")
    private String returnTypeDict;

    /**
     * 退货原因
     */
    @TableField("return_reason_dict")
    private String returnReasonDict;

    /**
     * 仓库id
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 仓库名称
     */
    @TableField("warehouse_name")
    private String warehouseName;

    /**
     * 仓位
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 来源明细id
     */
    @TableField("source_detail_id")
    private String sourceDetailId;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String MUST_QTY = "must_qty";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String REAL_QTY = "real_qty";

    public static final String RETURN_TYPE_DICT = "return_type_dict";

    public static final String RETURN_REASON_DICT = "return_reason_dict";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String REMARK = "remark";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
