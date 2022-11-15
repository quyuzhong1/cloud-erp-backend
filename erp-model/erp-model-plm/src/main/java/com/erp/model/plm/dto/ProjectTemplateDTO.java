package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Will
 * @version 1.0
 * @description: 项目模板DTO
 * @date 2022/11/11 11:36
 */
@Data
@NoArgsConstructor
public class ProjectTemplateDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 产品id
     */
    private String productId;

    /**
     * 模板名称
     */
    @NotBlank(message = "模板名称不能为空")
    private String name;

    /**
     * 模板类型
     */
    private Integer type;

    /**
     * 模板状态(1启用，0禁用)
     */
    private Integer status;

    /**
     * 创建人
     */
    private String createUserName;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private String updateUserName;

    /**
     * 更新人id
     */
    private String updateUserId;

    /**
     * 更新时间
     */
    private Date updateTime;

}
