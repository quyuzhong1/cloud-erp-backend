package com.erp.model.wms.entity;

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
 * 海外物流商
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("overseas_provider")
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
    @TableField("auth_time")
    private LocalDateTime authTime;
    /**
    * 授权的信息json格式 例如：{'app_key':'test','token':'test'}
    */
    @TableField("auth_json")
    private String authJson;


    public static final String CODE = "code";

    public static final String NAME = "name";

    public static final String AUTH_STATUS = "auth_status";

    public static final String AUTH_TIME = "auth_time";

    public static final String AUTH_JSON = "auth_json";

    @Override
    public Serializable pkVal() {
        return null;
    }

}