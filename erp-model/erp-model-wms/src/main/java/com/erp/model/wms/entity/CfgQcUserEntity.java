package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;


/**
 * <p>
 * 质检员配置
 * </p>
 *
 * @author wtr
 * @since 2026-05-27
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("cfg_qc_user")
public class CfgQcUserEntity extends BaseEntity<CfgQcUserEntity> {

    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;

    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
    * 入库质检员id
    */
    @TableField("stockin_qc_user_id")
    private String stockinQcUserId;

    /**
    * 入库质检员名称
    */
    @TableField("stockin_qc_user_name")
    private String stockinQcUserName;

    /**
    * 出库质检员id
    */
    @TableField("stockout_qc_user_id")
    private String stockoutQcUserId;

    /**
    * 出库质检员名称
    */
    @TableField("stockout_qc_user_name")
    private String stockoutQcUserName;

    /**
    * 外检质检员id
    */
    @TableField("outside_qc_user_id")
    private String outsideQcUserId;

    /**
    * 外检质检员名称
    */
    @TableField("outside_qc_user_name")
    private String outsideQcUserName;

    /**
    * 在库质检员id
    */
    @TableField("inside_qc_user_id")
    private String insideQcUserId;

    /**
    * 在库质检员名称
    */
    @TableField("inside_qc_user_name")
    private String insideQcUserName;

    /**
    * 新品入库质检员id
    */
    @TableField("new_product_stockin_qc_user_id")
    private String newProductStockinQcUserId;

    /**
    * 新品入库质检员名称
    */
    @TableField("new_product_stockin_qc_user_name")
    private String newProductStockinQcUserName;

    /**
    * B2B外检质检员id
    */
    @TableField("b2b_outside_qc_user_id")
    private String b2bOutsideQcUserId;

    /**
    * B2B外检质检员名称
    */
    @TableField("b2b_outside_qc_user_name")
    private String b2bOutsideQcUserName;

    /**
    * 退货质检员id
    */
    @TableField("return_qc_user_id")
    private String returnQcUserId;

    /**
    * 退货质检员名称
    */
    @TableField("return_qc_user_name")
    private String returnQcUserName;


    public static final String SUPPLIER_ID = "supplier_id";
    public static final String WAREHOUSE_ID = "warehouse_id";
    public static final String STOCKIN_QC_USER_ID = "stockin_qc_user_id";
    public static final String STOCKIN_QC_USER_NAME = "stockin_qc_user_name";
    public static final String STOCKOUT_QC_USER_ID = "stockout_qc_user_id";
    public static final String STOCKOUT_QC_USER_NAME = "stockout_qc_user_name";
    public static final String OUTSIDE_QC_USER_ID = "outside_qc_user_id";
    public static final String OUTSIDE_QC_USER_NAME = "outside_qc_user_name";
    public static final String INSIDE_QC_USER_ID = "inside_qc_user_id";
    public static final String INSIDE_QC_USER_NAME = "inside_qc_user_name";
    public static final String NEW_PRODUCT_STOCKIN_QC_USER_ID = "new_product_stockin_qc_user_id";
    public static final String NEW_PRODUCT_STOCKIN_QC_USER_NAME = "new_product_stockin_qc_user_name";
    public static final String B2B_OUTSIDE_QC_USER_ID = "b2b_outside_qc_user_id";
    public static final String B2B_OUTSIDE_QC_USER_NAME = "b2b_outside_qc_user_name";
    public static final String RETURN_QC_USER_ID = "return_qc_user_id";
    public static final String RETURN_QC_USER_NAME = "return_qc_user_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
