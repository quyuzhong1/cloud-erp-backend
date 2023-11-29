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
 * 发货单箱子信息表
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("first_mile_carton_detail")
public class FirstMileCartonDetailEntity extends BaseEntity<FirstMileCartonDetailEntity> {

    /**
    * first_mile_carton表id
    */
    @TableField("carton_id")
    private String cartonId;
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
    * 发货单id
    */
    @TableField("main_id")
    private String mainId;

    public static final String CARTON_ID = "carton_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PACK_QTY = "pack_qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}