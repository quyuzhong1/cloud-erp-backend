package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.AttachDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 退货单DTO
 * @Author Luo_WG
 * @Date 2023/4/7 10:52
 **/
@Data
public class PurchaseReturnOrderDTO {
    private PurchaseReturnOrderDTO() {
        throw new IllegalStateException("Utility PurchaseReturnOrderDTO class");
    }
    /**
     * 添加
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 成品采购退货单【日志记录使用】
         */
        private String parentReturnCode;
        /**
         * 子件委外退料单【日志记录使用】
         */
        private String childSubcontractCode;
        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * 退货方式
         */
        @NotBlank(message = "退货方式不能为空")
        private String returnMode;

        /**
         * 退货人组织id
         */
        private String returnOrgId;

        /**
         * 采购组织id
         */
        @NotBlank(message = "采购组织不能为空")
        private String purchaseOrgId;

        /**
         * 退货仓库id
         */
        @NotBlank(message = "退货仓库不能为空")
        private String returnWarehouseId;

        /**
         * 退货仓库库位
         */
        private String warehouseLocation;

        /**
         * 退货来源
         */
        @NotBlank(message = "退货来源不能为空")
        private String sourceType;
        /**
         * 退货数据来源（selfAdd 手动新增 , autoAdd 自动新增）
         * SourceTypeEnum
         */
        private String returnType;

        /**
         * 退货人id
         */
        private String returnUserId;

        /**
         * 退货原因
         */
        private String returnRemark;

        /**
         * 供应商id
         */
        @NotBlank(message = "供应商不能为空")
        private String supplierId;

        /**
         * 供应商联系人id
         */
        private String supplierContactId;

        /**
         * 采购员id
         */
        private String purchaseUserId;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 报价明细
         */
        @Valid
        private List<PurchaseReturnOrderDetailDTO.AddDTO> purchasePriceDetailList;

    }


    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {
        /**
         * 退货单id
         */
        private String id;

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 采购订单编号
         */
        private String purchaseOrderCode;

        /**
         * 单据日期
         */
        private LocalDate billDate;

        /**
         * 供应商id
         */
        @NotBlank(message = "供应商不能为空")
        private String supplierId;

        /**
         * 退货方式 退货扣款 退货补货
         */
        @NotBlank(message = "退货方式不能为空")
        private String returnMode;

        /**
         * 退货人id
         */
        private String returnUserId;

        /**
         * 退货人组织id
         */
        private String returnOrgId;

        /**
         * 采购组织id
         */
        @NotBlank(message = "采购组织不能为空")
        private String purchaseOrgId;

        /**
         * 退货原因
         */
        private String returnRemark;

        /**
         * 退货仓库id
         */
        @NotBlank(message = "退货仓库不能为空")
        private String returnWarehouseId;

        /**
         * 退货仓库名称
         */
        private String returnWarehouseName;

        /**
         * 供应商联系人id
         */
        private String supplierContactId;

        /**
         * 采购用户id
         */
        private String purchaseUserId;

        /**
         * 签收单明细
         */
        @Valid
        private List<PurchaseReturnOrderDetailDTO.UpdateDTO> purchasePriceDetailList;

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
         * 退货方式
         */
        private String returnMode;

        /**
         * 退货方式名称
         */
        private String returnModeName;

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
         * 供应商联系人名称
         */
        private String supplierContactName;

        /**
         * 供应商地址
         */
        private String supplierAddress;

        /**
         * 退货人id
         */
        private String returnUserId;

        /**
         * 退货人名称
         */
        private String returnUserName;

        /**
         * 退货部门id
         */
        private String returnDeptId;

        /**
         * 退货部门名称
         */
        private String returnDeptName;

        /**
         * 退货人组织id
         */
        private String returnOrgId;

        /**
         * 退货人组织名称
         */
        private String returnOrgName;

        /**
         * 采购组织id
         */
        private String purchaseOrgId;

        /**
         * 采购组织id
         */
        private String purchaseOrgName ;

        /**
         * 退货原因
         */
        private String returnRemark;

        /**
         * 退货日期
         */
        private LocalDate billDate;

        /**
         * 作废状态（false未作废，true已作废）
         */
        private Boolean invalidStatus;

        /**
         * 作废状态名称
         */
        private String invalidStatusName;

        /**
         * 作废时间
         */
        private LocalDateTime invalidTime;

        /**
         * 作废备注
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
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 退货来源
         */
        private String sourceType;

        /**
         * 退货来源名称
         */
        private String sourceTypeName;

        /**
         * 退货仓库id
         */
        private String returnWarehouseId;

        /**
         * 退货仓库名称
         */
        private String returnWarehouseName;

        /**
         * 采购用户id
         */
        private String purchaseUserId;

        /**
         * 采购用户名称
         */
        private String purchaseUserName;

        /**
         * 采购部门id
         */
        private String purchaseUserDeptId;

        /**
         * 采购部门名称
         */
        private String purchaseUserDeptName;

        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 报价明细
         */
        @Valid
        private List<PurchaseReturnOrderDetailDTO.ViewDTO> purchasePriceDetailList;

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
         * 明细id
         */
        private String detailId;

        /**
         * 退货单号
         */
        private String code;

        /**
         * 采购单号
         */
        private String purchaseOrderCode;
        /**
         * 采购订单类型(CGDD01_SYS标准采购订单，CGDD02_SYS委外采购订单，CGDD06-SYS补货采购订单)
         * 地址：/scm/dict/list 字典类型：purchaseOrderType
         */
        private String purchaseType;
        /**
         * 采购订单类型名称
         */
        private String purchaseTypeName;
        /**
         * 采购订单来源id
         */
        private String purchaseSourceId;
        /**
         * 采购订单来源编码
         */
        private String purchaseSourceCode;
        /**
         * 采购订单来源类型
         */
        private String purchaseSourceType;
        /**
         * 委外订单类型(child子级，parent父级)
         */
        private String subcontractType;

        /**
         * 供应商名称
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
         * skuId
         */
        private String skuId;

        /**
         * sku
         */
        private String skuNo;

        /**
         * 是否组合品 true 是
         */
        private Boolean isCombination;

        /**
         * 是否组合品中文名称
         */
        private String isCombinationName;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 退货日期
         */
        private LocalDate billDate;

        /**
         * 退货仓库Id
         */
        private String returnWarehouseId;

        /**
         * 退货仓库
         */
        private String returnWarehouseName;

        /**
         * 退货数量
         */
        private Integer returnQty;


        /**
         * 退款单价
         */
        private BigDecimal returnPrice;

        /**
         * 扣款数量
         */
        private Integer deductAmountQty;

        /**
         * 退款金额
         */
        private BigDecimal deductAmountAmount;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币别符号
         */
        private String currencySymbol;

        /**
         * 退货原因
         */
        private String returnRemark;

        /**
         * 退货方式
         */
        private String returnMode;

        /**
         * 退货单数据来源（selfAdd手动新增，autoAdd自动新增）
         */
        private String returnModeName;
        /**
         * 退货单数据来源
         */
        private String returnType;

        /**
         * 采购员名称
         */
        private String purchaseUserName;

        /**
         * 退货人
         */
        private String returnUserName;

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
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 备注
         */
        private String remark;

        /**
         * 仓位
         */
        private String warehouseLocation;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;

        /**
         * 单据来源
         */
        private String sourceType;

        /**
         * 退货来源
         * 参考枚举ReturnOrderSourceEnum
         */
        private String returnOrderSource;

        /**
         * 退货来源名称
         */
        private String returnOrderSourceName;

        /**
         * 采购订单表id
         */
        private String purchaseOrderId;

        /**
         * 采购订单详情表id
         */
        private String purchaseOrderDetailId;

        /**
         * 退货确认
         */
        private String confirmStatus;

        /**
         * 退货确认中文
         */
        private String confirmStatusName;

        /**
         * 异常分类
         */
        private String unusualType;

        /**
         * 异常分类中文
         */
        private String unusualTypeName;

        /**
         * 异常反馈描述
         */
        private String unusualRemark;

        /**
         * 异常处理人Id
         */
        private String unusualHandleUserId;

        /**
         * 异常处理人名称
         */
        private String unusualHandleUserName;

        /**
         * 退货确认日期
         */
        private LocalDate confirmDate;

        /**
         * 签收人
         */
        private String receiveUserName;

        /**
         * 签收时间
         */
        private LocalDateTime receiveTime;
        /**
         * 委外订单
         */
        private String subcontractCode;

    }

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {


        /**
         * 状态(waitSubmit 待提交，approveIng 审核中，reject 审核不通过，approve 已审核，waitForMeHandle 待我处理)
         * 来源：http://172.16.100.11:3002/project/92/interface/api/7186
         * 入参：key=poReturnStatus
         */
        private List<String> approveStatusList;

        /**
         * 作废状态（false未作废，true已作废）
         */
        private Boolean invalidStatus;
        /**
         * 退货日期
         */
        private List<LocalDate> billDateList;



        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

    }

    /**
     * 采购订单关联退货单的查询实体
     */
    @Data
    @NoArgsConstructor
    public static class OrderRefReceiveDTO {
        /**
         * 表id
         */
        private String id;

        /**
         * 退货单号
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
         * 退货日期
         */
        private LocalDate billDate;

        /**
         * 退货仓库
         */
        private String returnWarehouseName;

        /**
         * 退货数量
         */
        private Integer returnQty;

        /**
         * 退货原因
         */
        private String returnRemark;

        /**
         * 退货方式
         */
        private String returnMode;

        /**
         * 退货方式名称
         */
        private String returnModeName;

        /**
         * 采购员名称
         */
        private String purchaseUserName;

        /**
         * 退货人
         */
        private String returnUserName;

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
         * 采购订单明细id
         */
        private String purchaseOrderDetailId;
    }

    @Data
    @NoArgsConstructor
    public static class ReturnOrderCountDTO {

        /**
         * 状态(waitSubmit 待提交，approveIng 审核中，reject 审核不通过，approve 已审核，waitForMeHandle 待我处理)
         * 来源：http://172.16.100.11:3002/project/92/interface/api/7186
         * 入参：key=poReturnStatus
         */
        private String tabFlag;

        /**
         * tab名称
         */
        private String tabFlagName;
        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 获取签收数量
     */
    @Data
    @NoArgsConstructor
    public static class GetReturnQtyDTO {
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
         * 退货数量
         */
        private Integer returnQtyDTO;

    }

    @Data
    @NoArgsConstructor
    public static class ViewGeneratePurchaseReturnOrderDTO {

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源id
         */
        private String sourceId;
        /**
         * 收货id来源/采购订单id
         */
        private String receiveId;
        /**
         * 来源类型
         */
        private String receiveType;

        /**
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 采购订单单号
         */
        private String purchaseOrderCode;

        /**
         * 采购订单明细id
         */
        private String purchaseOrderDetailId;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 供应商名称
         */
        private String supplierId;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * skuId
         */
        private String skuId;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 交货仓库名称
         */
        private String deliveryWarehouseName;

        /**
         * 交货仓库id
         */
        private String deliveryWarehouseId;

        /**
         * 入库数量
         */
        private Integer stockInQty;

        /**
         * 收货数量
         */
        private Integer receiveQty;

        /**
         * 库位编码
         */
        private String warehouseLocation;
        /**
         * 库位名称
         */
        private String warehouseLocationName;

        /**
         * 币种
         */
        private String currency;

        /**
         * 币种符号
         */
        private String currencySymbol;

        /**
         * 实退数量
         */
        private Integer returnQty;


        /**
         * 扣款数量
         */
        private Integer deductAmountQty;

        /**
         * 退款单价
         */
        private BigDecimal taxPrice;

        /**
         * 采购员id
         */
        private String purchaseUserId;
    }

    @Data
    @NoArgsConstructor
    public static class UpdatePurchaseOrderAmount {
        /**
         * 采购单详情表id
         */
        private String purchaseOrderDetailId;

        /**
         * 采购金额
         */
        private BigDecimal purchaseAmount; 
    }

    /**
     * 供应商、单据日期 获取质检退货单据量、数量 查询条件
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierReturnParamDTO {

        /**
         * 供应商id集合
         */
        private List<String> supplierIds;

        /**
         * 单据日期范围
         */
        private List<LocalDate> dateList;

        /**
         * 单据来源
         */
        private List<String> sourceTypeList;

    }

    /**
     * 供应商 获取质检退货单据量、数量
     */
    @Data
    @NoArgsConstructor
    public static class SupplierReturnDTO {

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 质检退货单量
         */
        private Integer qcReturnCount;

        /**
         * 实退数量
         */
        private Integer qcReturnQty;

    }

    /**
     * PDA:列表查询
     * @Author Luo_WG
     * @Date 2023/8/18 16:21
     **/
    @Data
    @NoArgsConstructor
    public static class PdaPagingViewDTO {

        /**
         * 主键id
         */
        private String id;

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
         * 供应商
         */
        private String supplierName;

        /**
         * 退货仓库名称
         */
        private String returnWarehouseName;

        /**
         * 产品数量
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
         * 实退数量
         */
        private Integer returnQty;
    }

    /**
     * PDA:列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PdaPagingParamDTO extends SortDTO {
        /**
         * 审核状态：根据tab页传审核状态
         */
        private List<String> approveStatusList;

        /**
         * 退货日期
         */
        private List<LocalDate> billDateList;
    }

    /**
     * PDA:列表状态
     * @Author Luo_WG
     * @Date 2023/8/11 9:15
     **/
    @Data
    @NoArgsConstructor
    public static class PdaReturnOrderCountDTO {
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
     * SRM供应商分页信息
     */
    @Data
    @NoArgsConstructor
    public static class SupplierPagingViewDTO {
        /**
         * 表id
         */
        private String id;
        /**
         * 明细id
         */
        private String detailId;

        /**
         * 退货确认日期【可排序】
         */
        private LocalDate confirmDate;

        /**
         * 退货单号【可排序】
         */
        private String code;

        /**
         * 采购单号【可排序】
         */
        private String purchaseOrderCode;

        /**
         * 退货确认状态
         */
        private String confirmStatus;

        /**
         * 退货确认状态中文
         */
        private String confirmStatusName;

        /**
         * 退货方式
         */
        private String returnMode;

        /**
         * 退货方式名称
         */
        private String returnModeName;

        /**
         * skuId
         */
        private String skuId;

        /**
         * sku【可排序】
         */
        private String skuNo;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 退货数量【可排序】
         */
        private Integer returnQty;

        /**
         * 退货原因【可排序】
         */
        private String returnRemark;

        /**
         * 退货备注【可排序】
         */
        private String remark;

        /**
         * 退货来源
         */
        private String returnOrderSource;

        /**
         * 退货来源名称
         */
        private String returnOrderSourceName;

        /**
         * 退货人
         */
        private String returnUserName;

        /**
         * 异常分类
         */
        private String unusualType;

        /**
         * 异常分类名称
         */
        private String unusualTypeName;

        /**
         * 异常反馈描述【可排序】
         */
        private String unusualRemark;

        /**
         * 退货日期【可排序】
         */
        private LocalDate billDate;

        /**
         * 来源类型
         */
        private String sourceType;

    }


    /**
     * SRM供应商分页参数
     */
    @Data
    @NoArgsConstructor
    public static class SupplierPagingParamDTO extends SortDTO {
        /**
         * 订单编号
         */
        private String code;

        /**
         * skuNo集合
         */
        private List<String> skuNoList;

        /**
         * 退货确认状态
         * 来源：http://172.16.100.11:3002/project/92/interface/api/7186
         * 入参：key=poReturnConfirmStatus
         */
        private String confirmStatus;

        /**
         * 退货日期
         */
        private List<LocalDate> billDateList;

        /**
         * 退货原因
         */
        private String returnRemark;

        /**
         * 退货单来源类型
         * 来源：http://172.16.100.11:3002/project/92/interface/api/7186
         * 入参：key=returnOrderSource
         */
        private String returnOrderSource;

        /**
         * 异常分类
         * 来源：http://172.16.100.11:3002/project/92/interface/api/7186
         * 入参：key=poReturnUnusualType
         */
        private String unusualType;

        /**
         * 异常说明
         */
        private String unusualRemark;

        /**
         * 备注
         */
        private String remark;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

    }


    /**
     * SRM供应商退货列表tab
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierTabListDTO {
        /**
         * tab页状态
         * 来源：http://172.16.100.11:3002/project/92/interface/api/7186
         * 入参：key=poReturnConfirmStatus
         */
        private String tabFlag;

        /**
         * 数量
         */
        private Integer count;
    }

    /**
     * 异常反馈入参
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnusualFeedbackParamDTO {
        /**
         * 单据id
         */
        private String id;

        /**
         * 异常分类
         * 来源：http://172.16.100.11:3002/project/92/interface/api/7186
         * 入参：key=poReturnUnusualType
         */
        private String unusualType;

        /**
         * 异常反馈描述
         */
        private String unusualRemark;

        /**
         * 附件集合
         */
        private List<AttachDTO> attachList;
    }


    /**
     * 查询异常处理人下拉
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnusualHandleUserOptionDTO {
        /**
         * 编号
         */
        private String id;
        /**
         * 名称
         */
        private String name;
    }

    /**
     * 异常反馈详情
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnusualFeedbackView {
        /**
         * 单据id
         */
        private String id;

        /**
         * 异常分类
         * 来源：http://172.16.100.11:3002/project/92/interface/api/7186
         * 入参：key=poReturnUnusualType
         */
        private String unusualType;

        /**
         * 异常分类中文
         */
        private String unusualTypeName;

        /**
         * 异常反馈描述
         */
        private String unusualRemark;

        /**
         * 附件集合
         */
        private List<AttachDTO> attachList;
    }

    /**
     * 委外订单
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubcontractOrderDTO {
        /**
         * 采购订单id
         */
        private String purchaseId;
        /**
         * 采购订单编码
         */
        private String purchaseCode;
        /**
         * 退货单id
         */
        private String returnId;
        /**
         * 退货单编码
         */
        private String returnCode;
        /**
         * 委外订单id
         */
        private String subcontractId;
        /**
         * 委外订单编码
         */
        private String subcontractCode;
        /**
         * 供应商id
         */
        private String supplierId;
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
        private String skuId;
        private String skuNo;
        /**
         * 产品名称
         */
        private String productName;
    }
}
