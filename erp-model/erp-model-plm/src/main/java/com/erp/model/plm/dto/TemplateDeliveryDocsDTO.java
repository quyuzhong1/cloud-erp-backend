package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: 模板输出物DTO
 * @date 2022/11/14 18:00
 */
@Data
@NoArgsConstructor
public class TemplateDeliveryDocsDTO implements Serializable {

    /**
     * id
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 模板id
     */
    @NotBlank(message = "模板id不能为空")
    private String templateId;

    /**
     * 文档名id
     */
    @NotBlank(message = "文档id不能为空")
    private String docsNameId;

    /**
     * 文档名
     */
    @NotBlank(message = "文档id不能为空")
    private String docsName;

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
     * 任务id
     */
    private String taskId;

    /**
     * 是否是系统文档 1 是  0 不是
     */
    private Short isSys;

    /**
     * 辅助字段：模板状态(1启用，0禁用)
     */
    private Integer templateStatus;
}
