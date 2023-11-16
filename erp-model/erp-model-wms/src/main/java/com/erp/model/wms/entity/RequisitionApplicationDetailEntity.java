package com.erp.model.wms.entity;

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
 * 要货申请单明细表
 * </p>
 *
 * @author Luo_wg
 * @since 2023-11-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("requisition_application_detail")
public class RequisitionApplicationDetailEntity extends BaseEntity<RequisitionApplicationDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 产品id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 产品编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * bom版本
    */
    @TableField("bom_version")
    private String bomVersion;
    /**
    * 要货数量
    */
    @TableField("requisition_qty")
    private Integer requisitionQty;
    /**
    * 批准数量
    */
    @TableField("approve_qty")
    private Integer approveQty;
    /**
    * 拣货数量
    */
    @TableField("picking_qty")
    private Integer pickingQty;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String BOM_VERSION = "bom_version";

    public static final String REQUISITION_QTY = "requisition_qty";

    public static final String APPROVE_QTY = "approve_qty";

    public static final String PICKING_QTY = "picking_qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}