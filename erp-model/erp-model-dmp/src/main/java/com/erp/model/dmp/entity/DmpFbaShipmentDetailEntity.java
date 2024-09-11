package com.erp.model.dmp.entity;

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
 * FBA拣货明细表
 * </p>
 *
 * @author Jim
 * @since 2024-09-07
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_fba_shipment_detail")
public class DmpFbaShipmentDetailEntity extends BaseEntity<DmpFbaShipmentDetailEntity> {

    /**
     * 主表id
     */
    @TableField("main_id")
    private String mainId;
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
     * 申报数量
     */
    @TableField("declare_qty")
    private Integer declareQty;
    /**
     * 收发差异
     */
    @TableField("diff_qty")
    private Integer diffQty;
    /**
     * 收货数量
     */
    @TableField("receive_qty")
    private Integer receiveQty;
    /**
     * 发货数量
     */
    @TableField("delivery_qty")
    private Integer deliveryQty;
    /**
     * FBA货件ID
     */
    @TableField("fba_shipment_id")
    private String fbaShipmentId;
    /**
     * 输入任务ID
     */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
     * 亚马逊账号代号
     */
    @TableField("platform_shop_code")
    private String platformShopCode;
    /**
     * 任务转换ID
     */
    @TableField("convert_id")
    private String convertId;
    /**
     * 店铺ID
     */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
     * 任务来源唯一加密代号
     */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
     * 任务数据加密代号
     */
    @TableField("data_encrypt")
    private String dataEncrypt;


    public static final String MAIN_ID = "main_id";

    public static final String ASIN = "asin";

    public static final String MSKU = "msku";

    public static final String FN_SKU = "fn_sku";

    public static final String DECLARE_QTY = "declare_qty";

    public static final String DIFF_QTY = "diff_qty";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String FBA_SHIPMENT_ID = "fba_shipment_id";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String PLATFORM_SHOP_CODE = "platform_shop_code";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

}