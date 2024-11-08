package com.erp.model.wms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
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
import java.util.Map;

/**
 * @author Will
 * @version 1.0
 * @date 2023/4/10 11:11
 */
@Data
@NoArgsConstructor
public class PoInstockDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 明细主键id
         */
        private String detailId;
        /**
         * 入库单号
         */
        private String code;

        /**
         * 采购订单明细id
         */
        private String purchaseOrderDetailId;

        /**
         * 采购单id
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
         * skuid
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
         * 交货仓库Id
         */
        private String deliveryWarehouseId;

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
        private String stockInUserName;

        /**
         * 单价=含税单价/（1+税率）
         */
        private BigDecimal price;

        /**
         * 含税单价
         */
        private BigDecimal taxPrice;

        /**
         * 金额=未税价格*实收数量
         */
        private BigDecimal amount;


        /**
         * 价税合计=含税单价*实收数量
         */
        private BigDecimal taxAmount;


        /**
         * 税率
         */
        private BigDecimal taxRate;

        /**
         * 税率字符穿
         */
        private String taxRateStr;

        /**
         * 币别
         */
        private String currency;


        /**
         * 币别符号
         */
        private String currencySymbol;

        /**
         * 备注
         */
        private String remark;

        /**
         * 审核人名称
         */
        private String approveUserName;

        /**
         * 审核完成时间
         */
        private LocalDateTime approveTime;


        /**
         * 创建人名称
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
         * 采购订单类型(CGDD01_SYS标准采购订单，CGDD02_SYS委外采购订单，CGDD06-SYS补货采购订单)
         * 地址：/scm/dict/list 字典类型：purchaseOrderType
         */
        private String purchaseType;
        /**
         * 采购订单类型名称
         */
        private String purchaseTypeName;

        /**
         * 仓位
         */
        private String warehouseLocation;

        /**
         * 仓位名称
         */
        private String warehouseLocationName;
        /**
         * 采购订单-来源单id
         */
        private String sourceId;
        /**
         * 采购订单-来源单类型
         */
        private String sourceType;
        /**
         * 退货方式
         */
        private String returnMode;
        /**
         * 退货方式名称
         */
        private String returnModeName;

    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PagingTotalDTO {

        /**
         * 入库数量
         */
        private Integer totalQty;
    }

    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

        /**
         * 主键ids
         */
        private List<String> ids;

        /**
         * 入库单号
         */
        private String code;

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
         * 采购订单类型(CGDD01_SYS标准采购订单，CGDD02_SYS委外采购订单，CGDD06-SYS补货采购订单)
         * 字典类型：purchaseOrderType
         */
        private List<String> purchaseTypeList;

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
    public static class ExportParamDTO extends SearchParamDTO {
        /**
         * 是否有字段权限
         */
        private Boolean isHaveFieldPower;
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
    public static class CommonDTO {

        /**
         * 采购订单id
         */
        @NotBlank(message = "采购订单id不能为空")
        private String purchaseOrderId;

        /**
         * 入库员id
         */
        private String stockInUserId;

        /**
         * 交货仓库id
         */
        @NotBlank(message = "交货仓库不能为空")
        private String deliveryWarehouseId;

        /**
         * 入库部门id
         */
        private String stockInDeptId;

        /**
         * 入库日期
         */
        private LocalDate stockInDate;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 来源主键id
         */
        @NotBlank(message = "来源id不能为空")
        private String sourceId;

        /**
         * 来源 purchaseOrder采购订单
         */
        @NotBlank(message = "来源类型不能为空")
        private String sourceType;

        /**
         * 是否是自动入库
         */
        private Boolean isAutoInstock;

        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空")
        @Valid
        private List<PoInstockDetailDTO.AddDTO> details;
    }


    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;
        /**
         * 来源主键id
         */
        private String sourceId;
        /**
         * 明细
         */
        @NotEmpty(message = "明细不能为空")
        @Valid
        private List<PoInstockDetailDTO.UpdateDTO> details;
    }

    @Data
    @NoArgsConstructor
    public static class SupplierDTO {
        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商
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
        private String code;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 审核状态
         */
        private String approveStatus;

        /**
         * 审核状态名称
         */
        private String approveStatusName;

        /**
         * 入库日期
         */
        private LocalDate stockInDate;

        /**
         * 采购员id
         */
        private String purchaseUserId;

        /**
         * 采购员
         */
        private String purchaseUserName;

        /**
         * 收料组织id
         */
        private String receiveOrgId;

        /**
         * 收料组织
         */
        private String receiveOrgName;

        /**
         * 采购部门id
         */
        private String purchaseDeptId;

        /**
         * 采购部门
         */
        private String purchaseDeptName;

        /**
         * 仓管员
         */
        private String stockInUserName;

        /**
         * 交货仓库
         */
        private String deliveryWarehouseName;

        /**
         * 入库部门
         */
        private String stockInDeptName;

        /**
         * 新品首批（false否,true是）
         */
        private Boolean isFirstMassProduct;

        /**
         * 供应商信息
         */
        private SupplierDTO supplierDTO;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 采购单号
         */
        private String purchaseOrderCode;

        /**
         * 创建人
         */
        private LocalDateTime createTime;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 审核时间
         */
        private LocalDateTime approveTime;

        /**
         * 审核人
         */
        private String approveUserName;

        /**
         * 采购组织（采购订单组织）
         */
        private String purchaseOrgName;

        /**
         * 明细
         */
        private List<PoInstockDetailDTO.ViewDTO> details;
    }


    @Data
    @NoArgsConstructor
    public static class ListGeneratePurchaseReturnOrderDTO {

        @NotEmpty(message = "新增采购退货单数据不不能为空")
        @Valid
        private List<GeneratePurchaseReturnOrderDTO> list;
    }

    @Data
    @NoArgsConstructor
    public static class GeneratePurchaseReturnOrderDTO {

        /**
         * 来源类型
         */
        @NotBlank(message = "来源类型不能为空")
        private String sourceType;

        /**
         * 来源id
         */
        @NotBlank(message = "来源id不能为空")
        private String sourceId;

        /**
         * 来源明细id
         */
        @NotBlank(message = "来源明细id不能为空")
        private String sourceDetailId;

        /**
         * 采购订单id
         */
        private String purchaseOrderId;

        /**
         * 采购订单明细id
         */
        private String purchaseOrderDetailId;


        /**
         * 库位
         */
        private String warehouseLocation;

        /**
         * 采购订单单号
         */
        private String purchaseOrderCode;

        /**
         * sku编码
         */
        private String skuNo;

        /**
         * skuId
         */
        private String skuId;

        /**
         * 退货人id
         */
        private String returnUserId;

        /**
         * 退货方式
         */
        private String returnMode;
        /**
         * 入库数量
         */
        private Integer stockInQty;
        /**
         * 收货数量
         */
        private Integer receiveQty;
        /**
         * 实退数量
         */
        @Min(value = 1, message = "实退数量最小值为1")
        @Max(value = 999999999, message = "实退数量最大值为999999999")
        private Integer realityReturnQty;

        /**
         * 补货数量
         */
        @Min(value = 1, message = "补货数量最小值为1")
        @Max(value = 99999999, message = "补货数量最大值为99999999")
        private Integer replenishQty;

        /**
         * 扣款数量
         */
        @Min(value = 1, message = "扣款数量最小值为1")
        @Max(value = 99999999, message = "扣款数量最大值为99999999")
        private Integer deductAmountQty;

        /**
         * 含税单价
         */
        @Digits(integer = 16, fraction = 4, message = "含税单价最大16字符，小数位不能大于4个字符")
        private BigDecimal taxPrice;

        /**
         * 备注
         */
        @Size(max = 255, message = "备注不能大于255字符")
        private String remark;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币别符号
         */
        private String currencySymbol;

        /**
         * 采购员id
         */
        private String purchaseUserId;
    }

    /**
     * 获取签收数量
     */
    @Data
    @NoArgsConstructor
    public static class GetStockInQty {
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
         * 入库数量
         */
        private Integer stockInQty;

    }

    @Data
    @NoArgsConstructor
    public static class OrderRefStockInDTO {
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
         * 作废状态
         */
        private Boolean invalidStatus;

        /**
         * 作废状态
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
         * 入库日期
         */
        private LocalDate stockInDate;

        /**
         * 入库数量
         */
        private Integer stockInQty;

        /**
         * 超收数量
         */
        private Integer exceedQty;

        /**
         * 交货仓库
         */
        private String deliveryWarehouseName;

        /**
         * 采购员名称
         */
        private String purchaseUserName;

        /**
         * 入库员名称
         */
        private String stockInUserName;

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
     * 供应商、单据日期 获取入库批次和入库数量 查询条件
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SupplierInstockParamDTO {

        /**
         * 供应商id集合
         */
        private List<String> supplierIds;

        /**
         * 单据日期范围
         */
        private List<LocalDate> dateList;

    }

    /**
     * 供应商 入库批次和入库数量
     */
    @Data
    @NoArgsConstructor
    public static class SupplierInstockInfoDTO {

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 入库批次
         */
        private Integer instockCount;

        /**
         * 已入库量
         */
        private Integer instockQty;

    }

    /**
     * PDA:列表查询
     */
    @Data
    @NoArgsConstructor
    public static class PdaPagingView {

        /**
         * 主键id
         */
        private String id;

        /**
         * 入库单号
         */
        private String code;

        /**
         * 来源id
         */
        private String sourceId;

        /**
         * 入库单号
         */
        private String sourceType;

        /**
         * 采购单id
         */
        private String purchaseOrderId;

        /**
         * 供应商名称
         */
        private String supplierName;

        /**
         * 交货仓库
         */
        private String deliveryWarehouseName;

        /**
         * 审核状态
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
         * 质检状态名
         */
        private String qcStatusName;

        /**
         * 产品数量
         */
        private Integer detailCount;

        /**
         * 委外标识
         */
        private String subcontractType;

        /**
         * 产品信息
         */
        private List<PdaItemDTO> itemList;

    }

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
         * 实收数量
         */
        private Integer stockInQty;
    }

    /**
     * PDA:列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PdaSearchParamDTO extends SortDTO {
        /**
         * 单据编号
         */
        private String code;
        /**
         * 审核状态集合
         */
        private List<String> approveStatusList;

        /**
         * 入库日期集合
         */
        private List<LocalDate> stockInDateList;
    }

    /**
     * PDA:列表状态
     *
     * @Author Luo_WG
     * @Date 2023/8/11 9:15
     **/
    @Data
    @NoArgsConstructor
    public static class PdaPoInStockCountDTO {
        /**
         * 类型(waitSubmitAndReject 待提交/审核不通过，approveIng 审核中，approve 已审核)
         */
        private String tabFlag;
        /**
         * 数量
         */
        private Integer count;
    }
}
