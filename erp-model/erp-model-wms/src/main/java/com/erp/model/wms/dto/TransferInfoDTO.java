package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
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
         * 入库单号
         */
        private String  code;
        /**
         * 库存方向
         */
        private String   inventoryDirection;
        /**
         * 库存方向名称
         */
        private String   inventoryDirectionName;
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
         * 应收数量
         */
        private Integer planQty;
        /**
         * 实收数量
         */
        private Integer actualQty;
        /**
         * 单位
         */
        private String  unit;
        /**
         * 收货仓库名称
         */
        private String    warehouseName;
        /**
         * 审核人名称
         */
        private String    approveUserName;
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
         * 其他入库编号
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
         * 库存方向
         */
        private List<String>   inventoryDirection;
        /**
         * 入库日期集合
         */
        private List<LocalDate>  billDateList;
        /**
         * 收货仓库id集合
         */
        private List<String>  warehouseIdList;
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
         * 入库日期
         */
        private LocalDate billDate;

        /**
         * 库存方向
         */
        private String   inventoryDirection;

        /**
         * 仓管员id
         */
        private String   warehouseKeeperId;

        /**
         * 验收员id
         */
        private String   receiverId;

        /**
         * 库存组织id
         */
        private String  orgId;

        /**
         * 入库类型
         */
        private String   type;

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
         * 入库单号
         */
        private String  code;

        /**
         * 审核状态
         */
        private String  approveStatus;


        /**
         * 明细
         */
        private List<PoInstockDetailDTO.ViewDTO> details;
    }
}
