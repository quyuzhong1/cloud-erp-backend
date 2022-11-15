package com.erp.model.plm.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String templateId;

    /**
     * 文档名
     */
    private String docsName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
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
     * 文档名id
     */
    private String docsNameId;
}
