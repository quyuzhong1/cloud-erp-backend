package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 达人合作平台信息
 * </p>
 *
 * @author jack
 * @since 2025-12-02
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("kol_cooperation_platform")
public class KolCooperationPlatformEntity extends BaseEntity<KolCooperationPlatformEntity> {

    /**
    * main_id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 合作平台名称
    */
    @TableField("platform_name")
    private String platformName;
    /**
    * 平台ID
    */
    @TableField("platform_account_id")
    private String platformAccountId;
    /**
    * 账号名称
    */
    @TableField("platform_account_name")
    private String platformAccountName;
    /**
    * 粉丝数量
    */
    @TableField("follower_count")
    private Integer followerCount;
    /**
    * 主页链接
    */
    @TableField("homepage_url")
    private String homepageUrl;
    /**
    * 平台备注
    */
    @TableField("remark")
    private String remark;


    public static final String MAIN_ID = "main_id";

    public static final String PLATFORM_NAME = "platform_name";

    public static final String PLATFORM_ACCOUNT_ID = "platform_account_id";

    public static final String PLATFORM_ACCOUNT_NAME = "platform_account_name";

    public static final String FOLLOWER_COUNT = "follower_count";

    public static final String HOMEPAGE_URL = "homepage_url";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}