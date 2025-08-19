package com.erp.model.tms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.oms.enums.SoB2cPayStatusEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
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
 * 头程对账单请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-03-25
 */
@Data
@NoArgsConstructor
public class TmsFirstMileReconciliationDTO implements Serializable {


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
    @EqualsAndHashCode(callSuper = true)
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

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
        private String id;

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
         * 支付状态
         */
        private String payStatus;

        private String PayStatusName;

        /**
         * 支付时间【可排序】
         */
        private LocalDateTime payTime;

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
         * 审核日期【可排序】
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
         * 对账月份
         */
        private LocalDate reconciliationMonth;
        /**
         * 对账月份【导出使用】
         */
        private String reconciliationMonthStr;
        /**
         * 对账次数
         */
        private Integer reconciliationCount;
        /**
         * 对账次数名称【导出使用】
         */
        private String reconciliationCountName;

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
         * 对账类型
         */
        private String supplierType;
        /**
         * 对账类型名称
         */
        private String supplierTypeName;

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
         * 费用合计【可排序】
         */
        private BigDecimal totalCost;
        
        /**
         * 费用合计币种
         */
        private String totalCostCurrency = "CNY";
        
        /**
         * 费用合计币种符号
         */
        private String totalCostCurrencySymbol = "¥";

        /**
         * 审核不通过原因【可排序】
         */
        private String reason;

        /**
         * 实际物流费用【可排序】
         */
        private BigDecimal actualShippingCost;
        
        /**
         * 实际物流费用币种
         */
        private String actualShippingCostCurrency = "CNY";
        
        /**
         * 实际物流费用币种符号
         */
        private String actualShippingCostCurrencySymbol = "¥";

        /**
         * 实际报关费【可排序】
         */
        private BigDecimal actualDeclareCost;
        
        /**
         * 实际报关费币种
         */
        private String actualDeclareCostCurrency = "CNY";
        
        /**
         * 实际报关费币种符号
         */
        private String actualDeclareCostCurrencySymbol = "¥";

        /**
         * 实际其他费【可排序】
         */
        private BigDecimal actualOtherCost;
        
        /**
         * 实际其他费币种
         */
        private String actualOtherCostCurrency = "CNY";
        
        /**
         * 实际其他费币种符号
         */
        private String actualOtherCostCurrencySymbol = "¥";

        /**
         * 实际其他税费【可排序】
         */
        private BigDecimal actualOtherTaxCost;
        
        /**
         * 实际其他税费币种
         */
        private String actualOtherTaxCostCurrency = "CNY";
        
        /**
         * 实际其他税费币种符号
         */
        private String actualOtherTaxCostCurrencySymbol = "¥";

        /**
         * 实际计费重【可排序】
         */
        private BigDecimal actualBillingWeight;

        /**
         * 实际重量单位【可排序】
         */
        private String actualWeightUnit;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
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
        private String id;

        /**
         * 对账单号
         */
        private String code;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核名称
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
         * 对账月份（取值为对账周期末值所在月份）
         */
        private LocalDate reconciliationMonth;

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
         * 审核不通过原因
         */
        private String reason;
        /**
         * 明细数量
         */
        private Integer detailCount;
        /**
         * 供应商类型（logistics 物流对账单，warehouse仓储对账单，custom自定义物流商）
         * SupplierTypeEnum
         */
        private String supplierType;
        /**
         * 供应商名称
         */
        private String supplierTypeName;

        /**
         * 明细列表
         */
        private List<TmsFirstMileReconciliationDetailDTO.ListDTO> detailList;

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
    public static class UpdateDTO{

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;
        /**
         * 对账月份（取值为对账周期末值所在月份）
         */
        @NotNull(message = "对账月份不能为空")
        private LocalDate reconciliationMonth;
        /**
         * 明细id集合
         */
//        @NotEmpty(message = "明细不能为空")
        private List<TmsFirstMileReconciliationDetailDTO.UpdateDTO> detailList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 生成对账日期
         */
        private LocalDate reconciliationDate;

        /**
         * 提交日期
         */
        private LocalDate submitDate;

        /**
         * 提交日期
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
         * 物流商Id
         */
        @Size(max = 19, message = "物流商Id最大长度不能超过19位")
        private String logisticsSupplierId;

        /**
         * 物流商名称
         */
        @Size(max = 100, message = "物流商名称最大长度不能超过100位")
        private String logisticsSupplierName;

        /**
         * 币别
         */
        @Size(max = 32, message = "币别最大长度不能超过32位")
        private String currency;

        /**
         * 汇率
         */
        @Digits(integer = 12, fraction = 4, message = "汇率整数位不能超过12位，小数位不能超过4位")
        private BigDecimal exchangeRate;

        /**
         * 费用合计
         */
        @Digits(integer = 12, fraction = 4, message = "费用合计整数位不能超过12位，小数位不能超过4位")
        private BigDecimal totalCost;

        /**
         * 审核不通过原因
         */
        private String reason;


    }


    /**
     * 更新付款状态入参
     */
    @Data
    @NoArgsConstructor
    public static class UpdatePayStatusDTO {
        /**
         * 勾选的id集合
         */
        private List<String> ids;
        /**
         * 付款状态：
         * 接口：/oms/drop/down/dict/list?type=soB2cPayStatus
         */
        @StateEnumValue(clazz = SoB2cPayStatusEnum.class,message = "付款状态有误")
        private String payStatus;

        /**
         * 付款时间
         */
        private LocalDateTime payTime;
    }
}