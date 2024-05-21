package cn.wangdian.erp.sdk.api.sales.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@ToString
public class TradeQueryResponse {

    @SerializedName("total_count")
    private Integer totalCount;

    @SerializedName("order")
    private List<OrderItem> orders;

    @Getter
    @Setter
    public static class OrderItem {
        // 1无库存记录 2地址发生变化 4发票变化 8仓库变化16备注变化32平台更换货品64退款...512抢单异常
        public static final int BAD_REASON_ADDRESS_CHANGE = 2;
        public static final int BAD_REASON_INVOICE_CHANGE = 4;
        public static final int BAD_REASON_WAREHOUSE_CHANGE = 8;
        public static final int BAD_REASON_REMARK_CHANGE = 16; // 客服备注
        public static final int BAD_REASON_GOODS_CHANGE = 32;
        // public static final int BAD_REASON_REFUND = 64;
        // //申请退款,订单审核时通过refund_status判断
        public static final int BAD_REASON_BLOCK_GIFT = 128;
        public static final int BAD_REASON_BLOCK_SWAP = 256; // 拦截换货
        // (1.换货订单因再次申请退款而拦截
        // 2.平台换货单取消/换改退)
        public static final int BAD_REASON_BUYER_MESSAGE_CHANGE = 512; // 客户备注发生变化
        public static final int BAD_REASON_BLOCK_SYNC_LOGISTICS = 1024; // 拦截平台已发货。

        /**
         * 出库单ID
         */
        @SerializedName("stockout_id")
        private String stockoutId;
        /**
         * 出库单号
         */
        @SerializedName("order_no")
        private String orderNo;
        /**
         * 系统订单编号
         */
        @SerializedName("src_order_no")
        private String srcOrderNo;
        /**
         * 仓库编号
         */
        @SerializedName("warehouse_no")
        private String warehouseNo;
        /**
         * 仓库名称
         */
        @SerializedName("warehouse_name")
        private String warehouseName;
        /**
         * 发货时间
         */
        @SerializedName("consign_time")
        private String consignTime;
        /**
         * 源单据类别
         */
        @SerializedName("order_type")
        private String orderType;
        /**
         * 货品数量
         */
        @SerializedName("goods_count")
        private BigDecimal goodsCount;
        /**
         * 物流单号
         */
        @SerializedName("logistics_no")
        private String logisticsNo;
        /**
         * 收件人姓名
         */
        @SerializedName("receiver_name")
        private String receiverName;
        /**
         * 国家
         */
        @SerializedName("receiver_country")
        private String receiverCountry;
        /**
         * 省份ID
         */
        @SerializedName("receiver_province")
        private String receiverProvince;
        /**
         * 城市ID
         */
        @SerializedName("receiver_city")
        private String receiverCity;
        /**
         * 地区ID
         */
        @SerializedName("receiver_district")
        private String receiverDistrict;
        /**
         * 地址
         */
        @SerializedName("receiver_address")
        private String receiverAddress;
        /**
         * 收件人手机
         */
        @SerializedName("receiver_mobile")
        private String receiverMobile;
        /**
         * 收件人固话
         */
        @SerializedName("receiver_telno")
        private String receiverTelNo;
        /**
         * 收件人邮编
         */
        @SerializedName("receiver_zip")
        private String receiverZip;
        /**
         * 省市区
         */
        @SerializedName("receiver_area")
        private String receiverArea;
        /**
         * 出库单备注
         */
        @SerializedName("remark")
        private String remark;
        /**
         * 重量
         */
        @SerializedName("weight")
        private BigDecimal weight;
        /**
         * 截停原因
         * 0正常
         * 1申请退款
         * 2已退款
         * 4地址被修改
         * 8发票被修改
         * 16物流被修改
         * 32仓库变化
         * 64备注修改
         * 128更换货品
         * 256取消退款
         */
        @SerializedName("block_reason")
        private Integer blockReason;
        /**
         * 物流方式
         */
        @SerializedName("logistics_type")
        private Integer logisticsType;
        /**
         * 物流编号
         */
        @SerializedName("logistics_code")
        private String logisticsCode;
        /**
         * 物流公司名称
         */
        @SerializedName("logistics_name")
        private String logisticsName;
        /**
         * 出库单ID
         */
        @SerializedName("shop_id")
        private Integer shopId;
        /**
         * 仓库id
         */
        @SerializedName("warehouse_id")
        private Integer warehouseId;
        /**
         * 物流id
         */
        @SerializedName("logistics_id")
        private Integer logisticsId;

        /**
         * 异常原因
         * 0正常
         * 1无库存记录
         * 2地址发生变化
         * 4发票变化
         * 8仓库变化
         * 16备注变化
         * 32平台更换货品
         * 64退款
         */
        @SerializedName("bad_reason")
        private Integer badReason;
        /**
         * 大头笔
         */
        @SerializedName("receiver_dtb")
        private String receiverDtb;
        /**
         * 退款状态
         * 0无退款
         * 1申请退款
         * 2部分退款
         * 3全部退款
         */
        @SerializedName("refund_status")
        private Integer refundStatus;

        /**
         * 销售类型
         * 1网店销售
         * 2线下订单
         * 3售后换货
         * 4批发业务
         * 7现款销售
         * 8分销订单
         * 101 订单自定义属性1
         * 102 订单自定义属性2
         * 103 订单自定义属性3
         * 104 订单自定义属性4
         * 105 订单自定义属性5
         * 106 订单自定义属性6
         * 107 订单自定义属性7
         * 108 订单自定义属性8
         * 109 订单自定义属性9
         * 110 订单自定义属性10
         */
        @SerializedName("trade_type")
        private Integer tradeType;
        /**
         * 业务员编号
         */
        @SerializedName("salesman_no")
        private Integer salesmanNo;
        /**
         * 业务员姓名
         */
        @SerializedName("fullname")
        private Integer fullname;
        /**
         * 拣货员
         */
        @SerializedName("picker_name")
        private Integer pickerName;
        /**
         * 验货员
         */
        @SerializedName("examiner_name")
        private Integer examinerName;
        /**
         * 验货员
         */
        @SerializedName("consigner_name")
        private Integer consignerName;
        /**
         * 发货员
         */
        @SerializedName("printer_name")
        private Integer printerName;
        /**
         * 打包员
         */
        @SerializedName("packager_name")
        private Integer packagerName;
        /**
         * 订单状态
         * 4 线下退款
         * 5已取消
         * 6 待转预订单(待审核)
         * 7 待转已完成
         * 10未付款
         * 12待尾款
         * 15等未付
         * 16延时审核
         * 19预订单前处理
         * 20 审核前处理
         * 21自流转待发货
         * 23 异常预订单
         * 24 换货预订单
         * 25 待处理预订单
         * 27待分配预订单
         * 30待客审
         * 35待财审
         * 55已审核
         * 95已发货
         * 96 成本确认（待录入计划成本，订单结算时有货品无计划成本）
         * 101 已过账
         * 110已完成
         */
        @SerializedName("trade_status")
        private Integer tradeStatus;
        /**
         * 订单编号
         */
        @SerializedName("trade_no")
        private String tradeNo;
        /**
         * 原始单号
         */
        @SerializedName("src_trade_no")
        private String srcTradeNo;
        /**
         * 客户网名
         */
        @SerializedName("nick_name")
        private String nickName;
        /**
         * 客户编码
         */
        @SerializedName("customer_no")
        private String customerNo;
        /**
         * 客户姓名
         */
        @SerializedName("customer_name")
        private String customerName;
        /**
         * 下单时间
         */
        @SerializedName("trade_time")
        private String tradeTime;
        /**
         * 支付时间
         */
        @SerializedName("pay_time")
        private String payTime;
        /**
         * 标记名称
         */
        @SerializedName("flag_name")
        private String flagName;
        /**
         * 邮费
         */
        @SerializedName("post_amount")
        private BigDecimal postAmount;
        /**
         * 证件类别
         */
        @SerializedName("id_card_type")
        private Integer idCardType;
        /**
         * 证件号码
         */
        @SerializedName("id_card")
        private Integer idCard;
        /**
         * 店铺名称
         */
        @SerializedName("shop_name")
        private String shopName;
        /**
         * 店铺编号
         */
        @SerializedName("shop_no")
        private String shopNo;
        /**
         * 店铺备注
         */
        @SerializedName("shop_remark")
        private String shopRemark;
        /**
         * 出库单状态:
         * 5已取消
         * 10待放回(拣货待放回), 小于该值的都是已取消的单子
         * 51 缺货
         * 53 WMS已接单
         * 54 获取电子面单
         * 60 待分配
         * 61 排队中
         * 63 待补货
         * 65 待处理
         * 70 待发货
         * 73 爆款锁定
         * 75 待拣货
         * 77 拣货中,PDA拣货后
         * 79 已拣货
         * 90 延时发货
         * 110已完成
         */
        @SerializedName("status")
        private String status;
        /**
         * 发票类型:
         * 0：不需要
         * 1：电子普通发票
         * 2：增值税普通发票
         * 3：电子增值税专用发票
         * 4：纸质普通发票
         * 5：纸质增值税专用发票
         */
        @SerializedName("invoice_type")
        private Integer invoiceType;
        /**
         * 发票id:
         * 目前只设0-1，
         * 1表示已开发票
         */
        @SerializedName("invoice_id")
        private Integer invoiceId;
        /**
         * 货到付款金额
         */
        @SerializedName("cod_amount")
        private BigDecimal codAmount;
        /**
         * 发货条件:
         * 1款到发货
         * 2货到付款(包含部分货到付款)
         * 3分期付款
         * 4挂账
         */
        @SerializedName("delivery_term")
        private Integer deliveryTerm;
        /**
         * 平台ID
         */
        @SerializedName("platform_id")
        private Integer platformId;
        /**
         * 订单ID
         */
        @SerializedName("trade_id")
        private Integer tradeId;
        /**
         * 审核员编号
         */
        @SerializedName("employee_no")
        private String employeeNo;
        /**
         * 优惠金额
         */
        @SerializedName("discount")
        private BigDecimal discount;
        /**
         * 税额
         */
        @SerializedName("tax")
        private BigDecimal tax;
        /**
         * 税率
         */
        @SerializedName("tax_rate")
        private BigDecimal taxRate;
        /**
         * 币种
         */
        @SerializedName("currency")
        private String currency;
        /**
         * 建单时间
         */
        @SerializedName("created")
        private String created;
        /**
         * 出库单建单时间
         */
        @SerializedName("stock_check_time")
        private String stockCheckTime;
        /**
         * 打印备注
         */
        @SerializedName("print_remark")
        private String printRemark;
        /**
         * 买家留言
         */
        @SerializedName("buyer_message")
        private String buyerMessage;
        /**
         * 客服备注
         */
        @SerializedName("cs_remark")
        private String csRemark;
        /**
         * 发票抬头
         */
        @SerializedName("invoice_title")
        private String invoiceTitle;
        /**
         * 发票内容
         */
        @SerializedName("invoice_content")
        private String invoiceContent;
        /**
         * 称重预估邮资
         */
        @SerializedName("post_fee")
        private BigDecimal postFee;
        /**
         * 包装成本
         */
        @SerializedName("package_fee")
        private BigDecimal packageFee;
        /**
         * 已付金额(使用应收金额)
         */
        @SerializedName("receivable")
        private BigDecimal receivable;
        /**
         * 总成本价（对应销售出库明细内实际货品总成本）
         */
        @SerializedName("goods_total_cost")
        private BigDecimal goodsTotalCost;
        /**
         * 预估货品成本(使用单品审核时成本*数量计算得出该成本)
         */
        @SerializedName("goods_total_amount")
        private BigDecimal goodsTotalAmount;
        /**
         * 最后修改时间
         */
        @SerializedName("modified")
        private String modified;
        /**
         * 分销商昵称
         */
        @SerializedName("fenxiao_nick")
        private String fenxiaoNick;
        /**
         * 订单标签
         */
        @SerializedName("trade_label")
        private String tradeLabel;
        /**
         * 订单来源
         * 1、接口抓取
         * 2、手工建单
         * 3、Excel导入
         * 4、复制订单
         * 5、接口推送
         * 6、补发订单
         * 7、PDA选货开单
         * 8、分销补发订单
         */
        @SerializedName("trade_from")
        private Integer tradeFrom;
        /**
         * 分拣波次
         */
        @SerializedName("picklist_no")
        private String picklistNo;
        /**
         * 分拣序号
         */
        @SerializedName("picklist_seq")
        private Integer picklistSeq;
        /**
         * 物流单打印状态
         * 0：未打印
         * 1：打印中
         * 2：已打印
         * 3：无需打印
         */
        @SerializedName("logistics_print_status")
        private Integer logisticsPrintStatus;
        /**
         * 已付
         */
        @SerializedName("paid")
        private BigDecimal paid;
        /**
         * 店铺平台id
         */
        @SerializedName("shop_platform_id")
        private Integer shopPlatformId;
        /**
         * 子平台id
         */
        @SerializedName("sub_platform_id")
        private Integer subPlatformId;
        /**
         * 接口处理错误信息
         */
        @SerializedName("error_info")
        private String errorInfo;

        /**
         * 其他出库自定义子类别0,1,2,3,4
         */
        @SerializedName("custom_type")
        private Integer customType;
        /**
         * 发货单模板id
         */
        @SerializedName("sendbill_template_id")
        private Integer sendbillTemplateId;
        /**
         * 客户id
         */
        @SerializedName("customer_id")
        private Integer customerId;
        /**
         * 仓库类别
         * 0：不限
         * 1：普通仓库
         * 2：自动流转外部仓
         * 3：奇门仓储
         * 4：京东沧海
         * 6：抖店云仓
         * 125：代发仓
         * 126：分销委外仓
         */
        @SerializedName("warehouse_type")
        private Integer warehouseType;
        /**
         * 制单人id（操作员）
         */
        @SerializedName("operator_id")
        private Integer operatorId;
        /**
         * 外部单号
         */
        @SerializedName("outer_no")
        private String outerNo;
        /**
         * 出库状态
         * 0：无
         * 1：已验货
         * 2：已称重
         * 16：已拣货
         * 32：电子面单回传
         * 64：已分拣
         * 128：配送清单打印状态
         * 4096：订单回告（订单生成出库单中间量，暂时只有订单合并回告使用）
         */
        @SerializedName("consign_status")
        private Integer consignStatus;
        /**
         * 货品种类
         */
        @SerializedName("goods_type_count")
        private BigDecimal goodsTypeCount;
        /**
         * 预估邮资成本
         */
        @SerializedName("calc_post_cost")
        private BigDecimal calcPostCost;
        /**
         * 打印批次
         */
        @SerializedName("batch_no")
        private String batchNo;
        /**
         * 创建时间
         */
        @SerializedName("created_date")
        private String createdDate;
        /**
         * 分销原始单号
         */
        @SerializedName("fenxiao_tid")
        private String fenxiaoTid;
        /**
         * 分销商编号
         */
        @SerializedName("fenxiaoNickNo")
        private String fenxiaoNickNo;
        /**
         * 主播
         */
        @SerializedName("anchor_name")
        private String anchorName;
        /**
         * 助播
         */
        @SerializedName("assist_achor_name")
        private String assistAchorName;
        /**
         * 运营
         */
        @SerializedName("control_achor_name")
        private String controlAchorName;
        /**
         * 拣货分组名称
         */
        @SerializedName("operation_anchor_name")
        private String operationAnchorName;
        /**
         * 分销商编号
         */
        @SerializedName("pick_group_name")
        private String pickGroupName;
        /**
         * 物流单列表
         */
        @SerializedName("logistics_list")
        private List<LogisticsList> logisticsList;
        /**
         * 销售出库单详情
         */
        @SerializedName("details_list")
        private List<DetailItem> detailsList;

    }

    @Getter
    @Setter
    public static class LogisticsList {
        /**
         * 物流单id
         */
        @SerializedName("rec_id")
        private String recId;
        /**
         * 出库单id
         */
        @SerializedName("stockout_id")
        private String stockoutId;
        /**
         * 物流单号
         */
        @SerializedName("logistics_no")
        private String logisticsNo;
        /**
         * 估算重量
         */
        @SerializedName("calc_weight")
        private String calcWeight;
        /**
         * 称重重量
         */
        @SerializedName("weight")
        private String weight;
        /**
         * 包装
         */
        @SerializedName("package_name")
        private String packageName;
        /**
         * 物流名称
         */
        @SerializedName("logistics_name")
        private String logisticsName;
        /**
         * 物流ID
         */
        @SerializedName("logistics_id")
        private String logisticsId;
        /**
         * 估算邮资
         */
        @SerializedName("postage")
        private String postage;
        /**
         * 备注
         */
        @SerializedName("remark")
        private String remark;
        /**
         * 长
         */
        @SerializedName("length")
        private String length;
        /**
         * 宽
         */
        @SerializedName("width")
        private String width;
        /**
         * 高
         */
        @SerializedName("height")
        private String height;
        /**
         * 体积
         */
        @SerializedName("volume")
        private String volume;
    }

    @Getter
    @Setter
    public static class DetailItem {
        /**
         * 物流单id
         */
        @SerializedName("rec_id")
        private String recId;
        /**
         * 出库单ID
         */
        @SerializedName("stockout_id")
        private String stockoutId;
        /**
         * 订单明细id
         */
        @SerializedName("src_order_detail_id")
        private String srcOrderDetailId;
        /**
         * 单品id
         */
        @SerializedName("spec_id")
        private String specId;
        /**
         * 商家编码
         */
        @SerializedName("spec_no")
        private String specNo;
        /**
         * 货品数量
         */
        @SerializedName("goods_count")
        private BigDecimal goodsCount;
        /**
         * 总成本
         */
        @SerializedName("total_amount")
        private BigDecimal totalAmount;
        /**
         * 成交价
         */
        @SerializedName("sell_price")
        private BigDecimal sellPrice;
        /**
         * 备注
         */
        @SerializedName("remark")
        private String remark;
        /**
         * 货品编号
         */
        @SerializedName("goods_name")
        private String goodsName;
        /**
         * 货品编号
         */
        @SerializedName("goods_no")
        private String goodsNo;
        /**
         * 规格名称
         */
        @SerializedName("spec_name")
        private String specName;
        /**
         * 规格码
         */
        @SerializedName("spec_code")
        private String specCode;
        /**
         * 货品成本
         */
        @SerializedName("cost_price")
        private BigDecimal costPrice;
        /**
         * 总重量
         */
        @SerializedName("weight")
        private BigDecimal weight;
        /**
         * 货品id（系统货品主键）
         */
        @SerializedName("goods_id")
        private String goodsId;
        /**
         * 规格自定义属性1
         */
        @SerializedName("prop1")
        private String prop1;
        /**
         * 规格自定义属性2
         */
        @SerializedName("prop2")
        private String prop2;
        /**
         * 规格自定义属性3
         */
        @SerializedName("prop3")
        private String prop3;
        /**
         * 规格自定义属性4
         */
        @SerializedName("prop4")
        private String prop4;
        /**
         * 规格自定义属性5
         */
        @SerializedName("prop5")
        private String prop5;
        /**
         * 规格自定义属性6
         */
        @SerializedName("prop6")
        private String prop6;
        /**
         * 平台id
         */
        @SerializedName("platform_id")
        private Integer platformId;
        /**
         * 退款状态
         * 0无退款
         * 1申请退款
         * 2部分退款
         * 3全部退款
         */
        @SerializedName("refund_status")
        private Integer refundStatus;
        /**
         * 单价/货品原单价
         */
        @SerializedName("market_price")
        private BigDecimal marketPrice;
        /**
         * 货品总优惠
         */
        @SerializedName("discount")
        private BigDecimal discount;
        /**
         * 货品成交价
         */
        @SerializedName("share_price")
        private BigDecimal sharePrice;
        /**
         * 总货款/货品成交总价
         */
        @SerializedName("share_amount")
        private BigDecimal shareAmount;
        /**
         * 税率
         */
        @SerializedName("tax_rate")
        private BigDecimal taxRate;
        /**
         * 主条码
         */
        @SerializedName("barcode")
        private String barcode;
        /**
         * 单位名称
         */
        @SerializedName("unit_name")
        private String unitName;
        /**
         * 订单货品(子订单)id
         */
        @SerializedName("sale_order_id")
        private String saleOrderId;
        /**
         * 是否是赠品:
         * 0非赠品
         * 1自动赠送
         * 2手工赠送
         * 4周期购赠送
         * 8平台赠送
         */
        @SerializedName("gift_type")
        private Integer giftType;
        /**
         * 原始子订单号
         */
        @SerializedName("src_oid")
        private String srcOid;
        /**
         * 原始订单号
         */
        @SerializedName("src_tid")
        private String srcTid;
        /**
         * 订单内部来源:
         * 1手机
         * 2聚划算
         */
        @SerializedName("from_mask")
        private Integer fromMask;
        /**
         * 货品类型:
         * 0：其它
         * 1：销售货品
         * 2：原材料
         * 3：包装物
         * 4：周转材料
         * 5：虚拟商品
         * 6：固定资产
         * 8：分装箱
         */
        @SerializedName("goods_type")
        private Integer goodsType;
        /**
         * 货品自定义属性1
         */
        @SerializedName("good_prop1")
        private String goodProp1;
        /**
         * 货品自定义属性2
         */
        @SerializedName("good_prop2")
        private String goodProp2;
        /**
         * 货品自定义属性3
         */
        @SerializedName("good_prop3")
        private String goodProp3;
        /**
         * 货品自定义属性4
         */
        @SerializedName("good_prop4")
        private String goodProp4;
        /**
         * 货品自定义属性5
         */
        @SerializedName("good_prop5")
        private String goodProp5;
        /**
         * 货品自定义属性6
         */
        @SerializedName("good_prop6")
        private String goodProp6;
        /**
         * 组合装编码
         */
        @SerializedName("suite_no")
        private String suiteNo;
        /**
         * 分摊邮费
         */
        @SerializedName("share_post_amount")
        private BigDecimal sharePostAmount;
        /**
         * 已付
         */
        @SerializedName("paid")
        private BigDecimal paid;
        /**
         * 是否包装
         * true:是
         * false:否
         */
        @SerializedName("is_package")
        private Boolean isPackage;
        /**
         * 品牌编号
         */
        @SerializedName("brand_no")
        private String brandNo;
        /**
         * 品牌名称
         */
        @SerializedName("brand_name")
        private String brandName;
        /**
         * 源单据类别
         * 1：销售订单
         */
        @SerializedName("src_order_type")
        private Integer srcOrderType;
        /**
         * 基本单位
         */
        @SerializedName("base_unit_id")
        private Integer baseUnitId;
        /**
         * 辅助单位
         */
        @SerializedName("unit_id")
        private Integer unitId;
        /**
         * 单位换算
         */
        @SerializedName("unit_ratio")
        private BigDecimal unitRatio;
        /**
         * 辅助数量
         */
        @SerializedName("num2")
        private BigDecimal num2;
        /**
         * 货品数量
         */
        @SerializedName("num")
        private BigDecimal num;
        /**
         * 出库货位id
         */
        @SerializedName("position_id")
        private String positionId;
        /**
         * 指定出库批次
         */
        @SerializedName("batch_id")
        private String batchId;
        /**
         * 是否验货
         * true:是
         * false:否
         */
        @SerializedName("is_examined")
        private Boolean isExamined;
        /**
         * 有效期
         */
        @SerializedName("expire_date")
        private String expireDate;
        /**
         * 扫描方式
         * 0：未验货
         * 1：扫描验货
         * 2：手工验货
         * 3：快速验货
         * 4：无需验货
         */
        @SerializedName("scan_type")
        private Integer scanType;
        /**
         * 最后修改时间，时间格式：yyyy-MM-dd HH:mm:ss
         */
        @SerializedName("modified_date")
        private String modifiedDate;
        /**
         * 创建时间
         */
        @SerializedName("created_date")
        private String createdDate;
        /**
         * 分类名称
         */
        @SerializedName("class_name")
        private String className;
        /**
         * 出库货位明细
         */
        @SerializedName("position_details_list")
        private List<PositionDetailsList> positionDetailsList;
    }

    @Getter
    @Setter
    public static class PositionDetailsList {
        /**
         * 货位明细id
         */
        @SerializedName("rec_id")
        private String recId;
        /**
         * 销售出库单详情id
         */
        @SerializedName("stockout_detail_id")
        private String stockoutDetailId;
        /**
         * 货位id
         */
        @SerializedName("position_id")
        private String positionId;
        /**
         * 货位号
         */
        @SerializedName("position_no")
        private String positionNo;
        /**
         * 有效期
         */
        @SerializedName("expire_date")
        private String expireDate;
        /**
         * 批次号
         */
        @SerializedName("batch_no")
        private String batchNo;
        /**
         * 当前货位出库总货品数量
         */
        @SerializedName("position_goods_count")
        private BigDecimal positionGoodsCount;
    }
}