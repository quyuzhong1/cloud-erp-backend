package com.erp.model.msg.dto;

import com.common.business.enums.ErpServerModuleEnum;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 飞书系统预警请求实体
 * @CreateTime: 2023-05-31  18:38
 * @Author: zhangchunlin
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class WarnMsgInfoDTO implements Serializable {

    /**
     * 异常发生所属项目
     */
    @NotNull(message = "异常发生所属项目不能为空")
    private ErpServerModuleEnum erpServerModuleEnum;

    /**
     * 标题
     */
    @NotNull(message = "预警标题不能为空")
    private String title;

    /**
     * 异常发生所属关键业务名称
     */
    @NotEmpty(message = "异常发生所属关键业务名称不能为空")
    private String bizName;

    /**
     * 异常表名
     * 无法明确指定请赋予空字符串
     */
    @NotNull(message = "异常表名不能为空")
    private String tableName;

    /**
     * 异常表id
     * 无法明确指定请赋予空字符串
     */
    @NotNull(message = "异常表id不能为空")
    private String tableId;

    /**
     * 异常关键信息
     * 无法明确指定请赋予空字符串
     */
    @NotNull(message = "异常关键信息不能为空")
    private String keyInfo;

    /**
     * 预警类型
     * 主要用于发送到对应的飞书群
     */
    @NotNull(message = "预警类型不能为空")
    private WarnMsgTypeEnum warnMsgTypeEnum = WarnMsgTypeEnum.SYS_EXCEPTION;

    /**
     * 异常发生事件
     */
    private LocalDateTime happenTime;

}