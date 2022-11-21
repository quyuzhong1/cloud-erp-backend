package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

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
    private String docsName;

    /**
     * 任务id
     */
    private String taskId;

}
