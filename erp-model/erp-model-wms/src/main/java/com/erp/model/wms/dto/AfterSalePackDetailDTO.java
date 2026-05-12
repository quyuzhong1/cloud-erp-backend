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
 * 售后装箱明细表请求响应实体
 * </p>
 *
 * @author lei.nie
 * @since 2026-05-12
 */
@Data
@NoArgsConstructor
public class AfterSalePackDetailDTO implements Serializable {

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
         * 售后装箱id,after_sale_packing.id
         */
        private String mainId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * skuId
         */
        private String warehouseLocationId;

        /**
         * 装箱数量
         */
        private Integer packQty;

        /**
         * 实际数量
         */
        private Integer actualQty;

        /**
         * 差异数量
         */
        private Integer diffQty;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 创建人名称
         */
        private String createUserName;

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
         * 售后装箱id,after_sale_packing.id
         */
        private String mainId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

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

        /**
         * 装箱数量
         */
        private Integer packQty;

        /**
         * 实际数量
         */
        private Integer actualQty;

        /**
         * 差异数量
         */
        private Integer diffQty;

    }

    /**
     * 新增
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

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
        private String id;

    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {

        /**
         * 售后装箱id,after_sale_packing.id
         */
        @Size(max = 50, message = "售后装箱id,after_sale_packing.id最大长度不能超过50位")
        private String mainId;

        /**
         * skuId
         */
        @NotBlank(message = "skuId不能为空")
        @Size(max = 19, message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
         * skuId
         */
        @NotBlank(message = "仓位id,warehouse_location.id")
        private String warehouseLocationId;

        /**
         * 装箱数量
         */
        @NotNull(message = "装箱数量不能为空")
        private Integer packQty;

        /**
         * 实际数量
         */
        private Integer actualQty;

        /**
         * 差异数量
         */
        private Integer diffQty;

    }

}