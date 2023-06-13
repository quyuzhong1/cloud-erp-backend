package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.validator.AddGroup;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SoReturnReceiveDTO {
    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParam extends SortDTO {
        /**
         * 主键id
         */
        private List<String> ids;
        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;
        /**
         * 单据编号
         */
        private String code;
        /**
         * sku编号
         */
        private List<String> skuNoList;
        /**
         * 销售单号
         */
        private String sourceCode;
        /**
         * 单据类型
         */
        private String type;
        /**
         * 退货类型 wms/common/enumDropDown?type=ReturnType
         * 描述：refund 退货扣款 replenishment 退货补货
         */
        private String returnTypeDict;
        /**
         * 审核状态
         */
        private List<String> approveStatusList;
        /**
         * 作废状态
         */
        private Boolean invalidStatus;
        /**
         * 客户id
         */
        private List<String> customerIdList;
        /**
         * 库存组织id
         */
        private List<String> inventoryOrgIdList;
        /**
         * 销售员id
         */
        private List<String> sellerIdList;
        /**
         * 退货日期
         */
        private List<LocalDate> returnDateList;
        /**
         * 签收日期
         */
        private List<LocalDate> billDateList;
        /**
         * 创建人id
         */
        private List<String> createUserIdList;
    }

    /**
     * 分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingView {
        /**
         * id
         */
        private String id;
        /**
         * 明细表id
         */
        private String receiveDetailId;
        /**
         * 退货单id
         */
        private String sourceId;
        /**
         * 退货单明细id
         */
        private String sourceDetailId;
        /**
         * 退货签收单号
         */
        private String code;
        /**
         * 退货单号
         */
        private String sourceCode;
        /**
         * 销售单号
         */
        private String soCode;
        /**
         * 客户id
         */
        private String customerId;
        /**
         * 客户名称
         */
        private String customerName;
        /**
         * 库存组织
         */
        private String inventoryOrgName;
        /**
         * 单据状态编号
         */
        private String approveStatus;
        /**
         * 单据状态名称
         */
        private String approveStatusName;
        /**
         * 作废状态
         */
        private Boolean invalidStatus;
        /**
         * 作废状态名称
         */
        private String invalidStatusName;
        /**
         * sku_id
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
         * 退货类型 wms/common/enumDropDown?type=ReturnType
         * 描述：refund 退货扣款 replenishment 退货补货
         */
        private String returnTypeDict;
        /**
         * 退货类型名称
         */
        private String returnTypeDictName;
        /**
         * 销售数量
         */
        private Integer salesQty;
        /**
         * 退货数量
         */
        private Integer returnQty;
        /**
         * 签收数量
         */
        private Integer receiveQty;
        /**
         * 销售员
         */
        private String sellerName;
        /**
         * 退货日期
         */
        private LocalDate returnDate;
        /**
         * 签收日期
         */
        private LocalDate billDate;
        /**
         * 审核人
         */
        private String approveUserName;
        /**
         * 创建人
         */
        private String createUserName;
        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        /**
         * 仓库Id
         */
        private String warehouseId;
    }

    /**
     * 列表状态数量查询
     */
    @Data
    @NoArgsConstructor
    public static class StatusCountDTO {
        /**
         * 类型(waitSubmit 待提交，approveIng 审核中，reject 审核不通过，approve 已审核)
         */
        private String type;
        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class Add {
        /**
         * 退货单id
         */
        private String sourceId;
        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * 退货日期
         */
        private LocalDate returnDate;
        /**
         * 库存组织id
         */
        @NotBlank(message = "库存组织不能为空")
        private String inventoryOrgId;
        /**
         * 仓管员
         */
        private String warehouseKeeperId;
        /**
         * 签收日期
         */
        private LocalDate billDate;
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;
        /**
         * 明细信息
         */
        private List<SoReturnReceiveDetailDTO.Add> detailList;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class Update {
        /**
         * id
         */
        private String id;
        /**
         * 退货单id
         */
        private String sourceId;
        /**
         * 退货日期
         */
        private LocalDate returnDate;
        /**
         * 库存组织id
         */
        @NotBlank(message = "库存组织不能为空")
        private String inventoryOrgId;
        /**
         * 仓管员
         */
        private String warehouseKeeperId;
        /**
         * 签收日期
         */
        private LocalDate billDate;
        /**
         * 仓库id
         */
        @NotBlank(message = "仓库不能为空")
        private String warehouseId;
        /**
         * 明细信息
         */
        private List<SoReturnReceiveDetailDTO.Update> detailList;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class View {
        /**
         * id
         */
        private String id;
        /**
         * 退货单id
         */
        private String sourceId;
        /**
         * 退货单编号
         */
        private String sourceCode;
        /**
         * 单据编号
         */
        private String code;
        /**
         * 审核状态
         */
        private String approveStatus;
        /**
         * 审核状态名称
         */
        private String approveStatusName;
        /**
         * 单据类型
         */
        private String type;
        /**
         * 客户id
         */
        private String customerId;
        /**
         * 客户名称
         */
        private String customerName;
        /**
         * 销售组织id
         */
        private String salesOrgId;
        /**
         * 销售组织名称
         */
        private String salesOrgName;
        /**
         * 销售部门id
         */
        private String salesDeptId;
        /**
         * 销售部门名称
         */
        private String salesDeptName;
        /**
         * 销售员id
         */
        private String sellerId;
        /**
         * 销售员名称
         */
        private String sellerName;
        /**
         * 退货日期
         */
        private LocalDate returnDate;
        /**
         * 签收日期
         */
        private LocalDate billDate;
        /**
         * 库存组织
         */
        private String inventoryOrgId;
        /**
         * 库存组织名称
         */
        private String inventoryOrgName;
        /**
         * 仓管员id
         */
        private String warehouseKeeperId;
        /**
         * 仓管员名称
         */
        private String warehouseKeeperName;
        /**
         * 作废状态
         */
        private Boolean invalidStatus;
        /**
         * 作废状态名称
         */
        private String invalidStatusName;
        /**
         * 作废描述
         */
        private String invalidRemark;
        /**
         * 审核人id
         */
        private String approveUserId;
        /**
         * 审核人名称
         */
        private String approveUserName;
        /**
         * 审核时间
         */
        private LocalDateTime approveTime;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 明细信息
         */
        private List<SoReturnReceiveDetailDTO.View> detailList;
    }
}
