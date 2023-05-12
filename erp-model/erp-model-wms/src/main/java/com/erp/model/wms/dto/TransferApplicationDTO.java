package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/5/10 14:11
 */
@Data
@NoArgsConstructor
public class TransferApplicationDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String  id;
        /**
         * 调拨单号
         */
        private String  code;
        /**
         * 调拨方向
         */
        private String   transferDirection;
        /**
         * 调拨方向名称
         */
        private String   transferDirectionName;
        /**
         * 状态
         */
        private String   approveStatus;
        /**
         * 状态名称
         */
        private String   approveStatusName;
        /**
         * 作废状态
         */
        private Boolean  invalidStatus;
        /**
         * 作废状态名称
         */
        private String invalidStatusName;
        /**
         * skuId
         */
        private String  skuId;
        /**
         * sku编码
         */
        private String  skuNo;
        /**
         * 产品名称
         */
        private String  productName;
        /**
         * 调拨日期
         */
        private LocalDate billDate;
        /**
         * 数量
         */
        private Integer qty;
        /**
         * 单位
         */
        private String  unit;
        /**
         * 调入仓库名称
         */
        private String   inWarehouseName;
        /**
         * 调出仓库名称
         */
        private String   outWarehouseName;
        /**
         * 备注
         */
        private String  remark;
        /**
         * 申请人
         */
        private String  applyUserName;
        /**
         * 审核人名称
         */
        private String   approveUserName;
        /**
         * 创建人名称
         */
        private String  createUserName;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;

    }

    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {

        /**
         * 主键ids
         */
        private List<String> ids;
        /**
         * 搜索类型
         */
        private String  searchType;
        /**
         * 其他出库编号
         */
        private String  code;
        /**
         * sku编码集合
         */
        private List<String>  skuNoList;
        /**
         * 审核状态集合
         */
        private List<String>  approveStatusList;
        /**
         * 作废状态
         */
        private Boolean  invalidStatus;
        /**
         * 调拨方向
         */
        private String  transferDirection;
        /**
         * 调拨日期集合
         */
        private List<LocalDate>  billDateList;
        /**
         * 调入仓库id集合
         */
        private List<String>  inWarehouseIdList;
        /**
         * 调出仓库id集合
         */
        private List<String>  outWarehouseIdList;
        /**
         * 创建人id集合
         */
        private List<String>  createUserIdList;
        /**
         * 创建时间集合
         */
        private List<LocalDate>   createTimeList;

    }

    @Data
    @NoArgsConstructor
    public static class ListStatusCountDTO {

        /**
         * 类型(toBeApprove待审批，approve审核通过，reject不通过)
         */
        private String searchType;

        /**
         * 数量
         */
        private Integer count;
    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 调拨类型
         */
        private String  type;
        /**
         * 调拨日期
         */
        private LocalDate  billDate;
        /**
         * 调出仓库id
         */
        private String   outWarehouseId;
        /**
         * 调入仓库id
         */
        private String  inWarehouseId;
        /**
         * 申请人id
         */
        private String  applyUserId;
        /**
         * 申请日期
         */
        private LocalDate   applyDate;
        /**
         * 调拨方向
         */
        private String  transferDirection;
        /**
         * 备注
         */
        private String   remark;

    }

    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空")
        @Valid
        private List<TransferApplicationDetailDTO.AddDTO> detailList;
    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空")
        @Valid
        private List<TransferApplicationDetailDTO.UpdateDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 入库单号
         */
        private String  code;

        /**
         * 审核状态
         */
        private String  approveStatus;

        /**
         * 审核状态名称
         */
        private String  approveStatusName;

        /**
         * 调拨方向名称
         */
        private String  transferDirectionName;

        /**
         * 仓管员名称
         */
        private String   warehouseKeeperName;

        /**
         * 调出仓库名称
         */
        private String  outWarehouseName;

        /**
         * 调入仓库名称
         */
        private String  inWarehouseName;

        /**
         * 调出组织id
         */
        private String  outOrgId;

        /**
         * 调出组织名称
         */
        private String  outOrgName;

        /**
         * 调入组织id
         */
        private String  inOrgId;

        /**
         * 调入组织名称
         */
        private String  inOrgName;

        /**
         * 明细
         */
        private List<TransferApplicationDetailDTO.ViewDTO> detailList;
    }


    @Data
    @NoArgsConstructor
    public static class ViewGenerateTransferInfoDTO {

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 调拨申请单号
         */
        private String code;

        /**
         * 调拨方向
         */
        private String transferDirection;

        /**
         * 调拨方向名称
         */
        private String transferDirectionName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * SKU
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 调拨数量
         */
        private Integer qty;

        /**
         * 调拨日期
         */
        private LocalDate billDate;

        /**
         * 调出仓库id
         */
        private String   outWarehouseId;

        /**
         * 调出仓库名称
         */
        private String   outWarehouseName;

        /**
         * 调入仓库id
         */
        private String  inWarehouseId;

        /**
         * 调入仓库名称
         */
        private String   inWarehouseName;

        /**
         * 备注
         */
        private String remark;
    }

    @Data
    @NoArgsConstructor
    public static class generateTransferInfoDTO {

        /**
         * 来源明细id
         */
        @NotBlank(message = "来源明细id不能为空")
        private String sourceDetailId;

        /**
         * 来源类型
         */
        @NotBlank(message = "来源类型不能为空")
        private String sourceType;

        /**
         * 来源id
         */
        @NotBlank(message = "来源id不能为空")
        private String sourceId;

        /**
         * skuId
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;

        /**
         * SKU
         */
        private String skuNo;

        /**
         * 调拨数量
         */
        @NotNull(message = "调拨数量不能为空")
        @Min(value = 1,message = "调拨数量最小值为1")
        @Max(value = 99999999,message = "调拨数量最大值为99999999")
        private Integer qty;

        /**
         * 调拨日期
         */
        private LocalDate billDate;

        /**
         * 调出仓库id
         */
        @NotNull(message = "调出仓库不能为空")
        private String   outWarehouseId;

        /**
         * 调入仓库id
         */
        @NotNull(message = "调入仓库不能为空")
        private String  inWarehouseId;

        /**
         * 备注
         */
        private String remark;
    }
}
