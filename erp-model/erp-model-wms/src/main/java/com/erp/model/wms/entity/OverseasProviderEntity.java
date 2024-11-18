package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 海外物流商
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "overseas_provider", autoResultMap = true)
public class OverseasProviderEntity extends BaseEntity<OverseasProviderEntity> {

    /**
    * code
    */
    @TableField("code")
    private String code;
    /**
    * 服务商名称
    */
    @TableField("name")
    private String name;
    /**
    * 授权状态 already 已授权 not未授权 cancel 取消授权
    */
    @TableField("auth_status")
    private String authStatus;
    /**
    * 授权时间
    */
    @TableField(value = "auth_time",updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime authTime;
    /**
    * 授权的信息json格式 例如：{'app_key':'test','token':'test'}
    */
    @TableField(value = "auth_json", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> authJson;

    /**
     * 平台账号
     */
    @TableField("platform_account")
    private String platformAccount;

    /**
     * 仓库简称
     */
    @TableField("short_name")
    private String shortName;

    /**
     * 启用时间
     */
    @TableField("enable_date")
    private LocalDate enableDate;

    public static final String AUTH_STATUS = "auth_status";

    public static final String AUTH_TIME = "auth_time";

    public static final String AUTH_JSON = "auth_json";

    @Override
    public Serializable pkVal() {
        return null;
    }

}