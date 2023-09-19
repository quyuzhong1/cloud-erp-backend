package com.erp.model.bi.dto;

import com.common.core.anno.StateEnumValue;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 日期销售趋势DTO
 */
public class DateSalesTrendDTO {

    /**
     * 查询参数
     **/
    @Data
    public static class SearchDTO extends BiFilterDTO {
        /**
         * 搜索类型：/bi/common/enumDropDown?type=DateSalesTrendSearchType
         * 描述：dept 事业部，
         *  user 人员，
         */
        private String searchType;

        /**
         * 时间类型：日: DAY; 周: WEEK; 月: MONTH; 季度: QUARTER; 年: YEAR
         */
        @StateEnumValue( strValues = {"DAY","MONTH","QUARTER","YEAR"} ,message = "时间类型有误")
        @NotNull(message = "时间类型不能为空")
        private String dateType;
    }

    /**
     * 列表信息
     **/
    @Data
    public static class PagingDTO {

    }
}
