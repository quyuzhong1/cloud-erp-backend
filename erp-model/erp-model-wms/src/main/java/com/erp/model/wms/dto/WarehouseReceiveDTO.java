package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 收货单DTO
 *
 * @Author Luo_WG
 * @Date 2023/4/6 17:18
 **/
@Data
@NoArgsConstructor
public class WarehouseReceiveDTO implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    /**
     * 合计
     */
    @Data
    @NoArgsConstructor
    public static class PagingTotalDTO {

        /**
         * 收货数量
         */
        private Integer receiveQty;

        /**
         * 采购数量
         */
        private Integer purchaseQty;

        /**
         * 赠品数量
         */
        private Integer giftQty;

        /**
         * 入库数量
         */
        private Integer stockInQty;

        /**
         * 退货数量
         */
        private Integer returnQty;
    }
    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends BaseEntity<AddDTO> {

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 采购订单编号
         */
        private String purchaseOrderCode;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源code
         */
        private String sourceCode;
        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 收货人id
         */
        private String receiveUserId;

        /**
         * 收货人部门id
         */
        private String receiveDeptId;


        /**
         * 是否通过送货单下推 ，默认false
         */
        private Boolean generateByDelivery = false;

        /**
         * 收货日期
         */
        @NotNull(message = "质检日期不能为空")
        private LocalDate billDate;

        /**
         * 交货仓库id
         */
        @NotNull(message = "交货仓库不能为空")
        private String deliveryWarehouseId;

        /**
         * 收货单明细
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

        private String purchaseOrderId;

        /**
         * 收货人id
         */
        @NotBlank(message = "收货人不能为空")
        private String receiveUserId;

        /**
         * 收货人部门id
         */
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
         * 发货单号
         */
        private String deliveryCode;

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
         * 供应商联系人名称
         */
        private String supplierContactName;

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
         * 采购员Id
         */
        private String purchaseUserId;

        /**
         * 采购员名称
         */
        private String purchaseUserName;

        /**
         * 采购部门id
         */
        private String purchaseDeptId;

        /**
         * 采购部门名称
         */
        private String purchaseDeptName;

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
         * 明细Id
         */
        private String detailId;

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
         * 质检状态
         */
        private String qcStatus;

        /**
         * 质检状态名称
         */
        private String qcStatusName;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;

        /**
         * 作废状态名称
         */
        private String invalidStatusName;

        /**
         * 入库状态（0未入库，1部分入库，2已入库）
         */
        private String inStockStatus;
        /**
         * 入库状态（0未入库，1部分入库，2已入库）
         */
        private String inStockStatusName;

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
         * 入库数量
         */
        private Integer stockInQty = 0;
        /**
         * 退货数量
         */
        private Integer returnQty = 0;

        /**
         * 审核人
         */
        private String approveUserName;

        /**
         * 审核完成时间
         */
        private LocalDateTime approveTime;

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
         * 委外订单类型(child子级，parent父级)
         */
        private String subcontractType;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        /**
         * 采购组织id
         */
        private String purchaseOrgId;

        /**
         * 采购组织名
         */
        private String purchaseOrgName;
        /**
         * 收料组织id
         */
        private String receiveOrgId;
        /**
         * 收料组织名
         */
        private String receiveOrgName;
        /**
         * 送货单号
         */
        private String deliveryCode;
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
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

        private Boolean invalidStatus;
        /**
         * PDA用表头状态
         */
        private String tabFlag;
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
         * 交货仓库id
         */
        private String deliveryWarehouseId;

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
        private Integer receiveQty;

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
         * 库位
         */
        private String warehouseLocation;
        /**
         * 库位名称
         */
        private String warehouseLocationName;

        /**
         * 采购订单明细id
         */
        private String purchaseOrderDetailId;

        /**
         * 新品首批
         */
        private String firstMassProduct;

        /**
         * 新品首批
         */
        private String firstMassProductName;
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

        /**
         * 采购订单详情表id
         */
        private String purchaseOrderDetailId;

        /**
         * 仓库表id
         */
        private String deliveryWarehouseId;

        /**
         * 库位
         */
        private String warehouseLocation;

        /**
         * 新品首批
         */
        private String firstMassProduct;

        /**
         * 新品首批名称
         */
        private String firstMassProductName;
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

    /**
     * 供应商 收货批次和收货数量 查询参数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceParamDTO {

        /**
         * 来源ids
         */
        private List<String> sourceIds;


        /**
         * 来源类型
         */
        private String sourceType;

    }

    /**
     * 供应商 收货批次和收货数量 查询参数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierReceiveParamDTO {

        /**
         * 供应商id集合
         */
        private List<String> supplierIds;


        /**
         * 日期范围
         */
        private List<LocalDate> dateList;

    }

    /**
     * 供应商 收货批次和收货数量
     */
    @Data
    @NoArgsConstructor
    public static class SupplierReceiveInfoDTO {

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 收货批次
         */
        private Integer receivedCount;

        /**
         * 收货数量
         */
        private Integer receivedQty;

    }

    /**
     * PDA:分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PdaPagingViewDTO {

        /**
         * 表id
         */
        private String id;

        /**
         * 收货单号
         */
        private String code;

        /**
         * 单据状态
         */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 供应商名
         */
        private String supplierName;

        /**
         * 交货仓库
         */
        private String deliveryWarehouseName;

        /**
         * 委外订单类型(child子级，parent父级)
         */
        private String subcontractType;

        /**
         * 详情产品数量
         */
        private Integer detailCount;

        /**
         * 产品信息
         */
        private List<PdaItemDTO> itemList;
    }

    /**
     * PDA:商品信息
     */
    @Data
    @NoArgsConstructor
    public static class PdaItemDTO {
        /**
         * 明细id
         */
        private String id;
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
         * 收货数量
         */
        private Integer receiveQty;
    }

    /**
     * PDA:分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PdaPagingParamDTO extends SortDTO {
        /**
         * 审核状态：根据tab页传审核状态
         */
        private List<String> approveStatusList;

        /**
         * 收货日期
         */
        private List<LocalDate> billDate;
    }

    /**
     * PDA:列表状态
     * @Author Luo_WG
     * @Date 2023/8/11 9:15
     **/
    @Data
    @NoArgsConstructor
    public static class PdaPoReceiveCountDTO {
        /**
         * 类型(waitSubmitAndReject 待提交/审核不通过，approveIng 审核中，approve 已审核)
         */
        private String tabFlag;
        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * PDA:收货单查询
     */
    @Data
    @NoArgsConstructor
    public static class PdaPoReceive {
        /**
         * 主键id
         */
        private String id;
        /**
         * 收货单号
         */
        private String code;
        /**
         * 供应商名称
         */
        private String supplierName;
        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名称
         */
        private String warehouseName;
        /**
         * 审核状态
         */
        private String approveStatus;
        /**
         * 审核状态名称
         */
        private String approveStatusName;
    }

    /**
     * PDA:收货单参数
     */
    @Data
    @NoArgsConstructor
    public static class PdaPoReceiveParam {
        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 采购收货单号
         */
        private String code;
    }


    /**
     * PDA:待入库查询
     */
    @Data
    @NoArgsConstructor
    public static class WaitInStockPaging {

        /**
         * 表id
         */
        private String id;

        /**
         * 收货单号
         */
        private String code;

        /**
         * 单据状态
         */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 供应商名
         */
        private String supplierName;

        /**
         * 交货仓库
         */
        private String deliveryWarehouseName;

        /**
         * 委外订单类型(child子级，parent父级)
         */
        private String subcontractType;

        /**
         * 详情产品数量
         */
        private Integer detailCount;

        /**
         * 质检状态
         */
        private String qcStatus;

        /**
         * 质检状态名称
         */
        private String qcStatusName;

        /**
         * 产品信息
         */
        private List<PdaWaitInStockItemDTO> itemList;
    }
    /**
     * PDA:待入库查询产品信息
     */
    @Data
    @NoArgsConstructor
    public static class PdaWaitInStockItemDTO {
        /**
         * 明细id
         */
        private String id;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku编号
         */
        private String skuNo;
        /**
         * 收货数量
         */
        private String receiveQty;
    }

    /**
     *  PDA:待入库查询查询条件
     */
    @Data
    @NoArgsConstructor
    public static class WaitInStockPagingParam extends SortDTO {
        /**
         * 获取枚举接口：wms/common/enumDropDown?type=WaitInStockFlag
         * 内容描述：类型(waitInStockQc 待入库-已质检，waitInStockNotQc 待入库-待质检，all 全部待入库)
         */
        private String tabFlag;
    }

    @Data
    @NoArgsConstructor
    public static class WaitInStockCountDTO {
        /**
         * 获取枚举接口：wms/common/enumDropDown?type=WaitInStockFlag
         * 内容描述：类型(waitInStockQc 待入库-已质检，waitInStockNotQc 待入库-待质检，all 全部待入库)
         */
        private String tabFlag;
        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 获取销售订单的入库单明细
     */
    @Data
    @NoArgsConstructor
    public static class PurchaseOrderDetailDTO {
        /**
         * 采购订单id
         */
        private String purchaseOrderDetailId;
        /**
         * 来源记录id
         */
        private String sourceId;
        /**
         * 订单类型 deliveryOrder 送货单
         */
        private String sourceType;

        /**
         * 来源明细记录id
         */
        private String sourceDetailId;
        /**
         * 收货数量
         */
        private Integer receiveQty;
        /**
         * 赠品收货数量
         */
        private Integer giftReceiveQty;
    }

    /**
     * 根据收货单获取 退货明细
     */
    @Data
    @NoArgsConstructor
    public static class PoReturnDetailDTO{
        /**
         * 采购明细id来源
         */
        private String purchaseOrderDetailId;
        /**
         * 采购id来源
         */
        private String purchaseOrderId;
        /**
         * 收货明细id来源
         */
        private String receiveDetailId;
        /**
         * 收货id来源
         */
        private String receiveId;
        /**
         * 质检主键id
         */
        private String qcId;
        /**
         * 退货id
         */
        private String returnId;
        /**
         * 审核状态
         */
        private String approveStatus;
        /**
         * 退货明细id
         */
        private String returnDetailId;
        private String skuId;
        /**
         * sku
         */
        private String skuNo;
        /**
         * 补货数量
         */
        private Integer replenishQty;
        /**
         * 退货数量
         */
        private Integer returnQty;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReceiveSourceDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 编码
         */
        private String code;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * 来与id
         */
        private String sourceId;
        /**
         * 来源编码
         */
        private String sourceCode;
        /**
         * 来源类型
         */
        private String sourceType;
        /**
         * 来源明细id
         */
        private String sourceDetailId;

    }
}
