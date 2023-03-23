package com.erp.model.scm.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;

/**
 * @author Lambda
 * @Classname SupplierPhaseDTO
 * @Description TODO
 * @Date 2023-03-16 12:10
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class SupplierPhaseDTO implements Serializable {


    /**
     * 添加阶段
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO{
        /**
         * 供应商表id
         */
        @NotBlank(message = "供应商id不能为空")
        private String supplierId;

        /**
         * 操作类型
         */
        @NotBlank(message = "操作类型不能为空")
        @StateEnumValue(strValues = {"upgrade","degrade"},message = "操作类型有误")
        private String type;

        /**
         * 当前阶段
         */
        private String currentPhase;


        /**
         * 目标阶段
         */
        @NotBlank(message = "目标阶段不能为空")
        private String targetPhase;

        /**
         * 说明
         */
        private String description;


        /**
         * 附件地址
         */
        private List<String>  attachmentUrlList;
    }














}
