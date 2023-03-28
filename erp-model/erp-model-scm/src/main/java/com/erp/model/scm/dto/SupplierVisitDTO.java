package com.erp.model.scm.dto;

import com.common.core.anno.StateEnumValue;
import com.erp.model.scm.enums.SupplierVisitEnum;
import com.erp.model.scm.enums.SupplierVisitResultEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * @author Lambda
 * @Classname SupplierVisitDTO
 * @Description TODO
 * @Date 2023-03-16 14:31
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierVisitDTO implements Serializable {


    /**
     * 供应商拜访信息
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class AddDTO {

        /**
         * 供应商id
         */
        @NotBlank(message = "供应商不能为空")
        private String supplierId;

        /**
         * 类型
         */
        @NotBlank(message = "供应商拜访类型不能为空")
        @StateEnumValue(strValues = {"newProduct", "access", "other"}, message = "拜访类型有误")
        private String visitType;

        /**
         * 拜访时间
         */
        @NotNull(message = "拜访时间不能为空")
        private LocalDate visitTime;

        /**
         * 拜访人 集合
         */
        @NotNull(message = "拜访人不能为空")
        @Size(min = 1, message = "拜访人不能为空")
        private List<String> peopleList;

        /**
         * 内容
         */
        private String content;


        /**
         * 结果
         */
        @NotBlank(message = "拜访结果不能为空")
        @StateEnumValue(strValues = {"conformity", "nonconformity", "pending"}, message = "拜访类型有误")
        private String result;

        /**
         * 附件url
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;


        /**
         * 物料sku 集合
         */
        private List<String> skuIdList;

    }


    /**
     * 供应商拜访分页信息
     */
    @Data
    @NoArgsConstructor
    @Valid
    public static class PagingViewDTO {
        /**
         * 表id
         */
        private String id;


        /**
         * 表id
         */
        private SupplierVisitEnum visitType;


        /**
         * 类型名称
         */
        private String visitTypeName;

        /**
         * 拜访时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate visitTime;

        /**
         * 拜访人
         */
        private String people;

        /**
         * 拜访人
         */
        private String peopleName;


        /**
         * 物料信息
         */
        private String skuInfo;

        /**
         * 结果
         */
        private SupplierVisitResultEnum result;

        /**
         * 结果
         */
        private String resultName;

        /**
         * 内容
         */
        private String content;

        /**
         * 附件地址
         */
        private List<String> attachmentUrlList;

        /**
         * 附件名
         */
        private List<String> attachmentNameList;
    }


}
