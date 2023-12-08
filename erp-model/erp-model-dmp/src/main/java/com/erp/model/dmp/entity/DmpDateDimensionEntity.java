package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 时间维度表
 * </p>
 *
 * @author zdy
 * @since 2023-12-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_date_dimension")
public class DmpDateDimensionEntity extends BaseEntity<DmpDateDimensionEntity> {

    /**
    * 订单时间
    */
    @TableField("date_time")
    private LocalDateTime dateTime;
    /**
    * 数据时间名称（用于页面展示）
    */
    @TableField("date_day")
    private String dateDay;
    /**
    * 数据时间名称（用于页面展示）
    */
    @TableField("date_week")
    private String dateWeek;
    /**
    * 数据时间名称（用于页面展示）
    */
    @TableField("date_month")
    private String dateMonth;
    /**
    * 数据时间名称（用于页面展示）
    */
    @TableField("date_quarter")
    private String dateQuarter;
    /**
    * 数据时间名称（用于页面展示）
    */
    @TableField("date_year")
    private String dateYear;


    public static final String DATE_TIME = "date_time";

    public static final String DATE_DAY = "date_day";

    public static final String DATE_WEEK = "date_week";

    public static final String DATE_MONTH = "date_month";

    public static final String DATE_QUARTER = "date_quarter";

    public static final String DATE_YEAR = "date_year";

    @Override
    public Serializable pkVal() {
        return null;
    }

}