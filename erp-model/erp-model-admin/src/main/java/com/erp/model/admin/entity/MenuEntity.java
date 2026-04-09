package com.erp.model.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 菜单表
 *
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
@Data
@TableName("sys_menu")
public class MenuEntity implements Serializable {

    /**
     * $column.comments
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String menuId;
    /**
     * 父id
     */
    private String parentId;
    /**
     * 菜单图标
     */
    private String menuIcon;
    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单code 码
     */
    private String menuCode;

    /**
     * 菜单链接
     */
    private String menuUrl;
    /**
     * 类型 1：目录   2：菜单   3：按钮  4：功能
     */
    private Integer type;

    private String selectLightId;
    /**
     * 禁用
     */
    private Boolean disabled;

    /**
     * 菜单排序
     */
    private Integer index;

    /**
     * 所属系统,/api/sys/dictBasic/list?type=menuSystem
     */
    @NotBlank(message = "所属系统不能为空")
    private String system;

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
     * 修改人id
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name", fill = FieldFill.INSERT_UPDATE)
    private String updateUserName;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

    /**
     * 逻辑删除字段
     */
    @TableField(value = "is_deleted")
    @TableLogic
    private Boolean isDeleted;
}
