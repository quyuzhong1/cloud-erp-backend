package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: 角色DTO
 * @date 2022/11/15 11:07
 */
@Data
@NoArgsConstructor
public class TemplateRoleDTO implements Serializable {

    /**
     * 角色id
     */
    private String id;

    /**
     * 角色名
     */
    @NotBlank(message = "角色名称不能为空")
    private String name;

    /**
     * 创建时间
     */
    @TableField(value = "create_time",fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 模板id
     */
    @NotBlank(message = "模板id不能为空")
    private String templateId;


}
