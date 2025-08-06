package com.erp.model.tms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.tms.enums.LogisticsBillCostCheckStatusEnum;
import com.erp.model.tms.enums.LogisticsBillCostPayTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 自发货费用请求响应实体
 * </p>
 *
 * @author Will
 * @since 2023-11-06
*/
@Data
@NoArgsConstructor
public class LogisticsBillCostDTO implements Serializable {

    /**
     * 列表参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 排除的类型
         */
        private List<String> excludeOrderTypeList;
    }



    /**
     * 列表数据
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id【可排序】
         */
       private String id;
        /**
         * 来源类型名称
         */
        private String    sourceTypeName;

        /**
         * 订单类型【可排序】
         */
        private String    orderType;

        /**
         * 订单类型名称
         */
        private String    orderTypeName;

        /**
         * 对账状态【可排序】
         */
        private String    reconciliationStatus;

        /**
         * 对账状态
         */
        private String   reconciliationStatusName;

        /**
         * 渠道名称【可排序】
         */
        private String  channelName;
        /**
         * 物流商id
         */
        private String  logisticsSupplierId;
        /**
         * 物流商名称
         */
        private String  logisticsSupplierName;

        /**
         * 物流运单号【可排序】
         */
        private String  transportNo;

        /**
         * 物流跟踪单号【可排序】
         */
        private String  trackNo;
        /**
         * 渠道id
         */
        private String  channelId;

        /**
         * 运输状态【可排序】
         */
        private String  transportStatus;

        /**
         * 运输状态
         */
        private String  transportStatusName;

        /**
         * 实重【可排序】
         */
        private BigDecimal   actualWeight;

        /**
         * 体积重【可排序】
         */
        private BigDecimal  volumeWeight;

       /**
         * 计费重【可排序】
         */
        private BigDecimal  billingWeight;

        /**
         * 重量单位【可排序】
         */
        private String weightUnit;

        /**
         * 预估运费
         */
        private BigDecimal estimatedShippingCost;
        /**
         * 预估运费币别符号
         */
        private String estimatedShippingCostCurrencySymbol;
        
        private String estimatedShippingCostStr;
        /**
         * 预估关税费用
         */
        private BigDecimal estimatedDeclareCost;
        /**
         * 预估关税费用币别符号
         */
        private String estimatedDeclareCostCurrencySymbol;
        
        private String estimatedDeclareCostStr;

        /**
         * 预估其他费用
         */
        private BigDecimal estimatedOtherCost;
        /**
         * 预估其他费用币别符号
         */
        private String estimatedOtherCostCurrencySymbol;
        
        private String estimatedOtherCostStr;


        /**
         * 计费重（物流商）【可排序】
         */
        private BigDecimal  billingWeightLogistics;

        /**
         * 实际运费（物流商）【可排序】
         */
        private BigDecimal actualShippingCost;
        /**
         * 实际运费币别符号
         */
        private String actualShippingCostCurrencySymbol;
        
        private String actualShippingCostStr;

        /**
         * 实际报关费
         */
        private BigDecimal actualDeclareCost;
        /**
         * 实际报关费币别符号
         */
        private String actualDeclareCostCurrencySymbol;
        
        private String actualDeclareCostStr;

        /**
         * 实际其他费
         */
        private BigDecimal actualOtherCost;
        /**
         * 实际其他费币别符号
         */
        private String actualOtherCostCurrencySymbol;
        
        private String actualOtherCostStr;

        /**
         * 运费差异【可排序】
         */
        private BigDecimal diffShippingCost;
        
        private String diffShippingCostCurrencySymbol = "¥";
        
        private String diffShippingCostStr;

        /**
         * 平台【可排序】
         */
        private String   salesPlatform;

        /**
         * 平台名称
         */
        private String   salesPlatformName;

        /**
         * 来来源id【可排序】
         */
        private String   sourceId;

        /**
         *  来源单号【可排序】
         */
        private String  sourceType;

        /**
         * 来源单号【可排序】
         */
        private String  sourceCode;

        /**
         * 销售出库单编码【可排序】
         */
        private String  outstockCode;

        /**
         * 目的国家【可排序】
         */
        private String  toCountry;

        /**
         * 客户名称【可排序】
         */
        private String  customerName;

        /**
         * 订单时间【可排序】
         */
        private LocalDateTime  orderTime;

        /**
         * 发货时间
         */
        private LocalDateTime deliveryTime;

        /**
         * 备注【可排序】
         */
        private String  remark;

        /**
         * 币种【可排序】
         */
        private String currency;
        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 平台订单号【可排序】
         */
        private String platformCode;

        /**
         * 店铺负责人【可排序】
         */
        private String shopChargeName;
        /**
         * 费用规则
         */
        private String feeRule;
        /**
         * 费用规则名称
         */
        private String feeRuleName;
        /**
         * 账单确认时间【可排序】
         */
        private LocalDateTime confirmTime;
        
        /**
         * 体积 【可排序】
         */
        private String volume;

        /**
         * 退付款类型，pay=付款，refund=退款
         */
        private String payType;

        /**
         * 支付状态，payment=未支付，paid=已支付 【可排序】
         */
        private String payStatus;
        /**
         * 支付状态名称
         */
        private String payStatusName;

        /**
         *付款/退款时间 【可排序】
         */
        private LocalDateTime payTime;

        /**
         * 核算状态，checking=待生成，checked=已生成，confirm=已确认，名称为 checkStatusName 字段 【可排序】
         */
        private String checkStatus;
        private String checkStatusName;
        
        /**
         * 预估可抵扣税金
         */
        private BigDecimal estimatedDeductibleTax;
        /**
         * 预估可抵扣税金币别
         */
        private String estimatedDeductibleTaxCurrencySymbol;
        
        private String estimatedDeductibleTaxStr;

        /**
         * 实际可抵扣税金
         */
        private BigDecimal actualDeductibleTax;
        /**
         * 实际可抵扣税金币别
         */
        private String actualDeductibleTaxCurrencySymbol;
        
        private String actualDeductibleTaxStr;
        
        private String logisticsBillDetailId;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }

    /**
     * 列表tab
     */
    @Data
    @NoArgsConstructor
    public static class TabListDTO {
        /**
         * 类型
         */
        private String tabFlag;

        /**
         * 类型名称
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;
    }

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
         * 物流单号
         */
        private String transportNo;

        /**
         * 物流跟踪单号
         */
        private String trackNo;

        /**
        * 对账状态（字典reconciliationStatus）
        */
        private String reconciliationStatus;

        /**
        * 物流渠道id
        */
        private String channelId;

        /**
        * 物流单id
        */
        private String logisticsBillId;

        /**
        * 实重
        */
        private BigDecimal actualWeight;

        /**
         * 重量单位
         */
        private String weightUnit;

        /**
        * 体积重
        */
        private BigDecimal volumeWeight;

        /**
        * 计费重
        */
        private BigDecimal billingWeight;

        /**
        * 预估运费
        */
        private BigDecimal estimatedShippingCost ;

        /**
        * 计费重（物流商）
        */
        private BigDecimal billingWeightLogistics;

        /**
        * 实际运费（物流商）
        */
        private BigDecimal actualShippingCost;

        /**
        * 运费差异
        */
        private BigDecimal diffShippingCost;

        /**
        * 币别
        */
        private String currency;
        /**
         * 费用规则
         */
        private String feeRule;
        /**
         * 费用规则名称
         */
        private String feeRuleName;
        /**
         * 账单确认时间【可排序】
         */
        private LocalDateTime confirmTime;

        /**
        * 备注
        */
        private String remark;

        /**
         * 费用明细
         */
        private List<TmsCostDetailDTO.ViewDTO>  costDetailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
    	/**
         * 对账类型   http://172.16.100.11:3002/project/128/interface/api/25522 key=logisticsBillCostPayType
         */
         private String payType = LogisticsBillCostPayTypeEnum.PAY.getCode();
    }
    
    /**
     * 新增付款/退款，仅创建
     */
    @Data
    @NoArgsConstructor
    public static class AddDataDTO extends DataDTO{
    	/**
    	 * 对账类型   http://172.16.100.11:3002/project/128/interface/api/25522 key=logisticsBillCostPayType
    	 */
    	@NotBlank(message = "对账类型不能为空")
    	private String payType;
    	
    	/**
    	 * 选择单据id
    	 */
    	@NotBlank(message = "选择单据不能为空")
    	private String sourceId;
    	
    }
    
    /**
     * 新增付款/退款，仅创建
     */
    @Data
    @NoArgsConstructor
    public static class EditDataDTO extends DataDTO{
    	/**
    	 * id
    	 */
    	@NotBlank(message = "id不能为空")
    	private String id;
    	
    }
    
    /**
     * 新增付款/退款，仅创建
     */
    @Data
    @NoArgsConstructor
    public static class EditViewDTO extends DataDTO{
    	/**
    	 * id
    	 */
    	private String id;
    	
    	/**
    	 * 对账类型
    	 */
    	private String payType;
    	
    	/**
    	 * 对账类型名称
    	 */
    	private String payTypeName;
    	
    	/**
    	 * 物流单号
    	 */
    	private String trackNo;
    	
    	/**
    	 * 费用分类
    	 */
    	private String dictCostCategory;
    }
    
    /**
     * 新增付款/退款，仅创建
     */
    @Data
    @NoArgsConstructor
    public static class DataDTO extends TmsCostDetailDTO.DetailDTO{
    	
    	/**
    	 * 计费重[预估]
    	 */
    	private BigDecimal billingWeight;
    	
    	/**
    	 * 计费重[物流商]
    	 */
    	private BigDecimal billingWeightLogistics;
    	
    	/**
    	 * 实际金额币别
    	 */
    	@NotBlank(message = "实际金额币别不能为空")
    	private String currency;
    	
    	/**
    	 * 预估金额币别
    	 */
    	@NotBlank(message = "预估金额币别不能为空")
    	private String estimatedCurrency;
    }
    
    /**
     * 新增付款/退款，对账已确认
     */
    @Data
    @NoArgsConstructor
    public static class ConfirmAddDataDTO{
    	/**
    	 * 对账确认时间
    	 */
    	private LocalDateTime confirmTime;
    	
    	/**
    	 * 新增付款/退款数据
    	 */
    	private List<AddDataDTO> addDataDTOList;
    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO  {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 计费重（物流商）
         */
        private BigDecimal billingWeightLogistics;

        /**
         * 备注
         */
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
         * 币别
         */
        private String currency;

        /**
         * 费用明细
         */
        private List<TmsCostDetailDTO.UpdateDTO>  costDetailList;

        /**
         * 实重
         */
        private BigDecimal actualWeight;

        /**
         * 体积重
         */
        private BigDecimal volumeWeight;

        /**
         * 实际体积重(物流商)
         */
        private BigDecimal volumeWeightLogistics;

        /**
         * 实重(物流商)
         */
        private BigDecimal weightLogistics;

        private String logisticsBillDetailId;

        private String trackNo;
        
        /**
    	 * 计费重[预估]
    	 */
    	private BigDecimal billingWeight;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 对账状态
         */
        private String reconciliationStatus;

        /**
        * 物流单id
        */
        @NotBlank(message = "物流单id不能为空")
        @Size(max = 19,message = "物流单id最大长度不能超过19位")
        private String logisticsBillId;

        /**
         * 物流单明细id
         */
        @NotBlank(message = "物流单明细id不能为空")
        @Size(max = 19,message = "物流单明细id最大长度不能超过19位")
        private String logisticsBillDetailId;

        /**
        * 实重
        */
        private BigDecimal actualWeight;

        /**
        * 体积重
        */
        private BigDecimal volumeWeight;

        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 32,message = "币别最大长度不能超过32位")
        private String currency;

        /**
        * 备注
        */
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
         * 跟踪单号
         */
        private String trackNo;

        /**
         * 物流渠道id
         */
        private String channelId;

        /**
         * 汇率
         */
        private BigDecimal exchangeRate;

        /**
         * 实际体积重(物流商)
         */
        private BigDecimal volumeWeightLogistics;

        /**
         * 实重(物流商)
         */
        private BigDecimal weightLogistics;
        
        /**
    	 * 计费重[物流商]
    	 */
    	private BigDecimal billingWeightLogistics;

        /**
         * 费用明细
         */
        @NotEmpty(message = "费用明细不能为空")
        private List<TmsCostDetailDTO.AddDTO>  costDetailList;

    }
    /**
     * 修改导入数据
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDataDTO {

        /**
         * 物流单id
         */
        private String logisticsBillId ;

        /**
         * 计费重[物流商]
         */
        private BigDecimal billingWeightLogistics;

        /**
         * 币种
         */
        private String currency;

        /**
         * 费用明细
         */
        private List<TmsCostDetailDTO.AddDTO>  costDetailList;
    }


    /**
     * 修改状态
     */
    @Data
    @NoArgsConstructor
    public static class UpdateStatusDTO {

        /**
         * ids
         */
        private List<String> ids;

        /**
         * 状态 对账类型   http://172.16.100.11:3002/project/128/interface/api/25522 key=reconciliationStatus
         */
        private String reconciliationStatus;
        
        /**
         * 对账确认时间
         */
        private LocalDateTime confirmTime;

    }
    
    /**
     * 支付状态
     */
    @Data
    @NoArgsConstructor
    public static class PayStatusDTO {
    	
    	/**
    	 * ids
    	 */
    	private List<String> ids;
    	
    	/**
    	 * 支付状态	根据列表payType字段，pay=付款，refund=退款，支付状态的 paid=已付款/已退款	payment=待付款/待退款
    	 */
    	private String payStatus;
    	
    	/**
    	 *付款/退款时间
    	 */
    	private LocalDateTime payTime;
    	
    }
    
    /**
     * 支付状态
     */
    @Data
    @NoArgsConstructor
    public static class PushDTO {
    	
    	/**
    	 * ids
    	 */
    	private List<String> ids;
    	
    	/**
    	 *核算日期
    	 */
    	@NotBlank(message = "核算日期不能为空")
    	private String reportDate;
    	
    }
    @Data
    @NoArgsConstructor
    public static class OutStockDTO {
        /**
         * 销售出库单id
         */
        private String outstockId;
        /**
         * 销售出库单编码
         */
        private String outstockCode;
        /**
         * 运输编号
         */
        private String transportNo;
        /**
         * 对账状态
         */
        private String reconciliationStatus;
        /**
         * 物流单号
         */
        private String trackNo;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateShopChargeDTO {

        /**
         * 店铺Id
         */
        @NotBlank(message = "店铺Id不能为空")
        private String shopId;


        /**
         * 店铺负责人id
         */
        @NotBlank(message = "店铺负责人Id不能为空")
        private String shopChargeId;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExchangeRateDTO {
        private String id;
        private String currency;
        private BigDecimal exchangeRate;
        private LocalDateTime createTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillCostNoBillDTO {
        private String id;
        private String logisticsBillId;
        private String logisticsBillDetailId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostDetailDTO {
        private String costId;
        private String logisticsBillId;
        private String reconciliationId;
        private String reconciliationStatus;
        private String cfgId;
        private Boolean isAllocate;
        private Boolean isDefault;
        private String dictCostAttribution;
        private String dictCostCategory;
        private String detailId;
        private String currency;
        private BigDecimal exchangeRate;
        //账单类型 实际账单 actual 暂估账单
        private String type;
        private String sourceType;
        private BigDecimal costValue;
    }
}