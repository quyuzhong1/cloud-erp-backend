package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;

import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 系统产品文档
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_docs_name")
public class SysDocsEntity extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文档名
     */
    @TableField("name")
    private String name;

    /**
     * 启动状态1 启用  2 禁用
     */
    @TableField("start_state")
    private Integer startState;

}
