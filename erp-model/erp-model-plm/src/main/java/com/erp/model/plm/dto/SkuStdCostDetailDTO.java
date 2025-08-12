package com.erp.model.plm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.plm.enums.SkuStdCostImportTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * sku标准成本明细表请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2025-08-08
 */
@Data
@NoArgsConstructor
public class SkuStdCostDetailDTO implements Serializable {

    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型(all=全部, waitSubmit=待审核，approve=已审核, reject=不通过, history=历史价格)
         */
        private String tabFlag;

        /**
         * tab名称
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
        private Map<String, String> sqlMap;

        /**
         * SKU ID
         */
        private String skuId;

    }

    /**
     * 价格变更
     */
    @Data
    @NoArgsConstructor
    public static class ChangeDTO extends ChangeCommonDTO {
        /**
         * 列表id
         */
        @NotNull(message = "列表id不能为空")
        private String id;

    }

    /**
     * 价格变更
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangeCommonDTO {

        /**
         * 标准成本(不含税)
         */
        @NotNull(message = "标准成本(不含税)不能为空")
        @Digits(integer = 12, fraction = 4, message = "标准成本(不含税)整数位不能超过12位，小数位不能超过4位")
        private BigDecimal stdCostPrice;

        /**
         * 币别
         */
        @NotNull(message = "币别不能为空")
        private String currency;

        /**
         * 生效日期
         */
        @NotNull(message = "生效日期不能为空")
        private LocalDate effectiveDate;
    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {


        /**
         * 列表id
         */
        private String id;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * sku名称
         */
        private String skuName;

        /**
         * 是否组合装
         */
        private Boolean isComb;

        /**
         * 销售状态:接口地址：/plm/common/enumDropDown?type=SaleState
         */
        private Integer saleState;

        /**
         * 销售状态名称
         */
        private String saleStateName;

        /**
         * 审核状态:waitSubmit=待提交，approveIng=审核中，approve=已审核，reject=审核不通过
         */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 标准成本(不含税)
         */
        private BigDecimal stdCostPrice;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 生效日期
         */
        private LocalDate effectiveDate;

        /**
         * 失效日期
         */
        private LocalDate expireDate;

        /**
         * 最新出库时间
         */
        private LocalDate lastOutstockDate;

        /**
         * 审核人ID
         */
        private String approveUserId;

        /**
         * 审核人名称
         */
        private String approveUserName;

        /**
         * 审核时间
         */
        private LocalDateTime approveTime;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        /**
         * 更新人名称
         */
        private String updateUserName;

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
    public static class UpdateDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 标准成本(不含税)
         */
        @NotNull(message = "标准成本(不含税)不能为空")
        @Digits(integer = 12, fraction = 4, message = "标准成本(不含税)整数位不能超过12位，小数位不能超过4位")
        private BigDecimal stdCostPrice;

        /**
         * 币别
         */
        @NotNull(message = "币别不能为空")
        private String currency;

        /**
         * 生效日期 (无生效日期的时候可修改)
         */
        private LocalDate effectiveDate;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 主表ID:sku_std_cost
         */
        @NotBlank(message = "主表ID:sku_std_cost不能为空")
        @Size(max = 19, message = "主表ID:sku_std_cost最大长度不能超过19位")
        private String mainId;

        /**
         * 生效日期
         */
        @NotNull(message = "生效日期不能为空")
        private LocalDate effectiveDate;

        /**
         * 失效日期
         */
        private LocalDate expireDate;

        /**
         * 标准成本
         */
        @NotNull(message = "标准成本不能为空")
        @Digits(integer = 12, fraction = 4, message = "标准成本整数位不能超过12位，小数位不能超过4位")
        private BigDecimal stdCostPrice;

        /**
         * 标准零售价(不含税)
         */
        @NotNull(message = "标准零售价(不含税)不能为空")
        @Digits(integer = 12, fraction = 4, message = "标准零售价(不含税)整数位不能超过12位，小数位不能超过4位")
        private BigDecimal stdSalePrice;

        /**
         * bom版本
         */
        @NotBlank(message = "bom版本不能为空")
        @Size(max = 16, message = "bom版本最大长度不能超过16位")
        private String bomVersion;

        /**
         * 备注
         */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255, message = "备注最大长度不能超过255位")
        private String remark;


    }


    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 列表id
         */
        private String id;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编号
         */
        private String skuNo;

        /**
         * sku名称
         */
        private String skuName;

        /**
         * 是否组合装
         */
        private Boolean isComb;

        /**
         * 销售状态:接口地址：/plm/common/enumDropDown?type=SaleState
         */
        private Integer saleState;

        /**
         * 销售状态名称
         */
        private String saleStateName;

        /**
         * 审核状态:waitSubmit=待提交，approveIng=审核中，approve=已审核，reject=审核不通过
         */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 标准成本(不含税)
         */
        private BigDecimal stdCostPrice;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 生效日期
         */
        private LocalDate effectiveDate;

        /**
         * 失效日期
         */
        private LocalDate expireDate;

        /**
         * 最新出库时间
         */
        private LocalDate lastOutstockDate;

        /**
         * 审核人ID
         */
        private String approveUserId;

        /**
         * 审核人名称
         */
        private String approveUserName;

        /**
         * 审核时间
         */
        private LocalDateTime approveTime;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        /**
         * 更新人名称
         */
        private String updateUserName;

        /**
         * 主表id
         */
        private String mainId;
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

    @Data
    @NoArgsConstructor
    public static class ExcelImportDTO {

        /**
         * 导入类型，change导入变更，update导入更新
         */
        @NotNull(message = "导入类型不能为空")
        private String importType;

        /**
         * 文件URL
         */
        @NotNull(message = "【文件URL】不能为空")
        private String fileUrl;

    }

    /**
     * 报价分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class HistoryPagingParamDTO extends SortDTO {

        /**
         * SKU ID
         */
        @NotNull(message = "SKU ID不能为空")
        private String skuId;

    }
}