package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
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
public class SysTaskPhaseEntity extends BaseEntity implements Serializable {

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

}
