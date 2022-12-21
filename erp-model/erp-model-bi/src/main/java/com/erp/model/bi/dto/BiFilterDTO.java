package com.erp.model.bi.dto;

import com.erp.common.dto.base.PermissionsDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.collections.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * BI 筛选条件
 * @author Cloud
 */
@Data
@EqualsAndHashCode
@ToString
@NoArgsConstructor
public class BiFilterDTO extends PermissionsDTO {


    /**
     * 0 -订单时间 1-发货时间
     * 对应枚举 TimeTypeEnum
     */
    @NotNull(message = "时间类型不能为空")
    @NotNull(message = "时间类型不能为空", groups = SelectTargetModule.class )
    private Integer timeType;

    /**
     * 开始日期
     */
    @NotNull(message = "开始时间不能为空")
    @NotNull(message = "开始时间不能为空", groups = SelectTargetModule.class )
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束日期
     */
    @NotNull(message = "结束时间不能为空")
    @NotNull(message = "结束时间不能为空", groups = SelectTargetModule.class )
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

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
    private List<Long> userId;

    /**
     *是否可以支持源币种
     * @param dto
     * @return
     */
    public static Boolean validOriginalCurrency(BiFilterDTO dto){
        return CollectionUtils.isNotEmpty(dto.getSite()) || CollectionUtils.isNotEmpty(dto.getSku()) || CollectionUtils.isNotEmpty(dto.getShopName());
    }

    /**
     * 是否为新品 bool
     */
    private Boolean hasNewSign;


    public interface SelectTargetModule{}

}
