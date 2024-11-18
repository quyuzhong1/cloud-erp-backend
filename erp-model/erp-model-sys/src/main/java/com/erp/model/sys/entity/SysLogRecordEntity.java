package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.LogActionEnum;
import com.common.core.enums.LogStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/**
 * <p>
 * 操作日志
 * </p>
 *
 * @author Jim
 * @since 2023-08-25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("sys_log_record")
public class SysLogRecordEntity extends BaseEntity<SysLogRecordEntity> {

    /**
     * 请求ID
     */
    @TableField("request_id")
    private String requestId;

    /**
     * 系统模块:
     */
    @TableField("system_module")
    private String systemModule;

    /**
     * 操作路径
     */
    @TableField("path")
    private String path;

    /**
     * 操作类型：insert=插入,update=更新,delete=删除,
     * {@link LogActionEnum}
     */
    @TableField("action")
    private String action;

    /**
     * 描述
     */
    @TableField("description")
    private String description;

    /**
     * 操作人ip
     */
    @TableField("ip")
    private String ip;

    /**
     * 操作的参数json
     */
    @TableField("request_params")
    private String requestParams;

    /**
     * 响应的参数json
     */
    @TableField("response_params")
    private String responseParams;

    /**
     * 修改前参数json
     */
    @TableField("before_params")
    private String beforeParams;

    /**
     * 修改后参数json
     */
    @TableField("after_params")
    private String afterParams;

    /**
     * 修改的类名
     */
    @TableField("class_path")
    private String classPath;

    /**
     * 修改的记录id
     */
    @TableField("record_id")
    private String recordId;

    /**
     * 记录单据编号
     */
    @TableField("record_code")
    private String recordCode;

    /**
     * 异常信息
     */
    @TableField("error_msg")
    private String errorMsg;

    /**
     * 状态:正常/异常
     * {@link LogStatusEnum}
     */
    @TableField("status")
    private String status;


    public static final String SYSTEM_MODULE = "system_module";

    public static final String FIELD_ACTION = "action";

    public static final String FIELD_DESCRIPTION = "description";

    public static final String FIELD_IP = "ip";

    public static final String REQUEST_PARAMS = "request_params";

}