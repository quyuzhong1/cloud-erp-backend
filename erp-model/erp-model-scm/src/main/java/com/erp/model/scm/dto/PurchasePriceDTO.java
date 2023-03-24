package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
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
    public static class AddDTO{
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
        private List<String>  attachmentUrlList;
        /**
         * 报价明细
         */
        @Valid
        private List<PurchasePriceDetailDTO.AddDTO> purchasePriceDetailList;

    }
















}
