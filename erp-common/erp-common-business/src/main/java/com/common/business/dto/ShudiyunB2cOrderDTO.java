package com.common.business.dto;

import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.SyncOperateEnum;
import lombok.*;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@ToString
public class ShudiyunB2cOrderDTO {
    /**
     * 主表业务唯一键_id
     */
    private String biz_uni_key;
    /**
     * 交易单号
     */
    private String biz_no;
    /**
     * 交易时间
     */
    private String biz_time;
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
     * 操作状态
     */
    private String status;
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
    private String sales_company_code = "";
    /**
     * 收款组织编码
     */
    private String receiving_company_code = "";
    /**
     * 组织名称
     */
    private String organization_name = "";
    /**
     * 组织编码
     */
    private String organization_code = "";
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
    private String root_node_create_time;
    /**
     * 根节点交易修改日期
     */
    private String root_node_modify_time;
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
    private String spec_no = "";
    /**
     * 规格型号名称
     */
    private String spec_name = "";
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
    private BigDecimal price = BigDecimal.ZERO;
    /**
     * 商品成交金额
     */
    private BigDecimal goods_transaction_amount = BigDecimal.ZERO;
    /**
     * 商品优惠金额
     */
    private BigDecimal goods_discount_deduction_amount = BigDecimal.ZERO;
    /**
     * 商品的分摊运费
     */
    private BigDecimal freight = BigDecimal.ZERO;
    /**
     * 商品分摊税费
     */
    private BigDecimal goods_taxation = BigDecimal.ZERO;
    /**
     * 商品基准售价
     */
    private BigDecimal goods_benchmark_selling_price = BigDecimal.ZERO;
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
    private String parent_node_create_time;
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
    private String child_node_create_time;
    /**
     * 子节点类型
     */
    private String child_node_type;
    /**
     * 发货时间
     */
    private String delivery_time;
    /**
     * 物流签收时间
     */
    private String logistics_delivery_time;
    /**
     * 平台签收时间
     */
    private String platform_signing_time;
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
    private String logistic_company = "";
    /**
     * 快递公司编码
     */
    private String logistic_company_code = "";
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
    private String return_receipt_time;
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
    private String MODIFY_AT;
    /**
     * sd_修改人
     */
    private String MODIFY_BY;
    /**
     * sd_创建时间
     */
    private String CREATED_AT;
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
    private String logistics_invest_time;
    /**
     * 预估妥投时间
     */
    private String estimate_investment_time;
    /**
     * 配送地址
     */
    private String shipping_address;
    /**
     * 更新时间
     */
    private String update_time;
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

    /**
     * 区域编码
     */
    private String region_code;

    /**
     * 区域名称
     */
    private String region_name;

    /**
     * 军区编码
     */
    private String military_region_code;

    /**
     * 军区名称
     */
    private String military_region_name;

    /**
     * 部门编码
     */
    private String department_code;

    /**
     * 部门名称
     */
    private String department_name;

    /**
     * 发货类型：按照0/1推送【(0:平台配送1:自发货)】
     */
    private String fulfillment_type;


    public String sdyStatusHandle(String operateEnum, int mainVersion, int detailVersion) {
        int version = 0;
        if (mainVersion > 0) {
            version = mainVersion;
        } else {
            version = detailVersion;
        }

        if (SyncOperateEnum.OPERATE_DISAPPROVE.getCode().equals(operateEnum)) {
            return "已反审";
        } else if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operateEnum)) {
            return "已删除";
        } else {
            if (version > 0) {
                return "已更新";
            } else {
                return "已创建";
            }
        }
    }
    
    public void setDefaultValue() {
    	if(StringUtils.isBlank(country_code) || StringUtils.equals("ALL", country_code)) {
    		country_code = "CN";
    	}
    	if(StringUtils.isBlank(country)) {
    		country = "中国大陆";
    	}
    	if(StringUtils.isBlank(region_code)) {
    		region_code = "AS";
    	}
    	if(StringUtils.isBlank(region_name)) {
    		region_name = "亚洲";
    	}
    	if(StringUtils.isBlank(military_region_code)) {
    		military_region_code = "china";
    	}
    	if(StringUtils.isBlank(military_region_name)) {
    		military_region_name = "中国军区";
    	}
    	if(StringUtils.isBlank(department_code)) {
    		if(StringUtils.isNotBlank(organization_name)) {
    			if(organization_name.equals("深圳市十二篮子电商有限公司")){department_code = "BM002008";}
        		if(organization_name.equals("深圳市七筐子科技有限公司")){department_code = "BM001992";}
        		if(organization_name.equals("Ulanzi US co., LTD.")){department_code = "BM001976";}
        		if(organization_name.equals("深圳市小隼智能数码有限公司")){department_code = "BM001804";}
        		if(organization_name.equals("深圳市小隼商贸有限公司")){department_code = "BM001760";}
        		if(organization_name.equals("深圳市前海优拍供应链有限公司")){department_code = "BM001939";}
        		if(organization_name.equals("小隼智創科技有限公司 FALCAM LIMITED")){department_code = "BM001832";}
        		if(organization_name.equals("深圳市小隼智造科技有限公司")){department_code = "BM001861";}
        		if(organization_name.equals("优篮迹（厦门）科技有限公司")){department_code = "BM001932";}
        		if(organization_name.equals("深圳市小隼智创科技有限公司")){department_code = "BM001890";}
        		if(organization_name.equals("ULANZI SG PTE. LTD.")){department_code = "BM001949";}
        		if(organization_name.equals("Filmeasy US CO., LTD.")){department_code = "BM001958";}
        		if(organization_name.equals("深圳市珍赏科技有限公司")){department_code = "BM001967";}
        		if(organization_name.equals("广州市简拍科技有限公司")){department_code = "BM001983";}
        		if(organization_name.equals("东莞市简拍智造科技有限公司")){department_code = "BM002000";}
        		if(organization_name.equals("深圳市优篮子科技有限公司")){department_code = "BM002018";}
        		if(organization_name.equals("唯跡有限公司VIJIM LIMITED")){department_code = "BM002086";}
        		if(organization_name.equals("深圳市唯迹科技有限公司")){department_code = "BM002094";}
    		}
    	}
    	if(StringUtils.isBlank(department_code)) {
    		department_code = "BM002094";
    	}
    	if(StringUtils.isBlank(department_name)) {
    		department_name = "直播电商组（中国军区 ）";
    	}
    }
}
