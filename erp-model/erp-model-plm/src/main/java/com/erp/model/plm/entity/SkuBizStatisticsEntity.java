package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * sku业务统计表
 * </p>
 *
 * @author shukai
 * @since 2026-03-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("sku_biz_statistics")
public class SkuBizStatisticsEntity extends BaseEntity<SkuBizStatisticsEntity> {

    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 最新出库日期
    */
    @TableField("last_outstock_date")
    private LocalDate lastOutstockDate;


    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String LAST_OUTSTOCK_DATE = "last_outstock_date";

    @Override
    public Serializable pkVal() {
        return null;
    }

}