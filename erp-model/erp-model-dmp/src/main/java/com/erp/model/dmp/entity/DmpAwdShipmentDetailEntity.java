package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * FBA拣货明细表
 * </p>
 *
 * @author zdy
 * @since 2025-12-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("dmp_awd_shipment_detail")
public class DmpAwdShipmentDetailEntity extends BaseEntity<DmpAwdShipmentDetailEntity> {

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
     * 单箱数量
     */
    @TableField("per_box_qty")
    private String perBoxQty;
     /**
     * 箱子数量
     */
    @TableField("box_qty")
    private String boxQty;
    /**
     * 箱子长
     */
    @TableField("package_length")
    private BigDecimal packageLength;
    /**
     * 箱子宽
     */
    @TableField("package_width")
    private BigDecimal packageWidth;
    /**
     * 箱子高
     */
    @TableField("package_height")
    private BigDecimal packageHeight;
    /**
     * 箱子尺寸单位
     */
    @TableField("package_unit")
    private String packageUnit;
    /**
     * 箱子重量
     */
    @TableField("package_weight")
    private BigDecimal packageWeight;
    /**
     * 箱子重量单位
     */
    @TableField("package_weight_unit")
    private String packageWeightUnit;
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

    @Override
    public Serializable pkVal() {
        return null;
    }

}