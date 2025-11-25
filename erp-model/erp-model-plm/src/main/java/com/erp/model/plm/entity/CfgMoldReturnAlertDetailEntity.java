package com.erp.model.plm.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 模具返回策略明细
 * </p>
 *
 * @author jack
 * @since 2025-10-15
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_mold_return_alert_detail")
public class CfgMoldReturnAlertDetailEntity extends BaseEntity<CfgMoldReturnAlertDetailEntity> {

    /**
    * 明细备注
    */
    @TableField("remark")
    private String remark;
    /**
    * main_id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 返还数量上限
    */
    @TableField("return_qty_limit")
    private Integer returnQtyLimit;
    /**
    * 返回金额
    */
    @TableField("return_price")
    private BigDecimal returnPrice;


    public static final String REMARK = "remark";

    public static final String MAIN_ID = "main_id";

    public static final String RETURN_QTY_LIMIT = "return_qty_limit";

    public static final String RETURN_PRICE = "return_price";

    @Override
    public Serializable pkVal() {
        return null;
    }

}