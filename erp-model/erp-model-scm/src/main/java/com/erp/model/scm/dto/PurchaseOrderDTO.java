package com.erp.model.scm.dto;

import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 11:16
 */
@Data
@NoArgsConstructor
public class PurchaseOrderDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 采购单号
         */
        private String code;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 审核状态
         */
        private String approveStatusName;

        /**
         * 作废状态（0未作废，1已作废）
         */
        private String invalidStatusName;

        /**
         * 到货状态（0未到货，1部分到货，2已到货）
         */
        private String arrivalStatusName;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 预计交货日期
         */
        private Date planDeliveryDate;

        /**
         * 交货仓库名称
         */
        private String deliveryWarehouseName;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 采购数量
         */
        private Integer purchaseQty;

        /**
         * 采购金额
         */
        private BigDecimal purchaseAmount;

        /**
         * 签收数量
         */
        private Integer receiveQty;

        /**
         * 入库数量
         */
        private Integer stockInQty;

        /**
         * 交货数量
         */
        private Integer deliveryQty;

        /**
         * 退货数量
         */
        private Integer returnQty;

        /**
         * 备注
         */
        private String remark;

        /**
         * 审核人
         */
        private String approveUserName;

        /**
         * 申请人
         */
        private String purchaseUserName;

        /**
         * 创建人
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
         * 产品名称
         */
        private String productName;

        /**
         * 供应商id
         */
        private List<String> supplierIdList;

        /**
         * 审核状态
         */
        private List<String> approveStatusList;

        /**
         * 作废状态（0未作废，1已作废）
         */
        private String invalidStatus;

        /**
         * 到货状态（0未到货，1部分到货，2已到货）
         */
        private List<String> arrivalStatusList;

        /**
         * 是否加急（false否，true是）
         */
        private Boolean isUrgent;

        /**
         * 交货仓库id
         */
        private List<String> deliveryWarehouseIdList;

        /**
         * 新品首批（false否,true是）
         */
        private Boolean isFirstMassProduct;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;

        /**
         * 审核时间
         */
        private List<LocalDate> approveTimeList;

        /**
         * 申请人id
         */
        private List<String> purchaseUserIdList;

        /**
         * 创建人id
         */
        private List<String> createUserIdList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        /**
         * 采购日期
         */
        private Date purchaseDate;

        /**
         * 采购员id
         */
        private String purchaseUserId;

        /**
         * 采购部门id
         */
        private String purchaseDeptId;

        /**
         * 采购组织id
         */
        private String purchaseOrgId;

        /**
         * 新品首批（false否,true是）
         */
        private Boolean isFirstMassProduct;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO{

        /**
         * 供应商信息
         */
        @Valid
        private PurchaseOrderSupplierDTO.AddDTO purchaseOrderSupplierDTO;

        /**
         * 采购订单明细
         */
        @Valid
        private List<PurchaseOrderDetailDTO.AddDTO> details;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO{

        /**
         * 主表id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 供应商信息
         */
        @Valid
        private PurchaseOrderSupplierDTO.UpdateDTO purchaseOrderSupplierDTO;

        /**
         * 采购订单明细
         */
        @Valid
        private List<PurchaseOrderDetailDTO.UpdateDTO> details;
    }


    @Data
    @NoArgsConstructor
    public static class ViewDTO extends UpdateDTO {

        /**
         * 单据编码
         */
        private String code;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 操作流程（仅详情显示，无需传参）
         */
        private List<PurchaseOrderProcessDTO> process;

        /**
         * 关联单据（仅详情显示，无需传参）
         */
        private PurchaseOrderRefOtherDTO  purchaseOrderRefOtherDTO;
    }


}
