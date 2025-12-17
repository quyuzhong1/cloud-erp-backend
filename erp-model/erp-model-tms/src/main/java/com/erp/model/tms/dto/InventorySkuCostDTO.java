package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
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
 * SKU成本请求响应实体
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
         * 单据状态名称
         */
        private String statusName;

        /**
        * 分摊月份
        */
        private LocalDate allocatedMonth;
        /**
         * 分摊月份【导出使用】
         */
        private String allocatedMonthStr;

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

        /**
         * 明细记录
         */
        List<InventorySkuCostDetailDTO.ViewDTO> detailList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 明细记录
         */
        @Valid
        List<InventorySkuCostDetailDTO.AddDTO> detailList;
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
         * 明细记录
         */
        List<InventorySkuCostDetailDTO.UpdateDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 备注
        */
//        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 单据状态：waitSubmit=待提交，approveIng=审核中，reject=审核不通过，approve=已审核
        */
//        @NotBlank(message = "单据状态：waitSubmit=待提交，approveIng=审核中，reject=审核不通过，approve=已审核不能为空")
        @Size(max = 30,message = "单据状态：waitSubmit=待提交，approveIng=审核中，reject=审核不通过，approve=已审核最大长度不能超过30位")
        private String status;

        /**
        * 分摊月份
        */
        @NotNull(message = "分摊月份不能为空")
        private LocalDate allocatedMonth;

        /**
        * 币种
        */
//        @NotBlank(message = "币种不能为空")
        @Size(max = 30,message = "币种最大长度不能超过30位")
        private String currency;

        /**
        * 币别符号
        */
//        @NotBlank(message = "币别符号不能为空")
        @Size(max = 20,message = "币别符号最大长度不能超过20位")
        private String currencySymbol;

        /**
        * 汇率（兑换人民币汇率）
        */
//        @NotNull(message = "汇率（兑换人民币汇率）不能为空")
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
//        @NotBlank(message = "核算公司名称不能为空")
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
         * 单据编号 【可排序】
         */
        private String code;
        /**
         * 单据状态【可排序】
         */
        private String status;
        /**
         * 单据状态名称
         */
        private String statusName;
        /**
         * 分摊月份【可排序】
         */
        private LocalDate allocatedMonth;
        /**
         * 分摊月份【导出使用】 yyyy-MM
         */
        private String allocatedMonthStr;
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
         * 核算公司名称【可排序】
         */
        private String companyName;
        /**
         * 汇率（对人民币）
         */
        private BigDecimal exchangeRate;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * skuId
         */
        private String skuId;
        /**
         * SKU【可排序】
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
         * 材料成本【可排序】
         */
        private String productCost;
        /**
         * 材料成本【导出使用】
         */
        private String productCostStr;
        /**
         * 创建人【可排序】
         */
        private String createUserName;
        /**
         * 创建时间【可排序】
         */
        private LocalDateTime createTime;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 头程运费（6位小数）
         */
        private String firstMileShippingCost;
        /**
         * 头程运费 【导出使用】
         */
        private String firstMileShippingCostStr;
        /**
         * 清关税费（6位小数）
         */
        private String clearanceCustomsTax;
        /**
         * 清关税费 【导出使用】
         */
        private String clearanceCustomsTaxStr;
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

    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 成功返回数据
         */
        private List<InventorySkuCostDetailDTO.AddDTO> successList;

        /**
         * 错误url
         */
        private String errorUrl;
    }
    @Data
    @NoArgsConstructor
    public static class ExcelImportDTO {
        /**
         * 导入文件
         */
        @NotNull(message = "导入文件不能为空")
        private MultipartFile excelFile;
        /**
         * 明细
         */
        private List<InventorySkuCostDetailDTO.AddDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class SkuCostDTO {
        //核算月份
        private LocalDate allocatedMonth;
        //币种
        private String currency;
        //skuId
        private String skuId;
        //材料成本
        private BigDecimal productCost;
        //头程
        private BigDecimal firstMileShippingCost;
        //清关税费
        private BigDecimal clearanceCustomsTax;
        //仓库
        private String warehouseId;
        //组织
        private String salesOrgId;
        //汇总所有子件
        private Boolean countAllChild;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QueryB2BDTO {
        /**
         * sku集合
         */
        private List<String> skuIds;
        /**
         * 仓库列表
         */
        private String warehouseId;
        /**
         * 销售组织列表
         */
        private String salesOrgId;
        /**
         * 账单日期
         */
        private LocalDate billDate;
        /**
         * 对账年月份
         */
        private String month;
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QueryB2CDTO {
        /**
         * sku集合
         */
        private List<QueryB2CDetailDTO> detailDTOS;
        /**
         * 销售组织列表
         */
        private String salesOrgId;
        /**
         * 账单日期
         */
        private LocalDate billDate;
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QueryB2CDetailDTO{
        /**
         * 仓库列表
         */
        private String warehouseId;
        /**
         * skuId
         */
        private String skuId;
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QueryDetailDTO{
        private String shopId;
        /**
         * 销售组织列表
         */
        private String salesOrgId;
        /**
         * 仓库列表
         */
        private String warehouseId;
        /**
         * skuId
         */
        private String skuId;
        /**
         * 账单日期
         */
        private LocalDate billDate;
    }

    /**
     * SKU成本人民币查询请求DTO
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SkuCostCNYQueryDTO {
        /**
         * SKU ID列表
         */
        @NotNull(message = "SKU ID列表不能为空")
        private List<String> skuIds;
        
        /**
         * 仓库ID列表
         */
        @NotNull(message = "仓库ID列表不能为空")
        private List<String> warehouseIds;
        
        /**
         * 组织ID
         */
        @NotBlank(message = "组织ID不能为空")
        private String orgId;
    }

    /**
     * SKU成本人民币响应DTO
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SkuCostCNYDTO {
        /**
         * SKU ID
         */
        private String skuId;
        
        /**
         * 仓库ID
         */
        private String warehouseId;
        
        /**
         * 组织ID
         */
        private String orgId;
        
        /**
         * 材料成本（原币种）
         */
        private BigDecimal productCost;
        
        /**
         * 汇率
         */
        private BigDecimal exchangeRate;
        
        /**
         * 材料成本（人民币）
         */
        private BigDecimal productCostCNY;
        
        /**
         * 币种
         */
        private String currency;
        
        /**
         * 单据ID
         */
        private String mainId;
        
        /**
         * 单据编号
         */
        private String code;
        
        /**
         * 分摊月份
         */
        private LocalDate allocatedMonth;
    }
}