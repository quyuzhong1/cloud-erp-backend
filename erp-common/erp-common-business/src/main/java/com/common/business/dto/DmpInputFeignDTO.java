package com.common.business.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;


@Data
@NoArgsConstructor
public class DmpInputFeignDTO implements Serializable {

    /**
     * 反审核
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CfgOptionDTO{
        /**
         * 平台,system
         */
        @NotBlank(message = "平台不能为空")
        private String system;

        /**
         * DmpCfgInput中code
         */
        @NotBlank(message = "明细表编码不能为空")
        private String code;

        /**
         * 下一层级id
         */
        @NotBlank(message = "下一层级id不能为空")
        private String nextLevelId;

        /**
         * 原始下层级id
         */
        private String oldNextLevelId;

        /**
         * 操作，OperationTypeEnum
         */
        @NotBlank(message = "操作类型不能为空")
        private String option;
    }


}