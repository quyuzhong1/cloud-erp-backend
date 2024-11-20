package com.common.business.dto;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SyncOperateEnum;
import lombok.*;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ShudiyunB2cOrderDTO {
    /**
     * 主表业务唯一键_id
     */
    private String transaction_unique_key;
    /**
     * 交易单号
     */
    private String biz_no;
    /**
     * 交易时间
     */
    private LocalDateTime biz_time;
    /**
     * 交易类型
     */
    private String transaction_type;
    /**
     * 交易子类型
     */
    private String transaction_sub_type;
    /**
     * 交易状态
     */
    private String biz_status;
    /**
     * total_goods_transaction_amount
     * 商品总成交金额（合计）
     */
    private BigDecimal total_goods_transaction_amount = BigDecimal.ZERO;
    /**
     * 优惠抵扣金额|佣金（合计）
     */
    private BigDecimal discount_deduction_amount = BigDecimal.ZERO;
    /**
     * 取消商品总金额（合计）
     */
    private BigDecimal total_canceled_goods_amount = BigDecimal.ZERO;
    /**
     * 应付总金额（合计）
     */
    private BigDecimal total_amount_payable = BigDecimal.ZERO;
    /**
     * 买家实付
     */
    private BigDecimal buyer_actual_payment = BigDecimal.ZERO;
    /**
     * 订单总运费
     */
    private BigDecimal total_freight = BigDecimal.ZERO;
    /**
     * 商品总数量 （合计）
     */
    private Integer total_goods_quantity = 0;
    /**
     * 取消商品数量 （合计）
     */
    private Integer total_canceled_goods_quantity = 0;
    /**
     * 订单应发数量（合计）
     */
    private Integer order_quantity_to_be_shipped = 0;
    /**
     * 线上申请退货总数量
     */
    private Integer online_appled_return_quanty;
    /**
     * 客户应退数量
     */
    private Integer customer_refundable_quantity;
    /**
     * 客户实退数量
     */
    private Integer quantity_buyer_returned;
    /**
     * 线上应退商品总金额
     */
    private BigDecimal online_refundable_goods_amount;
    /**
     * 线上应退运费总额
     */
    private BigDecimal online_refundable_freight;
    /**
     * 线上申请总金额
     */
    private BigDecimal online_applied_amount;
    /**
     * 商家实退金额
     */
    private BigDecimal order_seller_payed;
    /**
     * 销售组织编码
     */
    private String sales_company_code;
    /**
     * 收款组织编码
     */
    private String receiving_company_code;
    /**
     * 组织名称
     */
    private String organization_name;
    /**
     * 组织编码
     */
    private String organization_code;
    /**
     * 平台名称
     */
    private String platform_name;
    /**
     * 平台编码
     */
    private String platform_id;
    /**
     * 子平台编码
     */
    private String subplatform_no;
    /**
     * 子平台名称
     */
    private String subplatform_name;
    /**
     * 店铺名称
     */
    private String shop_name;
    /**
     * 店铺编码
     */
    private String shop_no;
    /**
     * 店铺账号
     */
    private String shop_account;
    /**
     * 根节点交易号
     */
    private String root_node_no;
    /**
     * 根节点交易创建日期
     */
    private LocalDateTime root_node_create_time;
    /**
     * 根节点交易修改日期
     */
    private LocalDateTime root_node_modify_time;
    /**
     * 交易分区
     */
    private String biz_zone;
    /**
     * 计算节点
     */
    private String compute_node;
    /**
     * 计算节点单据号
     */
    private String compute_node_document_number;
    /**
     * 商品编码
     */
    private String goods_no;
    /**
     * 商品名称
     */
    private String goods_name;
    /**
     * 规格型号
     */
    private String spec_no;
    /**
     * 规格型号名称
     */
    private String spec_name;
    /**
     * 是否赠品
     */
    private Integer is_gift;
    /**
     * 是否组合装
     */
    private Integer is_comb = 0;
    /**
     * 是否虚拟商品
     */
    private Integer is_virtual;
    /**
     * 是否服务类商品
     */
    private String is_service;
    /**
     * 经销商名称
     */
    private String distributor_name;
    /**
     * 项目名称
     */
    private String project_name;
    /**
     * 主播名称
     */
    private String anchor_name;
    /**
     * 链接地址
     */
    private String url;
    /**
     * 对账状态
     */
    private String check_status;
    /**
     * 处理策略
     */
    private String order_fulfillment_label;
    /**
     * 备注
     */
    private String remark;
    /**
     * 批次号
     */
    private String batch_no;
    /**
     * 仓库编码
     */
    private String warehouse_no;
    /**
     * 仓库名称
     */
    private String warehouse_name;
    /**
     * 交易处理状态
     */
    private String transaction_process_status;
    /**
     * 商品状态
     */
    private String goods_status;
    /**
     * 商品成交数量
     */
    private Integer goods_transaction_quantity;
    /**
     * 单位
     */
    private String unit;
    /**
     * 商品成交单价
     */
    private BigDecimal price;
    /**
     * 商品成交金额
     */
    private BigDecimal goods_transaction_amount;
    /**
     * 商品的分摊运费
     */
    private BigDecimal freight;
    /**
     * 商品基准售价
     */
    private BigDecimal goods_benchmark_selling_price;
    /**
     * 商品分摊成交单价
     */
    private BigDecimal allocated_Transaction_unit_price;
    /**
     * 商品分摊成交金额
     */
    private BigDecimal allocated_Transaction_unit_amount;
    /**
     * 父节点单号
     */
    private String parent_node_no;
    /**
     * 父节点单日期
     */
    private LocalDateTime parent_node_create_time;
    /**
     * 父节点类型
     */
    private String parent_node_type;
    /**
     * 子节点单号
     */
    private String child_node_no;
    /**
     * 子节点单日期
     */
    private LocalDateTime child_node_create_time;
    /**
     * 子节点类型
     */
    private String child_node_type;
    /**
     * 发货时间
     */
    private LocalDateTime delivery_time;
    /**
     * 物流签收时间
     */
    private LocalDateTime logistics_delivery_time;
    /**
     * 平台签收时间
     */
    private LocalDateTime platform_signing_time;
    /**
     * 发货单号
     */
    private String delivery_number;
    /**
     * 商品发货状态
     */
    private String delivery_status;
    /**
     * 发货数量
     */
    private Integer delivery_quantity;
    /**
     * 发货金额
     */
    private BigDecimal delivery_amount;
    /**
     * 商品签收数量
     */
    private Integer signing_quantity;
    /**
     * 商品签收金额
     */
    private BigDecimal signing_amount;
    /**
     * 签收商品收入占比
     */
    private BigDecimal goods_proportion_signed_revenue;
    /**
     * 退货商品收入占比
     */
    private BigDecimal goods_proportion_return_revenue;
    /**
     * 快递公司名称
     */
    private String logistic_company;
    /**
     * 快递公司编码
     */
    private String logistic_company_code;
    /**
     * 发货运单号
     */
    private String waybill_number;
    /**
     * 发货国际跟踪号
     */
    private String foreign_waybill_number;
    /**
     * 退货入库单号
     */
    private String return_receipt_number;
    /**
     * 退货入库数量
     */
    private Integer returned_quantity;
    /**
     * 国内退货运单号
     */
    private String domestic_return_waybill_number;
    /**
     * 国际退货运单号
     */
    private String international_return_waybill_number;
    /**
     * 退货入库状态
     */
    private String return_status;
    /**
     * 退货入库时间
     */
    private LocalDateTime return_receipt_time;
    /**
     * 退货入库金额
     */
    private BigDecimal return_receipt_amount;
    /**
     * 交易币别
     */
    private String transaction_currency = "";
    /**
     * 交易币别代码
     */
    private String transaction_currency_code = "";
    /**
     * 店铺本位币
     */
    private String base_currency;
    /**
     * 店铺本位币代码
     */
    private String base_currency_code;
    /**
     * 汇率
     */
    private BigDecimal exchange_rate;
    /**
     * 邮费
     */
    private BigDecimal post_amount;
    /**
     * 账期
     */
    private String period;
    /**
     * 对账标志
     */
    private String checkid;
    /**
     * 扩展标识
     */
    private String extra;
    /**
     * sd_修改时间
     */
    private LocalDateTime MODIFY_AT;
    /**
     * sd_修改人
     */
    private String MODIFY_BY;
    /**
     * sd_创建时间
     */
    private LocalDateTime CREATED_AT;
    /**
     * sd_创建人
     */
    private String CREATED_BY;
    /**
     * 组合装编码
     */
    private String suite_no = "";
    /**
     * 组合装名称
     */
    private String suite_name = "";
    /**
     * MSKU编码
     */
    private String msku_code;
    /**
     * MSKU名称
     */
    private String msku_name;
    /**
     * 商家SKU编码
     */
    private String sku_code;
    /**
     * 商家SKU名称
     */
    private String sku_name;
    /**
     * 结算币别代码
     */
    private String settlement_currency_code;
    /**
     * 结算币别
     */
    private String settlement_currency;
    /**
     * 买家姓名
     */
    private String customer_name;
    /**
     * 国家
     */
    private String country;
    /**
     * 地区
     */
    private String area;
    /**
     * 退款原因
     */
    private String reason;
    /**
     * 订单交易标签
     */
    private String order_transaction_tag;
    /**
     * 商品成交金额(含税)
     */
    private BigDecimal goods_transaction_price_tax;
    /**
     * 税费
     */
    private BigDecimal taxation;
    /**
     * 运输成本
     */
    private BigDecimal transportation_cost;
    /**
     * 货件配送参数
     */
    private String shipment_delivery_parameters;
    /**
     * 客户应付金额
     */
    private BigDecimal amount_payable_customer;
    /**
     * 签收方式
     */
    private String signing_method;
    /**
     * 物流妥投时间
     */
    private LocalDateTime logistics_invest_time;
    /**
     * 预估妥投时间
     */
    private LocalDateTime estimate_investment_time;
    /**
     * 配送地址
     */
    private String shipping_address;
    /**
     * 更新时间
     */
    private LocalDateTime update_time;
    /**
     * 买家信息
     */
    private String buyer_information;
    /**
     * 来源系统
     */
    private String source_system;
    /**
     * 国家编码
     */
    private String country_code;
    /**
     * 地区编码
     */
    private String area_code;
    /**
     * 商品成交单价(不含税)
     */
    private BigDecimal price_excl_tax;
    /**
     * 商品成交金额(不含税)
     */
    private BigDecimal goods_transaction_amount_excl_tax;
    /**
     * 税率
     */
    private BigDecimal tax_rate;
    /**
     * 原始根节点交易号
     */
    private String root_node_no_initial;


    public String sdyStatusHandle(String operateEnum) {
        if (SyncOperateEnum.OPERATE_APPROVE.getCode().equals(operateEnum)) {
            return ApproveStatusEnum.APPROVE.getName();
        } else if (SyncOperateEnum.OPERATE_DISAPPROVE.getCode().equals(operateEnum)) {
            return ApproveStatusEnum.WAIT_SUBMIT.getName();
        } else if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operateEnum)) {
            return "已删除";
        } else if (SyncOperateEnum.OPERATE_INVALID.getCode().equals(operateEnum)) {
            return "已作废";
        }
        return ApproveStatusEnum.APPROVE.getName();
    }
}
