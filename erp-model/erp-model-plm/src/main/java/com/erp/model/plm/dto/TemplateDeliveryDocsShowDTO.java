package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: 模板输出物列表DTO
 * @date 2022/11/16 11:04
 */
@Data
@NoArgsConstructor
public class TemplateDeliveryDocsShowDTO implements Serializable {

    /**
     * id
     */
    private String id;

    /**
     * 模板id
     */
    private String templateId;

    /**
     * 文档名id
     */
    private String docsNameId;

    /**
     * 文档名
     */
    private String docsName;

    /**
     * 输出物状态(1启用，0禁用)
     */
    private Boolean status;



    /**
     * 创建人
     */
    private String createUserName;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private String updateUserName;

    /**
     * 更新时间
     */
    private Date updateTime;

}
