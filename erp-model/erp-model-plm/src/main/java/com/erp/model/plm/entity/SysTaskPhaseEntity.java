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


    /**
     * 表id
     * @author yl
     * @date 2022-10-09 10:47
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 阶段名
     * @author yl
     * @date 2022-10-09 10:47
     */
    @TableField("name")
    private String name;

    /**
     * 是否是立项阶段 0 不是 1 是
     * @author yl
     * @date 2022-10-09 10:47
     */
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
