package com.erp.model.sys.dto;

import com.common.core.enums.LogActionEnum;
import com.common.core.enums.LogStatusEnum;
import com.common.core.utils.BeanMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 系统日志mq同步
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysLogMqDTO implements Serializable {
    /**
     * 请求ID
     */
    @NotBlank(message = "请求ID不能为空")
    private String requestId;

    /**
     * 操作人id
     */
    @NotNull(message = "操作人id不能为空")
    private String operationUserId;

    /**
     * 操作人名称
     */
    @NotBlank(message = "操作人名称不能为空")
    private String operationUserName;

    /**
     * 系统模块
     */
    @NotBlank(message = "系统模块不能为空")
    @Size(max = 50, message = "系统模块最大长度不能超过50位")
    private String systemModule;

    /**
     * 操作路径
     */
    @NotBlank(message = "操作路径不能为空")
    private String path;

    /**
     * 操作类型
     * {@link LogActionEnum}
     */
    @NotBlank(message = "操作类型不能为空")
    @Size(max = 100, message = "操作路径最大长度不能超过100位")
    private String action;

    /**
     * 描述
     */
    @NotBlank(message = "描述不能为空")
    private String description;

    /**
     * 操作人ip
     */
    @NotBlank(message = "操作人ip不能为空")
    @Size(max = 16, message = "操作人ip最大长度不能超过16位")
    private String ip;

    /**
     * 操作的参数json
     */
    @NotBlank(message = "操作的参数json不能为空")
    private String requestParams;

    /**
     * 响应的参数json
     */
    @NotBlank(message = "响应的参数json不能为空")
    private String responseParams;

    /**
     * 修改前参数json
     */
    @NotBlank(message = "修改前参数json不能为空")
    private String beforeParams;

    /**
     * 修改后参数json
     */
    @NotBlank(message = "修改后参数json不能为空")
    private String afterParams;

    /**
     * 修改的类名
     */
    @NotBlank(message = "修改的类名不能为空")
    private String classPath;

    /**
     * 修改的记录id
     */
    @NotBlank(message = "修改的记录id不能为空")
    private String recordId;

    /**
     * 记录单据编号
     */
    @NotNull(message = "记录单据编号不能为NULL")
    private String recordCode;

    /**
     * 异常信息
     */
    @NotNull(message = "异常信息不能为NULL")
    private String errorMsg;

    /**
     * 状态:正常/异常
     * {@link LogStatusEnum}
     */
    @NotBlank(message = "状态不能为空")
    private String status;

    public static SysLogMqDTO init(String operationUserId, String operationUserName, SysLogRecordDTO.AddDTO recordDTO) {
        SysLogMqDTO dto = new SysLogMqDTO();
        BeanMapper.copy(recordDTO, dto);
        dto.setOperationUserId(operationUserId);
        dto.setOperationUserName(operationUserName);
        return dto;
    }
}