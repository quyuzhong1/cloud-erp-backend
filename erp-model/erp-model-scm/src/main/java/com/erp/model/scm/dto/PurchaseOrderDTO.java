package com.erp.model.scm.dto;

import com.common.business.dto.base.SortDTO;
import com.erp.model.plm.vo.ProductVO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Will
 * @version 1.0

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
         * 采购订单明细id
         */
        private String purchaseDetailId;

        /**
         * 单据类型
         */
        private String type;

        /**
         * 单据类型名称
         */
        private String typeName;

        /**
         * 采购单号
         */
        private String code;

        /**
         * 委外订单编号
         */
        private String subContractCode;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态名称（waitSubmit待提交，approveIng审核中，reject审核不通过，approve已审核）
         */
        private String approveStatusName;

        /**
         * 流程id
         */
        private String processId;

        /**
         * 作废状态
         */
        private Boolean invalidStatus;

        /**
         * 作废状态（0未作废，1已作废）
         */
        private String invalidStatusName;

        /**
         * 到货状态
         */
        private String arrivalStatus;

        /**
         * 到货状态（0未到货，1部分到货，2已到货）
         */
        private String arrivalStatusName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 变体信息
         */
        private String variantProperty;

        /**
         * 是否加急（false否，true是）
         */
        private Boolean isUrgent;

        /**
         * 计划交期
         */
        private String planDeliveryDate;

        /**
         * 交货仓库名称
         */
        private String deliveryWarehouseName;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 税率
         */
        private String taxRateStr;

        /**
         * 货币符号
         */
        private String currencySymbol;

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

        /**
         * 委外订单类型(child子级，parent父级)
         */
        private String subcontractType;

        /**
         * 是否是组合SKU
         */
        private Boolean isConstitute;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 是否结束交货
         */
        private Boolean isEndReceive;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 采购申请单id集合
         */
        @JsonIgnore
        private List<String> purchaseApplicationIds;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagingTotalDTO {

        /**
         * 合计数量
         */
        private Integer totalQty;

        /**
         * 合计金额
         */
        private BigDecimal totalAmount;
    }

    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {

        /**
         * 搜索类型
         */
        private String  searchType;

        /**
         * 主键ids
         */
        private List<String> ids;

        /**
         * 当前登录人能审核的ids
         */
        private List<String> idList;

        /**
         * 单据类型集合
         */
        private List<String> typeList;

        /**
         * 采购订单编号
         */
        private String code;

        /**
         * 委外订单编号
         */
        private String subContractCode;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * sku编码
         */
        private List<String> skuNoList;

        /**
         * 供应商id
         */
        private List<String> supplierIdList;

        /**
         * 审核状态（waitSubmit待提交，approveIng审核中，reject审核不通过，approve已审核）
         */
        private List<String> approveStatusList;

        /**
         * 作废状态（false未作废，true已作废）
         */
        private Boolean invalidStatus;

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
        @NotNull(message = "采购日期不能为空")
        private LocalDate purchaseDate;

        /**
         * 采购员id
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
         * 采购组织id
         */
        @NotBlank(message = "采购组织不能为空")
        private String purchaseOrgId;

        /**
         * 交货仓库id
         */
        @NotBlank(message = "交货仓库不能为空")
        private String deliveryWarehouseId;

        /**
         * 新品首批（false否,true是）
         */
        @NotNull(message = "新品首批不能为空")
        private Boolean isFirstMassProduct;

        /**
         * 委外订单类型(child子级，parent父级)
         */
        private String subcontractType;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源编码
         */
        private String sourceCode;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 单据类型
         */
        @NotBlank(message = "单据类型不能为空")
        private String type;

        /**
         * 供应商信息
         */
        @Valid
        @NotNull(message = "供应商信息不能为空")
        private PurchaseOrderSupplierDTO.AddDTO purchaseOrderSupplierDTO;

        /**
         * 采购订单明细
         */
        @Valid
        @NotEmpty(message = "采购订单明细信息不能为空")
        private List<PurchaseOrderDetailDTO.AddDTO> details;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主表id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 供应商信息
         */
        @Valid
        @NotNull(message = "供应商信息不能为空")
        private PurchaseOrderSupplierDTO.UpdateDTO purchaseOrderSupplierDTO;

        /**
         * 采购订单明细
         */
        @Valid
        @NotEmpty(message = "采购订单明细信息不能为空")
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
         * 单据类型
         */
        private String type;

        /**
         * 单据类型名称
         */
        private String typeName;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 收料组织id
         */
        private String receiveOrgId;

        /**
         * 收料组织名称
         */
        private String receiveOrgName;

        /**
         * 操作流程（仅详情显示，无需传参）
         */
        private List<PurchaseOrderProcessDTO> process;

        /**
         * 关联单据（仅详情显示，无需传参）
         */
        private PurchaseOrderRefOtherDTO purchaseOrderRefOtherDTO;
    }


    @Data
    @NoArgsConstructor
    public static class GetOneDTO extends CommonDTO {

        /**
         * 采购供应商信息
         */
        private PurchaseOrderSupplierDTO.UpdateDTO purchaseOrderSupplierDTO;

        /**
         * 收料组织id
         */
        private String receiveOrgId;

        /**
         * 收料组织名称
         */
        private String receiveOrgName;

        /**
         * 公司地址
         */
        private String companyAddress;

        /**
         * 供应商名
         */
        private String supplierName;

        /**
         * 采购组织
         */
        private String purchaseOrgName;

        /**
         * 仓库名
         */
        private String warehouseName;

        /**
         * 编码
         */
        private String code;

        /**
         * 类型
         */
        private String type;

    }


    @Data
    @NoArgsConstructor
    public static class GetQcProductDTO {


        /**
         * 供应商id
         */
        private String supplierId;


        /**
         * 供应商名
         */
        private String supplierName;


        /**
         * 仓库id
         */
        private String warehouseId;
        /**
         * 仓库名
         */
        private String warehouseName;


        /**
         * 质检类型
         */
        private String qcType;


        /**
         * 是否 是内检
         */
        private Boolean isInside;


        /**
         * 产品信息
         */
        private List<ProductVO.ProductPackVO> productList;

    }


    @Data
    @NoArgsConstructor
    public static class ExportPdfDTO {
        /**
         * 合同号
         */
        private String code;

        /**
         * 结算方式名称
         */
        private String payMethodName;

        /**
         * 合计
         */
        private BigDecimal totalAmount;

        /**
         * 币别
         */
        private String currency;

        /**
         * 甲方
         */
        private String purchaseOrgName;

        /**
         * 签订日期（甲方）
         */
        private LocalDate firstSignDate;

        /**
         * 收货地址（甲方）
         */
        private String deliveryWarehouseAddress;

        /**
         * 联系人（甲方）
         */
        private String deliveryWarehouseContract;

        /**
         * 联系电话（甲方）
         */
        private String deliveryWarehouseTel;

        /**
         * 乙方
         */
        private String supplierName;

        /**
         * 签订日期（乙方）
         */
        private LocalDate secondSignDate;

        /**
         * 供方地址（乙方）
         */
        private String supplierAddress;

        /**
         * 联系人（乙方）
         */
        private String supplierContract;

        /**
         * 联系电话（乙方）
         */
        private String supplierTel;

        /**
         * 邮箱（乙方）
         */
        private String supplierEmail;

        /**
         * 付款方式名称
         */
        private String paymentConditionName;

        /**
         * 明细信息
         */
        private List<PurchaseOrderDetailDTO.ExportPdfDTO> details;
    }

    /**
     * 生成入库单数据显示DTO
     */
    @Data
    @NoArgsConstructor
    public static class ViewGenerateReceiveDTO {
        /**
         * 采购订单主表id
         */
        private String id;

        /**
         * 采购订单明细Id
         */
        private String purchaseOrderDetailId;

        /**
         * 采购单号
         */
        private String code;

        /**
         * 供应商名称id
         */
        private String supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 交货仓库id
         */
        private String deliveryWarehouseId;

        /**
         * 交货仓库
         */
        private String deliveryWarehouseName;

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
         * 收货时间
         */
        private LocalDate billDate;

        /**
         * 收货人id
         */
        private String receiveUserId;

        /**
         * 收货人名称
         */
        private String receiveUserName;

        /**
         * 采购数量
         */
        private Integer purchaseQty;

        /**
         * 未交货数量
         */
        private Integer unReceiveQty;

        /**
         * 收货数量
         */
        private Integer receiveQty;

        /**
         * 超收数量
         */
        private Integer exceedQty;

        /**
         * 备注
         */
        private Integer remark;
    }

    /**
     * 生成入库单数据保存DTO
     */
    @Data
    @NoArgsConstructor
    public static class GenerateReceiveDTO {

        /**
         * 采购订单主表id
         */
        private String id;

        /**
         * 采购订单明细Id
         */
        private String purchaseOrderDetailId;

        /**
         * 采购单号
         */
        private String code;

        /**
         * 收货日期
         */
        private LocalDate billDate;

        /**
         * 收货人id
         */
        private String receiveUserId;

        /**
         * 收货数量
         */
        @NotNull(message = "收货数量不能为空")
        @Min(value = 1, message = "收货数量最小值为1")
        @Max(value = 999999999, message = "收货数量最大值为999999999")
        private Integer receiveQty;

        /**
         * 超收数量
         */
        @NotNull(message = "超收数量不能为空")
        @Min(value = 0, message = "超收数量最小值为1")
        @Max(value = 99999999, message = "超收数量最大值为99999999")
        private Integer exceedQty;

        /**
         * 备注
         */
        private String remark;

        /**
         * 仓库id
         */
        private String deliveryWarehouseId;
    }

    @Data
    @NoArgsConstructor
    public static class ViewGenerateStockInDTO {

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 采购订单明细id
         */
        private String purchaseOrderDetailId;

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
         * 交货仓库id
         */
        private String deliveryWarehouseId;

        /**
         * 交货仓库名称
         */
        private String deliveryWarehouseName;

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
         * 库位
         */
        private String warehouseLocation;
    }

    @Data
    @NoArgsConstructor
    public static class GenerateStockInDTO {

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 采购订单明细id
         */
        private String purchaseOrderDetailId;

        /**
         * 入库日期
         */
        private LocalDate stockInDate;

        /**
         * 入库员id
         */
        private String stockInUserId;

        /**
         * 实收数量
         */
        @NotNull(message = "实收数量不能为空")
        @Min(value = 0, message = "实收数量最小值为0")
        @Max(value = 999999999, message = "实收数量最大值为999999999")
        private Integer stockInQty;

        /**
         * 超收数量
         */
        @NotNull(message = "超收数量不能为空")
        @Min(value = 0, message = "超收数量最小值为0")
        @Max(value = 999999999, message = "超收数量最大值为999999999")
        private Integer exceedQty;

        /**
         * 备注
         */
        @Size(max = 255, message = "备注最大255个字符")
        private String remark;

        /**
         * 库位
         */
        private String warehouseLocation;
    }

    @Data
    @NoArgsConstructor
    public static class ListGenerateReceiveDTO {

        @NotEmpty(message = "仓库签收单不能为空")
        @Valid
        List<GenerateReceiveDTO> list;
    }

    @Data
    @NoArgsConstructor
    public static class ListGenerateStockInDTO {

        @NotEmpty(message = "采购入库单不能为空")
        @Valid
        List<GenerateStockInDTO> list;
    }

    @Data
    @NoArgsConstructor
    public static class AssociatedDocumentDTO {

        /**
         * 采购变更单
         */
        private List<PurchaseChangeDTO.ListDTO> purchaseChangeList;
    }

    @Data
    @NoArgsConstructor
    public static class DropDownListDTO {
        /**
         * 采购id
         */
        String id;
        /**
         * 采购单号
         */
        private String code;
    }


    /**
     * 采购订单信息
     */
    @Data
    @NoArgsConstructor
    public static class PurchaseOrderInfoDTO {


        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 收料组织
         */
        private String ReceiveOrgId;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名
         */
        private String supplierName;


        /**
         * 交货仓库id
         */
        private String deliveryWarehouseId;

        /**
         * 交货仓库
         */
        private String deliveryWarehouseName;


    }

    @Data
    @NoArgsConstructor
    public static class ViewSubcontractPoDTO {

        /**
         * 采购单号
         */
        private String code;

        /**
         * 委外订单编号
         */
        private String subContractCode;

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
         * SKUId
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
         * 预计交货日期
         */
        private LocalDate planDeliveryDate;

        /**
         * 交货仓库
         */
        private String deliveryWarehouseName;

        /**
         * 采购员
         */
        private String  purchaseUserName;
    }

    @Data
    @NoArgsConstructor
    public static class SubcontractOrderChildDTO {

        /**
         * 父级SKUbom版本
         */
        private String parentBomVersion;

        /**
         * 子级SKUbom版本
         */
        private String childBomVersion;

        /**
         * 父级采购明细id
         */
        private String parentPodId;
        /**
         * 父级采购明细id
         */
        private String parentSkuId;
        /**
         * 子级采购id
         */
        private String childPoId;
        /**
         * 子级采购明细id
         */
        private String childPodId;
        /**
         * 父级委外明细id
         */
        private String subParentDetailId;
        /**
         * 子级skuid
         */
        private String childSkuId;
        /**
         * 子级sku编号
         */
        private String childSkuNo;
        /**
         * 子级仓库
         */
        private String childWarehouseId;
        /**
         * 子级库位
         */
        private String childWarehouseLocation;
        /**
         * 子级单据状态
         */
        private String childApproveStatus;
        /**
         * 子级委外订单id
         */
        private String subChildId;
        /**
         * 子级委外订单编码
         */
        private String subChildCode;

        /**
         * 子级委外明细id
         */
        private String subChildDetailId;
    }


    /**
     * 导出网采合同
     */
    @Data
    @NoArgsConstructor
    public static class ExportPurchaseContractDTO {
        /**
         * 录单日期
         */
        private LocalDateTime createTime;

        /**
         * 编号
         */
        private String code;

        /**
         * 供货单位
         */
        private String supplierName;

        /**
         * 摘要
         */
        private String settleMethod;

        /**
         * 制单人
         */
        private String createUserName;

        /**
         * 采购主管
         */
        private String approveUserName;

        /**
         * 合计数量
         */
        private Integer sumQty;

        /**
         * 合计金额
         */
        private BigDecimal sumAmount;
    }
    /**
     * 网采合同明细
     */
    @Data
    @NoArgsConstructor
    public static class PurchaseContractDetailDTO {
        /**
         * 序号
         */
        private Integer sort;
        /**
         * 图片
         */
        private String img;
        /**
         * 物料编码
         */
        private String skuNo;
        /**
         * 商品名称
         */
        private String productName;
        /**
         * 单位
         */
        private String unit;
        /**
         * 数量
         */
        private Integer qty;
        /**
         * 单价
         */
        private BigDecimal price;
        /**
         * 金额
         */
        private BigDecimal amount;
        /**
         * 备注
         */
        private String remark;
    }

    /**
     * PDA:采购单查询
     */
    @Data
    @NoArgsConstructor
    public static class PdaPurchaseOrder {
        /**
         * 主键id
         */
        private String id;
        /**
         * 采购单号
         */
        private String code;
        /**
         * 供应商id
         */
        private String supplierId;
        /**
         * 供应商名称
         */
        private String supplierName;
        /**
         * 供应商联系人id
         */
        private String supplierContactId;
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
        /**
         * 商品信息
         */
//        private List<PurchaseOrderDetailDTO.PdaPurchaseOrderDetail> itemList;
    }

    /**
     * PDA:采购单查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PdaPurchaseOrderParam {
        /**
         * sku编号
         */
        private String skuNo;

        /**
         * 采购订单号
         */
        private String code;
    }

    /**
     * PDA:详情
     * @Author Luo_WG
     * @Date 2023/8/21 16:33
     **/
    @Data
    @NoArgsConstructor
    public static class PdaViewDTO extends CommonDTO {

        /**
         * 主表id
         */
        private String id;

        /**
         * 单据编码
         */
        private String code;

        /**
         * 单据类型
         */
        private String type;

        /**
         * 单据类型名称
         */
        private String typeName;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 收料组织id
         */
        private String receiveOrgId;

        /**
         * 收料组织名称
         */
        private String receiveOrgName;

        /**
         * 采购日期
         */
        @NotNull(message = "采购日期不能为空")
        private LocalDate purchaseDate;

        /**
         * 采购员id
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
         * 采购组织id
         */
        private String purchaseOrgId;

        /**
         * 采购组织名称
         */
        private String purchaseOrgName;

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
         * 委外订单类型(child子级，parent父级)
         */
        private String subcontractType;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源编码
         */
        private String sourceCode;

        /**
         * 供应商信息
         */
        @Valid
        @NotNull(message = "供应商信息不能为空")
        private PurchaseOrderSupplierDTO.PdaView purchaseOrderSupplierDTO;

        /**
         * 采购订单明细
         */
        @Valid
        @NotEmpty(message = "采购订单明细信息不能为空")
        private List<PurchaseOrderDetailDTO.PdaViewDTO> details;
    }
}
