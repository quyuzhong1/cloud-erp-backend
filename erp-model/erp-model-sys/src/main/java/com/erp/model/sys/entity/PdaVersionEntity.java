package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-14
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pda_version")
public class PdaVersionEntity extends BaseEntity<PdaVersionEntity> {

    /**
    * 下拉获取地址：sys/common/enumDropDown?type=SysType
    * 发版类型：PC PDA
    */
    @TableField("type")
    private String type;

    /**
    * 通知类型：1 系统通知  0 升级通知
    */
    @TableField("release_type")
    private String releaseType;

    /**
    * pda版本
    */
    @TableField("pda_version")
    private String pdaVersion;

    /**
    * 升级内容描述
    */
    @TableField("remark")
    private String remark;

    /**
    * 是否强制更新
    */
    @TableField("force")
    private Boolean force;

    /**
    * 升级包地址
    */
    @TableField("url")
    private String url;

    /**
    * 升级时间
    */
    @TableField("upgrade_time")
    private LocalDateTime upgradeTime;

    public static final String PDA_VERSION = "pda_version";

    public static final String FIELD_REMARK = "remark";

    public static final String FIELD_FORCE = "force";

    public static final String FIELD_URL = "url";

    @Override
    public Serializable pkVal() {
        return null;
    }

}