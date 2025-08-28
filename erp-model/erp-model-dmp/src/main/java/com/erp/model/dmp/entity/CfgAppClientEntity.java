package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 第三方应用程序信息表
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "cfg_app_client", autoResultMap = true)
public class CfgAppClientEntity extends BaseEntity<CfgAppClientEntity> {

    /**
    * 平台类型 如消息平台，销售平台
    */
    @TableField("platform_type")
    private String platformType;
    /**
    * 对应平台 如 微信 亚马逊
    */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
    * 业务类型如店铺授权
    */
    @TableField("business_type")
    private String businessType;
    /**
    * 客户端id
    */
    @TableField("client_id")
    private String clientId;
    /**
    * 客户端secret
    */
    @TableField("client_secret")
    private String clientSecret;
    /**
    * 对应的url
    */
    @TableField("url")
    private String url;
    /**
    * 回调的url
    */
    @TableField("redirect_url")
    private String redirectUrl;
    /**
     * 扩展字段的 数据+值
     */
    @TableField(value = "extend_data", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extendData;


    public static final String PLATFORM_TYPE = "platform_type";

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String BUSINESS_TYPE = "business_type";

    public static final String CLIENT_ID = "client_id";

    public static final String CLIENT_SECRET = "client_secret";

    public static final String URL = "url";

    public static final String REDIRECT_URL = "redirect_url";

    @Override
    public Serializable pkVal() {
        return null;
    }

}