package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 试产/量产 明细
 * </p>
 *
 * @author tmj
 * @since 2024-08-27
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pilot_application_detail")
public class PilotApplicationDetailEntity extends BaseEntity<PilotApplicationDetailEntity> {

    @TableField("main_id")
    private String mainId;
    /**
    * 单据类型：试产/量产
    */
    @TableField("type")
    private String type;

    @TableField("sku_id")
    private String skuId;

    @TableField("sku_no")
    private String skuNo;
    /**
    * 申请数量
    */
    @TableField("apply_qty")
    private Integer applyQty;
    /**
    * 批准数量
    */
    @TableField("approve_qty")
    private Integer approveQty;
    /**
    * 业务类型
    */
    @TableField("business_type")
    private String businessType;
    /**
    * 一级供应商ID
    */
    @TableField("main_supplier_id")
    private String mainSupplierId;
    /**
    * 二级供应商ID
    */
    @TableField("second_supplier_id")
    private String secondSupplierId;
    /**
    * 期望到货日期
    */
    @TableField("expect_arrive_date")
    private LocalDate expectArriveDate;
    /**
    * 明细备注
    */
    @TableField("remark")
    private String remark;


    public static final String MAIN_ID = "main_id";

    public static final String TYPE = "type";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String APPLY_QTY = "apply_qty";

    public static final String APPROVE_QTY = "approve_qty";

    public static final String BUSINESS_TYPE = "business_type";

    public static final String MAIN_SUPPLIER_ID = "main_supplier_id";

    public static final String SECOND_SUPPLIER_ID = "second_supplier_id";

    public static final String EXPECT_ARRIVE_DATE = "expect_arrive_date";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}