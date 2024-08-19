package com.common.business.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 任务信息实体类
 * @author Cloud
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DmpPushTaskFeignDTO {
    /**
     * 目标平台名称
     */
    @NotBlank(message = "目标平台名称不能为空")
    private String targetPlatformName;

    /**
     * MQ消息主题
     */
    @NotBlank(message = "MQ消息主题不能为空")
    private String mqTopic;

    /**
     * MQ消息TAG  推送第三方平台命名方式
     */
    @NotBlank(message = "MQ消息TAG不能为空")
    private String mqTag;

    /**
     * MQ消息内容
     */
    @NotBlank(message = "MQ消息内容不能为空")
    private String mqData;

    /**
     * 来源系统
     */
    @NotBlank(message = "来源系统不能为空")
    private String sourcePlatformName;

    /**
     * 来源单据类型
     */
    @NotBlank(message = "来源单据类型不能为空")
    private String sourceType;

    /**
     * 来源单据id
     */
    @NotBlank(message = "来源单据id不能为空")
    private String sourceId;

    /**
     * 来源单据编号
     */
    @NotBlank(message = "来源单据编号不能为空")
    private String sourceCode;

    /**
     * 操作类型
     */
    @NotBlank(message = "操作类型不能为空")
    private String syncOperate;

    /**
     * 来源单据上级单据id
     */
    private String parentId;

    /**
     * 第三方单号
     */
    private String thirdCode;
}
