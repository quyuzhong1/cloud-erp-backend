package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
* @Description 产品sku表
* @Author Luo_WG
* @Date 2022/9/22 15:18
**/
@TableName(value ="product_detail")
@Data
@NoArgsConstructor
public class ProductDetailEntity extends BaseEntity<ProductDetailEntity> implements Serializable {
    /**
     * 产品表id
     */
    @TableField(value = "product_id")
    private String productId;

    /**
     * sku_no
     */
    @TableField(value = "sku_no")
    private String skuNo;

    /**
     * 产品品名
     */
    @TableField(value = "name")
    private String name;

    /**
     * 产品品名(英文)
     */
    @TableField(value = "name_en")
    private String nameEn;

    /**
     * 属性
     */
    @TableField(value = "variant_property")
    private String variantProperty;

    /**
     * 计划上市时间
     */
    @TableField(value = "plan_listing_time")
    private LocalDate planListingTime;

    /**
     * 首批量产入库时间
     */
    @TableField(value = "first_mass_product_date")
    private LocalDate firstMassProductDate;
    
    /**
     * 单位表id
     */
    @TableField(value = "unit_id")
    private String unitId;

    /**
     * 产品开发状态 1:未开发 2:开发中 3:开发完成 4:中止开发 5:暂停开发
     */
    @TableField(value = "product_state")
    private Integer productState;

    /**
     * sku图片
     */
    @TableField(value = "images_url")
    private String imagesUrl;

    /**
     * 单位名称
     */
    @TableField(value = "unit_name")
    private String unitName;

    /**
     * 产品负责人Id
     */
    @TableField(value = "charge_id")
    private String chargeId;

    /**
     * 产品负责人姓名
     */
    @TableField(value = "charge_name")
    private String chargeName;

    /**
     *禁止修改的字段
     */
    @TableField(exist = false)
    private List<String> disableFieldList;

    /**
     * 任务状态 0待审核，1审核中，2审核通过，3审核不通过,4，待提交
     */
    @TableField("status")
    private Integer status;

    /**
     * 流程id
     */
    @TableField("process_id")
    private String processId;

    /**
     * 流程表id
     */
    @TableField("business_process_id")
    private String businessProcessId;

    /**
     * 是否变更（0否，1是）
     */
    private Integer isChange;

    /**
     * 同步金蝶id
     */
    @TableField("sync_kingdee_id")
    private String syncKingdeeId;

    /**
     * 是否占用 默认 false  占用为true 就不能删除
     */
    @TableField("occupy_status")
    private Boolean occupyStatus;

    /**
     * 推荐仓位(小货区)
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
     * 推荐仓位(大货区)
     */
    @TableField("warehouse_location_large")
    private String warehouseLocationLarge;
    
    /**
     * 启用时间，取首次审核通过时间
     */
    @TableField("enable_time")
    private LocalDateTime enableTime;
    
    /**
     * ean码
     */
    @TableField(exist = false)
    private String ean;

    /**
     * 目的国申报价
     */
    @TableField(exist = false)
    private BigDecimal destDeclarePrice;

    /**
     * 是否下推旺店通
     */
    @TableField(exist = false)
    private Boolean isPushWdt;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}