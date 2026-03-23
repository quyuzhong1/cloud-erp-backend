package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * FBT货件明细表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_fbt_shipment_detail")
public class DmpFbtShipmentDetailEntity extends BaseEntity<DmpFbtShipmentDetailEntity> {

    @TableField("main_id")
    private String mainId;

    @TableField("inbound_order_id")
    private String inboundOrderId;

    @TableField("goods_id")
    private String goodsId;

    @TableField("reference_code")
    private String referenceCode;

    @TableField("product_name")
    private String productName;

    @TableField("declare_qty")
    private Integer declareQty;

    @TableField("sku_ids_json")
    private String skuIdsJson;

    @TableField("input_task_id")
    private String inputTaskId;

    @TableField("convert_id")
    private String convertId;

    @TableField("next_level_id")
    private String nextLevelId;

    @TableField("unique_encrypt")
    private String uniqueEncrypt;

    @TableField("data_encrypt")
    private String dataEncrypt;
}
