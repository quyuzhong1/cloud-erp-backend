package com.sdk.wangdian.sdk.api.sales.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PushSelf2Request
{
	@SerializedName("business_code")
	private String businessCode;

	@SerializedName("shop_no")
	private String shopNo;

	@SerializedName("rawTradeList")
	private List<PushSelf2Request.RawTrade> rawTradeList;
	@SerializedName("rawTradeOrderList")
	private List<PushSelf2Request.RawTradeOrder> rawTradeOrderList;
	@SerializedName("discountList")
	private List<PushSelf2Request.Discount> discountList;

	/**
	 *
	 * 原始单信息
	 *
	 * */
	@Data
	public static class RawTrade{
		/**
		 * 必填
		 * 原始单号
		 * 指商城、官网等平台的订单编号，ERP称之为原始单号，同一个sid下通过本接口新增订单的tid保证唯一。
		 */
		@SerializedName("tid")
		private String tid;


		// 处理状态相关常量
		public static final Integer PROCESS_STATUS_WAIT_DELIVERY = 10; // 待递交
		public static final Integer PROCESS_STATUS_DELIVERED = 20; // 已递交
		public static final Integer PROCESS_STATUS_PART_DELIVERY = 30; // 部分发货
		public static final Integer PROCESS_STATUS_DELIVERED_ALL = 40; // 已发货
		public static final Integer PROCESS_STATUS_FINISHED = 60; // 已完成
		public static final Integer PROCESS_STATUS_CANCELED = 70; // 已取消

		/**
		 * 必填
		 * 处理状态
		 * 是否执行自动递交以系统配置为准
		 * 10: 待递交,
		 * 20: 已递交，
		 * 30: 部分发货，
		 * 40: 已发货，
		 * 60: 已完成，
		 * 70: 已取消
		 */
		@SerializedName("process_status")
		private Integer processStatus = PROCESS_STATUS_DELIVERED;


		// 平台状态相关常量
		public static final Integer TRADE_STATUS_UNCONFIRMED = 10; // 未确认
		public static final Integer TRADE_STATUS_PENDING = 20; // 待尾款
		public static final Integer TRADE_STATUS_DELIVERY = 30; // 待发货
		public static final Integer TRADE_STATUS_PART_DELIVERY = 40; // 部分发货
		public static final Integer TRADE_STATUS_DELIVERED = 50; // 已发货
		public static final Integer TRADE_STATUS_RECEIVED = 60; // 已签收
		public static final Integer TRADE_STATUS_FINISHED = 70; // 已完成
		public static final Integer TRADE_STATUS_REFUNDED = 80; // 已退款
		public static final Integer TRADE_STATUS_CLOSED = 90; // 已关闭(付款前取消)

		/**
		 * 必填
		 * 平台状态
		 * 10未确认20待尾款30待发货40部分发货50已发货60已签收70已完成80已退款90已关闭(付款前取消)，单据状态变更点击文档查看
		 */
		@SerializedName("trade_status")
		private Integer tradeStatus;


		// 退款状态相关常量
		public static final Integer REFUND_STATUS_NO = 0; // 无退款
		public static final Integer REFUND_STATUS_APPLY = 1; // 申请退款
		public static final Integer REFUND_STATUS_PART = 2; // 部分退款
		public static final Integer REFUND_STATUS_ALL = 3; // 全部退款

		/**
		 * 必填
		 * 退款状态
		 * 0无退款1申请退款2部分退款3全部退款，单据状态变更点击文档查看
		 */
		@SerializedName("refund_status")
		private Integer refundStatus;


		// 付款状态相关常量
		public static final Integer PAY_STATUS_UNPAID = 0; // 未付款
		public static final Integer PAY_STATUS_PART = 1; // 部分付款
		public static final Integer PAY_STATUS_PAID = 2; // 已付款

		/**
		 * 必填
		 * 付款状态
		 * 0未付款1部分付款2已付款
		 */
		@SerializedName("pay_status")
		private Integer payStatus;


		/**
		 * 必填
		 * 子订单个数
		 * 原始单包含的子订单数，rawTradeOrderList节点下所有子单数之和
		 */
		@SerializedName("order_count")
		private Integer orderCount;


		/**
		 * 必填
		 * 货品总数量
		 * 货品总数量，rawTradeOrderList节点下所有"num"数量之和
		 */
		@SerializedName("goods_count")
		private BigDecimal goodsCount;


		// 支付方式相关常量
		public static final Integer PAY_METHOD_ONLINE = 1; // 在线转账
		public static final Integer PAY_METHOD_CASH = 2; // 现金
		public static final Integer PAY_METHOD_BANK_TRANSFER = 3; // 银行转账
		public static final Integer PAY_METHOD_POSTAL = 4; // 邮局汇款
		public static final Integer PAY_METHOD_INSTALLMENT = 5; // 分期付款
		public static final Integer PAY_METHOD_CARD = 6; // 刷卡
		public static final Integer PAY_METHOD_ALIPAY = 7; // 支付宝
		public static final Integer PAY_METHOD_WECHAT = 8; // 微信支付

		/**
		 * 必填
		 * 支付方式
		 * 1在线转账2现金，3银行转账，4邮局汇款5分期付款6刷卡7支付宝8微信支付
		 */
		@SerializedName("pay_method")
		private Integer payMethod;


		/**
		 * 必填
		 * 下单时间
		 * 下单时间
		 */
		@SerializedName("trade_time")
		private String tradeTime;


		/**
		 * 非必填
		 * 支付时间
		 * 支付时间
		 */
		@SerializedName("pay_time")
		private String payTime;


		/**
		 * 必填
		 * 交易结束时间
		 * 交易结束时间，若无则传null
		 */
		@SerializedName("end_time")
		private String endTime;


		/**
		 * 必填
		 * 买家昵称/客户网名
		 * 若无则传一个固定值
		 */
		@SerializedName("buyer_nick")
		private String buyerNick;


		/**
		 * 非必填
		 * 买家备注
		 * 若无则传""
		 */
		@SerializedName("buyer_message")
		private String buyerMessage;


		/**
		 * 非必填
		 * 买家邮箱
		 */
		@SerializedName("buyer_email")
		private String buyerEmail;


		/**
		 * 非必填
		 * 买家地区
		 */
		@SerializedName("buyer_area")
		private String buyerArea;


		/**
		 * 必填
		 * 收件人姓名
		 * 收件人姓名
		 */
		@SerializedName("receiver_name")
		private String receiverName;


		/**
		 * 必填
		 * 省市区
		 * 省市区空格分隔，示例【北京 北京市 朝阳区】，省市区推送时进店地址库，点击查看
		 */
		@SerializedName("receiver_area")
		private String receiverArea;


		/**
		 * 必填
		 * 地址
		 * 收件人详细地址，不包含省市区，示例【xx街道xx小区xx号楼101】
		 */
		@SerializedName("receiver_address")
		private String receiverAddress;


		/**
		 * 非必填
		 * 收件人邮编
		 * 收件人邮编，若无则传''
		 */
		@SerializedName("receiver_zip")
		private String receiverZip;


		/**
		 * 必填
		 * 收件人手机
		 * 该字段必传，收件人手机号，为11位手机号码，示例【13888888888】
		 */
		@SerializedName("receiver_mobile")
		private String receiverMobile;


		/**
		 * 非必填
		 * 收件人电话
		 * 收件人固话号，为11位固话号码，示例【02288888888】
		 */
		@SerializedName("receiver_telno")
		private String receiverTelno;


		/**
		 * 必填
		 * 邮费
		 * 邮费
		 */
		@SerializedName("post_amount")
		private BigDecimal postAmount;


		/**
		 * 非必填
		 * 其他费用
		 * 其他费用
		 */
		@SerializedName("other_amount")
		private BigDecimal otherAmount;


		/**
		 * 必填
		 * 优惠金额
		 * 优惠金额
		 */
		@SerializedName("discount")
		private BigDecimal discount;


		/**
		 * 必填
		 * 应收金额
		 * 应收金额，售前退款会变化，"详细金额逻辑点击查看"
		 */
		@SerializedName("receivable")
		private BigDecimal receivable;


		/**
		 * 非必填
		 * 平台费用
		 * 0.0000
		 */
		@SerializedName("platform_cost")
		private BigDecimal platformCost;


		// 发票类别相关常量
		public static final Integer INVOICE_TYPE_NO = 0; // 不需要
		public static final Integer INVOICE_TYPE_GENERAL = 1; // 普通发票
		public static final Integer INVOICE_TYPE_VAT_GENERAL = 2; // 增值税普通发票
		public static final Integer INVOICE_TYPE_VAT_SPECIAL = 3; // 增值税专用发票

		/**
		 * 非必填
		 * 发票类别
		 * 0: 不需要
		 * 1: 普通发票
		 * 2: 增值税普通发票
		 * 3: 增值税专用发票
		 */
		@SerializedName("invoice_type")
		private Integer invoiceType;


		/**
		 * 非必填
		 * 发票抬头
		 * 发票抬头，若无则传''
		 */
		@SerializedName("invoice_title")
		private String invoiceTitle;


		/**
		 * 非必填
		 * 发票内容
		 * 发票内容，格式：纳税人识别号:******地址:******开户银行:******;银行账号******联系电话******电子邮箱****** 若无则传""
		 */
		@SerializedName("invoice_content")
		private String invoiceContent;


		/**
		 * 非必填
		 * 物流类别
		 * -1
		 */
		@SerializedName("logistics_type")
		private Integer logisticsType;


		/**
		 * 非必填
		 * 物流编号
		 * 系统物流编号（设置-基本设置-物流）
		 */
		@SerializedName("cust_data")
		private String custData;


		// 发货条件相关常量
		public static final Integer DELIVERY_TERM_PAY_FIRST = 1; // 款到发货
		public static final Integer DELIVERY_TERM_CASH_ON_DELIVERY = 2; // 货到付款(包含部分货到付款)
		public static final Integer DELIVERY_TERM_INSTALLMENT = 3; // 分期付款

		/**
		 * 必填
		 * 发货条件
		 * 1款到发货2货到付款(包含部分货到付款)3分期付款
		 */
		@SerializedName("delivery_term")
		private Integer deliveryTerm;


		/**
		 * 非必填
		 * 平台支付订单ID
		 */
		@SerializedName("pay_id")
		private String payId;


		/**
		 * 非必填
		 * 客服备注
		 * 客服备注
		 */
		@SerializedName("remark")
		private String remark;


		// 备注设备标签相关常量
		public static final Integer REMARK_FLAG_RED = 1; // 红
		public static final Integer REMARK_FLAG_YELLOW = 2; // 黄
		public static final Integer REMARK_FLAG_GREEN = 3; // 绿
		public static final Integer REMARK_FLAG_BLUE = 4; // 蓝
		public static final Integer REMARK_FLAG_PURPLE = 5; // 紫
		public static final Integer REMARK_FLAG_NONE = 0; // 无则填0

		/**
		 * 非必填
		 * 备注设备标签
		 * 标签（1红、2黄、3绿、4蓝、5紫 无则填0）
		 */
		@SerializedName("remark_flag")
		private Integer remarkFlag;


		/**
		 * 非必填
		 * 货到付款金额
		 * 若delivery_term=2，则为应付金额，否则为0
		 */
		@SerializedName("cod_amount")
		private BigDecimal codAmount;


		/**
		 * 必填
		 * 是否是自流转
		 * true自流转，false 非自流转 自动流转模式处理办法详解 单击这里
		 */
		@SerializedName("is_auto_wms")
		private Boolean isAutoWms;


		/**
		 * 必填
		 * 仓库编号
		 * 仅自流转订单填写该仓库编号,非自流转订单传入空字符串即可
		 */
		@SerializedName("warehouse_no")
		private String warehouseNo;


		/**
		 * 非必填
		 * 买家支付宝账号
		 */
		@SerializedName("pay_account")
		private String payAccount;


		/**
		 * 非必填
		 * 买家要求的送货日期
		 * 买家要求的送货日期
		 */
		@SerializedName("to_deliver_time")
		private String toDeliverTime;


		/**
		 * 非必填
		 * 已收
		 * 已从平台收款的金额
		 */
		@SerializedName("received")
		private BigDecimal received;


		/**
		 * 非必填
		 * 淘宝新增,物流到货时效，单位小时
		 * 淘宝新增,物流到货时效，单位小时
		 */
		@SerializedName("consign_interval")
		private Integer consignInterval;


		/**
		 * 非必填
		 * 已付
		 * 已支付金额
		 */
		@SerializedName("paid")
		private BigDecimal paid;


		// 是否能合并订单相关常量
		public static final Integer IS_SEALED_DEFAULT = 0; // 默认
		public static final Integer IS_SEALED_NOT_ALLOW = 3; // 不允许合并

		/**
		 * 非必填
		 * 是否能合并订单
		 * 0: 默认
		 * 3: 不允许合并
		 */
		@SerializedName("is_sealed")
		private Integer isSealed;


		/**
		 * 非必填
		 * 分销商名称
		 * 分销商名称
		 */
		@SerializedName("fenxiao_nick")
		private String fenxiaoNick;


		// 证件类型相关常量
		public static final Integer ID_CARD_TYPE_NONE = 0; // 无
		public static final Integer ID_CARD_TYPE_ID = 1; // 身份证
		public static final Integer ID_CARD_TYPE_MILITARY = 2; // 军官证
		public static final Integer ID_CARD_TYPE_PASSPORT = 3; // 护照

		/**
		 * 非必填
		 * 证件类型
		 * 0: 无
		 * 1: 身份证
		 * 2: 军官证
		 * 3: 护照
		 */
		@SerializedName("id_card_type")
		private Integer idCardType;


		/**
		 * 非必填
		 * 证件号
		 * 证件号
		 */
		@SerializedName("id_card")
		private String idCard;
	}

	/**
	 *
	 * 原始子单信息
	 *
	 * */
	@Data
	public static class RawTradeOrder {

		/**
		 * 必填
		 * 原始订单号
		 * 原始订单号
		 */
		@SerializedName("tid")
		private String tid;


		/**
		 * 必填
		 * 原始单号（子订单编号）
		 * 平台订单货品表主子订单唯一标识,同一个sid下通过本接口新增订单的oid（子订单编号）要保证唯一；如果oid重复ERP生成系统单（递交）时会提示“订单货品数量不一致xxxxxx”
		 */
		@SerializedName("oid")
		private String oid;


		// 平台的状态相关常量
		public static final Integer STATUS_UNCONFIRMED = 10; // 未确认
		public static final Integer STATUS_PENDING = 20; // 待尾款
		public static final Integer STATUS_WAIT_DELIVERY = 30; // 待发货
		public static final Integer STATUS_PART_DELIVERY = 40; // 部分发货
		public static final Integer STATUS_DELIVERED = 50; // 已发货
		public static final Integer STATUS_RECEIVED = 60; // 已签收
		public static final Integer STATUS_FINISHED = 70; // 已完成
		public static final Integer STATUS_REFUNDED = 80; // 已退款
		public static final Integer STATUS_CLOSED = 90; // 已关闭

		/**
		 * 必填
		 * 平台的状态
		 * 10未确认20待尾款30待发货40部分发货50已发货60已签收70已完成80已退款90已关闭，单据状态变更点击文档查看
		 */
		@SerializedName("status")
		private Integer status;


		// 退款标记相关常量
		public static final Integer REFUND_STATUS_NO = 0; // 无退款
		public static final Integer REFUND_STATUS_APPLY = 1; // 申请退款
		public static final Integer REFUND_STATUS_REVIEW = 2; // 已申请退款,等待审核
		public static final Integer REFUND_STATUS_WAIT_RETURN = 3; // 等待退货
		public static final Integer REFUND_STATUS_WAIT_RECEIVE = 4; // 等待收货
		public static final Integer REFUND_STATUS_SUCCESS = 5; // 退款成功
		public static final Integer REFUND_STATUS_PAY_CANCEL = 6; // 未付款取消

		/**
		 * 必填
		 * 退款标记
		 * 0无退款1申请退款2已申请退款,等待退货4等待收货5退款成功6未付款取消，单据状态变更点击文档查看
		 */
		@SerializedName("refund_status")
		private Integer refundStatus;


		/**
		 * 必填
		 * 平台货品ID
		 * 平台系统货品（SPU）的唯一标识。SPU和SKU概念介绍，单击这里
		 */
		@SerializedName("goods_id")
		private String goodsId;


		/**
		 * 必填
		 * 平台规格ID
		 * 平台系统单品（SKU）的的唯一标识。SPU和SKU概念介绍，单击这里
		 */
		@SerializedName("spec_id")
		private String specId;


		/**
		 * 必填
		 * 货品编号
		 * 货品编号
		 */
		@SerializedName("goods_no")
		private String goodsNo;


		/**
		 * 必填
		 * 规格编码
		 * 规格编码
		 */
		@SerializedName("spec_no")
		private String specNo;


		/**
		 * 必填
		 * 货品名称
		 * 货品名称
		 */
		@SerializedName("goods_name")
		private String goodsName;


		/**
		 * 非必填
		 * 规格名称
		 * 规格名称
		 */
		@SerializedName("spec_name")
		private String specName;


		// 子单类型相关常量
		public static final Integer ORDER_TYPE_NORMAL = 0; // 正常品
		public static final Integer ORDER_TYPE_VIRTUAL = 1; // 虚拟货品
		public static final Integer ORDER_TYPE_SERVICE = 2; // 服务（不传递认为0）

		/**
		 * 非必填
		 * 子单类型
		 * 0正常品1虚拟货品2服务（不传递认为0）
		 */
		@SerializedName("order_type")
		private Integer orderType;


		/**
		 * 非必填
		 * 平台类目
		 * 平台类目
		 */
		@SerializedName("cid")
		private String cid;


		/**
		 * 必填
		 * 数量
		 * 数量
		 */
		@SerializedName("num")
		private BigDecimal num;


		/**
		 * 必填
		 * 单价
		 * 单价
		 */
		@SerializedName("price")
		private BigDecimal price;


		/**
		 * 必填
		 * 优惠
		 * 优惠，平台折扣,不包含手工调整和分堆优惠
		 */
		@SerializedName("discount")
		private BigDecimal discount;


		/**
		 * 必填
		 * 分堆优惠
		 * 分堆优惠,退款不变
		 */
		@SerializedName("share_discount")
		private BigDecimal shareDiscount;


		/**
		 * 必填
		 * 总价格
		 * 总价格，"详细金额逻辑点击查看"
		 */
		@SerializedName("total_amount")
		private BigDecimal totalAmount;


		/**
		 * 必填
		 * 手工调整的优惠金额
		 * 手工调整的优惠金额，"详细金额逻辑点击查看"
		 */
		@SerializedName("adjust_amount")
		private BigDecimal adjustAmount;


		/**
		 * 必填
		 * 退款金额
		 * 退款金额
		 */
		@SerializedName("refund_amount")
		private BigDecimal refundAmount;


		/**
		 * 必填
		 * 备注
		 * 备注
		 */
		@SerializedName("remark")
		private String remark;


		/**
		 * 必填
		 * json串
		 * json串，若无可传""
		 */
		@SerializedName("json")
		private String json;


		// 赠品方式相关常量
		public static final Integer GIFT_TYPE_NOT_GIFT = 0; // 非赠品
		public static final Integer GIFT_TYPE_AUTO = 1; // 自动赠送
		public static final Integer GIFT_TYPE_MANUAL = 2; // 手工赠送
		public static final Integer GIFT_TYPE_PERIOD = 4; // 周期购赠送
		public static final Integer GIFT_TYPE_PLATFORM = 8; // 平台赠送
		public static final Integer GIFT_TYPE_FULL = 32; // 阶梯满赠
		public static final Integer GIFT_TYPE_CRM = 64; // CRM追加赠送
		public static final Integer GIFT_TYPE_MAIN = 65; // 主品

		/**
		 * 非必填
		 * 赠品方式
		 * 0非赠品
		 * 1自动赠送
		 * 2手工赠送
		 * 4周期购赠送
		 * 8平台赠送
		 * 32阶梯满赠
		 * 64CRM追加赠送
		 * 65主品
		 */
		@SerializedName("gift_type")
		private Integer giftType;
	}


	/**
	 *
	 * 优惠信息
	 *
	 * */
	@Data
	public static class Discount {

		/**
		 * 必填
		 * 原始订单号
		 * 原始订单号
		 */
		@SerializedName("tid")
		private String tid;


		/**
		 * 必填
		 * 原始子单号
		 * 原始子单号
		 */
		@SerializedName("oid")
		private String oid;


		/**
		 * 非必填
		 * 唯一编码
		 * 平台上优惠的唯一标识
		 */
		@SerializedName("sn")
		private String sn;


		/**
		 * 非必填
		 * 优惠名称
		 * 优惠名称
		 */
		@SerializedName("name")
		private String name;


		/**
		 * 非必填
		 * 优惠详情
		 * 优惠详情
		 */
		@SerializedName("detail")
		private String detail;


		/**
		 * 非必填
		 * 优惠金额
		 * 优惠金额
		 */
		@SerializedName("amount")
		private BigDecimal amount;
	}

}
