package com.erp.model.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @description:销售监控
 * @author Will
 * @date: 2022/12/29 16:19
 */
@Data
@TableName(value ="bi_sales_monitoring")
public class BiSalesMonitoringEntity implements Serializable {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name", fill = FieldFill.INSERT)
    private String createUserName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name", fill = FieldFill.INSERT_UPDATE)
    private String updateUserName;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 监控类型（字典bi_dict中salesMonitoringType类型，0销售额，1销量，2新品销售额，3老品销售额，4品牌销售监控，5品类销售监控，6人员销售监控）
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 最新月基础值
     */
    @TableField(value = "latest_month_value")
    private BigDecimal latestMonthValue;

    /**
     * 环比(百分比)
     */
    @TableField(value = "relative_ratio")
    private BigDecimal relativeRatio;

    /**
     * 负责人
     */
    @TableField(value = "charge_id")
    private String chargeId;

    /**
     * 负责人名称
     */
    @TableField(value = "charge_name")
    private String chargeName;

    /**
     * 最新月基础值比较符（>=,<=,>,<）
     */
    @TableField(value = "latest_month_compare")
    private String latestMonthCompare;

    /**
     * 环比比较符（>=,<=,>,<）
     */
    @TableField(value = "relative_ratio_compare")
    private String relativeRatioCompare;

}
