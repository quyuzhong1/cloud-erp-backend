package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/4/10 11:11
 */
@Data
@NoArgsConstructor
public class PurchaseStockInDTO implements Serializable {

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
        private String code;

        /**
         * 采购单号
         */
        private String purchaseOrderCode;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 作废状态（false未作废，true已作废)
         */
        private Boolean invalidStatus;

        /**
         * 作废状态名称
         */
        private String invalidStatusName;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 入库日期
         */
        private LocalDate stockInDate;

        /**
         * 采购数量
         */
        private Integer purchaseQty;

        /**
         * 收货数量
         */
        private Integer receiveQty;

        /**
         * 入库数量
         */
        private Integer stockInQty;

        /**
         * 超出数量
         */
        private Integer exceedQty;

        /**
         * 交货仓库名称
         */
        private String deliveryWarehouseName;

        /**
         * 采购员名称
         */
        private String purchaseUserName;

        /**
         * 入库员名称
         */
        private String storageUserName;

        /**
         * 备注
         */
        private String remark;

        /**
         * 审核人名称
         */
        private String approveUserName;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

    }

    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {
        /**
         * sku编码
         */
        private List<String> skuNoList;

        /**
         * 作废状态（false未作废，true已作废）
         */
        private Boolean invalidStatus;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 供应商id集合
         */
        private List<String> supplierIdList;

        /**
         * 采购单号
         */
        private String purchaseOrderCode;

        /**
         * 采购员id集合
         */
        private List<String> purchaseUserIdList;

        /**
         * 审核状态集合
         */
        private List<String> approveStatusList;

        /**
         * 入库日期集合
         */
        private List<LocalDate> stockInDateList;

        /**
         * 交货仓库名称
         */
        private List<String> deliveryWarehouseIdList;

        /**
         * 创建人id集合
         */
        private List<String> createUserIdList;

        /**
         * 创建时间集合
         */
        private List<LocalDate> createTimeList;
    }

    @Data
    @NoArgsConstructor
    public static class ListStatusCountDTO {

        /**
         * 类型(toBeApprove待审批，approve审核通过，reject不通过)
         */
        private String type;

        /**
         * 数量
         */
        private Integer count;
    }

    @Data
    @NoArgsConstructor
    public static class ProductSearchParamDTO {

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * sku编号集合
         */
        private List<String>  skuNoList;

    }

    @Data
    @NoArgsConstructor
    public static class ViewProductDTO {

        /**
         * sku编码
         */
        private String  skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 采购数量
         */
        private Integer purchaseQty;

        /**
         * 收货数量
         */
        private Integer receiveQty;

        /**
         * 本次入库数量
         */
        private Integer stockInQty;

        /**
         * 实退数量
         */
        private Integer realityReturnQty;

        /**
         * 收料组织名称
         */
        private String  receiveOrgName;

        /**
         * 是否赠品
         */
        private Boolean isGift;

        /**
         * 是否加急
         */
        private Boolean isUrgent;

        /**
         * 备注
         */
        private String remark;

        /**
         * 订单明细id
         */
        private String purchaseOrderDetailId;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 入库员id
         */
        private String stockInUserId;

        /**
         * 交货仓库id
         */
        private String deliveryWarehouseId;

        /**
         * 入库部门id
         */
        private String stockInDeptId;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 来源主键id
         */
        private String sourceId;

        /**
         * 来源
         */
        private String sourceType;

        /**
         * 明细
         */
        private List<PurchaseStockInDetailDTO.AddDTO> detail;
    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 明细
         */
        private List<PurchaseStockInDetailDTO.UpdateDTO> detail;
    }

    @Data
    @NoArgsConstructor
    public static class SupplierDTO {
        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商联系人id
         */
        private String supplierContactId;

        /**
         * 供应商地址
         */
        private String supplierAddress;
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
         * 入库日期
         */
        private LocalDate stockInDate;

        /**
         * 采购员id
         */
        private String purchaseUserId;

        /**
         * 收料组织id
         */
        private String receiveOrgId;

        /**
         * 采购部门id
         */
        private String purchaseDeptId;

        /**
         * 新品首批（false否,true是）
         */
        private Boolean isFirstMassProduct;

        /**
         * 供应商信息
         */
        private SupplierDTO supplierDTO;

        /**
         * 明细
         */
        private List<PurchaseStockInDetailDTO.ViewDTO> detail;
    }



    @Data
    @NoArgsConstructor
    public static class ViewGeneratePurchaseReturnOrderDTO {

        /**
         * 采购入库id
         */
        private String purchaseStockInId;

        /**
         * 采购入库明细id
         */
        private String purchaseStockInDetailId;

        /**
         * 采购单号
         */
        private String purchaseOrderCode;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 交货仓库名称
         */
        private String deliveryWarehouseName;

        /**
         * 入库数量
         */
        private Integer stockInQty;

        /**
         * 库位名称
         */
        private String warehouseLocationName;

    }

    @Data
    @NoArgsConstructor
    public static class GeneratePurchaseReturnOrderDTO {

        /**
         * 采购入库id
         */
        private String purchaseStockInId;

        /**
         * 采购入库明细id
         */
        private String purchaseStockInDetailId;

        /**
         * 退货人id
         */
        private String returnUserId;

        /**
         * 退货方式
         */
        private String returnMode;

        /**
         * 实退数量
         */
        private Integer realityReturnQty;

        /**
         * 补货数量
         */
        private Integer replenishQty;

        /**
         * 扣款数量
         */
        private Integer deductAmountQty;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 备注
         */
        private String remark;

        /**
         * 币别
         */
        private String currency;

    }
}
