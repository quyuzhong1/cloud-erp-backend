package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 仓位售后推荐表
 *
 * @author liuchao
 * @date 2026-04-30
 */
@Data
public class AfterSalesWarehouseLocationSuggestDto implements Serializable {

    /**
     * 搜索参数
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class SearchParamDTO extends SortDTO {
        private String skuNo;

        /**
         * 仓位编码
         */
        private String warehouseLocationCode;

        /**
         * 仓库ID
         */
        private String warehouseId;

        /**
         * 库区编码
         */
        private String warehouseAreaCode;

        /**
         * 库区类型
         */
        private String warehouseAreaType;

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
     * 详情
     */
    @Data
    public static class ViewDTO {
        private String id;

        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 仓位编码
         */
        private String warehouseLocation;

        /**
         * 仓库ID
         */
        private String warehouseId;

        /**
         * 所属仓库
         */
        private String warehouseName;

        /**
         * 所属库区编码
         */
        private String warehouseArea;

        /**
         * 所属库区名称
         */
        private String warehouseAreaName;

        /**
         * 库区类型
         */
        private String warehouseAreaType;

        /**
         * 优先级
         */
        private Integer priority;
    }

    /**
     * 新增/编辑
     */
    @Data
    @NoArgsConstructor
    public static class AddOrEditDTO {
        /**
         *  id
         */
        private String id;
        /**
         * SKU编码
         */
        private String skuNo;
        /**
         * SKUID
         */
        private String skuId;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 库区编码
         */
        private String warehouseAreaCode;
        /**
         * 推荐仓位code
         */
        private String suggestWarehouseLocationCode;
        /**
         * 推荐仓位id
         */
        private String suggestWarehouseLocationId;
        /**
         * 优先级
         */
        private Integer priority;
        /**
         * 是否禁用
         */
        private Boolean disabled;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdsDTO extends PermissionsDTO {

        /**
         * id
         */
        @NotEmpty(message = "ids不能为空")
        private List<String> ids;

    }

    @Data
    @NoArgsConstructor
    public static class UpdateStatusDto {
        /**
         * 仓位ID
         */
        private String id;
        /**
         * 仓位ID List
         */
        private List<String> ids;
        /**
         * 状态：false启用，true禁用
         */
        private String disabled;
    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        private String id;

        private String skuNo;

        private String eanNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 仓库ID
         */
        private String warehouseId;

        /**
         * 所属仓库
         */
        private String warehouseName;

        /**
         * 所属库区ID
         */
        private String warehouseAreaId;

        /**
         * 所属库区编码
         */
        private String warehouseAreaCode;

        /**
         * 所属库区名称
         */
        private String warehouseAreaName;

        /**
         * 推荐仓位id
         */
        private String suggestWarehouseLocationId;

        /**
         * 推荐仓位编码
         */
        private String suggestWarehouseLocationCode;

        /**
         * 推荐仓位名称
         */
        private String suggestWarehouseLocationName;

        /**
         * 优先级
         */
        private Integer priority;

        /**
         * 状态名称，（启用/禁用）
         */
        private Boolean disabled;

        /**
         * 更新人
         */
        private String updateUser;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

    }

    /**
     * 导出
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ExportParamDTO extends AfterSalesWarehouseLocationSuggestDto.SearchParamDTO {
        private List<String> ids;
    }

    /**
     * 导出Excel
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends AwdInventoryDTO.PagingParamDTO {
        /**
         * 勾选的id集合
         */
        private List<String> ids;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PdaSearchDto {
        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 仓库id （目前暂时只有 东莞售后仓库）
         */
        private String warehouseId;

        /**
         * 取出仓位code （目前暂时只有 空仓位）
         */
        private String warehouseLocationCode;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PdaListDto {
//        /**
//         * id
//         */
//        private String id;
//        /**
//         * sku编码
//         */
//        private String skuNo;
//
//        /**
//         * 仓库id
//         */
//        private String warehouseId;
//
//        /**
//         * 库区code
//         */
//        private String warehouseAreaCode;
        /**
         * 建议仓位code
         */
        private String suggestWarehouseLocationCode;
    }
}
