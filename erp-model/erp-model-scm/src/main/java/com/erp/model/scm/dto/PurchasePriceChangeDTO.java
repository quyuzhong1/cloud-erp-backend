package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

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
public class PurchasePriceChangeDTO implements Serializable {


    /**
     * 添加采购价目变更
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
        private List<PurchasePriceChangeDetailDTO.AddDTO> purchasePriceChangeDetailList;


    }



















}
