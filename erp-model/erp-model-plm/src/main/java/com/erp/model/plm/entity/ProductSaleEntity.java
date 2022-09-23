package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description 产品销售信息表
 * @Author Luo_WG
 * @Date 2022/9/23 15:06
 * @param
 * @return
 **/
@TableName(value ="product_sale")
@Data
public class ProductSaleEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id")
    private String id;

    /**
     * 产品sku表id
     */
    @TableField(value = "sku_id")
    private String skuId;

    /**
     * 年目标销售量
     */
    @TableField(value = "year_sale_qty")
    private Integer yearSaleQty;

    /**
     * 年目标销售额
     */
    @TableField(value = "year_sale_amount")
    private BigDecimal yearSaleAmount;

    /**
     * 月目标销售量
     */
    @TableField(value = "month_sale_qty")
    private Integer monthSaleQty;

    /**
     * 月目标销售额
     */
    @TableField(value = "month_sale_amount")
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
    private Date listingTime;

    /**
     * 退市时间
     */
    @TableField(value = "delisting_time")
    private Date delistingTime;

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
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id")
    private String createUserId;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id")
    private String updateUserId;

    /**
     * 产品上市（含培训）资料链接
     */
    @TableField(value = "data_url")
    private String dataUrl;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}