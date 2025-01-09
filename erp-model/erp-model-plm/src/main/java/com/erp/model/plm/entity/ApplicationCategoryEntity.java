package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 产品应用分类
 * </p>
 *
 * @author liaohui
 * @since 2025-01-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("application_category")
public class ApplicationCategoryEntity extends BaseEntity<ApplicationCategoryEntity> {

    /**
    * 分类名
    */
    @TableField("name")
    private String name;
    /**
    * 分类代码
    */
    @TableField("code")
    private String code;


    public static final String NAME = "name";

    public static final String CODE = "code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}