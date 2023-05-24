package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SoReturnInstockDTO {
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
         * 退货类型
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
         * 销售员id
         */
        private List<String> sellerIdList;
        /**
         * 入库日期
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
         * 来源id
         */
        private String sourceId;
        /**
         * 来源明细id
         */
        private String sourceDetailId;
        /**
         * 退货入库单号
         */
        private String code;
        /**
         * 退货单号
         */
        private String sourceCode;
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
         * 销售员Id
         */
        private String sellerId;
        /**
         * 销售员
         */
        private String sellerName;
        /**
         * SkuId
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
         * 退货类型
         */
        private String returnTypeDict;
        /**
         * 销售数量
         */
        private Integer salesQty;
        /**
         * 已出库数量
         */
        private Integer deliveryQty;
        /**
         * 应退数量
         */
        private Integer mustQty;
        /**
         * 签收数量
         */
        private Integer receiveQty;
        /**
         * 实退数量
         */
        private Integer realQty;
        /**
         * 入库日期
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
         * 入库日期
         */
        private LocalDate billDate;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓管员
         */
        private String warehouseKeeperId;
        /**
         * 明细信息
         */
        private List<SoReturnInstockDetailDTO.Add> detailList;
    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class Update {
        /**
         * 主键id
         */
        private String id;
        /**
         * 退货单id
         */
        private String sourceId;
        /**
         * 入库日期
         */
        private LocalDate billDate;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓管员
         */
        private String warehouseKeeperId;
        /**
         * 明细信息
         */
        private List<SoReturnInstockDetailDTO.Update> detailList;
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
         * 审核状态
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
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 作废状态
         */
        private Boolean invalidStatus;
        /**
         * 作废状态
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
         * 明细信息
         */
        private List<SoReturnInstockDetailDTO.View> detailList;
    }

    /**
     * 下推退货入库单-列表查询
     */
    @Data
    @NoArgsConstructor
    public static class GenerateSoReturnInstockView {
        /**
         * id
         */
        private String id;
        /**
         * 主表id
         */
        private String mainId;
        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 来源明细id
         */
        private String sourceDetailId;
        /**
         * 来源单号
         */
        private String sourceCode;
        /**
         * 单据编号
         */
        private String code;
        /**
         * 退货客户id
         */
        private String customerId;
        /**
         * 退货客户
         */
        private String customerName;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku
         */
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
        /**
         * 销售数量
         */
        private Integer salesQty;
        /**
         * 已出库数量
         */
        private Integer deliveryQty;
        /**
         * 应退数量
         */
        private Integer mustQty;
        /**
         * 签收数量
         */
        private Integer receiveQty;
        /**
         * 实退数量
         */
        private Integer realQty;
        /**
         * 退货类型
         */
        private String returnTypeDict;
        /**
         * 退货原因
         */
        private String  returnReasonDict;
        /**
         * 仓库
         */
        private String warehouseLocation;
        /**
         * 仓位
         */
        private String warehouseId;
        /**
         * 备注
         */
        private String remark;
        /**
         * 质检状态
         */
        private String qcStatus;
    }
}
