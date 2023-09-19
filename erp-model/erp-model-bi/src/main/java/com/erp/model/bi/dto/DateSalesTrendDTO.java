package com.erp.model.bi.dto;

import com.common.core.anno.StateEnumValue;
import com.erp.model.bi.enums.DateSalesTrendSearchTypeEnum;
import com.erp.model.bi.enums.MetricsEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
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
         * 描述：salesAmount 销售额，
         *  salesQuantity 销量，
         *  financeSalesQuantity 财务销售额，
         *  salesPrice 客单价
         */
        @NotBlank(message = "搜索类型不能为空")
        @StateEnumValue(clazz = DateSalesTrendSearchTypeEnum.class, message = "搜索类型有误")
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
