package com.erp.model.workflow.enums;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.common.core.constant.EnumMessage;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/**
 * <p>
 * 查询option配置表(数大臣单据字段) 配置单据 枚举
 * </p>
 *
 * @author hcg
 * @since 2025-05-15 12:14:18
 */
public enum CfgQueryOptionBussinessKeyEnum implements EnumMessage {
	PURCHASEORDER("purchaseOrder", "采购订单"),
	PURCHASEAPPLICATION("purchaseApplication", "采购申请单"),
	PURCHASECHANGE("purchaseChange", "采购变更单"),
	PRODUCTCHANGE("productChange", "变更管理"),
	PRODUCTBOMINFO("productBomInfo", "BOM管理"),
	SALESDEMAND("salesDemand", "备货申请单"),
	POINSTOCK("poInstock", "入库单"),
	SOOUTSTOCK("soOutstock", "销售出库单"),
	CUSTOMERB2BCHANGESELLER("customerB2bChangeSeller", "B2B客户表变更销售员"),
	REQUISITIONAPPLICATIONCHANGE("requisitionApplicationChange", "要货申请变更单"),
	PRODUCTLOGISTICS("ProductLogistics", "物流产品信息"),
	TRANSFERAPPLICATION("transferApplication", "调拨申请单"),
	SUPPLIER("supplier", "供应商列表"),
	PORETURN("poReturn", "采购退货单"),
	PILOTAPPLICATION("pilotApplication", "试产量产单"),
	SODELIVERYNOTICECHANGE("soDeliveryNoticeChange", "发货通知变更单"),
	STOCKTAKINGTASK("stocktakingTask", "盘点任务"),
	TRANSFERINFO("transferInfo", "直接调拨单"),
	PORECEIVE("poReceive", "收货单"),
	PRODUCTDETAIL("productDetail", "产品管理"),
	PROJECTTASK("projectTask", "任务列表"),
	SUBCONTRACTORDER("subcontractOrder", "委外订单"),
	PURCHASEPRICE("purchasePrice", "采购价目表"),
	PURCHASEPRICECHANGE("purchasePriceChange", "采购调价表"),
	SOB2C("soB2c", "B2C销售订单"),
	SOINFO("soInfo", "B2B销售订单"),
	FIRSTMILEDELIVERY("firstMileDelivery", "发货单"),
	CUSTOMERINFO("customerInfo", "B2B客户列表"),
	SOCHANGE("soChange", "B2B销售变更单"),
	DELIVERYPLAN("deliveryPlan", "第三方仓发货计划"),
	REQUISITIONAPPLICATION("requisitionApplication", "要货申请"),
	SOPRICE("soPrice", "销售价目表"),
	SOPRICECHANGE("soPriceChange", "销售调价表"),
	TRANSFERIN("transferIn", "分步式调入单"),
	TRANSFEROUT("transferOut", "分步式调出单"),
	SO_MULTI_CHANNEL("soMultiChannel", "多渠道订单"),

	SAMPLE_RETURN_INFO("sampleReturnInfo", "样品归还单"),
	SAMPLE_BORROW_INFO("sampleBorrowInfo", "样品借用单"),
	SAMPLE_SCRAP_INFO("sampleScrapInfo", "样品报废单"),
	EXHIBITION_ORDER("exhibitionOrder", "展会订单"),
	MOLD_INFO("moldInfo", "模具档案"),
    ;
    /**
     * 类型
     */
    @EnumValue
    @JsonValue
    private String code;
    /**
     * 名称
     */
    private String name;

    CfgQueryOptionBussinessKeyEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    public static String getName(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (CfgQueryOptionBussinessKeyEnum statusEnum : CfgQueryOptionBussinessKeyEnum.values()) {
            if (code.equals(statusEnum.getCode())) {
                return statusEnum.getName();
            }
        }
        return "";
    }
	public static CfgQueryOptionBussinessKeyEnum getByCode(String code) {
		if (StringUtils.isBlank(code)) {
			return null;
		}
		for (CfgQueryOptionBussinessKeyEnum statusEnum : CfgQueryOptionBussinessKeyEnum.values()) {
			if (code.equals(statusEnum.getCode())) {
				return statusEnum;
			}
		}
		return null;
	}
}
