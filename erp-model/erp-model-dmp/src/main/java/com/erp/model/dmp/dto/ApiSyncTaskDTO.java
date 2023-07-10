package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @author Will
 * @version 1.0
 * @description: 推送任务
 * @date 2023/7/10 16:19
 */
@Data
@NoArgsConstructor
public class ApiSyncTaskDTO {

    /**
     * 平台id
     */
    @NotBlank(message = "平台不能为空")
    private String apiPlatformId;

    /**
     * 授权id
     */
    private String apiAuthId;

    /**
     * 模块类型 ApiModuleTypeEnum枚举
     */
    @NotBlank(message = "模块类型不能为空")
    private Integer moduleType;

    /**
     * 业务id(模块数据对应主键id)
     */
    @NotBlank(message = "业务id不能为空")
    private String businessId;

    /**
     * API请求参数
     */
    @NotBlank(message = "API请求参数不能为空")
    private String requestParamJson;

    /**
     * 请求次数(重试)
     */
    private Integer retryCount;

}
