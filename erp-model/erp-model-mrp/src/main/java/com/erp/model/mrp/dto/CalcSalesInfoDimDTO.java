package com.erp.model.mrp.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.mrp.entity.CfgRuleSalesDenoisingCalcEntity;
import com.erp.model.mrp.entity.CfgRuleSalesFormulaCalcEntity;
import com.erp.model.mrp.enums.TimePeriodEnum;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 销量试算表请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
 */
@Data
@NoArgsConstructor
public class CalcSalesInfoDimDTO implements Serializable {


    /**
     * 高级查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

        /**
         * 试算模板id
         */
        private String cfgRuleCalcId;

    }


    @Getter
    @Setter
    public static class PagingView {
        /**
         * 主表id
         */
        private String id;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku
         */
        private String skuNo;
        /**
         * sku图片
         */
        private String skuImgUrl;
        /**
         * 品名
         */
        private String productName;
        /**
         * 国家
         */
        private String country;
        /**
         * 国家名字
         */
        private String countryName;
        /**
         * 国家图片
         */
        private String countryImgUrl;
        /**
         * 店铺
         */
        private String shopId;
        /**
         * 店铺名字
         */
        private String shopName;
        /**
         * 平台
         */
        private String platform;

        /**
         * 销量分析
         */
        private SalesEstimateVO salesEstimateVO;
        /**
         * 分时段销量
         */
        private String salesQtyJson;
        /**
         * 分时段销量
         */
        private List<SalesVO> salesQtyList;
        /**
         * 分时段日均销
         */
        private String avgSalesQtyJson;
        /**
         * 分时段日均销
         */
        private List<SalesVO> avgSalesQtyList;
        /**
         * 预估销量
         */
        private String monthSalesEstimateQtyJson;
        /**
         * 预估销量
         */
        private List<MonthSalesVO> monthSalesEstimateQtyList;
        /**
         * 真实销量
         */
        private String monthRealSalesQtyJson;
        /**
         * 真实销量
         */
        private List<MonthSalesVO> monthRealSalesQtyList;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
        /**
         * 试算配置id
         */
        private String cfgRuleCalcId;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesEstimateVO {
        /**
         * 日期
         */
        private List<LocalDate> date;
        /**
         * 数量
         */
        private List<BigDecimal> qty;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalesVO {
        /**
         * 类型
         */
        private String type;
        /**
         * 数量
         */
        private BigDecimal qty;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthSalesVO {
        /**
         * 类型
         */
        private Integer month;
        /**
         * 数量
         */
        private BigDecimal qty;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku
         */
        private String skuNo;
        /**
         * sku图片
         */
        private String skuImgUrl;
        /**
         * 品名
         */
        private String productName;
        /**
         * 国家
         */
        private String country;
        /**
         * 国家名字
         */
        private String countryName;
        /**
         * 店铺
         */
        private String shopId;
        /**
         * 店铺名字
         */
        private String shopName;
        /**
         * 平台
         */
        private String platform;

        /**
         * 试算配置id
         */
        private String cfgRuleCalcId;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        /**
         * 备注
         */
        private String remark;

    }

    @Getter
    @Setter
    public static class CalcResultDTO {

        /**
         * sku_id
         */
        private String calcSalesInfoDimId;
        /**
         * 试算开始日期
         */
        private LocalDate startCalcDate;

        /**
         * 试算结束日期
         */
        private LocalDate endCalcDate;

        /**
         * 历史销量
         */
        private Map<LocalDate, Integer> salesHistoryMap;

        /**
         * 试算销量公式
         */
        private List<CfgRuleSalesFormulaCalcEntity> formulaCalcEntities;

        /**
         * 试算销量去噪信息
         */
        private List<CfgRuleSalesDenoisingCalcEntity> salesDenoising;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimePeriodSalesDTO {

        private TimePeriodEnum code;

        private BigDecimal qty;
    }


    @Getter
    @Setter
    public static class HistorySalesVO {

        /**
         * 日期
         */
        private List<LocalDate> dateList;
        /**
         * 历史销量数量
         */
        private List<Integer> historySalesList;

        /**
         * 去噪销量数量
         */
        private List<BigDecimal> denoisingSalesList;
    }

    @Getter
    @Setter
    public static class SalesEstimateDTO {
        /**
         * 日期
         */
        private List<LocalDate> dateList;
        /**
         * 预估销量数量
         */
        private List<Integer> salesEstimateList;

        /**
         * 真实销量数量
         */
        private List<BigDecimal> realSalesList;
    }

    @Getter
    @Setter
    public static class HistorySalesDTO {

        /**
         * id
         */
        private String id;
        /**
         * 开始日期
         */
        private LocalDate startDate;
        /**
         * 结束日期
         */
        private LocalDate endDate;
    }
}