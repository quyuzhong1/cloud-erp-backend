package com.erp.server.dmp.entity.dmp;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 
 * @TableName dmp_sku_info
 */
@TableName(value ="dmp_sku_info")
@Data
@ToString
public class DmpSkuInfoEntity implements Serializable {
    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * sku编号
     */
    @TableField(value = "sku_no")
    private String skuNo;

    /**
     * 中文名
     */
    @TableField(value = "name_cn")
    private String nameCn;

    /**
     * 英文名
     */
    @TableField(value = "name_en")
    private String nameEn;

    /**
     * 统一成本价
     */
    @TableField(value = "default_cost")
    private BigDecimal defaultCost;

    /**
     * 商品状态:1.自动创建;2.待开发;3.正常;4.清仓;5.停止销售
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 商品创建时间
     */
    @TableField(value = "sku_create_time")
    private Date skuCreateTime;

    /**
     * 商品修改时间
     */
    @TableField(value = "sku_update_time")
    private Date skuUpdateTime;

    /**
     * 品牌
     */
    @TableField(value = "brand_name")
    private String brandName;

    /**
     * 商品目录(一级)
     */
    @TableField(value = "parent_category_name")
    private String parentCategoryName;

    /**
     * 商品目录(二级)
     */
    @TableField(value = "category_name")
    private String categoryName;

    /**
     * 售价
     */
    @TableField(value = "sale_price")
    private BigDecimal salePrice;

    /**
     * 申报价格
     */
    @TableField(value = "declare_price")
    private BigDecimal declarePrice;

    /**
     * 开发员id
     */
    @TableField(value = "developer_id")
    private String developerId;

    /**
     * 开发员名称
     */
    @TableField(value = "developer_name")
    private String developerName;

    /**
     * 拉取时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT_UPDATE)
    private Date createTime;

    /**
     * 平台标识
     */
    @TableField(value = "platform_sign")
    private String platformSign;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


    @Override
    public String toString() {
        return "DmpSkuInfoEntity{" +
                "skuNo='" + skuNo + '\'' +
                ", nameCn='" + nameCn + '\'' +
                ", nameEn='" + nameEn + '\'' +
                ", defaultCost=" + defaultCost +
                ", status=" + status +
                ", skuCreateTime=" + skuCreateTime +
                ", skuUpdateTime=" + skuUpdateTime +
                ", brandName='" + brandName + '\'' +
                ", parentCategoryName='" + parentCategoryName + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", salePrice=" + salePrice +
                ", declarePrice=" + declarePrice +
                ", developerId='" + developerId + '\'' +
                ", developerName='" + developerName + '\'' +
                ", platformSign='" + platformSign + '\'' +
                '}';
    }
}