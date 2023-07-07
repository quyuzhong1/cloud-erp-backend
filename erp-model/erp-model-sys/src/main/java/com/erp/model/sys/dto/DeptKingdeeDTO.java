package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Lambda
 * @Classname DeptKingdeeFTO
 * @Description TODO
 * @Date 2023-07-07 1:50
 * @Created by yl
 */
public class DeptKingdeeDTO {


    @Data
    @NoArgsConstructor
    public static class FindDeptKingdeeDTO{

        private String orgCode;

        private String deptId;

    }
}
