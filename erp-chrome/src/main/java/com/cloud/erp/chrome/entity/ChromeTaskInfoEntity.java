package com.cloud.erp.chrome.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author yl
 * @since 2022-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("chrome_task_info")
public class ChromeTaskInfoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * erp 平台
     */
    @TableField("platform")
    private String platform;

    /**
     * 业务类型  默认 订单 order
     */
    @TableField("business_type")
    private String businessType;

    /**
     * json 的参数
     */
    @TableField("parameter")
    private String parameter;

    /**
     * 任务状态 0 :为执行 1：执行中 2: 已完成
     */
    @TableField("task_status")
    private Integer taskStatus;

    /**
     * 开始时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}
