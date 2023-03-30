package com.erp.model.dmp.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.erp.model.dmp.enums.PlatformEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 17:25
 */
@Data
@NoArgsConstructor
public class ApiPlmSyncLogDTO {

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
     * API授权信息表id
     */
    private String apiAuthId;

    /**
     * 模块类型 ApiModuleTypeEnum枚举,（0产品信息，1BOM管理）
     */
    private Integer moduleType;

    /**
     * 发送状态（1发送成功，2发送失败）
     */
    private Integer status;

    /**
     * 错误消息
     */
    @TableField(value = "msg")
    private String msg;

    /**
     * 业务id(模块数据对应主键id)
     */
    private String businessId;

    /**
     * API请求参数
     */
    private String requestParamJson;

    public ApiPlmSyncLogDTO(PlatformEnum platform, Integer code, String receivingCode, Integer sendResult, String msg, String jsonStr) {
        this.apiPlatform = platform.getDesc();
        this.apiPlatformId = platform.getCode().toString();
        this.businessId = receivingCode;
        this.moduleType = code;
        this.status = sendResult;
        this.msg = msg;
        this.requestParamJson = jsonStr;
    }
}
