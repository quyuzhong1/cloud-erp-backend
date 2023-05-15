package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/5/10 14:19
 */
@Data
@NoArgsConstructor
public class TransferInfoDTO implements Serializable {

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
         * 调拨单编号
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
        private List<String>   transferDirection;
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
        @NotBlank(message = "调拨类型不能为空")
        private String type;

        /**
         * 调拨日期
         */
        @NotNull(message = "调拨日期不能未空")
        private LocalDate billDate;

        /**
         * 调拨方向
         */
        @NotBlank(message = "调拨方向不能为空")
        private String transferDirection;

        /**
         * 仓管员id
         */
        private String warehouseKeeperId;

        /**
         * 调出仓库id
         */
        @NotBlank(message = "调出仓库不能为空")
        private String outWarehouseId;

        /**
         * 调入仓库id
         */
        @NotBlank(message = "调入仓库不能为空")
        private String inWarehouseId;

        /**
         * 来源id
         */
        @NotBlank(message = "来源id不能为空")
        private String sourceId;

        /**
         * 来源编码
         */
        @NotBlank(message = "来源编码不能为空")
        private String sourceCode;

        /**
         * 来源类型
         */
        private String sourceType;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空")
        @Valid
        private List<TransferInfoDetailDTO.AddDTO> detailList;
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
        private List<TransferInfoDetailDTO.UpdateDTO> detailList;
    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 调拨单号
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
        private List<TransferInfoDetailDTO.ViewDTO> detailList;
    }
}
