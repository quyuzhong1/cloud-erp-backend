package com.erp.model.bi.dto;

import com.common.business.dto.base.PermissionsDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.collections.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * BI 筛选条件
 *
 * @author Cloud
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString
@NoArgsConstructor
public class BiSalesFilterDTO extends PermissionsDTO {


    /**
     * 0 -订单时间 1-发货时间
     * 对应枚举 TimeTypeEnum
     */
    private Integer timeType;

    /**
     * 开始日期
     */
    private LocalDateTime startTime;
    /**
     * 结束日期
     */
    private LocalDateTime endTime;

    /**
     * 区间类型 1 国内 2国外
     */
    @NotNull(message = "区间类型不能为空")
    private Integer rangeType;

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
     * 店铺编号
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
        if (null == this.endTime){
            return LocalDateTime.now(ZoneId.systemDefault()).with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);
        }
        return LocalDateTime.of(endTime.plusDays(1).toLocalDate(), LocalTime.MIN);
    }

    /**
     * 是否为新品 bool
     */
    private Boolean hasNewSign;
}
