package com.erp.model.tms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * b2c报关对账单请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-03-19
*/
@Data
@NoArgsConstructor
public class TmsB2cDeclareReconciliationDTO implements Serializable {


     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
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
     * 分页列表查询参数
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
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id【可排序】
        */
        private String  id;

        /**
        * 对账单号【可排序】
        */
        private String code;

        /**
        * 审核状态【可排序】
        */
        private String approveStatus;

        /**
         * 审核名称
         */
        private String approveStatusName;

        /**
        * 审核人id【可排序】
        */
        private String approveUserId;

        /**
        * 审核人名称【可排序】
        */
        private String approveUserName;

        /**
        * 生成对账日期【可排序】
        */
        private LocalDate reconciliationDate;

        /**
        * 提交日期【可排序】
        */
        private LocalDate submitDate;

        /**
        * 提交日期【可排序】
        */
        private LocalDate approveDate;

        /**
        * 对账开始日期【可排序】
        */
        private LocalDate startDate;

        /**
        * 对账结束日期【可排序】
        */
        private LocalDate endDate;

        /**
         * 对账周期
         */
        private String cycle;

        /**
        * 物流商Id【可排序】
        */
        private String logisticsSupplierId;

        /**
        * 物流商名称【可排序】
        */
        private String logisticsSupplierName;

        /**
        * 币别【可排序】
        */
        private String currency;

        /**
         * 币别名称
         */
        private String currencyName;


        /**
         * 币别符号
         */
        private String currencySymbol;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 费用合计
        */
        private BigDecimal totalCost;

        /**
        * 审核不通过原因【可排序】
        */
        private String reason;

        /**
         * 实际物流费用【可排序】
         */
        private BigDecimal actualShippingCost;

        /**
         * 实际报关费【可排序】
         */
        private BigDecimal actualDeclareCost;

        /**
         * 实际其他费【可排序】
         */
        private BigDecimal actualOtherCost;

        /**
         * 实际计费重【可排序】
         */
        private BigDecimal actualBillingWeight;

        /**
         * 实际重量单位【可排序】
         */
        private String actualWeightUnit;
        
        /**
         * 东莞仓费用
         */
        private BigDecimal dgWarseHouseFee;
        
        /**
         * 香港仓费用
         */
        private BigDecimal xgWarseHouseFee;
        
        /**
         * 支付状态
         */
        private String payStatus;
        private String payStatusName;
        
        /**
         * 付款时间
         */
        private LocalDateTime payTime;

    }

    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
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
        * 对账单号
        */
        private String code;

        /**
        * 审核状态
        */
        private ApproveStatusEnum approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
        * 审核人id
        */
        private String approveUserId;

        /**
        * 审核人名称
        */
        private String approveUserName;

        /**
        * 生成对账日期
        */
        private LocalDate reconciliationDate;

        /**
        * 提交日期
        */
        private LocalDate submitDate;

        /**
        * 审核日期
        */
        private LocalDate approveDate;

        /**
        * 对账开始日期
        */
        private LocalDate startDate;

        /**
        * 对账结束日期
        */
        private LocalDate endDate;

        /**
         * 对账周期
         */
        private String cycle;


        /**
        * 物流商Id
        */
        private String logisticsSupplierId;

        /**
        * 物流商名称
        */
        private String logisticsSupplierName;

        /**
        * 币别
        */
        private String currency;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 费用合计
        */
        private BigDecimal totalCost;

        /**
        * 审核不通过原因
        */
        private String reason;
        
        /**
         * 东莞仓费用
         */
        private BigDecimal dgWarseHouseFee;
        
        /**
         * 香港仓费用
         */
        private BigDecimal xgWarseHouseFee;

        /**
         * 报关对账单明细
         */
        private List<TmsB2cDeclareReconciliationDetailDTO.ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


        /**
         * 对账开始日期
         */
        private LocalDate startDate;

        /**
         * 对账结束日期
         */
        private LocalDate endDate;

        /**
         * 供应商id
         */
        private String logisticsSupplierId;

        /**
         * 供应商名称
         */
        private String logisticsSupplierName;
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

        /**
         * 结算币别
         */
        @NotBlank(message = "结算币别不能为空")
        private String currency;
        
        /**
         * 东莞仓费用
         */
        private BigDecimal dgWarseHouseFee;
        
        /**
         * 香港仓费用
         */
        private BigDecimal xgWarseHouseFee;
    }
    
    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class PayStatusUpdateDTO extends CommonDTO {
    	
    	/**
    	 * 主键id列表
    	 */
    	private List<String> ids;
    	
    	/**
    	 * 支付状态	http://172.16.100.11:3002/project/128/interface/api/25522 key=b2cDeclarePayStatus
    	 */
    	@NotEmpty(message = "支付状态不能为空")
    	private String payStatus;
    	
    	/**
    	 * 支付时间
    	 */
    	private LocalDateTime payTime;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 明细id集合
         */
        @NotEmpty(message = "明细不能为空")
        private List<TmsB2cDeclareReconciliationDetailDTO.UpdateDTO> detailList;

    }


}