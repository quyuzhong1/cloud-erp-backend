package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 平台token授权表
 * </p>
 *
 * @author Jim
 * @since 2025-08-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_platform_auth")
public class DmpPlatformAuthEntity extends BaseEntity<DmpPlatformAuthEntity> {

    /**
     * 来源系统
     */
    @TableField("source_system")
    private String sourceSystem;

    /**
     * 输入任务id
     */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
     * 转换id
     */
    @TableField("convert_id")
    private String convertId;
    /**
     * 下一层级id
     */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
     * 唯一字段md5值
     */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
     * 数据字段md5值
     */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
     * 登陆token
     */
    @TableField("token")
    private String token;
    /**
     * 授权的token
     */
    @TableField("access_token")
    private String accessToken;
    /**
     * 中台cfg_app_client的id
     */
    @TableField("app_client_id")
    private String appClientId;
    /**
     * 访问令牌过期之前的秒数
     */
    @TableField("expires_in")
    private Integer expiresIn;
    /**
     * 刷新token
     */
    @TableField("refresh_token")
    private String refreshToken;
    /**
     * token失效时间（不是平台标准的失效时间，要存往前推提前刷新的时间，不能失败了再刷新）
     */
    @TableField("expire_time")
    private LocalDateTime expireTime;
    /**
     * 响应的appId唯一标识
     */
    @TableField("resp_app_id")
    private String respAppId;


    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String TOKEN = "token";

    public static final String ACCESS_TOKEN = "access_token";

    public static final String APP_CLIENT_ID = "app_client_id";

    public static final String EXPIRES_IN = "expires_in";

    public static final String REFRESH_TOKEN = "refresh_token";

    public static final String EXPIRE_TIME = "expire_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}