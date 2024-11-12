package com.erp.model.mrp.entity;

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
 * 试算关注表
 * </p>
 *
 * @author liaohui
 * @since 2024-11-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("calc_sales_info_favorite")
public class CalcSalesInfoFavoriteEntity extends BaseEntity<CalcSalesInfoFavoriteEntity> {

    /**
    * 用户id
    */
    @TableField("user_id")
    private String userId;
    /**
    * 试算id
    */
    @TableField("calc_sales_info_dim_id")
    private String calcSalesInfoDimId;


    public static final String USER_ID = "user_id";

    public static final String CALC_SALES_INFO_DIM_ID = "calc_sales_info_dim_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}