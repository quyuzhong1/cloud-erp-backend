package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Classname SysTaskPhaseEntity
 * @Description TODO
 * @Date 2022-09-13 16:32
 * @Created by yl
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_project_phase")
public class SysTaskPhaseEntity  implements Serializable {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    @TableField("name")
    private String name;

    @TableField("is_project_approval")
    private Integer isProjectApproval;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
