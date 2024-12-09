package com.erp.model.tms.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 小包费用分摊请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-12-02
*/
@Data
@NoArgsConstructor
public class SmallBagCostAllocationDTO implements Serializable {




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
        * 小包费用id
        */
        private String costId;

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
         * 签收
         */
        private String deliveryStatus;
        /**
         * 签收
         */
        private String deliveryStatusName;

        /**
        * 大表状态：toDo=待生成，done=已生成
        */
        private String bigTableStatus;
        /**
         * 大表状态名称
         */
        private String bigTableStatusName;
        
        private String channelId;
        
        /**
         * 物流商：名称+渠道
         */
        private String supplierName;
        
        /**
         * 出库单
         */
        private String outstockCode;
        
        /**
         * 运单
         */
        private String transportNo;
        
        /**
         * 跟踪号
         */
        private String trackNo;

        /**
         * 发货时间
         */
        private LocalDateTime deliveryTime;
        
        /**
         * 签收时间
         */
        private String trackStatus;
        /**
         * 签收时间
         */
        private LocalDateTime signTime;
        
        /**
         * 确认时间
         */
        private LocalDateTime confirmTime;
        
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
        * 销售出库单明细id
        */
        private String outstockDetailId;

        /**
        * 发货数量
        */
        private Integer deliveryQty;
        
        /**
    	 * 预估收费重
    	 */
    	private BigDecimal billingWeight;
    	
    	/**
    	 * 实际计费重
    	 */
    	private BigDecimal billingWeightLogistics;
    	
    	/**
    	 * 单SKU计费重
    	 */
    	private BigDecimal skuWeight;
    	
    	/**
    	 * 单位成本币别
    	 */
    	private String unitCurrency;
    	
    	/**
    	 * 单位成本
    	 */
    	private BigDecimal unitCost;
    	
    	/**
    	 * 总成本
    	 */
    	private BigDecimal totalCost;
    	
    	/**
    	 * 费用来源
    	 */
    	private String feeSource;
    	
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
           * 分摊组织
           */
         private String orgName;
           
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
        /**
         * 支付类型
         */
        private String payType;

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
        * 小包费用id
        */
        @NotBlank(message = "小包费用id不能为空")
        @Size(max = 19,message = "小包费用id最大长度不能超过19位")
        private String costId;

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
        * 销售出库单明细id
        */
        @NotBlank(message = "销售出库单明细id不能为空")
        @Size(max = 19,message = "销售出库单明细id最大长度不能超过19位")
        private String outstockDetailId;

        /**
        * 发货数量
        */
        @NotNull(message = "发货数量不能为空")
        private Integer deliveryQty;


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