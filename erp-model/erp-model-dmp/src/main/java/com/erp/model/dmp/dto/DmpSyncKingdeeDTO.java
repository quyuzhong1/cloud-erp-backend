package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Will
 * @version 1.0
 * @description: 同步金蝶dto
 * @date 2023/10/17 17:40
 */
@Data
@NoArgsConstructor
public class DmpSyncKingdeeDTO {

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ParamDTO {

        /**
         * 业务对象表单Id,FormId
         */
        private String formId;

        /**
         * 需查询的字段key集合，字符串类型，格式："key1,key2,...",FieldKeys
         */
        private String fieldKeys;

        /**
         * 过滤条件,拼接录入
         */
        private String filterString;

        /**
         * 排序字段，字符串类型,OrderString
         */
        private String orderString;

        /**
         * 返回总行数，整型,TopRowCount
         */
        private Integer topRowCount;

        /**
         * 开始行索引，整型,startRow
         */
        private Integer startRow;

        /**
         * 最大行数，整型,Limit
         */
        private Integer limit;

        /**
         * 表单所在的子系统内码，字符串类型,SubSystemId
         */
        private String subSystemId;
    }
}
