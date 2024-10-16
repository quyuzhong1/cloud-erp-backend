package com.erp.model.mrp.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 建议采购请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-29
*/
@Data
@NoArgsConstructor
public class PurchaseSuggestDTO implements Serializable {


    /**
     * 高级查询参数
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
     * 列表参数
     */
    @Data
    @NoArgsConstructor
    public static class ListParamDTO {

        /**
         * 来源id
         */
        private String sourceId;
    }

    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {


        /**
         * 主键id
         */
        private String  id;

        /**
         * 编码
         */
        private String code;

        /**
         * 创建类型（auto系统，manual人工）
         */
        private String createType;
        /**
         * 创建类型名称
         */
        private String createTypeName;

        /**
         * 建议采购量
         */
        private Integer suggestPurchaseQty;

        /**
         * 建议采购日期
         */
        private LocalDate suggestPurchaseDate;

        /**
         * 物流方式
         */
        private String logisticsMethod;

        /**
         * 物流方式名称
         */
        private String logisticsMethodName;

        /**
         * 物流时效（天）
         */
        private Integer logisticsDays;

        /**
         * 预计入库日期
         */
        private LocalDate estimateInstockDate;

        /**
         * 预计可售日期
         */
        private LocalDate estimateSalesDate;

        /**
         * 采购成本
         */
        private BigDecimal purchaseCost;

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
        * 编码
        */
        private String code;

        /**
        * 创建类型（auto系统，manual人工）
        */
        private String createType;

        /**
        * 建议采购量
        */
        private Integer suggestPurchaseQty;

        /**
        * 建议采购日期
        */
        private LocalDate suggestPurchaseDate;

        /**
        * 物流方式
        */
        private String logisticsMethod;

        /**
        * 物流时效（天）
        */
        private Integer logisticsDays;

        /**
        * 预计入库日期
        */
        private LocalDate estimateInstockDate;

        /**
        * 预计可售日期
        */
        private LocalDate estimateSalesDate;

        /**
        * 采购成本
        */
        private BigDecimal purchaseCost;

        /**
        * 作废状态
        */
        private Boolean invalidStatus;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源类型
        */
        private String sourceType;


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
        * 创建类型（auto系统，manual人工）
        */
        private String createType;

        /**
        * 建议采购量
        */
        @NotNull(message = "建议采购量不能为空")
        private Integer suggestPurchaseQty;

        /**
        * 建议采购日期
        */
        private LocalDate suggestPurchaseDate;

        /**
        * 物流方式
        */
        @NotBlank(message = "物流方式不能为空")
        @Size(max = 64,message = "物流方式最大长度不能超过64位")
        private String logisticsMethod;

        /**
        * 物流时效（天）
        */
        @NotNull(message = "物流时效（天）不能为空")
        private Integer logisticsDays;

        /**
        * 预计入库日期
        */
        private LocalDate estimateInstockDate;

        /**
        * 预计可售日期
        */
        private LocalDate estimateSalesDate;

        /**
        * 采购成本
        */
        @NotNull(message = "采购成本不能为空")
        @Digits(integer = 12, fraction = 4, message = "采购成本整数位不能超过12位，小数位不能超过4位")
        private BigDecimal purchaseCost;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;


    }


}