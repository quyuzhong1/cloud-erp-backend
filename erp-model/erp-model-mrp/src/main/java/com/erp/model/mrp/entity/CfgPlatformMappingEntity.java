package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;


/**
 * <p>
 * 平台映射表
 * </p>
 *
 * @author will
 * @since 2024-08-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_platform_mapping")
public class CfgPlatformMappingEntity extends BaseEntity<CfgPlatformMappingEntity> {

    /**
    * 平台
    */
    @TableField("platform")
    private String platform;
    /**
    * 归属平台
    */
    @TableField("type")
    private String type;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 生效时间
    */
    @TableField("effective_date")
    private LocalDate effectiveDate;

    /**
     * 备货模式
     */
    @TableField("stocking_mode")
    private String stockingMode;


    public static final String PLATFORM = "platform";

    public static final String TYPE = "type";

    public static final String DISABLED = "disabled";

    public static final String EFFECTIVE_DATE = "effective_date";

    @Override
    public Serializable pkVal() {
        return null;
    }

}