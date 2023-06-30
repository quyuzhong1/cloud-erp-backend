package com.erp.model.dmp.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-06-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_fba_delivery_detail")
public class DmpFbaDeliveryDetailEntity extends BaseEntity<DmpFbaDeliveryDetailEntity> {


    /**
    * MSKUID
    */
    @TableField("fbastock_id")
    private String fbastockId;

    /**
    * 货件id
    */
    @TableField("shipp_batchnew_id")
    private String shippBatchnewId;

    /**
    * MSKU
    */
    @TableField("platform_sku")
    private String platformSku;

    /**
    * 商品id
    */
    @TableField("stock_id")
    private String stockId;

    /**
    * 货件号
    */
    @TableField("shipp_no")
    private String shippNo;

    /**
    * 重量
    */
    @TableField("weight")
    private BigDecimal weight;

    /**
    * 发货数量
    */
    @TableField("delivery_num")
    private Integer deliveryNum;

    /**
    * 已发货数量
    */
    @TableField("use_delivery_num")
    private Integer useDeliveryNum;

    /**
    * 发货单详情备注
    */
    @TableField("remark")
    private String remark;

    /**
    * 成本
    */
    @TableField("cost")
    private BigDecimal cost;

    /**
    * 企业编号
    */
    @TableField("company_id")
    private String companyId;

    /**
    * fba仓库id
    */
    @TableField("fba_warehouse_id")
    private String fbaWarehouseId;

    /**
    * 国家
    */
    @TableField("amazonsite")
    private String amazonsite;

    /**
    * 体积
    */
    @TableField("volume")
    private BigDecimal volume;

    /**
    * 分摊物流费用
    */
    @TableField("logic_compute_cost")
    private BigDecimal logicComputeCost;

    /**
    * 分摊自定义费用
    */
    @TableField("custom_compute_cost")
    private BigDecimal customComputeCost;

    /**
    * 已经与入库队列表关联的数量
    */
    @TableField("shared_quantity")
    private Integer sharedQuantity;

    /**
    * 销售员id 多个销售员逗号隔开
    */
    @TableField("sale_id")
    private String saleId;

    /**
    * 包材费
    */
    @TableField("package_cost")
    private BigDecimal packageCost;

    /**
    * 库存锁定状态 1无需锁定 2锁定中 3 锁定成功 4部分成功 5锁定失败
    */
    @TableField("lock_state")
    private Integer lockState;

    /**
    * 锁定数量
    */
    @TableField("lock_qty")
    private Integer lockQty;

    /**
    * 锁定时间
    */
    @TableField("lock_time")
    private String lockTime;

    /**
    * 失败或成功备注
    */
    @TableField("lock_remark")
    private String lockRemark;

    /**
    * 0锁定 默认值0 每次更新+1
    */
    @TableField("lock_version")
    private Integer lockVersion;

    /**
    * 销售名称
    */
    @TableField("salename")
    private String salename;

    /**
    * 包装类型
    */
    @TableField("packtype")
    private String packtype;

    /**
    * asin
    */
    @TableField("asin")
    private String asin;

    /**
    * msku
    */
    @TableField("msku")
    private String msku;

    /**
    * 关联量
    */
    @TableField("correlation_num")
    private Integer correlationNum;

    /**
    * 申报量
    */
    @TableField("apply_quantity")
    private Integer applyQuantity;

    /**
    * 货件状态
    */
    @TableField("shipment_status")
    private String shipmentStatus;

    /**
    * 物流中心编码
    */
    @TableField("logistics_code")
    private String logisticsCode;

    /**
    * 品名
    */
    @TableField("stock_name")
    private String stockName;

    /**
    * 图片
    */
    @TableField("pictururl")
    private String pictururl;

    /**
    * fnsku
    */
    @TableField("fnsku")
    private String fnsku;

    /**
    * 商品SKU
    */
    @TableField("sku_no")
    private String skuNo;

    /**
    * 国家
    */
    @TableField("state")
    private String state;

    /**
    * 店铺名称
    */
    @TableField("shop_name")
    private String shopName;

    /**
    * 店铺id
    */
    @TableField("shop_id")
    private String shopId;

    /**
    * 主单id
    */
    @TableField("main_id")
    private String mainId;

    /**
     * 发货单详情id
     */
    @TableField("delivery_detail_id")
    private String deliveryDetailId;


    public static final String FBASTOCK_ID = "fbastock_id";

    public static final String SHIPP_BATCHNEW_ID = "shipp_batchnew_id";

    public static final String PLATFORM_SKU = "platform_sku";

    public static final String STOCK_ID = "stock_id";

    public static final String SHIPP_NO = "shipp_no";

    public static final String WEIGHT = "weight";

    public static final String DELIVERY_NUM = "delivery_num";

    public static final String USE_DELIVERY_NUM = "use_delivery_num";

    public static final String REMARK = "remark";

    public static final String COST = "cost";

    public static final String COMPANY_ID = "company_id";

    public static final String FBA_WAREHOUSE_ID = "fba_warehouse_id";

    public static final String AMAZONSITE = "amazonsite";

    public static final String VOLUME = "volume";

    public static final String LOGIC_COMPUTE_COST = "logic_compute_cost";

    public static final String CUSTOM_COMPUTE_COST = "custom_compute_cost";

    public static final String SHARED_QUANTITY = "shared_quantity";

    public static final String SALE_ID = "sale_id";

    public static final String PACKAGE_COST = "package_cost";

    public static final String LOCK_STATE = "lock_state";

    public static final String LOCK_QTY = "lock_qty";

    public static final String LOCK_TIME = "lock_time";

    public static final String LOCK_REMARK = "lock_remark";

    public static final String LOCK_VERSION = "lock_version";

    public static final String SALENAME = "salename";

    public static final String PACKTYPE = "packtype";

    public static final String ASIN = "asin";

    public static final String MSKU = "msku";

    public static final String CORRELATION_NUM = "correlation_num";

    public static final String APPLY_QUANTITY = "apply_quantity";

    public static final String SHIPMENT_STATUS = "shipment_status";

    public static final String LOGISTICS_CODE = "logistics_code";

    public static final String STOCK_NAME = "stock_name";

    public static final String PICTURURL = "pictururl";

    public static final String FNSKU = "fnsku";

    public static final String SKU = "sku";

    public static final String STATE = "state";

    public static final String SHOP_NAME = "shop_name";

    public static final String SHOP_ID = "shop_id";

    public static final String MAIN_ID = "main_id";

    public static final String DELIVERY_DETAIL_ID = "delivery_detail_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

    @Override
    public String toString() {
        return "DmpFbaDeliveryDetailEntity{" +
                "fbastockId='" + fbastockId + '\'' +
                ", shippBatchnewId='" + shippBatchnewId + '\'' +
                ", platformSku='" + platformSku + '\'' +
                ", stockId='" + stockId + '\'' +
                ", shippNo='" + shippNo + '\'' +
                ", weight=" + weight +
                ", deliveryNum=" + deliveryNum +
                ", useDeliveryNum=" + useDeliveryNum +
                ", remark='" + remark + '\'' +
                ", cost=" + cost +
                ", companyId='" + companyId + '\'' +
                ", fbaWarehouseId='" + fbaWarehouseId + '\'' +
                ", amazonsite='" + amazonsite + '\'' +
                ", volume=" + volume +
                ", logicComputeCost=" + logicComputeCost +
                ", customComputeCost=" + customComputeCost +
                ", sharedQuantity=" + sharedQuantity +
                ", saleId='" + saleId + '\'' +
                ", packageCost=" + packageCost +
                ", lockState=" + lockState +
                ", lockQty=" + lockQty +
                ", lockTime='" + lockTime + '\'' +
                ", lockRemark='" + lockRemark + '\'' +
                ", lockVersion=" + lockVersion +
                ", salename='" + salename + '\'' +
                ", packtype='" + packtype + '\'' +
                ", asin='" + asin + '\'' +
                ", msku='" + msku + '\'' +
                ", correlationNum=" + correlationNum +
                ", applyQuantity=" + applyQuantity +
                ", shipmentStatus='" + shipmentStatus + '\'' +
                ", logisticsCode='" + logisticsCode + '\'' +
                ", stockName='" + stockName + '\'' +
                ", pictururl='" + pictururl + '\'' +
                ", fnsku='" + fnsku + '\'' +
                ", skuNo='" + skuNo + '\'' +
                ", state='" + state + '\'' +
                ", shopName='" + shopName + '\'' +
                ", shopId='" + shopId + '\'' +
                ", mainId='" + mainId + '\'' +
                ", deliveryDetailId='" + deliveryDetailId + '\'' +
                '}';
    }
}