package com.erp.server.dmp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.erp.model.dmp.dto.GoodcangDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 海外仓上架详情表
 * </p>
 *
 * @author Cloud
 * @since 2023-03-29
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@TableName("dmp_warehouse_inbound_item")
public class DmpWarehouseInboundItemEntity extends BaseEntity<DmpWarehouseInboundItemEntity> {

    /**
     * 主表主键id
     */
    @TableField("record_id")
    private String recordId;

    /**
     * 平台商品编码 唯一
     */
    @TableField("product_barcode")
    private String productBarcode;

    /**
     * sku编码
     */
    @TableField("sku_no")
    private String skuNo;

    /**
     * 箱号编码
     */
    @TableField("box_no")
    private String boxNo;

    /**
     * 参考箱号
     */
    @TableField("reference_box_no")
    private String referenceBoxNo;

    /**
     * 送货数量
     */
    @TableField("delivery_qty")
    private Integer deliveryQty;

    /**
     * 收货数量
     */
    @TableField("receipt_qty")
    private Integer receiptQty;

    /**
     * 上架数量
     */
    @TableField("put_away_qty")
    private Integer putAwayQty;

    /**
     * 不良品数量
     */
    @TableField("unsellable_qty")
    private Integer unsellableQty;

    /**
     * 良品数量
     */
    @TableField("sellable_qty")
    private Integer sellableQty;


    public static final String RECORD_ID = "record_id";

    public static final String PRODUCT_BARCODE = "product_barcode";

    public static final String SKU_NO = "sku_no";

    public static final String BOX_NO = "box_no";

    public static final String REFERENCE_BOX_NO = "reference_box_no";

    public static final String DELIVERY_QTY = "delivery_qty";

    public static final String RECEIPT_QTY = "receipt_qty";

    public static final String PUT_AWAY_QTY = "put_away_qty";

    public static final String UNSELLABLE_QTY = "unsellable_qty";

    public static final String SELLABLE_QTY = "sellable_qty";

    public DmpWarehouseInboundItemEntity(GoodcangDTO.ReceivingDetailDTO detail, String id) {
        this.recordId = id;
        this.productBarcode = detail.getProductBarcode();
        this.skuNo = detail.getProductSku();
        this.boxNo = detail.getBoxNo();
        this.referenceBoxNo = detail.getReferenceBoxNo();
        this.deliveryQty = detail.getDeliveryQty();
        this.receiptQty = detail.getReceiptQty();
        this.putAwayQty = detail.getPutAwayQty();
        this.unsellableQty = detail.getUnsellableQty();
        this.sellableQty = detail.getSellableQty();
    }

    @Override
    public Serializable pkVal() {
        return null;
    }


}
