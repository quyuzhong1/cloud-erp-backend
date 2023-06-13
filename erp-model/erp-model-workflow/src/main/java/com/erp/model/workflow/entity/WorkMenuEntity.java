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
 * 工作台菜单基础表
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("work_menu")
public class WorkMenuEntity extends BaseEntity<WorkMenuEntity> {

    /**
     * 系统分类 plm wms scm
     */
    @TableField("sys_classify")
    private String sysClassify;

    /**
     * 模块分类
     */
    @TableField("module_classify")
    private String moduleClassify;

    /**
     * 模块编码
     */
    @TableField("module_code")
    private String moduleCode;

    /**
     * 详情url
     */
    @TableField("detail_url")
    private String detailUrl;


    public static final String SYS_CLASSIFY = "sys_classify";

    public static final String MODULE_CLASSIFY = "module_classify";

    public static final String MODULE_URL = "module_url";

    public static final String MODULE_PARAM = "module_param";

    public static final String MODULE_CODE = "module_code";

    public static final String DETAIL_URL = "detail_url";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
