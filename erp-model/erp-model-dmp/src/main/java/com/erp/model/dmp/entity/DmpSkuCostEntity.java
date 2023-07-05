package com.erp.model.dmp.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * sku bom关系表
 * </p>
 *
 * @author Cloud
 * @since 2023-06-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_sku_cost")
public class DmpSkuCostEntity extends BaseEntity<DmpSkuCostEntity> {


    /**
    * sku编号
    */
    @TableField("sku_no")
    private String skuNo;

    /**
    * 成本日期
    */
    @TableField("cost_date")
    private LocalDate costDate;

    /**
    * 备注
    */
    @TableField("remark")
    private String remark;

    /**
    * 有效状态
    */
    @TableField("status")
    private Boolean status;

    /**
    * 成本价格
    */
    @TableField("cost_price")
    private BigDecimal costPrice;


    public static final String SKU_NO = "sku_no";

    public static final String COST_DATE = "cost_date";

    public static final String REMARK = "remark";

    public static final String STATUS = "status";

    public static final String COST_PRICE = "cost_price";

    @Override
    public Serializable pkVal() {
        return null;
    }

}