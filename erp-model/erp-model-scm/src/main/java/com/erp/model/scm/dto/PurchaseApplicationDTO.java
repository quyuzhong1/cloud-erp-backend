package com.erp.model.scm.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.anno.StateEnumValue;
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
 * @description: TODO
 * @date 2023/3/15 18:05
 */
@Data
@NoArgsConstructor
public class PurchaseApplicationDTO implements Serializable {


    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主键id
         */
        private String id;

        /**
         * 申请单明细id
         */
        private String  purchaseApplicationDetailId;

        /**
         * 申请单号
         */
        private String code;

        /**
         * 是否加急（false否，true是）
         */
        private Boolean isUrgent;

        /**
         * 单据状态
         */
        private String approveStatus;

        /**
         * 单据状态（waitSubmit待提交，approveIng审核中，reject审核不通过，approve已审核）
         */
        private String approveStatusName;

        /**
         * 流程id
         */
        private String processId;

        /**
         * 新品首批（false否,true是）
         */
        private Boolean isFirstMassProduct;

        /**
         * 采购单关联状态
         */
        private String createPoType;

        /**
         * 采购单关联状态（0未生成，1部分生成，2已生成)
         */
        private String createPoTypeName;

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
         * 计划交期
         */
        private String planDeliveryDate;

        /**
         * 申请数量
         */
        private Integer applyQty;

        /**
         * 实际采购数量
         */
        private Integer realPurchaseQty;

        /**
         * 签收数量
         */
        private Integer receiveQty;

        /**
         * 入库数量
         */
        private Integer stockInQty;

        /**
         * 目的仓库名称
         */
        private String destWarehouseName;

        /**
         * 备注
         */
        private String remark;

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
    }

    @Data
    @NoArgsConstructor
    public static class SearchParamDTO extends SortDTO {


        /**
         * 主键ids
         */
        private List<String> ids;

        /**
         * 采购申请单号
         */
        private String code;

        /**
         * 审核状态（waitSubmit待提交，approveIng审核中，reject审核不通过，approve已审核）
         */
        private List<String> approveStatusList;

        /**
         * 采购订单生成状态（0未生成，1部分生成，2已生成)
         */
        private List<String> createPoTypeList;

        /**
         * 新品首批（false否,true是）
         */
        private Boolean isFirstMassProduct;

        /**
         * sku编码
         */
        private List<String> skuNoList;

        /**
         * 产品名称
         */
        private String productName;

        /**
         * 是否加急（false否，true是）
         */
        private Boolean isUrgent;

        /**
         * 计划交期
         */
        private List<LocalDate> planDeliveryDateList;

        /**
         * 目的仓库id
         */
        private List<String> destWarehouseIdList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;

        /**
         * 审核时间开始
         */
        private List<LocalDate> approveTimeList;

        /**
         * 申请人id
         */
        private List<String> applyUserIdList;

        /**
         * 创建人id
         */
        private List<String> createUserIdList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {
        /**
         * 申请日期
         */
        @NotNull(message = "申请日期不能为空")
        private LocalDate applyDate;

        /**
         * 申请人id
         */
        private String applyUserId;

        /**
         * 申请人部门id
         */
        private String applyDeptId;

        /**
         * 新品首批（false否,true是）
         */
        @NotNull(message = "新品首批不能为空")
        private Boolean isFirstMassProduct;
    }


    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        /**
         * 采购申请明细
         */
        @Valid
        private List<PurchaseApplicationDetailDTO.AddDTO> details;
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
         * 采购申请明细
         */
        @Valid
        private List<PurchaseApplicationDetailDTO.UpdateDTO> details;

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

    }

    @Data
    @NoArgsConstructor
    public static class ViewGeneratePurchaseOrderDTO {

        /**
         * 采购申请id
         */
        private String id;

        /**
         * 采购申请明细id
         */
        private String purchaseApplicationDetailId;

        /**
         * 申请单号
         */
        private String code;

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
         * 变体信息
         */
        private String variantProperty;

        /**
         * 采购组织id
         */
        private String purchaseOrgId;

        /**
         * 采购组织
         */
        private String purchaseOrgName;

        /**
         * 收料组织id
         */
        private String receiveOrgId;

        /**
         * 收料组织
         */
        private String receiveOrgName;

        /**
         * 仓库id
         */
        private String destWarehouseId;

        /**
         * 仓库
         */
        private String destWarehouseName;

        /**
         * 最小起订量
         */
        private Integer moq;

        /**
         * 采购交期（天）
         */
        private Integer deliveryDay;

        /**
         * 申请数量
         */
        private Integer applyQty;

        /**
         * 已采购数量
         */
        private Integer purchasedQty;
    }

    @Data
    @NoArgsConstructor
    public static class GeneratePurchaseOrderDTO {

        /**
         * 采购申请单id
         */
        @NotBlank(message = "采购申请单id不能为空")
        private String id;

        /**
         * 采购申请单明细id
         */
        @NotBlank(message = "采购申请单明细id不能为空")
        private String purchaseApplicationDetailId;

        /**
         * 供应商id
         */
        @NotBlank(message = "供应商id不能为空")
        private String supplierId;

        /**
         * 币别
         */
        private String currency;

        /**
         * 币别符号
         */
        private String currencySymbol;

        /**
         * 采购数量
         */
        @NotNull(message = "采购数量不能为空")
        @DecimalMin(value = "1",message = "采购数量最小值为1")
        private Integer purchaseQty;

        /**
         * 含税单价
         */
        @Digits(integer = 16,fraction = 4,message = "含税单价最大16字符，小数位不能大于4个字符")
        private BigDecimal taxPrice;

        /**
         * 是否赠品
         */
        @NotNull(message = "是否赠品不能为空")
        private Boolean isGift;

        /**
         * skuId
         */
        @NotBlank(message = "skuId不能为空")
        private String skuId;

        /**
         * 收料组织id
         */
        @NotBlank(message = "收料组织id不能为空")
        private String receiveOrgId;

        /**
         * 采购组织id
         */
        @NotBlank(message = "采购组织id不能为空")
        private String purchaseOrgId;

        /**
         * 仓库id
         */
        @NotBlank(message = "仓库id不能为空")
        private String destWarehouseId;

        /**
         * 采购交期（天）
         */
        private Integer deliveryDay;

    }

    @Data
    @NoArgsConstructor
    public static class ListGeneratePurchaseOrderDTO {

        @NotEmpty(message = "采购订单明细不能为空")
        @Valid
        List<GeneratePurchaseOrderDTO> list;
    }

    @Data
    @NoArgsConstructor
    public static class ViewGenerateSubcontractOrderDTO {

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
         * 来源明细id
         */
        private String sourceDetailId;

        /**
         * 审核状态
         */
        private String approveStatus;

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
         * 采购组织id
         */
        private String purchaseOrgId;

        /**
         * 采购组织名称
         */
        private String purchaseOrgName;

        /**
         * 收料组织id
         */
        private String receiveOrgId;

        /**
         * 收料组织名称
         */
        private String receiveOrgName;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 仓库名称
         */
        private String warehouseName;

        /**
         * 可下推数量
         */
        private Integer toPushdownQty;

        /**
         * 采购数量
         */
        private Integer qty;

        /**
         * 领料数量
         */
        private Integer deliveryQty;

        /**
         * 子集
         */
        private List<ViewGenerateSubcontractOrderDTO> childList;
    }

    @Data
    @NoArgsConstructor
    public static class GenerateSubcontractOrderDTO {
        /**
         * 来源id
         */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;
        /**
         * 来源类型
         */
        @NotBlank(message = "来源类型不能为空")
        @StateEnumValue(clazz = SourceTypeEnum.class, message = "单据来源错误")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String sourceType;
        /**
         * 来源编码
         */
        @NotBlank(message = "来源编码不能为空")
        @Size(max = 50,message = "来源编码最大长度不能超过50位")
        private String sourceCode;

        /**
         * 来源明细id
         */
        @NotBlank(message = "来源明细id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceDetailId;

        /**
         * skuId
         */
        @NotBlank(message = "SKU不能为空")
        private String skuId;

        /**
         * 采购组织id
         */
        private String purchaseOrgId;

        /**
         * 收料组织id
         */
        private String receiveOrgId;

        /**
         * 仓库id
         */
        private String warehouseId;

        /**
         * 供应商id
         */
        @NotBlank(message = "供应商不能为空")
        private String supplierId;

        /**
         * 采购数量
         */
        @NotNull(message = "采购数量不能为空")
        @Min(value = 1,message = "采购数量最小值为1")
        @Max(value = 99999999,message = "采购数量最大值为99999999")
        private Integer qty;


        /**
         * 领料数量
         */
        @NotNull(message = "领料数量不能为空")
        @Min(value = 1,message = "领料数量最小值为1")
        @Max(value = 99999999,message = "领料数量最大值为99999999")
        private Integer deliveryQty;

        /**
         * 单价
         */
        private BigDecimal price;

        /**
         * 是否赠品
         */
        private Boolean isGift;

        /**
         * 备注
         */
        private String remark;

        /**
         * 是否自动生成采购订单
         */
        private Boolean isGeneratePo;

        /**
         * 子集
         */
        @NotEmpty(message = "子件SKU不能为空")
        private List<GenerateSubcontractOrderDTO> childList;
    }
}
