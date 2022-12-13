package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 销售数据统计条件构建类
 * @author Cloud
 */
@Data
@NoArgsConstructor
public class IndicatorSaleDTO implements Serializable {


    /**
     * 0 -订单时间 1-发货时间
     */
    @NotNull(message = "时间类型不能为空")
    private Integer timeType;

    /**
     * 开始日期
     */
    @NotNull(message = "开始时间不能为空")
    private Date startTime;

    /**
     * 结束日期
     */
    @NotNull(message = "结束时间不能为空")
    private Date endTime;

    /**
     * 币种code
     */
    @NotBlank(message = "币种不能为空")
    private String currency;

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
    private List<String> shop;

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
    private List<Long> userId;


}
