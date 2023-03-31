package com.erp.model.scm.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Lambda
 * @Classname PurchasePriceDTO
 * @Description TODO
 * @Date 2023-03-16 14:54
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class PurchasePriceChangeDTO implements Serializable {


    /**
     * 添加采购价目变更
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO {

        /**
         * 供应商表id
         */
        @NotBlank(message = "供应商不能为空")
        private String supplierId;

        /**
         * 采购价目表id
         */
        @NotBlank(message = "采购价目表id 不能为空")
        private String purchasePriceId;

        /**
         * 调价日期
         */
        @NotNull(message = "调价日期不能为空")
        private LocalDate adjustDate;

        /**
         * 调价人id
         */
        private String adjustUserId;


        /**
         * 原因
         */
        private String reason;


        /**
         * 采购组织
         */
        @NotBlank(message = "采购组织不能为空")
        private String purchaseOrgId;


        /**
         * 资质附件url
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;

        /**
         * 报价明细
         */
        private List<PurchasePriceChangeDetailDTO.AddDTO> purchasePriceChangeDetailList;


    }


    /**
     * 修改采购价目变更
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {



        @NotBlank(message = "id不能为空")
        private String id;


        /**
         * 供应商表id
         */
        @NotBlank(message = "供应商不能为空")
        private String supplierId;

        /**
         * 采购价目表id
         */
        @NotBlank(message = "采购价目表id 不能为空")
        private String purchasePriceId;

        /**
         * 调价日期
         */
        @NotNull(message = "调价日期不能为空")
        private LocalDate adjustDate;

        /**
         * 调价人id
         */
        private String adjustUserId;


        /**
         * 采购组织
         */
        @NotBlank(message = "采购组织不能为空")
        private String purchaseOrgId;


        /**
         * 资质附件url
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;

        /**
         * 报价明细
         */
        private List<PurchasePriceChangeDetailDTO.UpdateDTO> purchasePriceChangeDetailList;

    }



    /**
     * 修改采购价目详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {



        @NotBlank(message = "id不能为空")
        private String id;


        /**
         * 供应商表id
         */
        @NotBlank(message = "供应商不能为空")
        private String supplierId;

        /**
         * 采购价目表id
         */
        @NotBlank(message = "采购价目表id 不能为空")
        private String purchasePriceId;

        /**
         * 调价日期
         */
        @NotNull(message = "调价日期不能为空")
        private LocalDate adjustDate;

        /**
         * 调价人id
         */
        private String adjustUserId;


        /**
         * 采购组织
         */
        @NotBlank(message = "采购组织不能为空")
        private String purchaseOrgId;


        /**
         * 资质附件url
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;

        /**
         * 报价明细
         */
        private List<PurchasePriceChangeDetailDTO.ViewDTO> purchasePriceChangeDetailList;

    }


    /**
     * 采购价目变更分页
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {

        /**
         * 表id
         */
        private String id;


        /**
         * code
         */
        private String code;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 供应商名
         */
        private String supplierName;


        /**
         * sku id
         */
        private String skuId;


        /**
         * sku no
         */
        private String skuNo;


        /**
         * 产品名称
         */
        private String productName;

        /**
         * 最小数量
         */
        private Integer minQty;


        /**
         * 最大数量
         */
        private Integer maxQty;

        /**
         * 单据状态名
         */
        private String approveStatusName;

        /**
         * 单据状态code
         */
        private String approveStatusCode;

        /**
         * 单据状态
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 币种
         */
        private String currency;


        /**
         * 币种 符号
         */
        private String currencySymbol;

        /**
         *  禁用启用状态
         *  true 禁用
         *  fase 启用
         */
        private Boolean disabled;


        /**
         * 含税单价
         */
        private BigDecimal taxPrice;


        /**
         * 税率
         */
        private BigDecimal taxRate;


        /**
         * 生效时间
         */
        private LocalDate effectiveDate;


        /**
         * 采购组织
         */
        private String purchaseOrgId;



        /**
         * 采购组织名
         */
        private String purchaseOrgName;

        /**
         * 创建人名称
         */
        private String createUserName;


        /**
         * 创建时间
         */
        private LocalDateTime createTime;



    }

    /**
     * 分页参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO  extends SortDTO {

        /**
         * 产品名称
         */
        private String productName;


        /**
         * sku id 集合
         */
        private List<String> skuIdList;


        /**
         * 采购组织id集合
         */
        private List<String> purchaseOrgIdList;


        /**
         * 单据状态
         */
        private List<String> approveStatusList;

        /**
         * 生效时间
         */
        private List<LocalDate> effectiveDateList;

        /**
         * 生效时间
         */
        private List<LocalDate> expireDateList;

        /**
         * 创建时间
         */
        private List<LocalDate> createTimeList;


        /**
         * 创建人id 集合
         */
        private List<String> createUserIdList;
    }
}
