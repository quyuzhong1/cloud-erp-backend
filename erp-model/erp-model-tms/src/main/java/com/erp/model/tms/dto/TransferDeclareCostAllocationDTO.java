package com.erp.model.tms.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 中转费用分摊请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-12-03
*/
@Data
@NoArgsConstructor
public class TransferDeclareCostAllocationDTO implements Serializable {




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
          * 主表id
          */
          private String  mainId;

         /**
         * 中转报关id
         */
         private String transferDeclareId;

         /**
         * 核算期间
         */
         private String reportDate;

         /**
         * 会计
         */
         private String accountDate;

         /**
         * 核算状态：toBeConfirm=待确认，confirmed=已确认
         */
         private String reportStatus;
         /**
          * 核算状态名称
          */
         private String reportStatusName;
         
         /**
          * 对账
          */
         private String reconciliationStatus;
         
         /**
          * 对账
          */
         private String reconciliationStatusName;
         
         /**
         * 大表状态：toDo=待生成，done=已生成
         */
         private String bigTableStatus;
         /**
          * 大表状态名称
          */
         private String bigTableStatusName;
         
         /**
          * 物流商：名称+渠道
          */
         private String channelId;
         
         /**
          * 物流商：名称+渠道
          */
         private String supplierName;
         
         /**
          * 销售订单
          */
         private String soCode;
         
         /**
          * 预报
          */
         private LocalDate instockForecastDate;
         
         /**
          * 确认时间
          */
         private LocalDate confirmTime;
         
         /**
          * 店铺名称
          */
         private String shopId;
         
         /**
          * 店铺名称
          */
         private String shopName;
         
         /**
          * 国家
          */
         private String toCountry;
         
         /**
          * sku
          */
          private String skuId;
         
         /**
         * sku
         */
         private String skuNo;
         
         /**
          * 产品名称
          */
          private String skuName;

         /**
         * 发货数量
         */
         private Integer deliveryQty;
         
         /**
     	 * 预估收费重
     	 */
     	private BigDecimal billingWeight;
     	/**
     	 * 预估收费重单位
     	 */
     	private String estimateWeightUnit;
     	
     	/**
     	 * 实际计费重
     	 */
     	private BigDecimal billingWeightLogistics;
     	
     	/**
     	 * 单SKU计费重
     	 */
     	private BigDecimal skuWeight;
     	
     	/**
     	 * 单位成本
     	 */
     	private BigDecimal unitCost;
     	/**
     	 * 单位成本币别
     	 */
     	private String unitCurrency;
     	
     	/**
     	 * 总成本
     	 */
     	private BigDecimal totalCost;
     	
     	/**
     	 * 费用来源
     	 */
     	private String feeSource = "实际账单";
     	
     	/**
          * 费用类型
          */
          private String feeType;
          /**
          * 费用类型名称
          */
         private String feeTypeName;
         
         /**
          * 账单金额
          */
          private BigDecimal billAmount;
          
          /**
           * 分摊金额
           */
          private BigDecimal allocatedAmount;
           
           /**
            * 单个产品分摊
            */
          private BigDecimal productAllocatedAmount;
           
            /**
             * 费用分摊方式
             */
          private String feeAllocationType;
             
             /**
              * 费用分摊方式名称
              */
          private String feeAllocationTypeName;
            
            /**
             * 重量分摊方式
             */
          private String weightAllocationType;
            /**
             * 重量分摊方式名称
             */
          private String weightAllocationTypeName;
          
          /**
           * 创建时间
           */
          private LocalDateTime createTime;

          /**
           * 更新人名称
           */
          private String updateUserName;

          /**
           * 更新时间
           */
          private LocalDateTime updateTime;
          
          /**
           * 币别
           */
          private String currency;
          
          /**
          * 币别符号
          */
         private String currencySymbol;

    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 中转报关id
        */
        @NotBlank(message = "中转报关id不能为空")
        @Size(max = 19,message = "中转报关id最大长度不能超过19位")
        private String transferDeclareId;

        /**
        * 核算期间
        */
        @NotBlank(message = "核算期间不能为空")
        @Size(max = 50,message = "核算期间最大长度不能超过50位")
        private String reportDate;

        /**
        * 会计
        */
        @NotBlank(message = "会计不能为空")
        @Size(max = 50,message = "会计最大长度不能超过50位")
        private String accountDate;

        /**
        * 核算状态：toBeConfirm=待确认，confirmed=已确认
        */
        @NotBlank(message = "核算状态：toBeConfirm=待确认，confirmed=已确认不能为空")
        @Size(max = 20,message = "核算状态：toBeConfirm=待确认，confirmed=已确认最大长度不能超过20位")
        private String reportStatus;

        /**
        * 大表状态：toDo=待生成，done=已生成
        */
        @NotBlank(message = "大表状态：toDo=待生成，done=已生成不能为空")
        @Size(max = 20,message = "大表状态：toDo=待生成，done=已生成最大长度不能超过20位")
        private String bigTableStatus;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 30,message = "skuId最大长度不能超过30位")
        private String skuId;

        /**
        * 报关对账id
        */
        @NotBlank(message = "报关对账id不能为空")
        @Size(max = 19,message = "报关对账id最大长度不能超过19位")
        private String declareReconciliationId;

        /**
        * 报关对账明细id
        */
        @NotBlank(message = "报关对账明细id不能为空")
        @Size(max = 19,message = "报关对账明细id最大长度不能超过19位")
        private String declareReconciliationDetailId;

        /**
        * 发货数量
        */
        @NotNull(message = "发货数量不能为空")
        private Integer deliveryQty;

        /**
        * 单SKU计费重
        */
        @NotNull(message = "单SKU计费重不能为空")
        @Digits(integer = 12, fraction = 4, message = "单SKU计费重整数位不能超过12位，小数位不能超过4位")
        private BigDecimal skuWeight;


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

    }

    /**
     * 列表数据
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO extends ViewDTO{
    	
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
         * 会计期间
         */
        private String reportDate;
        
        /**
         * 核算状态 http://172.16.100.11:3002/project/128/interface/api/25522 key=reportStatus
         */
        private String reportStatus;

    }

}