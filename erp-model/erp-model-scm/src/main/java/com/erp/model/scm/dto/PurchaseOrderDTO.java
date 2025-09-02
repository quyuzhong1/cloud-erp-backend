package com.erp.model.scm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.SortDTO;
import com.common.core.anno.StateEnumValue;
import com.erp.model.plm.vo.ProductVO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
         * 下单时间 取自审核时间 【可排序】
         */
        private LocalDateTime approveTime;

        /**
         * 确认类型 auto系统 手动 【可排序】
         */
        private String confirmType;
        private String confirmTypeName;
        /**
         * 客户 【可排序】
         */
        private String purchaseOrgName;

        /**
         * 单据类型 【可排序】
         */
        private String type;

        /**
         * 单据类型名称
         */
        private String typeName;
        /**
         * 退货方式
         */
        private String returnType;

        /**
         * 退货方式名称
         */
        private String returnTypeName;

        /**
         * 采购单号【可排序】
         */
        private String code;

        /**
         * 委外订单编号
         */
        private String subContractCode;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名称【可排序】
         */
        private String supplierName;

        /**
         * 审核状态【可排序】
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
         * 作废状态【可排序】
         */
        private Boolean invalidStatus;

        /**
         * 作废状态（0未作废，1已作废）
         */
        private String invalidStatusName;

        /**
         * 执行状态【可排序】
         */
        private String executionStatus;

        /**
         * 执行状态名称
         */
        private String executionStatusName;

        /**
         * 接收说明
         */
        private String confirmRemark;

        /**
         * skuId【可排序】
         */
        private String skuId;

        /**
         * sku编码 【可排序】
         */
        private String skuNo;

        /**
         * 报关型号
         */
        private String declareModel;

        /**
         * 报关名称
         */
        private String declareName;

        /**
         * 产品名称【可排序】
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
         * 计划交期 [可排序] 预计交货日期
         */
        private String planDeliveryDate;

        /**
         * 交货仓库名称（目的仓库）【可排序】
         */
        private String deliveryWarehouseName;

        /**
         * 含税单价[可排序]
         */
        private BigDecimal taxPrice;
        /**
         * 含税单价【导出使用】
         */
        private String taxPriceName;
        /**
         * 税率【可排序】
         */
        private BigDecimal taxRate;

        /**
         * 税率
         */
        private String taxRateStr;

        /**
         * 货币符号【可排序】
         */
        private String currencySymbol;

        /**
         * 采购数量/订单数量[可排序]
         */
        private Integer purchaseQty;

        /**
         * 采购金额/价税合计【可排序】
         */
        private BigDecimal purchaseAmount;
        /**
         * 采购金额/价税合计【导出使用】
         */
        private String purchaseAmountName;

        /**
         * 签收数量/已送货数量/已收货数量（已签收）
         */
        private Integer receiveQty;
//        /**
//         * 已送货未签收数量/已送货数量（待发货页面使用）
//         */
//        private Integer waitReceiveQty;
        /**
         * 入库数量/已收货数量
         */
        private Integer stockInQty;

        /**
         * 未交货数量/待交货量 srm改为 剩余送货量
         */
        private Integer deliveryQty;
        /**
         * 已送货数量
         */
        private Integer deliveredQty;
        /**
         * 剩余送货量
         */
        private Integer waitDeliveryQty;

        /**
         * 退货数量/已退货数量
         */
        private Integer returnQty;

        /**
         * 质检退货数量
         */
        private Integer qcReturnQty;

        /**
         * 库存退货数量
         */
        private Integer stockReturnQty;


        /**
         * srm协同（true 未开启，false 已开启）
         */
        private Boolean srmDisabled;

        /**
         * srm协同名称（true 未开启，false 已开启）
         */
        private String srmDisabledName;

        /**
         * 备注/明细备注
         */
        private String remark;

        /**
         * 审核人【可排序】
         */
        private String approveUserName;


        /**
         * 申请人/客户联系人【可排序】
         */
        private String purchaseUserName;

        /**
         * 创建人【可排序】
         */
        private String createUserName;

        /**
         * 创建时间【可排序】
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
         * 结束交货时间【可排序】
         */
        private LocalDateTime endReceiveTime;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源类型【可排序】
         */
        private String sourceType;

        /**
         * 退货方式 退货扣款 退货补货
         */
        private String returnMode;

        /**
         * 交货周期
         * product_purchase
         */
        private Integer deliveryCycle;
        /**
         * 交货周期描述
         */
        private String deliveryCycleName;
        /**
         * 交货周期标识 true 红色  false 无
         */
        private Boolean deliveryCycleFlag;

        /**
         * 仓位
         */
        private String warehouseLocation;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
         * 合同盖章状态 ContractStampStatusEnum
         */
        private String contractStampStatus;

        private String contractStampStatusName;

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
         * 页面高级查询
         * tabFlag,(waitSubmit待提交,toBeApprove待审批,toBeConfirm待确认,confirm已确认,reject已拒绝,delivery送货中,finish已完成,closed已关闭,approveReject不通过)
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }

    @Data
    @NoArgsConstructor
    public static class SrmSearchParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 供应商id
         */
        private String supplierId;
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
         * 采购员名称/客户联系人
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
         * 采购组织名称/客户
         */
        private String purchaseOrgName;
        /**
         * 交货仓库id
         */
        @NotBlank(message = "交货仓库不能为空")
        private String deliveryWarehouseId;
        /**
         * 目的仓库/交货仓库
         */
        private String deliveryWarehouseName;
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
         * 供应商账户id
         * http://172.16.100.11:3002/project/83/interface/api/36188
         */
        private String supplierAccountId;
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
         * 执行状态 ,PurchaseOrderConfirmTypeEnum枚举
         */
        private String executionStatus;
        /**
         * 执行状态描述
         */
        private String executionStatusName;
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
         * 审核状态
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
         * 已审核通过时间/下单时间
         */
        private LocalDateTime approveTime;

        /**
         * 操作流程（仅详情显示，无需传参）
         */
        private List<PurchaseOrderProcessDTO> process;

        /**
         * 关联单据（仅详情显示，无需传参）
         */
        private PurchaseOrderRefOtherDTO purchaseOrderRefOtherDTO;

        /**
         * 能否编辑
         * ture 能编辑  false 不能编辑
         */
        private Boolean  canEdit = Boolean.FALSE;

        /**
         * 供应商账户信息
         */
        /**
         * 收款方  账户名称
         */
        private String payee;
        /**
         * 收款银行
         */
        private String bankName;
        /**
         * 银行账号
         */
        private String bankAccount;
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
        private String codeStr;

        /**
         * 单据类型
         */
        private String type;
        private String typeName;

        /**
         * 采购日期
         */
        private LocalDate purchaseDate;

        /**
         * 采购部门名称
         */
        private String purchaseDeptName;

        /**
         * 采购员名称
         */
        private String purchaseUserName;

        /**
         * 交货仓库名称
         */
        private String deliveryWarehouseName;

        /**
         * 结算方式名称
         */
        private String payMethodName;

        /**
         * 含税金额合计
         */
        private BigDecimal totalAmount;
        /**
         * 含税金额合计 增加千分位分割
         */
        private String totalAmountStr;

        /**
         * 不含税金额合计
         */
        private BigDecimal totalNotTaxAmount;
        /**
         * 不含税金额合计 增加千分位分割
         */
        private String totalNotTaxAmountStr;
        /**
         * 不含税金额合计 中文大写
         */
        private String totalNotTaxAmountChinese;

        /**
         * 币别
         */
        private String currency;
        /**
         * 供应商账户名称
         */
        private String supplierAccountName;
        /**
         * 供应商收款银行
         */
        private String supplierBankName;
        /**
         * 供应商银行账号
         */
        private String supplierBankNo;

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
         * 付款方式名称
         */
        private String logoUrl;

        /**
         * 打印时间
         */
        private LocalDate printTime;

        /**
         * 打印人
         */
        private String printName;

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
        /**
         * 库位名称
         */
        private String warehouseLocationName;
        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 新品首批
         */
        private String firstMassProduct;

        /**
         * 新品首批
         */
        private String firstMassProductName;
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
        /**
         * 新品首批
         */
        private String firstMassProduct;
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
         * 采购订单编号
         */
        private String code;

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

        /**
         * 采购组织
         */
        private String purchaseOrgId;
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
        /**
         * 采购数量
         */
        private Integer purchaseQty;
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
         * 合计未含税金额
         */
        private BigDecimal sumAmount;
        /**
         * 合计含税金额
         */
        private BigDecimal sumTaxAmount;
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
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 税率
         */
        private String taxRate;

        /**
         * 金额
         */
        private BigDecimal amount;
        /**
         * 含税金额
         */
        private BigDecimal taxAmount;
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
         * 采购组织id
         */
        private String purchaseOrgId;

        /**
         * 采购组织名称
         */
        private String purchaseOrgName;

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

    /**
     * SRM 整单接受/拒绝
     * @Author zdy
     * @Date 2023/8/21 16:33
     **/
    @Data
    @NoArgsConstructor
    public static class ConfirmDTO{
        /**
         * 主键id 订单id
         */
        @NotEmpty(message = "订单不能为空")
        private List<String> ids;
        /**
         * 1整单接受 2整单拒绝
         */
        @NotNull(message = "处理状态不能为空")
        @StateEnumValue(intValues = {1,2},message = "处理状态有误")
        private Integer status;

        /**
         * 操作说明 接受原因/拒绝原因
         */
        private String remark;
        /**
         * 供应商id
         */
        private String supplierId;
    }


    @Data
    @NoArgsConstructor
    public static class PushIdDTO{
        /**
         * 采购订单明细id
         */
        @NotEmpty(message = "采购单明细Id不能为空")
        private List<String>  purchaseDetailIdList;
    }

    @Data
    @NoArgsConstructor
    public static class PurchasePriceDTO {
        /**
         * 批量表单数据
         */
        private PurchaseOrderDTO.UpdateDTO dto;
        /**
         * 批量校验结果
         */
        private List<BatchResultDTO> batchResultDTOList;
    }

    @Data
    @NoArgsConstructor
    public static class SourceCodeParamDTO extends SortDTO{

        /**
         * 关键词
         */
        private String searchKeyword;

        /**
         * 审核状态
         */
        private String approveStatus;
    }

    @Data
    @NoArgsConstructor
    public static class SourceCodeDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 编码
         */
        private String code;
    }


    @Data
    @NoArgsConstructor
    public static class PurchaseCalcQtyDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 采购订单明细id
         */
        private String purchaseDetailId;
        /**
         * 采购单号
         */
        private String code;
        /**
         * 供应商id
         */
        private String supplierId;
        /**
         * skuId
         */
        private String skuId;

        /**
         * sku编码
         */
        private String skuNo;
        /**
         * 采购数量/订单数量
         */
        private Integer purchaseQty;

        /**
         * 签收数量/已送货数量/已收货数量（已签收）
         */
        private Integer receiveQty;

        /**
         * 入库数量/已收货数量
         */
        private Integer stockInQty;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PurchaseCalcQtyParamsDTO {
        /**
         * sku
         */
        private List<String> skuIdList;

        /**
         * supplier
         */
        private List<String> supplierIdList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContractStampStatusParamsDTO extends BaseIdsDTO.IdsDTO{

        /**
         * 合同盖章状态
         */
        @NotBlank(message = "合同盖章状态不能为空")
        private String contractStampStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateContractStampStatusParamsDTO{
        /**
         * 唯一键name
         */
        @NotBlank(message = "合同盖章状态不能为空")
        private String field;

        /**
         * 唯一键value
         */
        @NotBlank(message = "合同盖章状态不能为空")
        private String value;

        /**
         * 合同盖章状态
         */
        @NotBlank(message = "合同盖章状态不能为空")
        private String contractStampStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
     public static class InsertDTO extends AddDTO {

        private String approveStatus;

        private LocalDateTime approveTime;

        private String approveUserId;

        private String approveUserName;
     }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateApprovalStatusDTO {
        private PurchaseOrderEntity purchaseOrderEntity;
        private String approveStatus;
    }
    @Data
    @NoArgsConstructor
    public static class SupplierSkuDTO {
        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * skuN
         */
        private String skuNo;
    }



    @Data
    @NoArgsConstructor
    public static class SearchAdjustParamDTO extends SortDTO {

        /**
         * 页面高级查询
         * tabFlag,(waitSubmit待提交,toBeApprove待审批,toBeConfirm待确认,confirm已确认,reject已拒绝,delivery送货中,finish已完成,closed已关闭,approveReject不通过)
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 供应商id
         */
        private String supplierId;
        /**
         * skuId
         */
        private String skuId;
        /**
         * 最小数量
         */
        private Integer minQty;
        /**
         * 最大数量
         */
        private Integer maxQty;

        /**
         * 调价
         */
        @NotBlank(message = "采购调价表id不能为空")
        private String purchasePriceChangeDetailId;
    }


    @Data
    @NoArgsConstructor
    public static class AdjustListDTO {

        /**
         * 主键id
         */
        private String id;
        /**
         * 采购单号
         */
        private String code;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * 供应商Id
         */
        private String supplierId;
        /**
         * 采购组织id
         */
        private String purchaseOrgId;
        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 执行状态，ExecutionStatusEnum枚举
         */
        private String executionStatus;

        /**
         * 执行状态，ExecutionStatusEnum枚举
         */
        private String executionStatusName;

        /**
         * 单据日期
         */
        private LocalDate purchaseDate;

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
         * 采购数量
         */
        private Integer purchaseQty;
        /**
         * 税率
         */
        private BigDecimal taxRate;
        /**
         * 税率，带百分比
         */
        private String taxRateStr;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 待调整单价
         */
        private BigDecimal adjustTaxPrice;
        /**
         * 币别
         */
        private String currency;
        /**
         * 币别符号
         */
        private String currencySymbol;
        /**
         * 单价是否一致，一致/不一致
         */
        private Boolean isSame;

        /**
         * 单价是否一致，一致/不一致
         */
        private String isSameName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
    }

    @Data
    @NoArgsConstructor
    public static class AdjustPriceDTO {

        /**
         * 采购调价表明细id
         */
        @NotBlank(message = "采购调价表明细id不能为空")
        private String purchasePriceChangeDetailId;

        /**
         * 主键明细id
         */
        @NotEmpty(message = "主键明细id不能为空")
        private List<String> detailIdList;
    }
}
