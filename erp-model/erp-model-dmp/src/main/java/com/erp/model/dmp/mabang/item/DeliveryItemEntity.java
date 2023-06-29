package com.erp.model.dmp.mabang.item;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @CreateTime: 2023-06-29  19:27
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
@ToString
public class DeliveryItemEntity implements Serializable {

    /**
     * 发货单详情id
     */
    private String id;

    /**
     * 发货单id
     */
    private Long shipp_batch_delivery_id;

    /**
     * MSKUID
     */
    private String fbastock_id;

    /**
     * 货件id
     */
    private String shipp_batchnew_id;

    /**
     * MSKU
     */
    private String platform_sku;

    /**
     * 商品id
     */
    private String stock_id;

    /**
     * 货件号
     */
    private String shipp_no;

    /**
     * 重量
     */
    private String weight;

    /**
     * 发货数量
     */
    private Integer delivery_num;

    /**
     * 已发货数量
     */
    private Integer use_delivery_num;

    /**
     * 发货单详情备注
     */
    private String remark;

    /**
     * 成本
     */
    private String cost;

    /**
     * 企业编号
     */
    private String company_id;

    /**
     * fba仓库id
     */
    private String fba_warehouse_id;

    /**
     * 国家
     */
    private String amazonsite;

    /**
     * 体积
     */
    private String volume;

    /**
     * 打印次数
     */
    private String print_num;

    /**
     * 分摊物流费用
     */
    private BigDecimal logic_compute_cost;

    /**
     * 分摊自定义费用
     */
    private BigDecimal custom_compute_cost;

    /**
     * 已经与入库队列表关联的数量
     */
    private Integer shared_quantity;

    /**
     * 销售员id 多个销售员逗号隔开
     */
    private String sale_id;

    /**
     * 包材费
     */
    private BigDecimal package_cost;

    /**
     * 库存锁定状态 1无需锁定 2锁定中 3 锁定成功 4部分成功 5锁定失败
     */
    private Integer lock_state;

    /**
     * 锁定数量
     */
    private Integer lock_qty;

    /**
     * 锁定时间
     */
    private String lock_time;

    /**
     * 失败或成功备注
     */
    private String lock_remark;

    /**
     * 0锁定 默认值0 每次更新+1
     */
    private Integer lock_version;

    /**
     * 销售名称
     */
    private String saleName;

    /**
     * 包装类型
     */
    private String packType;

    /**
     * asin
     */
    private String asin;

    /**
     * msku
     */
    private String msku;

    /**
     * 关联量
     */
    private Integer correlation_num;

    /**
     * 申报量
     */
    private Integer applyQuantity;

    /**
     * 货件状态
     */
    private String ShipmentStatus;

    /**
     * 物流中心编码
     */
    private String logistics_code;

    /**
     * 品名
     */
    private String stock_name;

    /**
     * 图片
     */
    private String picturUrl;

    /**
     * fnsku
     */
    private String fnsku;

    /**
     * 商品SKU
     */
    private String sku;

    /**
     * 国家
     */
    private String state;

    /**
     * 店铺名称
     */
    private String shop_name;

    /**
     * 店铺id
     */
    private String shop_id;

}