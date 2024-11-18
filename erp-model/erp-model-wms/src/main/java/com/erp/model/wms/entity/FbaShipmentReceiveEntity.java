package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


/**
 * <p>
 * FBA货件签收信息
 * </p>
 *
 * @author Jim
 * @since 2023-11-01
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "fba_shipment_receive", autoResultMap = true)
public class FbaShipmentReceiveEntity extends BaseEntity<FbaShipmentReceiveEntity> {

    /**
    * FBA拣货明细表id
    */
    @TableField("detail_id")
    private String detailId;
    /**
    * 平台sku
    */
    @TableField("asin")
    private String asin;
    /**
    * 卖家sku
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
     * ERP的SKU ID
     */
    @TableField("sku_id")
    private String skuId;
    /**
    * 申报数量
    */
    @TableField("declare_qty")
    private Integer declareQty;
    /**
    * 发货数量
    */
    @TableField("delivery_qty")
    private Integer deliveryQty;
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
    * 当地最新签收日期
    */
    @TableField("receive_date")
    private LocalDateTime receiveDate;
    /**
     * 当地签收日期:格式:yyyy-MM-dd'T'HH:mm:ssXXX
     */
    @TableField(value = "receive_locale_date")
    private String receiveLocaleDate;
    /**
     * UTC 签收日期:格式:yyyy-MM-dd'T'HH:mm:ssXXX
     */
    @TableField(value = "receive_utc_date")
    private String receiveUTCDate;
    /**
     * 亚马逊FBA货件单号
     */
    @TableField("fba_shipment_id")
    private String fbaShipmentId;
    /**
     * 处理状态：none=暂无处理, wait=待处理， already=已处理
     */
    @TableField("handle_status")
    private String handleStatus;
    /**
     * 来源类型：erp=系统生成, lingxing=领星 amazon=亚马逊报告
     */
    @TableField("source_type")
    private String sourceType;
    /**
     * 亚马逊仓储中心ID
     */
    @TableField("fulfillment_center")
    private String fulfillmentCenter;
    /**
     * 单内签收日期索引
     */
    @TableField("unique_index")
    private String uniqueIndex;
    /**
     * 与unique_index组成唯一索引
     */
    @TableField("unique_md5")
    private String uniqueMd5;
    /**
     * ERP的店铺ID
     */
    @TableField(exist = false)
    private String shopId;


    public static final String DETAIL_ID = "detail_id";

    public static final String FBA_SHIPMENT_ID = "fba_shipment_id";


    public static final String M_SKU = "m_sku";

    public static final String FN_SKU = "fn_sku";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String DECLARE_QTY = "declare_qty";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String DIFF_QTY = "diff_qty";

    public static final String IS_COMBO = "is_combo";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String RECEIVE_DATE = "receive_date";


}