package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
        private List<String> linkStatusList;

        /** 库存组织 ID 列表 */
        private List<String> inventoryOrgIdList;

        /** 签收仓库 ID 列表 */
        private List<String> warehouseIdList;

        /** 入库时间范围 */
        private List<LocalDateTime> returnInstockTimeList;

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
        private String linkStatus;

        /** 关联状态名称 */
        private String linkStatusName;

        /** 库存组织名称 */
        private String inventoryOrgName;

        /** 签收仓库名称 */
        private String warehouseName;

        /** 入库时间 */
        private LocalDateTime returnInstockTime;

        /** 操作时间 */
        private LocalDateTime operateTime;

        /** 来源类型 */
        private String sourceType;

        /** 来源类型名称 */
        private String sourceTypeName;

        /** 创建人 */
        private String createUserName;

        /** 创建时间 */
        private LocalDateTime createTime;
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
        private String returnTypeDict;

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

        /** 来源单 ID（海外仓场景写入，手动创建时可不填） */
        private String sourceId;

        /** 来源单号 */
        private String sourceCode;

        /** 第三方单据编号 */
        private String thirdCode;

        /** 备注 */
        private String remark;

        /** 详情行列表 */
        @Valid
        @NotEmpty(message = "详情行不能为空")
        private List<SoReturnPrestockDetailDTO.Add> detailList;
    }

    // ===================== 修改 =====================

    /**
     * 修改预入库单入参
     */
    @Data
    @NoArgsConstructor
    public static class Update {

        /** 主键 ID */
        @NotBlank(message = "ID 不能为空")
        private String id;

        /** 版本号（乐观锁，必传，用于校验数据是否已被他人修改） */
        @NotNull(message = "版本号不能为空")
        private Integer version;

        /** 退货类型字典值 */
        private String returnTypeDict;

        /** 库存组织 ID */
        private String inventoryOrgId;

        /** 库存组织名称 */
        private String inventoryOrgName;

        /** 签收仓库 ID */
        private String warehouseId;

        /** 签收仓库名称 */
        private String warehouseName;

        /** 备注 */
        private String remark;

        /** 详情行列表 */
        private List<SoReturnPrestockDetailDTO.Update> detailList;
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
        private String linkStatus;

        /** 关联状态名称 */
        private String linkStatusName;

        /** 来源单 ID */
        private String sourceId;

        /** 来源单号 */
        private String sourceCode;

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
        private String returnTypeDict;

        /** 入库时间 */
        private LocalDateTime returnInstockTime;

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
