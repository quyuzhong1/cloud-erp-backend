package com.erp.model.plm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.*;

import com.common.business.dto.AdvanceQueryDTO;
import org.python.antlr.ast.Str;

import java.util.Map;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author wtr
 * @since 2025-10-16
*/
@Data
@NoArgsConstructor
public class AssetPurchaseOrderDTO implements Serializable {


     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {

         /**
         * 类型
         */
         private String tabFlag;

         /**
          * 类型名称
          */
         private String tabFlagName;

         /**
         * 数量
         */
         private Integer count;

     }
     /**
     * 分页列表查询参数
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
        private Map<String,String> sqlMap;

     }
    /**
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
         * 明细id
         */
        private String  detailId;

        /**
        * 资产采购单号
        */
        private String code;

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
        * 单据状态
        */
        private String approveStatus;

        /**
        * 合同盖章状态：waitSubmit=待申请,approveIng=已申请,approve=已完成,reject=未完成
        */
        private String contractStampStatus;

        /**
         * 合同盖章状态：waitSubmit=待申请,approveIng=已申请,approve=已完成,reject=未完成
         */
        private String contractStampStatusName;

        /**
        * 单据类型
        */
        private String orderType;

        /**
        * 来源订单id
        */
        private String sourceId;

        /**
        * 来源订单号
        */
        private String sourceCode;

        /**
        * 来源订单类型
        */
        private String sourceType;

        /**
        * 采购日期
        */
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
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 作废原因
        */
        private String invalidRemark;


        /**
        * 审核状态名称
        */
        private String approveStatusName;

        /**
        * 作废状态名称
        */
        private String invalidStatusName;

        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;

        /**
         * 单据类型名称
         */
        private String orderTypeName;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 结束验收AssetPurchaseOrderReceiveEnum
         */
        private String endReceive;

        /**
         * 结束验收名称AssetPurchaseOrderReceiveEnum
         */
        private String endReceiveName;

        /**
         * 结束验收时间
         */
        private LocalDateTime endReceiveTime;

        /**
         * skuId
         */
        private String assetId;

        /**
         * sku编码
         */
        private String assetCode;

        /**
         * 产品名称
         */
        private String assetName;

        /**
         * 计划交期
         */
        private LocalDate planDeliveryDate;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 采购数量
         */
        private BigDecimal purchaseQty;

        /**
         * 价税合计
         */
        private BigDecimal totalAmount;

        /**
         * 待验收数量
         */
        private BigDecimal unAcceptQty;

        /**
         * 已验收数量
         */
        private BigDecimal acceptQty;

        /**
         * 备注
         */
        private String remark;
    }

    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
    }

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 资产采购单号
        */
        private String code;

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
        private LocalDateTime approveUserTime;

        /**
        * 单据状态
        */
        private String approveStatus;

        /**
         * 单据状态名称
         */
        private String approveStatusName;

        /**
        * 合同盖章状态：waitSubmit=待申请,approveIng=已申请,approve=已完成,reject=未完成
        */
        private String contractStampStatus;

        /**
        * 单据类型
        */
        private String orderType;

        /**
        * 来源订单id
        */
        private String sourceId;

        /**
        * 来源订单号
        */
        private String sourceCode;

        /**
        * 来源订单类型
        */
        private String sourceType;

        /**
        * 采购日期
        */
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
        * 作废状态（false未作废，true已作废）
        */
        private Boolean invalidStatus;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;

        /**
        * 作废原因
        */
        private String invalidRemark;

        /**
         * 供应商信息
         */
        private AssetPurchaseOrderSupplierDTO.ViewDTO assetPurchaseOrderSupplierDTO;

        /**
         * 产品明细
         */
        private List<AssetPurchaseOrderDetailDTO.ViewDTO>  assetPurchaseOrderDetailDTOList;
    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 供应商信息
         */
        @Valid
        @NotNull(message = "供应商信息不能为空")
        private AssetPurchaseOrderSupplierDTO.AddDTO assetPurchaseOrderSupplierDTO;

        /**
         *
         */
        @Valid
        @NotNull(message = "资产采购订单明细信息不能为空")
        private List<AssetPurchaseOrderDetailDTO.AddDTO> assetPurchaseOrderDetailDTO;
    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 供应商信息
         */
        @Valid
        @NotNull(message = "供应商信息不能为空")
        private AssetPurchaseOrderSupplierDTO.UpdateDTO assetPurchaseOrderSupplierDTO;

        /**
         *
         */
        @Valid
        @NotNull(message = "资产采购订单明细信息不能为空")
        private List<AssetPurchaseOrderDetailDTO.UpdateDTO> assetPurchaseOrderDetailDTOList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 审核时间
        */
        private LocalDateTime approveUserTime;

        /**
        * 合同盖章状态：waitSubmit=待申请,approveIng=已申请,approve=已完成,reject=未完成
        */
        private String contractStampStatus;

        /**
        * 单据类型
        */
        @NotBlank(message = "单据类型不能为空")
        private String orderType;

        /**
        * 来源订单id
        */
        private String sourceId;

        /**
        * 来源订单号
        */
        private String sourceCode;

        /**
        * 来源订单类型
        */
        private String sourceType;

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
        @NotBlank(message = "采购组织id不能为空")
        private String purchaseOrgId;

        /**
        * 采购组织名称
        */
        @NotBlank(message = "采购组织名称不能为空")
        private String purchaseOrgName;

        /**
        * 作废时间
        */
        private LocalDateTime invalidTime;


    }

    @Data
    @NoArgsConstructor
    public static class ImportDTO {
        /**
         * 成功返回数据
         */
        private List<AssetPurchaseOrderDetailDTO.MoldImportDTO> successList;

        /**
         * 错误url
         */
        private String errorUrl;
    }

    /**
     * 下拉选择DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 资产采购单号
         */
        private String code;

        /**
         * 供应商ID
         */
        private String supplierId;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 采购日期
         */
        private LocalDate purchaseDate;

        /**
         * 采购员名称
         */
        private String purchaseUserName;

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
     * 下拉选择查询参数
     */
    @Data
    @NoArgsConstructor
    public static class SelectParamDTO {
        /**
         * 关键字（支持code和supplierName模糊查询）
         */
        private String keyword;

    }

    /**
     * 资产采购订单明细（用于资产验收单添加明细）
     */
    @Data
    @NoArgsConstructor
    public static class DetailForAcceptDTO {

        /**
         * 采购订单明细ID
         */
        private String id;

        /**
         * SKU ID
         */
        private String skuId;

        /**
         * SKU编码
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
         * 待验收数量
         */
        private Integer pendingAcceptQty;

        /**
         * 已验收数量
         */
        private Integer acceptedQty;

        /**
         * 可验收数量
         */
        private Integer availableAcceptQty;

        /**
         * 是否加急
         */
        private Boolean isUrgent;

        /**
         * 备注
         */
        private String remark;

        /**
         * 模具编码
         */
        private String moldCode;

        /**
         * 模具名称
         */
        private String moldName;
    }


    @Data
    @NoArgsConstructor
    public static class ContractStampStatusParamsDTO extends BaseIdsDTO.IdsDTO{
        /**
         * 合同盖章状态
         */
        @NotBlank(message = "合同盖章状态不能为空")
        private String contractStampStatus;
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
        private BigDecimal sumQty;

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
        private BigDecimal qty;
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
        private BigDecimal totalAmount;
        /**
         * 含税金额
         */
        private BigDecimal taxAmount;
        /**
         * 备注
         */
        private String remark;
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
         * 明细信息
         */
        private List<AssetPurchaseOrderDetailDTO.ExportPdfDTO> details;
    }

}