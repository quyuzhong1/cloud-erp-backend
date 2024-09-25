package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableField;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;


/**
 * <p>
 * 中台FBA货件签收明细信息
 * </p>
 *
 * @author Jim
 * @since 2024-08-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_fba_shipment_receive")
public class DmpFbaShipmentReceiveEntity extends BaseEntity<DmpFbaShipmentReceiveEntity> {

    /**
     * 第三方店铺ID
     */
    @TableField("third_id")
    private String thirdId;
    /**
     * ERP店铺ID
     */
    @TableField("shop_id")
    private String shopId;
    /**
     * 亚马逊FBA货件单号
     */
    @TableField("fba_shipment_id")
    private String fbaShipmentId;
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
     * 收货数量
     */
    @TableField("receive_qty")
    private Integer receiveQty;
    /**
     * 最新签收日期
     */
    @TableField("receive_date")
    private String receiveDate;
    /**
     * 当地签收日期:格式yyyy-MM-dd'T'HH:mm:ssXXX
     */
    @TableField("receive_locale_date")
    private String receiveLocaleDate;
    /**
     * 亚马逊仓储中心ID
     */
    @TableField("fulfillment_center")
    private String fulfillmentCenter;
    /**
     * 与unique_index组成唯一索引
     */
    @TableField("unique_md5")
    private String uniqueMd5;
    /**
     * 单内签收日期索引
     */
    @TableField("unique_index")
    private Integer uniqueIndex;
    /**
     * 来源类型：lingxing=领星接口获取
     */
    @TableField("source_type")
    private String sourceType;
    /**
     * 报告签收日期
     */
    @TableField("received_date_report")
    private LocalDate receivedDateReport;
    /**
     * 任务转换ID
     */
    @TableField("convert_id")
    private String convertId;
    /**
     * 输入任务ID
     */
    @TableField("input_task_id")
    private String inputTaskId;
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

    public static final String SHOP_ID = "shop_id";

    public static final String FBA_SHIPMENT_ID = "fba_shipment_id";

    public static final String ASIN = "asin";

    public static final String MSKU = "msku";

    public static final String FN_SKU = "fn_sku";

    public static final String SKU_NO = "sku_no";

    public static final String RECEIVE_QTY = "receive_qty";

    public static final String RECEIVE_DATE = "receive_date";

    public static final String SKU_ID = "sku_id";

    public static final String RECEIVE_LOCALE_DATE = "receive_locale_date";

    public static final String FULFILLMENT_CENTER = "fulfillment_center";

    public static final String UNIQUE_MD5 = "unique_md5";

    public static final String UNIQUE_INDEX = "unique_index";

    public static final String SOURCE_TYPE = "source_type";

    public static final String RECEIVE_UTC_DATE = "receive_utc_date";

}
