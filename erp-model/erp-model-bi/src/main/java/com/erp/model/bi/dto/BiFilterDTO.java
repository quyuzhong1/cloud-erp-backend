package com.erp.model.bi.dto;

import com.common.business.dto.base.SortDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.apache.commons.collections.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;

/**
 * BI 筛选条件
 *
 * @author Cloud
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiFilterDTO extends SortDTO {


    /**
     * 0 -订单时间 1-发货时间
     * 对应枚举 TimeTypeEnum
     */
//    @NotNull(message = "时间类型不能为空")
//    @NotNull(message = "时间类型不能为空", groups = SelectTargetModule.class)
    private Integer timeType;

    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;


    /**
     * 结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 区间类型 1 国内 2国外
     */
//    @NotNull(message = "区间类型不能为空")
    private Integer rangeType;
    /**
     * 数据类型  1销售额 2销量
     */
    private Integer dataType;
    /**
     * 1 新品
     * 0 老品
     */
    private Integer newSign;

    /**
     * 0 CNY实时  1 CNY结算  2原币种
     * 对应枚举 SettleMethodEnum
     */
    @NotNull(message = "结算方式不能为空")
    private Integer settleMethod;

    /**
     * 事业部
     */
    private List<String> department;

    /**
     * 平台
     */
    private List<String> platform;

    /**
     * 站点
     */
    private List<String> site;

    /**
     * 店铺
     */
    private List<String> shopName;


    /**
     * 品类
     */
    private List<String> category;

    /**
     * 品牌
     */
    private List<String> brand;

    /**
     * sku
     */
    private List<String> sku;

    /**
     * 用户id
     */
    private List<String> userId;

    /**
     * 产品属性id
     */
    private List<String> propertyIdList;

    /**
     * 客户编码
     */
    private List<String> customerCodes;

    /**
     * 是否可以支持源币种
     *
     * @param dto
     * @return
     */
    public static Boolean validOriginalCurrency(BiFilterDTO dto) {
        return CollectionUtils.isNotEmpty(dto.getSite()) || CollectionUtils.isNotEmpty(dto.getSku()) || CollectionUtils.isNotEmpty(dto.getShopName());
    }

    /**
     * 默认当月开始时间
     */
    public LocalDateTime getStartTime() {
        if (null == this.startTime){
            return LocalDateTime.now(ZoneId.systemDefault()).with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        }
        return startTime;
    }

    /**
     * 默认当月结束时间
     */
    public LocalDateTime getEndTime() {
        if (null == this.endTime) {
            return LocalDateTime.now(ZoneId.systemDefault())
                    .with(TemporalAdjusters.lastDayOfMonth())
                    .with(LocalTime.MIN);
        }
        return LocalDateTime.of(endTime.toLocalDate(), LocalTime.MIN);
    }

    public void setEndTime(LocalDateTime endTime, Integer day) {
        this.endTime = endTime.plusDays(day);
    }

    /**
     * 是否为新品 bool
     */
    private Boolean hasNewSign;

    public Integer getNewSign() {
        if (Objects.nonNull(this.hasNewSign)) {
            if (hasNewSign) {
                return 1;
            } else {
                return 0;
            }

        }
        return null;
    }

    /**
     * 排行数量
     */
    private Integer rankNum = 5;

    /**
     * 排序字段
     */
    private String rankKey;


    public interface SelectTargetModule {
    }

}
