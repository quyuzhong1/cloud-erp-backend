package com.erp.model.tms.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * SKU存货成本请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-08-16
*/
@Data
@NoArgsConstructor
public class InventorySkuCostDTO implements Serializable {




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
        * 备注
        */
        private String remark;

        /**
        * 单据编号
        */
        private String code;

        /**
        * 单据状态：waitSubmit=待提交，approveIng=审核中，reject=审核不通过，approve=已审核
        */
        private String status;

        /**
        * 分摊月份
        */
        private LocalDate allocatedMonth;

        /**
        * 币种
        */
        private String currency;

        /**
        * 币别符号
        */
        private String currencySymbol;

        /**
        * 汇率（兑换人民币汇率）
        */
        private BigDecimal exchangeRate;

        /**
        * 核算公司id(sys.sys_accounting_company)
        */
        private String companyId;

        /**
        * 核算公司名称
        */
        private String companyName;


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
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 单据状态：waitSubmit=待提交，approveIng=审核中，reject=审核不通过，approve=已审核
        */
        @NotBlank(message = "单据状态：waitSubmit=待提交，approveIng=审核中，reject=审核不通过，approve=已审核不能为空")
        @Size(max = 30,message = "单据状态：waitSubmit=待提交，approveIng=审核中，reject=审核不通过，approve=已审核最大长度不能超过30位")
        private String status;

        /**
        * 分摊月份
        */
        private LocalDate allocatedMonth;

        /**
        * 币种
        */
        @NotBlank(message = "币种不能为空")
        @Size(max = 30,message = "币种最大长度不能超过30位")
        private String currency;

        /**
        * 币别符号
        */
        @NotBlank(message = "币别符号不能为空")
        @Size(max = 20,message = "币别符号最大长度不能超过20位")
        private String currencySymbol;

        /**
        * 汇率（兑换人民币汇率）
        */
        @NotNull(message = "汇率（兑换人民币汇率）不能为空")
        @Digits(integer = 12, fraction = 4, message = "汇率（兑换人民币汇率）整数位不能超过12位，小数位不能超过4位")
        private BigDecimal exchangeRate;

        /**
        * 核算公司id(sys.sys_accounting_company)
        */
        @NotBlank(message = "核算公司id(sys.sys_accounting_company)不能为空")
        @Size(max = 19,message = "核算公司id(sys.sys_accounting_company)最大长度不能超过19位")
        private String companyId;

        /**
        * 核算公司名称
        */
        @NotBlank(message = "核算公司名称不能为空")
        @Size(max = 100,message = "核算公司名称最大长度不能超过100位")
        private String companyName;


    }



    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        /**
         * 类型
         */
        private String tabFlag;

        /**
         * 类型名
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;
    }
    /**
     * 分页
     */
    @Data
    @NoArgsConstructor
    public static class PagingVO {
        /**
         * 主表id
         */
        private String id;
        /**
         * 单据编号
         */
        private String code;
        /**
         * 单据状态
         */
        private String status;
        /**
         * 单据状态名称
         */
        private String statusName;
        /**
         * 分摊月份
         */
        private LocalDate allocatedMonth;
        /**
         *币种
         */
        private String currency;
        /**
         * 币种单位符号
         */
        private String currencySymbol;
        /**
         * 核算公司id
         */
        private String companyId;
        /**
         * 核算公司名称
         */
        private String companyName;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * skuId
         */
        private String skuId;
        /**
         * SKU
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 单位
         */
        private String unit;
        /**
         * 产品成本
         */
        private BigDecimal productCost;
    }

    /**
     * 分页参数
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
}