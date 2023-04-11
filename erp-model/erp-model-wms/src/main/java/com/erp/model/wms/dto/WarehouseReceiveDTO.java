package com.erp.model.wms.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 收货单DTO
 * @Author Luo_WG
 * @Date 2023/4/6 17:18
 **/
@Data
@NoArgsConstructor
public class WarehouseReceiveDTO {

    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 采购订单编号
         */
        @NotBlank(message = "采购订单编号不能为空")
        private String purchaseOrderCode;

        /**
         * 收货人id
         */
        @NotBlank(message = "收货人不能为空")
        private String receiveUserId;

        /**
         * 收货人部门id
         */
        @NotBlank(message = "收货人部门不能为空")
        private String receiveDeptId;

        /**
         * 收货日期
         */
        @NotBlank(message = "收货日期不能为空")
        private LocalDate receiveTime;

        /**
         * 报价明细
         */
        @Valid
        private List<WarehouseReceiveDetailDTO.AddDTO> warehouseReceiveDetailList;

    }


    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {

        /**
         * 表id
         */
        @NotBlank(message = "签收单主表id不能为空")
        private String id;

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 采购订单编号
         */
        @NotBlank(message = "采购订单编号不能为空")
        private String purchaseOrderCode;

        /**
         * 收货人id
         */
        @NotBlank(message = "收货人不能为空")
        private String receiveUserId;

        /**
         * 收货人部门id
         */
        @NotBlank(message = "收货人部门不能为空")
        private String receiveDeptId;

        /**
         * 收货日期
         */
        @NotBlank(message = "收货日期不能为空")
        private LocalDate receiveTime;

        /**
         * 审核状态
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 签收单明细
         */
        @Valid
        private List<WarehouseReceiveDetailDTO.UpdateDTO> warehouseReceiveDetailList;

    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        /**
         * id
         */
        private String id;

        /**
         * 审核状态 waitSubmit待提交，approveIng审核中，reject审核不通过，approve已审核
         */
        private String approveStatus;

        /**
         * 单据编号
         */
        private String code;

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 采购订单编号
         */
        private String purchaseOrderCode;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 收货人id
         */
        private String receiveUserId;

        /**
         * 收货人名称
         */
        private String receiveUserName;

        /**
         * 收货人部门id
         */
        private String receiveDeptId;

        /**
         * 收货人部门名称
         */
        private String receiveDeptName;

        /**
         * 收货人组织id
         */
        private String receiveOrgId;

        /**
         * 收货人组织名称
         */
        private String receiveOrgName;

        /**
         * 收货日期
         */
        private LocalDate receiveTime;

        /**
         * 作废状态（0未作废，1已作废）
         */
        private String invalidStatus;

        /**
         * 作废时间
         */
        private LocalDateTime invalidTime;

        /**
         * 报价明细
         */
        @Valid
        private List<WarehouseReceiveDetailDTO.ViewDTO> warehouseReceiveDetailList;

    }


    /**
     * 分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * 表id
         */
        private String id;

        /**
         * 收货单号
         */
        private String code;

        /**
         * 采购单号
         */
        private String purchaseOrderCode;

        /**
         * 供应商名
         */
        private String supplierName;

        /**
         * 单据状态
         */
        private String approveStatus;

        /**
         * 作废状态
         */
        private String invalidStatus;

        /**
         * sku
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 收货日期
         */
        private LocalDate receiveTime;

        /**
         * 签收数量
         */
        private Integer receiveQty;

        /**
         * 交货仓库
         */
        private String deliveryWarehouseName;

        /**
         * 收货人名称
         */
        private String receiveUserName;

        /**
         * 采购员名称
         */
        private String purchaseUserName;

        /**
         * 采购数量
         */
        private Integer purchaseQty;

        /**
         * 审核人
         */
        private String approveUserName;

        /**
         * 创建人
         */
        private String createUserName;
    }


    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * skuNo集合
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 供应商id
         */
        private List<String> supplierIdList;

        /**
         * 采购订单编号
         */
        private String purchaseOrderCode;

        /**
         * 采购员id
         */
        private List<String> purchaseUserIdList;

        /**
         * 单据状态
         */
        private List<String> approveStatusList;

        /**
         * 作废状态（0未作废，1已作废）
         */
        private String invalidStatus;

        /**
         * 计划交货时间开始时间
         */
        private LocalDate planReceiveStartTime;

        /**
         * 计划交货时间结束时间
         * @return
         */
        private LocalDate planReceiveEndTime;

        /**
         * 交货仓库id
         */
        private List<String> deliveryWarehouseIdList;

        /**
         * 创建人id 集合
         */
        private List<String> createUserIdList;


        /**
         * 创建开始时间
         */
        private LocalDate createStartTime;

        /**
         * 创建结束时间
         * @return
         */
        private LocalDate createEndTime;
    }
}
