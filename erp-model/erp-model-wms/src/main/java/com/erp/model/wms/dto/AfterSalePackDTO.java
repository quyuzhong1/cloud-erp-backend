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
         * 是否被单据使用 false 未使用 true 已使用
         */
        private Boolean isUse;

        /**
         * 是否被单据使用 false 未使用 true 已使用
         */
        private String isUseName;

        /**
         * 箱唛状态
         */
        private String packStatus;

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
         * 是否移仓 false 否 true 是
         */
        private Boolean isMoveWarehouse;

        /**
         * 是否移仓 false 否 true 是
         */
        private String isMoveWarehouseName;

        /**
         * sku种类数
         */
        private Integer skuSpeciesQty;

        /**
         * 总数量
         */
        private Integer totalQty;

        /**
         * 驳回原因
         */
        private String rejectDescription;

        /**
         * 备注
         */
        private String remark;

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
         * 是否被单据使用 false 未使用 true 已使用
         */
        private Boolean isUse;

        /**
         * 是否被单据使用 false 未使用 true 已使用
         */
        private String isUseName;

        /**
         * 箱唛状态
         */
        private String packStatus;

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
         * 是否移仓 false 否 true 是
         */
        private Boolean isMoveWarehouse;

        /**
         * 是否移仓 false 否 true 是
         */
        private String isMoveWarehouseName;

        /**
         * sku种类数
         */
        private Integer skuSpeciesQty;

        /**
         * 总数量
         */
        private Integer totalQty;

        /**
         * 备注
         */
        private String remark;

        /**
         * 驳回原因
         */
        private String rejectDescription;

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
         * 箱唛类型
         */
        @Size(max = 32, message = "箱唛类型最大长度不能超过32位")
        private String type;

        /**
         * 箱唛状态
         */
        private String packStatus;

        /**
         * sku种类数
         */
        private Integer skuSpeciesQty;

        /**
         * 总数量
         */
        private Integer totalQty;

        /**
         * 驳回原因
         */
        private String rejectDescription;

        /**
         * 备注
         */
        private String remark;

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
         * 售后装箱明细id。
         * 采购整箱退货新增/修改时必传，用于回写 after_sale_pack_detail。
         */
        private String id;

        /**
         * 售后装箱id。
         * 采购整箱退货新增/修改时必传，用于定位 after_sale_pack 主表。
         */
        private String mainId;

        /**
         * 箱唛
         */
        private String code;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 装箱数量。
         * 来源于售后装箱明细原始数量，后端用 packQty - actualQty 计算 diffQty。
         */
        private Integer packQty;

        /**
         * 实退数量。
         * 采购整箱退货时前端传本次实际退货数量；移除单行/SKU 时传 0。
         */
        private Integer actualQty;

        /**
         * 差异数量。
         * 后端按装箱数量 packQty - 实退数量 actualQty 回写，前端展示用。
         */
        private Integer diffQty;

        /**
         * 拣货仓位id
         */
        private String outWarehouseLocationId;

        /**
         * 拣货仓位code
         */
        private String outWarehouseLocationCode;

        /**
         * 拣货仓位名称
         */
        private String outWarehouseLocationName;

        /**
         * 移入仓位id
         */
        private String inWarehouseLocationId;

        /**
         * 移入仓位code
         */
        private String inWarehouseLocationCode;

        /**
         * 移入仓位名称
         */
        private String inWarehouseLocationName;
    }

}