package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @Description 产品销售信息表请求参数
 * @Author Luo_WG
 * @Date 2022/9/23 12:14
 **/
@Data
@NoArgsConstructor
public class ProductSaleDTO implements Serializable {
    /**
     * 主键id
     */
    private String id;

    /**
     * sku表id
     */
    private String skuId;

    /**
     * 年目标销售量
     */
    @DecimalMax(value = "99999999",message ="年目标销售量最大值为99999999" )
    @DecimalMin(value = "0",message ="最小值为0" )
    private Long yearSaleQty;

    /**
     * 年目标销售额
     */
    @Digits(integer = 16,fraction = 4,message = "年目标销售额最大16字符，小数位不能大于4位")
    private BigDecimal yearSaleAmount;

    /**
     * 月目标销售量
     */
    @DecimalMax(value = "99999999",message ="月目标销售量最大值为99999999" )
    @DecimalMin(value = "0",message ="最小值为0" )
    private Long monthSaleQty;

    /**
     * 月目标销售额
     */
    @Digits(integer = 16,fraction = 4,message = "月目标销售额最大16字符，小数位不能大于4位")
    private BigDecimal monthSaleAmount;

    /**
     * 销售国家
     */
    @Size(max = 255,message = "国家最大不能超过255字符")
    private String saleCountry;

    /**
     * 上市时间
     */
    private LocalDate listingTime;

    /**
     * 退市时间
     */
    private LocalDate delistingTime;

    /**
     * 图片是否完成 1.是 2.否
     */
    private Integer isFinishedImg;

    /**
     * 视频是否完成 1.是 2.否
     */
    private Integer isFinishedVideo;

    /**
     * 销售状态 1.未销售 2.销售中 3.清仓中 4.已下架
     */
    private Integer saleState;

    /**
     * 产品上市（含培训）资料链接
     */
    private String dataUrl;

    /**
     * 首季度目标销量
     */
    @Digits(integer = 16,fraction = 2,message = "首季度目标销量最大16字符")
    private BigDecimal targetSalesQty;

    /**
     * 销售平台(SalesPlatformEnum枚举)
     */
    @Size(max = 200,message = "销售平台最大不能超过200字符")
    private String salesPlatform;

    /**
     * 是否可销售(0否，1是)
     */
    private Integer isMarketable;


    private static final long serialVersionUID = 1L;
}