package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author Lambda
 * @Classname DeptKingdeeFTO
 * @Date 2023-07-07 1:50
 * @Created by yl
 */
public class DeptKingdeeDTO {

    private DeptKingdeeDTO() {
    }

    @Data
    @NoArgsConstructor
    public static class FindDeptKingdeeDTO{

        /**
         * 组织id
         */
        private String orgId;
        /**
         * 组织编码
         */
        private String orgCode;
        /**
         * 部门id
         */
        @NotBlank(message = "部门id不能为空")
        private String deptId;

    }
}
