package com.erp.model.scm.dto;

import com.common.business.dto.base.SortDTO;
import com.common.business.enums.ApproveStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
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
public class PurchasePriceDTO implements Serializable {


    /**
     * 添加
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
         * 报价日期
         */
        @NotNull(message = "报价日期不能为空")
        private LocalDate quotedDate;


        /**
         * 报价人id
         */
        private String pricingUserId;


        /**
         * 采购组织id
         */
        private String purchaseOrgId;

        /**
         * 附件地址
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;
        /**
         * 报价明细
         */
        @Valid
        private List<PurchasePriceDetailDTO.AddDTO> purchasePriceDetailList;

    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 表id
         */
        private String id;
        /**
         * 供应商表id
         */
        @NotBlank(message = "供应商不能为空")
        private String supplierId;

        /**
         * 报价日期
         */
        @NotNull(message = "报价日期不能为空")
        private LocalDate quotedDate;


        /**
         * 报价人id
         */
        private String pricingUserId;


        /**
         * 采购组织id
         */
        private String purchaseOrgId;

        /**
         * 附件地址
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名称
         */
        private List<String> attachmentNameList;


        /**
         * 报价明细
         */
        @Valid
        private List<PurchasePriceDetailDTO.UpdateDTO> purchasePriceDetailList;

    }


    /**
     * 分页信息
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO extends SortDTO {

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
         * 单据状态
         */
        private ApproveStatusEnum approveStatus;

        /**
         * 单据状态code
         */
        private String approveStatusCode;


        /**
         * 单据状态名
         */
        private String approveStatusName;



        /**
         * 最大数量
         */
        private Integer maxQty;

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


}
