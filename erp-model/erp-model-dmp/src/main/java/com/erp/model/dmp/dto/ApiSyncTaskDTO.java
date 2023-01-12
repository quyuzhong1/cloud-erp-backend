package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 17:33
 */
@Data
@NoArgsConstructor
public class ApiSyncTaskDTO {


    /**
     * 主键id
     */
    private String id;

    /**
     * 平台名称
     */
    private String apiPlatform;

    /**
     * 平台id
     */
    private String apiPlatformId;

    /**
     * 模块类型 ApiModuleTypeEnum枚举,（0产品信息，1BOM管理）
     */
    private String moduleType;

    /**
     * 业务id(模块数据对应主键id)
     */
    private String businessId;

    /**
     * API请求参数
     */
    private String requestParamJson;

    /**
     * 请求次数(重试)
     */
    private Integer retryCount;
}
