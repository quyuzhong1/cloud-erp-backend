package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 预入库单 DTO
 *
 * @author auto
 * @since 2026-06-30
 */
public class SoReturnPrestockDTO {

    private SoReturnPrestockDTO() {
        throw new IllegalStateException("Utility SoReturnPrestockDTO class");
    }

    // ===================== 分页查询 =====================

    /**
     * 分页查询入参
     */
    @Data
    @NoArgsConstructor
    public static class PagingParam extends SortDTO {

        /** 页面高级查询条件 */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /** sqlMap 默认 key：default */
        private Map<String, String> sqlMap;

        /** 预入库单编号 */
        private String code;

        /** 物流单号 */
        private String returnLogisticCode;

        /** 关联状态（UNLINKED / PARTIAL / LINKED） */
        private List<String> claimStatusList;

        /** 库存组织 ID 列表 */
        private List<String> inventoryOrgIdList;

        /** 签收仓库 ID 列表 */
        private List<String> warehouseIdList;

        /** 实际收货时间/签收时间范围 */
        private List<LocalDateTime> receivedTimeList;

        /** 操作时间范围 */
        private List<LocalDateTime> operateTimeList;

        /** 售后单据类型（B2B / B2C） */
        private String type;

        /** 来源类型（MANUAL / OVERSEAS_WH） */
        private String sourceType;
    }

    /**
     * 分页列表展示字段
     */
    @Data
    @NoArgsConstructor
    public static class PagingView {

        private String id;

        /** 预入库单编号 */
        private String code;

        /** 售后单据类型 */
        private String type;

        /** 售后单据类型名称 */
        private String typeName;

        /** 物流单号 */
        private String returnLogisticCode;

        /** 关联状态 */
        private String claimStatus;

        /** 关联状态名称 */
        private String claimStatusName;

        /** 来源类型 */
        private String sourceType;

        /** 来源类型名称 */
        private String sourceTypeName;

        /** 第三方单据编号 */
        private String thirdCode;

        /** 库存组织 ID */
        private String inventoryOrgId;

        /** 库存组织名称 */
        private String inventoryOrgName;

        /** 签收仓库 ID */
        private String warehouseId;

        /** 签收仓库名称 */
        private String warehouseName;

        /** 退货类型字典值 */
        private String dictReturnType;

        /** 实际收货时间/签收时间 */
        private LocalDateTime receivedTime;

        /** 操作时间 */
        private LocalDateTime operateTime;

        /** 备注 */
        private String remark;

        /** 版本号（乐观锁） */
        private Integer version;

        /** 创建人 ID */
        private String createUserId;

        /** 创建人 */
        private String createUserName;

        /** 创建时间 */
        private LocalDateTime createTime;

        /** 修改人 ID */
        private String updateUserId;

        /** 修改人 */
        private String updateUserName;

        /** 更新时间 */
        private LocalDateTime updateTime;

        /** 详情行列表 */
        private List<SoReturnPrestockDetailDTO.View> detailList;
    }

    // ===================== 新增 =====================

    /**
     * 手动新增预入库单入参
     */
    @Data
    @NoArgsConstructor
    public static class Add {

        /** 物流单号 */
        @NotBlank(message = "物流单号不能为空")
        private String returnLogisticCode;

        /** 售后单据类型：B2B / B2C */
        @NotBlank(message = "售后单据类型不能为空")
        private String type;

        /** 退货类型字典值 */
        private String dictReturnType;

        /** 库存组织 ID */
        @NotBlank(message = "库存组织不能为空")
        private String inventoryOrgId;

        /** 库存组织名称 */
        private String inventoryOrgName;

        /** 签收仓库 ID */
        @NotBlank(message = "签收仓库不能为空")
        private String warehouseId;

        /** 签收仓库名称 */
        private String warehouseName;

        /** 第三方单据编号 */
        private String thirdCode;

        /** 备注 */
        private String remark;

        /** 详情行列表 */
        @Valid
        @NotEmpty(message = "详情行不能为空")
        private List<SoReturnPrestockDetailDTO.Add> detailList;
    }

    // ===================== 由退货入库单表单创建 =====================

    /**
     * 由【退货入库单】新增/修改表单参数创建预入库单-入参
     * <p>与退货入库单表单 DTO（{@code SoReturnInstockDTO.Add}/{@code Update}）解耦，
     * 仅包含创建预入库单实际用到的字段。</p>
     */
    @Data
    @NoArgsConstructor
    public static class FromInstock {

        /** 退货客户 ID；必须为空才允许创建预入库单，否则应直接保存退货入库单 */
        private String customerId;

        /** 退货物流单号 */
        @NotBlank(message = "退货物流单号不能为空")
        private String returnLogisticCode;

        /** 第三方单据编号 */
        private String thirdCode;

        /**
         * 签收仓库 ID；可不传，未传时服务端会按 {@code detailList} 各行的 warehouseId 推导
         * （退货入库单表单按明细行填写仓库）；最终仍取不到仓库时报错
         */
        private String warehouseId;

        /** 售后单据类型：B2B / B2C */
        private String type;

        /** 退货单编号 */
        private String soReturnCode;

        /** 详情行列表 */
        @Valid
        @NotEmpty(message = "产品明细不能为空")
        private List<SoReturnPrestockDetailDTO.FromInstock> detailList;
    }

    // ===================== 详情 =====================

    /**
     * 详情查询出参
     */
    @Data
    @NoArgsConstructor
    public static class View {

        private String id;

        /** 版本号（乐观锁，修改时需原样带回） */
        private Integer version;

        /** 预入库单编号 */
        private String code;

        /** 售后单据类型 */
        private String type;

        /** 售后单据类型名称 */
        private String typeName;

        /** 物流单号 */
        private String returnLogisticCode;

        /** 关联状态 */
        private String claimStatus;

        /** 关联状态名称 */
        private String claimStatusName;

        /** 来源类型 */
        private String sourceType;

        /** 来源类型名称 */
        private String sourceTypeName;

        /** 第三方单据编号 */
        private String thirdCode;

        /** 库存组织 ID */
        private String inventoryOrgId;

        /** 库存组织名称 */
        private String inventoryOrgName;

        /** 签收仓库 ID */
        private String warehouseId;

        /** 签收仓库名称 */
        private String warehouseName;

        /** 退货类型字典值 */
        private String dictReturnType;

        /** 实际收货时间/签收时间 */
        private LocalDateTime receivedTime;

        /** 操作时间 */
        private LocalDateTime operateTime;

        /** 备注 */
        private String remark;

        /** 创建人 */
        private String createUserName;

        /** 创建时间 */
        private LocalDateTime createTime;

        /** 详情行列表 */
        private List<SoReturnPrestockDetailDTO.View> detailList;
    }
}
