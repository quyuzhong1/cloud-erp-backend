package com.erp.model.bi.dto;

import com.common.core.anno.StateEnumValue;
import com.erp.model.bi.enums.DateSalesTrendSearchTypeEnum;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 人员/部门完成率排行榜
 */
public class CompletionRateRankingDTO implements Serializable {
    /**
     * 查询参数
     **/
    @Data
    public static class SearchDTO extends BiFilterDTO {
        /**
         * 搜索类型：/bi/common/enumDropDown?type=TargetMetricsSearchType
         * 描述：dept 事业部，
         *  user 人员，
         */
        private String searchType;

        /**
         * 统计维度：/bi/common/enumDropDown?type=CompletionRateRanking
         * 描述：salesAmount 销售额完成率，
         *  financeSalesAmount 财务销售额完成率，
         */
        private String completionRateRankingType;

    }

    /**
     * 列表信息
     **/
    @Data
    public static class PagingDTO {
        /**
         * 排行
         */
        private Integer ranking;

        /**
         * 用户/部门名称
         */
        private String name;

        /**
         * 月销售额
         */
        private BigDecimal monthSales;

        /**
         * 本月完成率
         */
        private BigDecimal monthCompletionRate;

        /**
         * 上月排行
         */
        private Integer lastMonthRanking;

        public PagingDTO() {
            this.ranking = 0;
            this.name = "";
            this.monthSales = BigDecimal.ZERO;
            this.monthCompletionRate = BigDecimal.ZERO;
            this.lastMonthRanking = 0;
        }
    }
}
