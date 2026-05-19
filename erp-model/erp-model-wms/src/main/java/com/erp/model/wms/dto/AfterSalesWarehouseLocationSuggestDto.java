package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
         * 产品名称（模糊，对应 product_detail.name）
         */
        private String productName;

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * 勾选导出：主键列表（与列表接口一致，放在 params 内）
         */
        private List<String> ids;

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
        private Integer sort;
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
        private String warehouseLocationCode;
        /**
         * 推荐仓位id
         */
        private String warehouseLocationId;
        /**
         * 优先级
         */
        private Integer sort;
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
        private String warehouseLocationId;

        /**
         * 推荐仓位编码
         */
        private String warehouseLocationCode;

        /**
         * 推荐仓位名称
         */
        private String warehouseLocationName;

        /**
         * 优先级
         */
        private Integer sort;

        /**
         * 是否禁用
         */
        private Boolean disabled;
        /**
         * 状态名称，（启用/禁用）
         */
        private String statusName;

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
     * 导出查询参数（与列表 {@link SearchParamDTO} 一致，可配合 {@link com.common.business.dto.base.PagingDTO} 的 params 使用）
     */
    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ExportParamDTO extends AfterSalesWarehouseLocationSuggestDto.SearchParamDTO {
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

    /**
     * PDA 售后：货品上架提交（单 SKU，源仓位可为空仓位）
     */
    @Data
    @NoArgsConstructor
    public static class PdaGoodsShelvingSubmitDto {

//        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19, message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        @Size(max = 50, message = "源仓位编码最大长度不能超过50位")
        private String sourceWarehouseLocationCode;

        @NotBlank(message = "目标仓位不能为空")
        @Size(max = 50, message = "目标仓位编码最大长度不能超过50位")
        private String targetWarehouseLocationCode;

        @NotBlank(message = "sku编码不能为空")
        @Size(max = 255, message = "sku编码最大长度不能超过255位")
        private String skuNo;

        @Size(max = 19, message = "skuId最大长度不能超过19位")
        private String skuId;

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量必须大于0")
        private Integer qty;

        // /**
        //  * 备注（暂不使用，后续需要再放开）
        //  */
        // @Size(max = 500, message = "备注最大长度不能超过500位")
        // private String remark;
    }

    /**
     * 箱唛查询入参（箱唛号对应 {@link com.erp.model.wms.entity.AfterSalePackEntity#getCode()}）
     */
    @Data
    @NoArgsConstructor
    public static class BoxLabelQueryRequestDto {

        /**
         * 仓库 id（可选；查询装箱详情仅按箱唛号，本字段可保留兼容旧请求体）
         */
        @Size(max = 19, message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        /**
         * 箱唛号（售后装箱单 code）
         */
        @NotBlank(message = "箱唛号不能为空")
        @Size(max = 64, message = "箱唛号最大长度不能超过64位")
        private String boxLabelCode;
    }

    /**
     * PDA 售后：整箱移仓 — 提交。
     * <p>{@link #lines} 与 {@code /queryBoxByLabel} 返回结构对齐：对 {@link com.erp.model.wms.dto.AfterSalePackDTO.ViewDTO#getDetailViewDTOList()}
     * 每一项合并主单 {@link com.erp.model.wms.dto.AfterSalePackDTO.ViewDTO#getCode()}（箱唛）即可，行内字段与
     * {@link com.erp.model.wms.dto.AfterSalePackDetailDTO.ViewDTO} 同名（{@code mainId} 与主单 {@code id} 一致）。</p>
     */
    @Data
    @NoArgsConstructor
    public static class PdaFullBoxTransferSubmitDto {

//        @NotBlank(message = "仓库id不能为空")
        @Size(max = 19, message = "仓库id最大长度不能超过19位")
        private String warehouseId;

        @NotBlank(message = "目标仓位不能为空")
        @Size(max = 50, message = "目标仓位编码最大长度不能超过50位")
        private String targetWarehouseLocationCode;

        /**
         * 展平明细：每行在 {@link com.erp.model.wms.dto.AfterSalePackDetailDTO.ViewDTO} 基础上增加主单 {@code code}（箱唛）。
         */
        @NotEmpty(message = "移仓明细不能为空")
        @Valid
        private List<PdaFullBoxTransferSubmitLineDto> lines;

        /**
         * 附加备注（写入每条明细备注前缀「整箱移仓」之后）
         */
        @Size(max = 500, message = "备注最大长度不能超过500位")
        private String remark;
    }

    /**
     * 整箱移仓 — 单行提交参数，字段命名与 {@link com.erp.model.wms.dto.AfterSalePackDTO.ViewDTO}、
     * {@link com.erp.model.wms.dto.AfterSalePackDetailDTO.ViewDTO} 对齐，便于前端将 {@code /queryBoxByLabel} 结果
     * （主单 + {@code detailViewDTOList} 每一项）直接拼成 {@code lines}。
     */
    @Data
    @NoArgsConstructor
    public static class PdaFullBoxTransferSubmitLineDto {

        /**
         * 售后装箱单主键，与主单 {@link com.erp.model.wms.dto.AfterSalePackDTO.ViewDTO#getId()}、
         * 明细 {@link com.erp.model.wms.dto.AfterSalePackDetailDTO.ViewDTO#getMainId()} 相同。
         */
        @NotBlank(message = "装箱主键mainId不能为空")
        @Size(max = 19, message = "装箱主键最大长度不能超过19位")
        private String mainId;

        /**
         * 箱唛号，与 {@link com.erp.model.wms.dto.AfterSalePackDTO.ViewDTO#getCode()} 相同；提交时用于再次校验 usageStatus；不传则仅按 mainId 查主单。
         */
        @Size(max = 64, message = "箱唛号最大长度不能超过64位")
        private String code;

        /**
         * SKU 编码，与装箱明细 {@link com.erp.model.wms.dto.AfterSalePackDetailDTO.ViewDTO#getSkuNo()} 相同。
         */
        @NotBlank(message = "sku编码不能为空")
        @Size(max = 255, message = "sku编码最大长度不能超过255位")
        private String skuNo;

        /**
         * SKU 主键，与装箱明细 {@link com.erp.model.wms.dto.AfterSalePackDetailDTO.ViewDTO#getSkuId()} 相同。
         */
        @Size(max = 19, message = "skuId最大长度不能超过19位")
        private String skuId;

        /**
         * 源仓位编码，与装箱明细 {@link com.erp.model.wms.dto.AfterSalePackDetailDTO.ViewDTO#getOutWarehouseLocationCode()} 相同。
         */
        @NotBlank(message = "源仓位不能为空")
        @Size(max = 50, message = "源仓位编码最大长度不能超过50位")
        private String outWarehouseLocationCode;

        /**
         * 本行移动数量，与装箱明细 {@link com.erp.model.wms.dto.AfterSalePackDetailDTO.ViewDTO#getPackQty()} 相同。
         */
        @NotNull(message = "装箱数量不能为空")
        @Min(value = 1, message = "装箱数量必须大于0")
        private Integer packQty;
    }

    /**
     * 移仓箱唛明细列表 VO（对应 wms_move_carton_detail 表，用于移箱明细弹窗展示）
     */
    @Data
    @NoArgsConstructor
    public static class BoxMoveDetailListDto {

        /** 仓位移动主单 ID（main_id） */
        private String mainId;

        /** 汇总明细 ID（detail_id） */
        private String detailId;

        /** 箱唛号 */
        private String cartonCode;

        /** 装箱单主键（after_sale_pack.id） */
        private String cartonId;

        /** 装箱明细主键（after_sale_pack_detail.id） */
        private String cartonDetailId;

        /** SKU 主键 */
        private String skuId;

        /** SKU 编码 */
        private String skuNo;

        /** 移出仓位编码（取货仓位） */
        private String outWarehouseLocation;

        /** 移入仓位编码（上架仓位） */
        private String inWarehouseLocation;

        /** 移动数量 */
        private Integer qty;
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
        private String warehouseLocationCode;
    }
}
