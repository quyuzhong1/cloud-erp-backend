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
 * fba货件装箱信息
 * </p>
 *
 * @author lrp
 * @since 2024-09-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("fba_shipment_packing")
public class FbaShipmentPackingEntity extends BaseEntity<FbaShipmentPackingEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 货件箱号
    */
    @TableField("box_no")
    private String boxNo;
    /**
    * 平台产品id（ASIN）
    */
    @TableField("asin")
    private String asin;
    /**
    * 平台sku（msku）
    */
    @TableField("msku")
    private String msku;
    /**
    * FNSKU
    */
    @TableField("fn_sku")
    private String fnSku;
    /**
    * ERP的SKU
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * ERP的skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 装箱数量
    */
    @TableField("qty")
    private Integer qty;

    /**
     * 货件号
     */
    @TableField(value = "fba_shipment_code",exist = false)
    private String fbaShipmentCode;

    public static final String MAIN_ID = "main_id";

    public static final String BOX_NO = "box_no";

    public static final String ASIN = "asin";

    public static final String MSKU = "msku";

    public static final String FN_SKU = "fn_sku";

    public static final String SKU_NO = "sku_no";

    public static final String SKU_ID = "sku_id";

    public static final String QTY = "qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}