package com.erp.model.mrp.dto;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.mrp.entity.CalcSalesInfoDenoisingEntity;
import com.erp.model.mrp.entity.CalcSalesInfoEstimateEntity;
import com.erp.model.mrp.entity.CfgRuleSalesDenoisingCalcEntity;
import com.erp.model.mrp.entity.CfgRuleSalesFormulaCalcEntity;
import com.erp.model.mrp.enums.*;
import lombok.*;
import org.springframework.util.ObjectUtils;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @Setter
    @Getter
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

    /**
     * 高级查询参数
     */
    @Getter
    @Setter
    public static class ParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;
        /**
         * 是否关注
         */
        private Boolean favorite;
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

        /**
         * MAPE
         */
        private BigDecimal mapeScore;
        /**
         * R2
         */
        private BigDecimal r2Score;
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

        /**
         * 试算模板名字
         */
        private String name;

        /**
         * 是否关注
         */
        private Boolean favorite;

        /**
         * 编码
         */
        private String code;
        /**
         * 状态
         */
        @Dict(enumClass = CalcStatusEnum.class)
        private String status;
    }

    @Getter
    @Setter
    public static class CalcResultDTO {

        /**
         * sku id
         */
        private String skuId;
        /**
         * 店铺
         */
        private String shopId;

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
        private List<BigDecimal> salesEstimateList;

        /**
         * 真实销量数量
         */
        private List<Integer> realSalesList;

        /**
         * MAE
         */
        private BigDecimal maeScore;
        /**
         * MSE
         */
        private BigDecimal mseScore;
        /**
         * RMSE
         */
        private BigDecimal rmseScore;
        /**
         * MAPE
         */
        private BigDecimal mapeScore;
        /**
         * R2
         */
        private BigDecimal r2Score;

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


    /**
     * 历史销量，去噪销量
     */
    @Getter
    @Setter
    public static class SalesInfoDenoisingDTO {
        /**
         * sku id
         */
        private String skuId;
        /**
         * sku
         */
        private String skuNo;
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
         * 日期
         */
        private LocalDate date;
        /**
         * 历史销量
         */
        private Integer hisSalesQty;
        /**
         * 去噪类型，percentage百分比去噪：fixedValue=固定值去噪，completely=完全去噪  枚举：CfgRuleSalesDenoisingCalcDenoisingTypeEnum
         */
        private String denoisingType;
        /**
         * 去噪类型名字
         */
        private String denoisingTypeName;
        /**
         * 有效值（去噪后的）
         */
        private String effectiveValue;
        /**
         * 去噪销量
         */
        private BigDecimal denoisingQty;


        public static SalesInfoDenoisingDTO buildSalesInfoDenoisingDTO(ExportDTO record, String shopName, String platform, LocalDate startDate,
                                                                       Map<LocalDate, CalcSalesInfoDenoisingEntity> calcDenoisingMap, Map<LocalDate, Integer> calcSalesInfoHisMap) {
            SalesInfoDenoisingDTO dto = new SalesInfoDenoisingDTO();
            dto.setSkuId(record.getSkuId());
            dto.setSkuNo(record.getSkuNo());
            dto.setShopId(record.getShopId());
            dto.setShopName(shopName);
            dto.setPlatform(platform);
            dto.setDate(startDate);
            Integer hisQty = Optional.ofNullable(calcSalesInfoHisMap.get(startDate)).orElse(0);
            dto.setHisSalesQty(hisQty);
            CalcSalesInfoDenoisingEntity entity = calcDenoisingMap.get(startDate);
            if (ObjectUtils.isEmpty(entity)) {
                dto.setDenoisingQty(new BigDecimal(hisQty));
            } else {
                dto.setDenoisingType(entity.getDenoisingType());
                dto.setDenoisingTypeName(CfgRuleSalesDenoisingDenoisingTypeEnum.getName(entity.getDenoisingType()));
                if (!ObjectUtils.isEmpty(entity.getEffectiveValue())) {
                    if (CfgRuleSalesDenoisingDenoisingTypeEnum.PERCENTAGE.getCode().equals(entity.getDenoisingType())) {
                        dto.setEffectiveValue(entity.getEffectiveValue() + "%");
                    } else {
                        dto.setEffectiveValue(String.valueOf(entity.getEffectiveValue()));
                    }
                }
                dto.setDenoisingQty(entity.getQty());
            }
            return dto;
        }
    }

    /**
     * 预估销量
     */
    @Getter
    @Setter
    public static class SalesInfoEstimateDTO {
        /**
         * sku id
         */
        private String skuId;
        /**
         * sku
         */
        private String skuNo;
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
         * 日期
         */
        private LocalDate date;
        /**
         * 预估日销量类型
         */
        private String salesInfoEstimateType;

        /**
         * 规则
         */
        private String rule;
        /**
         * 预估销量
         */
        private BigDecimal estimateQty;

        /**
         * 真实销量
         */
        private Integer realSalesQty;

        /**
         * 偏差比例
         */
        private String deviationRatio;
        /**
         * 近三日销量
         */
        private BigDecimal avgThreeSalesQty;
        /**
         * 近七日销量
         */
        private BigDecimal avgSevenSalesQty;
        /**
         * 近十四日销量
         */
        private BigDecimal avgFourteenSalesQty;
        /**
         * 近三十日销量
         */
        private BigDecimal avgThirtySalesQty;
        /**
         * 近六十日销量
         */
        private BigDecimal avgSixtySalesQty;
        /**
         * 近九十日销量
         */
        private BigDecimal avgNinetySalesQty;
        /**
         * 近一百八十日销量
         */
        private BigDecimal avgOneHundredAndEightySalesQty;
        /**
         * 近二百七十日销量
         */
        private BigDecimal avgTwoHundredAndSeventySalesQty;
        /**
         * 近三百六十日销量
         */
        private BigDecimal avgThreeHundredAndSixtySalesQty;
        /**
         * 近三日销量
         */
        private BigDecimal threeSalesQty;
        /**
         * 近七日销量
         */
        private BigDecimal sevenSalesQty;
        /**
         * 近十四日销量
         */
        private BigDecimal fourteenSalesQty;
        /**
         * 近三十日销量
         */
        private BigDecimal thirtySalesQty;
        /**
         * 近六十日销量
         */
        private BigDecimal sixtySalesQty;
        /**
         * 近九十日销量
         */
        private BigDecimal ninetySalesQty;
        /**
         * 近一百八十日销量
         */
        private BigDecimal oneHundredAndEightySalesQty;
        /**
         * 近二百七十日销量
         */
        private BigDecimal twoHundredAndSeventySalesQty;
        /**
         * 近三百六十日销量
         */
        private BigDecimal threeHundredAndSixtySalesQty;

        /**
         * MAE
         */
        private BigDecimal maeScore;
        /**
         * MSE
         */
        private BigDecimal mseScore;
        /**
         * RMSE
         */
        private BigDecimal rmseScore;
        /**
         * MAPE
         */
        private BigDecimal mapeScore;
        /**
         * R2
         */
        private BigDecimal r2Score;

        public static SalesInfoEstimateDTO buildSalesInfoEstimateDTO(CalcSalesInfoEstimateEntity entity, ExportDTO record, String shopName, String platform, Map<LocalDate, Integer> realSalesQtyMap) {
            StringBuilder salesInfoEstimateType = new StringBuilder();
            String rule;
            String baseRuleName = "日销量";
            if (CfgRuleSalesFormulaTypeEnum.FIXED.getCode().equals(entity.getType())) {
                salesInfoEstimateType.append(CfgRuleSalesFormulaTypeEnum.FIXED.getName()).append(baseRuleName);
                rule = String.valueOf(entity.getFixedValue());
            } else if (CfgRuleSalesFormulaTypeEnum.DYNAMIC.getCode().equals(entity.getType())) {
                salesInfoEstimateType.append(CfgRuleSalesFormulaTypeEnum.DYNAMIC.getName()).append(baseRuleName);
                rule = TimePeriodEnum.buildRule(entity.getPercentJsonDTO());
            } else {
                salesInfoEstimateType.append(CfgRuleSalesFormulaTypeEnum.DEFAULT.getName()).append(baseRuleName);
                if (CfgRuleSalesFormulaDefaultTypeEnum.FIXED.getCode().equals(entity.getDefaultType())) {
                    salesInfoEstimateType.append("(").append(CfgRuleSalesFormulaDefaultTypeEnum.FIXED.getName()).append(")");
                    rule = String.valueOf(entity.getFixedValue());
                } else {
                    salesInfoEstimateType.append("(").append(CfgRuleSalesFormulaDefaultTypeEnum.DYNAMIC.getName()).append(")");
                    rule = TimePeriodEnum.buildRule(entity.getPercentJsonDTO());
                }
            }
            List<CalcSalesInfoDimDTO.SalesVO> salesQtyList = JSON.parseObject(record.getSalesQtyJson(), new TypeReference<List<CalcSalesInfoDimDTO.SalesVO>>() {
            });
            Map<String, BigDecimal> salesQtyMap = salesQtyList.stream()
                    .collect(Collectors.toMap(SalesVO::getType, SalesVO::getQty, BigDecimal::add));
            List<CalcSalesInfoDimDTO.SalesVO> avgSalesQtyList = JSON.parseObject(record.getAvgSalesQtyJson(), new TypeReference<List<CalcSalesInfoDimDTO.SalesVO>>() {
            });
            Map<String, BigDecimal> avgSalesQtyMap = avgSalesQtyList.stream()
                    .collect(Collectors.toMap(SalesVO::getType, SalesVO::getQty, BigDecimal::add));
            SalesInfoEstimateDTO dto = new SalesInfoEstimateDTO();
            dto.setSkuId(record.getSkuId());
            dto.setSkuNo(record.getSkuNo());
            dto.setShopId(record.getShopId());
            dto.setShopName(shopName);
            dto.setPlatform(platform);
            dto.setDate(entity.getDate());
            dto.setSalesInfoEstimateType(salesInfoEstimateType.toString());
            dto.setRule(rule);
            dto.setEstimateQty(entity.getQty());
            int realQty = Optional.ofNullable(realSalesQtyMap.get(entity.getDate())).orElse(0);
            dto.setRealSalesQty(realQty);
            BigDecimal deviationRatio = realQty == 0 ? BigDecimal.ZERO : (entity.getQty().subtract(new BigDecimal(realQty))).divide(new BigDecimal(realQty), 2, RoundingMode.HALF_UP);
            dto.setDeviationRatio(deviationRatio.multiply(new BigDecimal(100)) + "%");
            dto.setAvgThreeSalesQty(avgSalesQtyMap.get(TimePeriodEnum.THREE.getName()));
            dto.setAvgSevenSalesQty(avgSalesQtyMap.get(TimePeriodEnum.SEVEN.getName()));
            dto.setAvgFourteenSalesQty(avgSalesQtyMap.get(TimePeriodEnum.FOURTEEN.getName()));
            dto.setAvgThirtySalesQty(avgSalesQtyMap.get(TimePeriodEnum.THIRTY.getName()));
            dto.setAvgSixtySalesQty(avgSalesQtyMap.get(TimePeriodEnum.SIXTY.getName()));
            dto.setAvgNinetySalesQty(avgSalesQtyMap.get(TimePeriodEnum.NINETY.getName()));
            dto.setAvgOneHundredAndEightySalesQty(avgSalesQtyMap.get(TimePeriodEnum.ONE_HUNDRED_AND_EIGHTY.getName()));
            dto.setAvgTwoHundredAndSeventySalesQty(avgSalesQtyMap.get(TimePeriodEnum.TWO_HUNDRED_AND_SEVENTY.getName()));
            dto.setAvgThreeHundredAndSixtySalesQty(avgSalesQtyMap.get(TimePeriodEnum.THREE_HUNDRED_AND_SIXTY.getName()));
            dto.setThreeSalesQty(salesQtyMap.get(TimePeriodEnum.THREE.getName()));
            dto.setSevenSalesQty(salesQtyMap.get(TimePeriodEnum.SEVEN.getName()));
            dto.setFourteenSalesQty(salesQtyMap.get(TimePeriodEnum.FOURTEEN.getName()));
            dto.setThirtySalesQty(salesQtyMap.get(TimePeriodEnum.THIRTY.getName()));
            dto.setSixtySalesQty(salesQtyMap.get(TimePeriodEnum.SIXTY.getName()));
            dto.setNinetySalesQty(salesQtyMap.get(TimePeriodEnum.NINETY.getName()));
            dto.setOneHundredAndEightySalesQty(salesQtyMap.get(TimePeriodEnum.ONE_HUNDRED_AND_EIGHTY.getName()));
            dto.setTwoHundredAndSeventySalesQty(salesQtyMap.get(TimePeriodEnum.TWO_HUNDRED_AND_SEVENTY.getName()));
            dto.setThreeHundredAndSixtySalesQty(salesQtyMap.get(TimePeriodEnum.THREE_HUNDRED_AND_SIXTY.getName()));
            dto.setR2Score(record.getR2Score());
            dto.setMapeScore(record.getMapeScore());
            dto.setMaeScore(record.getMaeScore());
            dto.setMseScore(record.getMseScore());
            dto.setRmseScore(record.getRmseScore());
            return dto;
        }
    }

    @Getter
    @Setter
    public static class ExportSalesInfoDTO extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String, String> sqlMap;

        /**
         * id
         */
        private String cfgRuleCalcId;
    }

    /**
     * 导出
     */
    @Getter
    @Setter
    public static class ExportDTO {

        private String id;
        /**
         * skuId
         */
        private String skuId;
        /**
         * sku
         */
        private String skuNo;
        /**
         * 国家
         */
        private String country;
        /**
         * 店铺
         */
        private String shopId;
        /**
         * 平台
         */
        private String platform;
        /**
         * 分时段销量
         */
        private String salesQtyJson;
        /**
         * 分时段日均销
         */
        private String avgSalesQtyJson;
        /**
         * 试算配置id
         */
        private String cfgRuleCalcId;
        /**
         * 试算开始日期
         */
        private LocalDate startCalcDate;
        /**
         * 试算结束日期
         */
        private LocalDate endCalcDate;

        /**
         * MAE
         */
        private BigDecimal maeScore;
        /**
         * MSE
         */
        private BigDecimal mseScore;
        /**
         * RMSE
         */
        private BigDecimal rmseScore;
        /**
         * MAPE
         */
        private BigDecimal mapeScore;
        /**
         * R2
         */
        private BigDecimal r2Score;
    }

    @Getter
    @Setter
    public static class ExportResultDTO {

        private List<CalcSalesInfoDimDTO.SalesInfoDenoisingDTO> salesInfoDenoising;
        private List<CalcSalesInfoDimDTO.SalesInfoEstimateDTO> salesInfoEstimate;
        private List<CfgRuleSalesFormulaCalcDTO.ExportDTO> defaultSalesQtyExportList;
        private List<CfgRuleSalesFormulaCalcDTO.ExportDTO> dynamicSalesQtyExportList;
        private List<CfgRuleSalesFormulaCalcDTO.ExportDTO> fixedSalesQtyExportList;
        private List<CfgRuleSalesDenoisingCalcDTO.ExportDTO> cfgRuleSalesDenoising;
    }

    @Getter
    @Setter
    public static class DetailViewDTO {
        /**
         * id
         */
        private String cfgRuleCalcId;
        /**
         * 试算配置编号
         */
        private String code;
        /**
         * 试算配置名称
         */
        private String name;

        /**
         * 试算开始日期
         */
        private LocalDate startCalcDate;

        /**
         * 试算结束日期
         */
        private LocalDate endCalcDate;

        /**
         * 历史销量类型
         */
        @Dict(enumClass = HistorySalesTypeEnum.class)
        private String saleType;

        /**
         * 文件地址
         */
        private String fileUrl;
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
         * 修改人名称
         */
        private String updateUserName;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        /**
         * 备注
         */
        private String remark;

        /**
         * 是否关注
         */
        private Boolean favorite;
        /**
         * MAE
         */
        private BigDecimal maeScore;
        /**
         * MSE
         */
        private BigDecimal mseScore;
        /**
         * RMSE
         */
        private BigDecimal rmseScore;
        /**
         * MAPE
         */
        private BigDecimal mapeScore;
        /**
         * R2
         */
        private BigDecimal r2Score;

        /**
         * 状态
         */
        @Dict(enumClass = CalcStatusEnum.class)
        private String status;

        /**
         * 序号
         */
        private Integer serialNo;
    }

    @Getter
    @Setter
    public static class TemplateViewDTO {
        /**
         * id
         */
        private String cfgRuleCalcId;
        /**
         * 试算配置编号
         */
        private String code;
        /**
         * 试算配置名称
         */
        private String name;

        /**
         * 试算开始日期
         */
        private LocalDate startCalcDate;

        /**
         * 试算结束日期
         */
        private LocalDate endCalcDate;

        /**
         * 历史销量类型
         */
        @Dict(enumClass = HistorySalesTypeEnum.class)
        private String saleType;

        /**
         * 文件地址
         */
        private String fileUrl;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 试算数据id
         */
        private List<String> calcSalesInfoDimIds;
        /**
         * 是否关注
         */
        private Boolean favorite;

        /**
         * 状态
         */
        @Dict(enumClass = CalcStatusEnum.class)
        private String status;
    }

    /**
     * 日期加数量
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HisSalesDTO {

        /**
         * 日期
         */
        private LocalDate date;
        /**
         * 数量
         */
        private Integer qty;
    }

    /**
     * 试算比较
     */
    @Getter
    @Setter
    public static class CalcCompareParamsDTO {

        /**
         * id (选中的)
         */
        private List<String> ids;
        /**
         * sku
         */
        private String skuId;
        /**
         * 店铺
         */
        private String shopId;
        /**
         * 试算开始日期
         */
        private LocalDate startCalcDate;

        /**
         * 开始日期
         */
        private LocalDate startDate;
        /**
         * 结束日期
         */
        private LocalDate endDate;

        /**
         * 预测类型
         * @see MetricsTypeEnum
         */
        @NotBlank(message = "试算类型不能为空")
        private String metricsType;
    }

    /**
     * 试算比较
     */
    @Getter
    @Setter
    public static class CalcCompareDTO {

        /**
         * 日期
         */
        private List<LocalDate> dateList;


        private List<LineDTO> lineList;
    }

    /**
     * 线
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LineDTO {

        /**
         * 模板名称
         */
        private String name;

        /**
         * 相似度
         */
        private BigDecimal similarity;

        /**
         * 销量
         */
        private List<BigDecimal> qty;
    }

    @Getter
    @Setter
    public static class CompareResultDTO {
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
         * 店铺
         */
        private String shopId;
        /**
         * 国家
         */
        private String country;
        /**
         * 平台
         */
        private String platform;
        /**
         * 试算配置id
         */
        private String cfgRuleCalcId;
        /**
         * 试算模板名字
         */
        private String name;
        /**
         * 试算开始日期
         */
        private LocalDate startCalcDate;
        /**
         * 试算结束日期
         */
        private LocalDate endCalcDate;
        /**
         * 历史数据base
         */
        private String hisDataMd5;
    }


    @Getter
    @Setter
    public static class CalcCompareDataDTO {
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
         * 国家名字
         */
        private String countryName;
        /**
         * 店铺名字
         */
        private String shopName;
        /**
         * 平台
         */
        private String platform;
        /**
         * 试算开始日期
         */
        private LocalDate startCalcDate;
    }

    /**
     * 列表导出
     */
    @Getter
    @Setter
    public static class ExportSalesInfoListDTO {
        /**
         * 试算配置编号
         */
        private String code;
        /**
         * 试算配置名称
         */
        private String name;

        /**
         * 试算开始日期
         */
        private LocalDate startCalcDate;

        /**
         * 试算结束日期
         */
        private LocalDate endCalcDate;

        /**
         * 历史销量类型
         */
        private String saleType;

        /**
         * 历史销量类型
         */
        private String saleTypeName;

        /**
         * sku id
         */
        private String skuId;

        /**
         * sku
         */
        private String skuNo;
        /**
         * 品名
         */
        private String productName;
        /**
         * 店铺
         */
        private String shopId;
        /**
         * 店铺名字
         */
        private String shopName;
        /**
         * 国家
         */
        private String country;
        /**
         * 国家名字
         */
        private String countryName;
        /**
         * 平台
         */
        private String platform;
        /**
         * MAE
         */
        private BigDecimal maeScore;
        /**
         * MSE
         */
        private BigDecimal mseScore;
        /**
         * RMSE
         */
        private BigDecimal rmseScore;
        /**
         * MAPE
         */
        private BigDecimal mapeScore;
        /**
         * R2
         */
        private BigDecimal r2Score;

        /**
         * 修改人名称
         */
        private String updateUserName;
        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;
        /**
         * 备注
         */
        private String remark;

        /**
         * 状态
         */
        private String status;

        /**
         * 状态名
         */
        private String statusName;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TabListDTO {

        /**
         * 状态
         */
        private String status;
        /**
         * 状态名
         */
        private String statusName;

    }

    @Getter
    @Setter
    public static class DataIdPageDTO {

        /**
         * 配置id
         */
        private String cfgRuleCalcId;
        /**
         * 主表id
         */
        private String id;

    }
}