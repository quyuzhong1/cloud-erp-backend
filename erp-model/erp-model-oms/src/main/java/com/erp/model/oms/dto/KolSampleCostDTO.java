package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 寄样费用表请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-12-01
*/
@Data
@NoArgsConstructor
public class KolSampleCostDTO implements Serializable {


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
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String  id;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源编码（B2B/B2C-KOL寄样申请单号）
         */
        private String sourceCode;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 寄样类型
         */
        private String type;

        /**
         * 达人表id
         */
        private String partnerId;

        /**
         * 达人昵称
         */
        private String partnerNickname;

        /**
         * 回片链接
         */
        private String feedbackUrl;

        /**
         * 销售单号
         */
        private String soCode;

        /**
         * 销售id
         */
        private String soId;

        /**
         * 销售组织id
         */
        private String soOrgId;

        /**
         * 销售组织名称
         */
        private String soOrgName;

        /**
         * 销售出库单id
         */
        private String soOutstockId;

        /**
         * 销售出库单编码
         */
        private String soOutstockCode;

        /**
         * 物流商id
         */
        private String logisticsSupplierId;

        /**
         * 物流商
         */
        private String logisticsSupplierName;

        /**
         * 物流渠道id
         */
        private String logisticsChannelId;

        /**
         * 物流渠道
         */
        private String logisticsChannelName;

        /**
         * 销售出库日期
         */
        private LocalDate soOutstockDate;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 军区id
         */
        private String partitionId;

        /**
         * 军区名称
         */
        private String partitionName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 实发数量
         */
        private Integer qty;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币别符号
         */
        private String currencySymbol;

        /**
         * 汇率
         */
        private BigDecimal exchangeRate;

        /**
         * 总费用
         */
        private BigDecimal totalCost;

        /**
         * 材料成本
         */
        private BigDecimal productCost;

        /**
         * 头程费用
         */
        private BigDecimal firstMileShippingCost;

        /**
         * 清关税费
         */
        private BigDecimal clearanceCustomsTax;

        /**
         * 运费
         */
        private BigDecimal shippingCost;

        /**
         * 关税
         */
        private BigDecimal customsTax;

        /**
         * 其他费用
         */
        private BigDecimal otherCost;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
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
        * 来源id
        */
        private String sourceId;

        /**
        * 来源编码（B2B/B2C-KOL寄样申请单号）
        */
        private String sourceCode;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 寄样类型
        */
        private String type;

        /**
        * 达人表id
        */
        private String partnerId;

        /**
        * 达人昵称
        */
        private String partnerNickname;

        /**
        * 回片链接
        */
        private String feedbackUrl;

        /**
        * 销售单号
        */
        private String soCode;

        /**
        * 销售id
        */
        private String soId;

        /**
        * 销售组织id
        */
        private String soOrgId;

        /**
        * 销售组织名称
        */
        private String soOrgName;

        /**
        * 销售出库单id
        */
        private String soOutstockId;

        /**
        * 销售出库单编码
        */
        private String soOutstockCode;

        /**
        * 物流商id
        */
        private String logisticsSupplierId;

        /**
        * 物流商
        */
        private String logisticsSupplierName;

        /**
        * 物流渠道id
        */
        private String logisticsChannelId;

        /**
        * 物流渠道
        */
        private String logisticsChannelName;

        /**
        * 销售出库日期
        */
        private LocalDate soOutstockDate;

        /**
        * 仓库id
        */
        private String warehouseId;

        /**
        * 仓库名称
        */
        private String warehouseName;

        /**
        * 军区id
        */
        private String partitionId;

        /**
        * 军区名称
        */
        private String partitionName;

        /**
        * skuId
        */
        private String skuId;

        /**
        * sku编码
        */
        private String skuNo;

        /**
        * 实发数量
        */
        private Integer qty;

        /**
        * 币别
        */
        private String currency;

        /**
        * 币别符号
        */
        private String currencySymbol;

        /**
        * 汇率
        */
        private BigDecimal exchangeRate;

        /**
        * 总费用
        */
        private BigDecimal totalCost;

        /**
        * 材料成本
        */
        private BigDecimal productCost;

        /**
        * 头程费用
        */
        private BigDecimal firstMileShippingCost;

        /**
        * 清关税费
        */
        private BigDecimal clearanceCustomsTax;

        /**
        * 运费
        */
        private BigDecimal shippingCost;

        /**
        * 关税
        */
        private BigDecimal customsTax;

        /**
        * 其他费用
        */
        private BigDecimal otherCost;


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
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源编码（B2B/B2C-KOL寄样申请单号）
        */
        @NotBlank(message = "来源编码（B2B/B2C不能为空")
        @Size(max = 64,message = "来源编码（B2B/B2C最大长度不能超过64位")
        private String sourceCode;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 64,message = "来源类型最大长度不能超过64位")
        private String sourceType;

        /**
        * 寄样类型
        */
        @NotBlank(message = "寄样类型不能为空")
        @Size(max = 32,message = "寄样类型最大长度不能超过32位")
        private String type;

        /**
        * 达人表id
        */
        @NotBlank(message = "达人表id不能为空")
        @Size(max = 19,message = "达人表id最大长度不能超过19位")
        private String partnerId;

        /**
        * 达人昵称
        */
        @NotBlank(message = "达人昵称不能为空")
        @Size(max = 255,message = "达人昵称最大长度不能超过255位")
        private String partnerNickname;

        /**
        * 回片链接
        */
        @NotBlank(message = "回片链接不能为空")
        private String feedbackUrl;

        /**
        * 销售单号
        */
        @NotBlank(message = "销售单号不能为空")
        @Size(max = 32,message = "销售单号最大长度不能超过32位")
        private String soCode;

        /**
        * 销售id
        */
        @NotBlank(message = "销售id不能为空")
        @Size(max = 19,message = "销售id最大长度不能超过19位")
        private String soId;

        /**
        * 销售组织id
        */
        @NotBlank(message = "销售组织id不能为空")
        @Size(max = 19,message = "销售组织id最大长度不能超过19位")
        private String soOrgId;

        /**
        * 销售组织名称
        */
        @NotBlank(message = "销售组织名称不能为空")
        @Size(max = 64,message = "销售组织名称最大长度不能超过64位")
        private String soOrgName;

        /**
        * 销售出库单id
        */
        @NotBlank(message = "销售出库单id不能为空")
        @Size(max = 19,message = "销售出库单id最大长度不能超过19位")
        private String soOutstockId;

        /**
        * 销售出库单编码
        */
        @NotBlank(message = "销售出库单编码不能为空")
        @Size(max = 32,message = "销售出库单编码最大长度不能超过32位")
        private String soOutstockCode;

        /**
        * 物流商id
        */
        @Size(max = 19,message = "物流商id最大长度不能超过19位")
        private String logisticsSupplierId;

        /**
        * 物流商
        */
        @Size(max = 128,message = "物流商最大长度不能超过128位")
        private String logisticsSupplierName;

        /**
        * 物流渠道id
        */
        @Size(max = 19,message = "物流渠道id最大长度不能超过19位")
        private String logisticsChannelId;

        /**
        * 物流渠道
        */
        @Size(max = 128,message = "物流渠道最大长度不能超过128位")
        private String logisticsChannelName;

        /**
        * 销售出库日期
        */
        private LocalDate soOutstockDate;

        /**
        * 仓库id
        */
        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19,message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
        * 仓库名称
        */
        @NotBlank(message = "仓库名称不能为空")
        @Size(max = 64,message = "仓库名称最大长度不能超过64位")
        private String warehouseName;

        /**
        * 军区id
        */
        @NotBlank(message = "军区id不能为空")
        @Size(max = 19,message = "军区id最大长度不能超过19位")
        private String partitionId;

        /**
        * 军区名称
        */
        @NotBlank(message = "军区名称不能为空")
        @Size(max = 64,message = "军区名称最大长度不能超过64位")
        private String partitionName;

        /**
        * skuId
        */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19,message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
        * 实发数量
        */
        @NotNull(message = "实发数量不能为空")
        private Integer qty;

        /**
        * 币别
        */
        @NotBlank(message = "币别不能为空")
        @Size(max = 32,message = "币别最大长度不能超过32位")
        private String currency;

        /**
        * 币别符号
        */
        @NotBlank(message = "币别符号不能为空")
        @Size(max = 32,message = "币别符号最大长度不能超过32位")
        private String currencySymbol;

        /**
        * 汇率
        */
        @NotNull(message = "汇率不能为空")
        @Digits(integer = 12, fraction = 4, message = "汇率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal exchangeRate;

        /**
        * 总费用
        */
        @NotNull(message = "总费用不能为空")
        @Digits(integer = 12, fraction = 4, message = "总费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal totalCost;

        /**
        * 材料成本
        */
        @NotNull(message = "材料成本不能为空")
        @Digits(integer = 12, fraction = 4, message = "材料成本整数位不能超过12位，小数位不能超过4位")
        private BigDecimal productCost;

        /**
        * 头程费用
        */
        @NotNull(message = "头程费用不能为空")
        @Digits(integer = 12, fraction = 4, message = "头程费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal firstMileShippingCost;

        /**
        * 清关税费
        */
        @NotNull(message = "清关税费不能为空")
        @Digits(integer = 12, fraction = 4, message = "清关税费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal clearanceCustomsTax;

        /**
        * 运费
        */
        @NotNull(message = "运费不能为空")
        @Digits(integer = 12, fraction = 4, message = "运费整数位不能超过12位，小数位不能超过4位")
        private BigDecimal shippingCost;

        /**
        * 关税
        */
        @NotNull(message = "关税不能为空")
        @Digits(integer = 12, fraction = 4, message = "关税整数位不能超过12位，小数位不能超过4位")
        private BigDecimal customsTax;

        /**
        * 其他费用
        */
        @NotNull(message = "其他费用不能为空")
        @Digits(integer = 12, fraction = 4, message = "其他费用整数位不能超过12位，小数位不能超过4位")
        private BigDecimal otherCost;


    }

    /**
     * 导出Excel
     */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends KolSampleCostDTO.PagingParamDTO {
        /**
         * 勾选的id集合
         */
        private List<String> ids;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateCostDTO {
        /**
         * 日期
         */
        @NotBlank(message = "日期不能为空")
        @Pattern(regexp = "\\d{4}-\\d{2}", message = "日期格式必须为 YYYY-MM")
        private String date;
    }
}