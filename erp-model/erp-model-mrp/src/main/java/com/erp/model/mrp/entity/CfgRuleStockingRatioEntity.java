package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


/**
 * <p>
 * 备货系数（规则设置）
 * </p>
 *
 * @author will
 * @since 2024-08-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_rule_stocking_ratio")
public class CfgRuleStockingRatioEntity extends BaseEntity<CfgRuleStockingRatioEntity> {

    /**
    * 备货主表id
    */
    @TableField("stock_up_id")
    private String stockUpId;
    /**
    * 排序字段
    */
    @TableField("index")
    private Integer index;
    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 开始时间
    */
    @TableField("start_date")
    private LocalDate startDate;
    /**
    * 结束日期
    */
    @TableField("end_date")
    private LocalDate endDate;
    /**
    * 备货系数
    */
    @TableField("stocking_ratio")
    private BigDecimal stockingRatio;

    /**
     * 类型，conventional常规品，new新品
     */
    @TableField("type")
    private String type;

    /**
     * 时间数组
     */
    @TableField(exist = false)
    private List<LocalDate> dateList;

    public static final String STOCK_UP_ID = "stock_up_id";

    public static final String INDEX = "index";

    public static final String NAME = "name";

    public static final String START_DATE = "start_date";

    public static final String END_DATE = "end_date";

    public static final String STOCKING_RATIO = "stocking_ratio";

    public static final String TYPE = "type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}