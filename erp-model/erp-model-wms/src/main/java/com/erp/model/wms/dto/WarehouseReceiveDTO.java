package com.erp.model.wms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.validator.AddGroup;
import com.common.business.validator.UpdateGroup;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
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
    public static class AddDTO extends BaseEntity {

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 采购订单编号
         */
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
        @NotNull(message = "质检日期不能为空")
        private LocalDate billDate;

        /**
         * 交货仓库id
         */
        private String deliveryWarehouseId;

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
        @NotNull(message = "收货日期不能为空")
        private LocalDate billDate;

        /**
         * 交货仓库id
         */
        private String deliveryWarehouseId;

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
         * 审核状态名称
         */
        private String approveStatusName;

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
         * 供应商联系人
         */
        private String supplierContactId;

        /**
         * 供应商地址
         */
        private String supplierAddress;

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
        private LocalDate billDate;

        /**
         * 作废状态（false未作废，true已作废）
         */
        private Boolean invalidStatus;

        /**
         * 作废时间
         */
        private LocalDateTime invalidTime;

        /**
         * 交货仓库id
         */
        private String deliveryWarehouseId;

        /**
         * 交货仓库名称
         */
        private String deliveryWarehouseName;

        /**
         * 新品首批（false否,true是）
         */
        private Boolean isFirstMassProduct;

        /**
         * 采购员Id
         */
        private String purchaseUserId;

        /**
         * 采购部门id
         */
        private String purchaseDeptId;

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
         * 审核状态名称
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
         * sku
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 收货日期
         */
        private LocalDate billDate;

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
         * 超收数量
         */
        private Integer exceedQty;

        /**
         * 审核人
         */
        private String approveUserName;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 采购单主表id
         */
        private String purchaseOrderId;

        /**
         * 采购单详情表id
         */
        private String purchaseOrderDetailId;

        /**
         * 收货备注
         */
        private String remark;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }

    @Data
    @NoArgsConstructor
    public static class WarehouseReceiveCountDTO {

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
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        /**
         * 单据编号
         */
        private String code;

        /**
         * 主键集合
         */
        private List<String> ids;

        /**
         * skuNo集合
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
         * 作废状态（false未作废，true已作废）
         */
        private Boolean invalidStatus;

        /**
         * 收货日期
         */
        private List<LocalDate> billDate;

        /**
         * 交货仓库id
         */
        private List<String> deliveryWarehouseIdList;

        /**
         * 创建人id 集合
         */
        private List<String> createUserIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTime;
    }


    /**
     * 采购订单关联收货单的查询实体
     */
    @Data
    @NoArgsConstructor
    public static class OrderRefReceiveDTO {
        /**
         * 收货单号
         */
        private String code;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 单据状态
         */
        private String approveStatus;

        /**
         * 单据状态名称
         */
        private String approveStatusName;

        /**
         * 作废状态名称
         */
        private Boolean invalidStatus;

        /**
         * 作废状态名称
         */
        private String invalidStatusName;

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
        private LocalDate billDate;

        /**
         * 签收数量
         */
        private Integer receiveQty;

        /**
         * 超收数量
         */
        private Integer exceedQty;

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
         * 收货备注
         */
        private String remark;

        /**
         * 采购订单明细id
         */
        private String purchaseOrderDetailId;
    }

    /**
     * 下推单据列表
     */
    @Data
    @NoArgsConstructor
    public static class GenerateStockInViewDTO {
        /**
         * 详情表id
         */
        private String id;

        /**
         * 主表id
         */
        private String mainId;

        /**
         * 采购单Id
         */
        private String purchaseOrderId;

        /**
         * 采购单号
         */
        private String purchaseOrderCode;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * skuNo
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 交货仓库
         */
        private String deliveryWarehouseName;

        /**
         * 入库日期
         */
        private LocalDate stockInDate;

        /**
         * 入库员id
         */
        private String stockInUserId;

        /**
         * 入库员名称
         */
        private String stockInUserName;

        /**
         * 已签收数量
         */
        private Integer  receiveQty;

        /**
         * 未入库数量
         */
        private Integer unStockInQty;

        /**
         * 实收数量
         */
        private Integer stockInQty;

        /**
         * 超收数量
         */
        private Integer exceedQty;

        /**
         * 备注
         */
        private String remark;

        /**
         * 采购订单明细id
         */
        private String purchaseOrderDetailId;
    }

    /**
     * 下推单据列表
     */
    @Data
    @NoArgsConstructor
    public static class ListGenerateStockInDTO {
        private List<WarehouseReceiveDTO.GenerateStockInDTO> list;
    }

    /**
     * 下推单据列表
     */
    @Data
    @NoArgsConstructor
    public static class GenerateStockInDTO {

        /**
         * 详情表id
         */
        private String id;

        /**
         * 主表id
         */
        private String mainId;

        /**
         * 入库日期
         */
        @NotNull(message = "入库日期不能为空")
        private LocalDate stockInDate;

        /**
         * 入库员Id
         */
        @NotBlank(message = "入库员不能为空")
        private String stockInUserId;

        /**
         * 实收数量
         */
        private Integer stockInQty;

        /**
         * 超收数量
         */
        private Integer exceedQty;

        /**
         * 备注
         */
        private String remark;
    }

    /**
     * 获取签收数量
     */
    @Data
    @NoArgsConstructor
    public static class GetReceiveDTO {
        /**
         * 采购单id
         */
        private String purchaseOrderId;

        /**
         * skuId
         */
        private String skuId;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 签收数量
         */
        private Integer receiveQty;

    }


}
