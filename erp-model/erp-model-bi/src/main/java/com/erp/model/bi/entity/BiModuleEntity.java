package com.erp.model.bi.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 模块表(BiModule)实体类
 *
 * @author yl
 * @since 2022-12-08 12:31:20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bi_module")
public class BiModuleEntity implements Serializable {
    private static final long serialVersionUID = -87185314332831686L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;
    /**
     * 模块名
     */
    private String name;
    /**
     * 系统模块id
     */
    private String sysModuleId;

    /**
     * 分类id
     */
    private String categoryId;


    /**
     * 备注说明
     */
    private String remark;
    /**
     * 缩略图地址
     */
    private String imageUrl;
    /**
     * 开启状态1 开启 0 未开启
     */
    private Integer state;
    /**
     * 前端组件名
     */
    private String viewCode;
    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    /**
     * 更新人id
     */
    @TableField(value = "update_user_id",fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;



}

