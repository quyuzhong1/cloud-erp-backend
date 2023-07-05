package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Description 产品销售信息表
 * @Author Luo_WG
 * @Date 2022/9/23 15:06
 * @param
 * @return
 **/
@TableName(value ="product_sale")
@Data
public class ProductSaleEntity extends BaseEntity implements Serializable {
    /**
     * 产品sku表id
     */
    @TableField(value = "sku_id")
    private String skuId;

    /**
     * 年目标销售量
     */
    @TableField(value = "year_sale_qty", fill = FieldFill.INSERT_UPDATE)
    private Long yearSaleQty;

    /**
     * 年目标销售额
     */
    @TableField(value = "year_sale_amount", fill = FieldFill.INSERT_UPDATE)
    private BigDecimal yearSaleAmount;

    /**
     * 月目标销售量
     */
    @TableField(value = "month_sale_qty", fill = FieldFill.INSERT_UPDATE)
    private Long monthSaleQty;

    /**
     * 月目标销售额
     */
    @TableField(value = "month_sale_amount", fill = FieldFill.INSERT_UPDATE)
    private BigDecimal monthSaleAmount;

    /**
     * 销售国家
     */
    @TableField(value = "sale_country")
    private String saleCountry;

    /**
     * 上市时间
     */
    @TableField(value = "listing_time")
    private LocalDate listingTime;

    /**
     * 退市时间
     */
    @TableField(value = "delisting_time")
    private LocalDate delistingTime;

    /**
     * 图片是否完成 1.是 2.否
     */
    @TableField(value = "is_finished_img")
    private Integer isFinishedImg;

    /**
     * 视频是否完成 1.是 2.否
     */
    @TableField(value = "is_finished_video")
    private Integer isFinishedVideo;

    /**
     * 销售状态 1.未销售 2.销售中 3.清仓中 4.已下架
     */
    @TableField(value = "sale_state")
    private Integer saleState;

    /**
     * 首季度目标销量
     */
    @TableField(value = "target_sales_qty")
    private BigDecimal targetSalesQty;

    /**
     * 销售平台(ProductSalesPlatformEnum枚举)
     */
    @TableField(value = "sales_platform")
    private String salesPlatform;

    /**
     * 是否可销售(0否，1是)
     */
    @TableField(value = "is_marketable")
    private Integer isMarketable;

    /**
     * 产品上市（含培训）资料链接
     */
    @TableField(value = "data_url")
    private String dataUrl;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}