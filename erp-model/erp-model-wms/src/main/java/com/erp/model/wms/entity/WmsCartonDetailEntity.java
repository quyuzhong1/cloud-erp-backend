package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * <p>
 * 发货单箱子信息表
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("wms_carton_detail")
public class WmsCartonDetailEntity extends BaseEntity<WmsCartonDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
     * 装箱任务id
     */
    @TableField(exist = false)
    private String taskId;
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
    * 装箱数量
    */
    @TableField("pack_qty")
    private Integer packQty;
    /**
     * 预计毛重（装箱更新时计算）
     */
    @TableField("gross_weight")
    private BigDecimal grossWeight;
    /**
     * 重量单位（g） 页面展示kg，数据库存储g
     */
    @TableField("weight_unit")
    private String weightUnit;
    /**
     * fn_sku
     */
    @TableField("fn_sku")
    private String fnSku;

    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PACK_QTY = "pack_qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}