package com.erp.model.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/12/14 15:21
 */
@TableName(value ="bi_data_source_custom_detail")
@Data
public class BiDataSourceCustomDetailEntity implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

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
     * 日期
     */
    @TableField("date")
    private Integer date;

    /**
     * 周日期开始
     */
    @TableField("week_begin")
    private String weekBegin;

    /**
     * 周日期结束
     */
    @TableField("week_end")
    private String weekEnd;

    /**
     * 月
     */
    @TableField("month")
    private Integer month;

    /**
     * 季度
     */
    @TableField("quarter")
    private Integer quarter;

    /**
     * 年
     */
    @TableField("year")
    private Integer year;

    /**
     * 年
     */
    @TableField("value")
    private String value;

    /**
     * 自助数据主表id
     */
    @TableField("custom_id")
    private String customId;
}
