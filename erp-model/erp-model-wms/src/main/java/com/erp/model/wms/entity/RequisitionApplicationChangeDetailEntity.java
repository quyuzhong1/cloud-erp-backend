package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 要货申请变更明细
 * </p>
 *
 * @author lrp
 * @since 2024-11-18
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("requisition_application_change_detail")
public class RequisitionApplicationChangeDetailEntity extends BaseEntity<RequisitionApplicationChangeDetailEntity> {

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
    * 来源明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * 变更类型
    */
    @TableField("change_type")
    private String changeType;
    /**
    * 原发货通知数量
    */
    @TableField("origin_qty")
    private Integer originQty;
    /**
    * 新发货通知数量
    */
    @TableField("new_qty")
    private Integer newQty;
    /**
    * 平台sku,msku
    */
    @TableField("platform_sku_no")
    private String platformSkuNo;
    /**
    * bom版本
    */
    @TableField("bom_version")
    private String bomVersion;
    /**
    * fn_sku
    */
    @TableField("fn_sku")
    private String fnSku;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 平台Sku名称
    */
    @TableField("platform_sku_name")
    private String platformSkuName;

    /**
     * 平台spu
     */
    @TableField("platform_spu")
    private String platformSpu;

    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String CHANGE_TYPE = "change_type";

    public static final String ORIGIN_QTY = "origin_qty";

    public static final String NEW_QTY = "new_qty";

    public static final String PLATFORM_SKU_NO = "platform_sku_no";

    public static final String BOM_VERSION = "bom_version";

    public static final String FN_SKU = "fn_sku";

    public static final String REMARK = "remark";

    public static final String PLATFORM_SKU_NAME = "platform_sku_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}