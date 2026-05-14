package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.base.SuperDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 售后装箱表请求响应实体
 * </p>
 *
 * @author lei.nie
 * @since 2026-05-12
 */
@Data
@NoArgsConstructor
public class AfterSalePackDTO implements Serializable {

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
         * 数量
         */
        private Integer count;

    }

    /**
     * 分页列表查询参数
     */
    @EqualsAndHashCode(callSuper = true)
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

    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 箱唛
         */
        private String code;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源单据类型
         */
        private String sourceType;

        /**
         * 箱唛类型
         */
        private String type;

        /**
         * 箱唛类型名称
         */
        private String typeName;

        /**
         * 单据使用状态 false 未使用 true 已使用
         */
        private Boolean usageStatus;

        /**
         * 单据使用状态 false 未使用 true 已使用
         */
        private String usageStatusName;

        /**
         * 装箱状态 false 未装箱 true 已装箱
         */
        private Boolean packStatus;

        /**
         * 装箱状态 false 未装箱 true 已装箱
         */
        private String packStatusName;

        /**
         * 是否存在差异 false 否 true 是
         */
        private Boolean isDifference;

        /**
         * 是否存在差异 false 否 true 是
         */
        private String isDifferenceName;

        /**
         * sku种类数
         */
        private Integer skuSpeciesQty;

        /**
         * 总数量
         */
        private Integer totalQty;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 关联供应商id
         */
        private String supplierId;

        /**
         * 关联供应商
         */
        private String supplierName;
    }

    /**
     * 导出Excel
     */
    @EqualsAndHashCode(callSuper = true)
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
         * 箱唛
         */
        private String code;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源单据类型
         */
        private String sourceType;

        /**
         * 箱唛类型
         */
        private String type;

        /**
         * 箱唛类型名称
         */
        private String typeName;

        /**
         * 单据使用状态 false 未使用 true 已使用
         */
        private Boolean usageStatus;

        /**
         * 装箱状态 false 未装箱 true 已装箱
         */
        private Boolean packStatus;

        /**
         * 是否存在差异 false 否 true 是
         */
        private Boolean isDifference;

        /**
         * sku种类数
         */
        private Integer skuSpeciesQty;

        /**
         * 总数量
         */
        private Integer totalQty;

        /**
         * 关联供应商id
         */
        private String supplierId;

        /**
         * 关联供应商
         */
        private String supplierName;

        /**
         * 装箱明细列表
         */
        private List<AfterSalePackDetailDTO.ViewDTO> detailViewDTOList;
    }

    /**
     * 新增
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 装箱明细列表
         */
        private List<AfterSalePackDetailDTO.UpdateDTO> detailList;

    }

    /**
     * 修改
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 装箱明细列表
         */
        private List<AfterSalePackDetailDTO.UpdateDTO> detailList;

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {

        /**
         * 来源id
         */
        @Size(max = 19, message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
         * 来源单号
         */
        @Size(max = 32, message = "来源单号最大长度不能超过32位")
        private String sourceCode;

        /**
         * 来源单据类型
         */
        @Size(max = 32, message = "来源单据类型最大长度不能超过32位")
        private String sourceType;

        /**
         * 箱唛类型
         */
        @Size(max = 32, message = "箱唛类型最大长度不能超过32位")
        private String type;

        /**
         * 单据使用状态 false 未使用 true 已使用
         */
        private Boolean usageStatus;

        /**
         * 装箱状态 false 未装箱 true 已装箱
         */
        private Boolean packStatus;

        /**
         * 是否存在差异 false 否 true 是
         */
        private Boolean isDifference;

        /**
         * sku种类数
         */
        private Integer skuSpeciesQty;

        /**
         * 总数量
         */
        private Integer totalQty;

    }

    /**
     * 箱码申请
     */
    @Data
    @NoArgsConstructor
    public static class BoxCodeApplicationDTO {

        /**
         * 箱唛类型
         */
        @NotNull(message = "箱唛类型不能为空")
        @Size(max = 32, message = "箱唛类型最大长度不能超过32位")
        private String type;

        /**
         * 申请数量
         */
        @NotNull(message = "申请数量不能为空")
        private Integer applicationQty;
    }

    @Data
    @NoArgsConstructor
    public static class DetailDTO {

        /**
         * 箱唛
         */
        private String code;

        /**
         * 实退数量
         */
        private Integer packQty;

        /**
         * 仓位id,warehouse_location.id
         */
        private String warehouseLocationId;

        /**
         * 仓位code
         */
        private String warehouseLocationCode;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;
    }

}