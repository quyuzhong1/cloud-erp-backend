package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 工作台选项表
 * </p>
 *
 * @author Luo_WG
 * @since 2023-04-11
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("work_option")
public class WorkOptionEntity extends BaseEntity<WorkOptionEntity> {

    /**
     * 菜单表id
     */
    @TableField("work_menu_id")
    private String workMenuId;

    /**
     * 属于用户id
     */
    @TableField("option_user_id")
    private String optionUserId;

    /**
     * 属于用户名称
     */
    @TableField("option_user_name")
    private String optionUserMame;

    /**
     * 模块地址
     */
    @TableField("module_url")
    private String moduleUrl;

    /**
     * 模块参数（预留）
     */
    @TableField("module_param")
    private String moduleParam;

    /**
     * 类型  1：常用模块  2：待办模块
     */
    @TableField("type")
    private String type;

    /**
     * 模块名称
     */
    @TableField("module_name")
    private String moduleName;

    public static final String DICT_BASIC_ID = "dict_basic_id";

    public static final String DICT_BASIC_NAME = "dict_basic_name";

    public static final String FIELD_TYPE = "type";

    public static final String FIELD_NAME = "name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
