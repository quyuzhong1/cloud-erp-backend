
package com.sdk.wangdian.sdk.api.wms.stockin.dto;

import lombok.Data;
import lombok.ToString;
import java.math.BigDecimal;
import java.util.List;

/**
 * 旺店通B2C销售订单请求类
 *
 * @author jack
 * @date 2025-12-10
 */
@Data
public class CreateSoB2cRequest {

	/**
	 * 店铺编号，由旺店通分配
	 * 必填
	 */
	private String shopNo;

	/**
	 * 主订单数据列表
	 * 必填
	 */
	private List<RawTrade> rawTradeList;

	/**
	 * 订单明细数据列表
	 * 必填
	 */
	private List<RawTradeOrder> rawTradeOrderList;

	/**
	 * 优惠列表
	 * 非必填
	 */
	private List<DiscountInfo> discountList;

	@ToString
	@Data
	public static class RawTrade {

		/**
		 * 主订单号
		 * 必填
		 */
		private String tid;

		/**
		 * 订单处理状态 10未处理 20已审核 30已递交
		 * 必填
		 */
		private Integer processStatus;

		/**
		 * 订单状态
		 * 必填
		 */
		private Integer tradeStatus;

		/**
		 * 退款状态
		 * 必填
		 */
		private Integer refundStatus;

		/**
		 * 支付状态
		 * 必填
		 */
		private Integer payStatus;

		/**
		 * 主订单包含商品明细数量
		 * 必填
		 */
		private Integer orderCount;

		/**
		 * 商品总数量
		 * 必填
		 */
		private BigDecimal goodsCount;

		/**
		 * 支付方式
		 * 必填
		 */
		private Integer payMethod;

		/**
		 * 下单时间 yyyy-MM-dd HH:mm:ss
		 * 必填
		 */
		private String tradeTime;

		/**
		 * 支付时间
		 * 非必填
		 */
		private String payTime;

		/**
		 * 结束时间
		 * 可空
		 */
		private String endTime;

		/**
		 * 买家昵称
		 * 必填
		 */
		private String buyerNick;

		/**
		 * 买家留言
		 * 非必填
		 */
		private String buyerMessage;

		/**
		 * 买家邮箱
		 * 非必填
		 */
		private String buyerEmail;

		/**
		 * 买家地区；省市区
		 * 必填
		 */
		private String buyerArea;

		/**
		 * 收货人姓名
		 * 必填
		 */
		private String receiverName;

		/**
		 * 收货地区
		 * 必填
		 */
		private String receiverArea;

		/**
		 * 收货地址
		 * 必填
		 */
		private String receiverAddress;

		/**
		 * 收货邮编
		 * 非必填
		 */
		private String receiverZip;

		/**
		 * 收货手机号
		 * 非必填
		 */
		private String receiverMobile;

		/**
		 * 收货座机
		 * 非必填
		 */
		private String receiverTelno;

		/**
		 * 运费金额
		 * 必填
		 */
		private BigDecimal postAmount;

		/**
		 * 其他费用
		 * 非必填
		 */
		private BigDecimal otherAmount;

		/**
		 * 系统优惠金额（整单优惠）
		 * 非必填
		 */
		private BigDecimal discount;

		/**
		 * 应付金额
		 * 必填
		 */
		private BigDecimal receivable;

		/**
		 * 平台服务费
		 * 非必填
		 */
		private BigDecimal platformCost;

		/**
		 * 发票类型
		 * 非必填
		 */
		private Integer invoiceType;

		/**
		 * 发票抬头
		 * 非必填
		 */
		private String invoiceTitle;

		/**
		 * 发票内容
		 * 非必填
		 */
		private String invoiceContent;

		/**
		 * 承运物流类型
		 * 非必填
		 */
		private Integer logisticsType;

		/**
		 * 客户自定义字段 JSON格式
		 * 非必填
		 */
		private String custData;

		/**
		 * 交付方式 1现款现货 2货到付款
		 * 必填
		 */
		private Integer deliveryTerm;

		/**
		 * 支付单号
		 * 非必填
		 */
		private String payId;

		/**
		 * 支付账号
		 * 非必填
		 */
		private String payAccount;

		/**
		 * 卖家备注
		 * 非必填
		 */
		private String remark;

		/**
		 * 卖家备注标记
		 * 非必填
		 */
		private Integer remarkFlag;

		/**
		 * 货到付款金额
		 * 非必填
		 */
		private BigDecimal codAmount;

		/**
		 * 是否自动发仓储接口
		 * 非必填
		 */
		private Boolean isAutoWms;

		/**
		 * 仓库编号
		 * 非必填
		 */
		private String warehouseNo;

		/**
		 * 预约送货时间 yyyy-MM-dd HH:mm:ss
		 * 非必填
		 */
		private String toDeliverTime;

		/**
		 * 已收金额
		 * 非必填
		 */
		private BigDecimal received;

		/**
		 * 超时天数
		 * 非必填
		 */
		private Integer consignInterval;

		/**
		 * 实付金额
		 * 非必填
		 */
		private BigDecimal paid;

		/**
		 * 是否加密字段
		 * 非必填
		 */
		private Byte isSealed;

		/**
		 * 分销卖家昵称
		 * 非必填
		 */
		private String fenxiaoNick;

		/**
		 * 证件类型
		 * 非必填
		 */
		private Integer idCardType;

		/**
		 * 证件号码
		 * 非必填
		 */
		private String idCard;
	}


	@ToString
	@Data
	public static class RawTradeOrder {

		/**
		 * 主订单号
		 * 必填
		 */
		private String tid;

		/**
		 * 子单号
		 * 必填
		 */
		private String oid;

		/**
		 * 子单状态
		 * 必填
		 */
		private Integer status;

		/**
		 * 退款状态
		 * 必填
		 */
		private Integer refundStatus;

		/**
		 * 商品ID
		 * 必填
		 */
		private String goodsId;

		/**
		 * 规格ID
		 * 必填
		 */
		private String specId;

		/**
		 * 商品编码
		 * 非必填
		 */
		private String goodsNo;

		/**
		 * 规格编码
		 * 非必填
		 */
		private String specNo;

		/**
		 * 商品名称
		 * 必填
		 */
		private String goodsName;

		/**
		 * 规格名称
		 * 必填
		 */
		private String specName;

		/**
		 * 订单类型 0正常 1赠品
		 * 非必填
		 */
		private Integer orderType;

		/**
		 * 类目ID
		 * 非必填
		 */
		private String cid;

		/**
		 * 数量
		 * 必填
		 */
		private BigDecimal num;

		/**
		 * 单价
		 * 必填
		 */
		private BigDecimal price;

		/**
		 * 订单级优惠金额
		 * 非必填
		 */
		private BigDecimal discount;

		/**
		 * 分摊优惠金额
		 * 非必填
		 */
		private BigDecimal shareDiscount;

		/**
		 * 实际金额
		 * 必填
		 */
		private BigDecimal totalAmount;

		/**
		 * 调整金额
		 * 非必填
		 */
		private BigDecimal adjustAmount;

		/**
		 * 退款金额
		 * 非必填
		 */
		private BigDecimal refundAmount;

		/**
		 * 单行备注
		 * 非必填
		 */
		private String remark;

		/**
		 * JSON 扩展字段
		 * 非必填
		 */
		private String json;

		/**
		 * 赠品方式 1平台赠品 2商家赠送
		 * 非必填
		 */
		private Integer giftType;
	}

	@ToString
	@Data
	public static class DiscountInfo {

		/**
		 * 主订单号
		 * 必填
		 */
		private String tid;

		/**
		 * 行项目子单号
		 * 非必填
		 */
		private String oid;

		/**
		 * 优惠编号
		 * 非必填
		 */
		private String sn;

		/**
		 * 优惠说明
		 * 非必填
		 */
		private String name;

		/**
		 * 优惠明细
		 * 非必填
		 */
		private String detail;

		/**
		 * 金额
		 * 非必填
		 */
		private BigDecimal amount;
	}

}