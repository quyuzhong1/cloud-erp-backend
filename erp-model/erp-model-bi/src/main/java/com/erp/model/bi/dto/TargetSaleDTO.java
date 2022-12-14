package com.erp.model.bi.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections.CollectionUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售数据统计条件构建类
 * @author Cloud
 */
@Data
@NoArgsConstructor
public class TargetSaleDTO implements Serializable {


    /**
     * 0 -订单时间 1-发货时间
     */
    @NotNull(message = "时间类型不能为空")
    private Integer timeType;

    /**
     * 开始日期
     */
    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    /**
     * 结束日期
     */
    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    /**
     * 0 CNY实时  1 CNY结算  2原币种
     */
    @NotBlank(message = "结算方式不能为空")
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
     * 店铺编号
     */
    private List<String> shopNo;


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

    public static Boolean validOriginalCurrency(TargetSaleDTO dto){
        return CollectionUtils.isNotEmpty(dto.getSite()) || CollectionUtils.isNotEmpty(dto.getSku()) || CollectionUtils.isNotEmpty(dto.getShop());
    }

    private List<String> shopList;


}
