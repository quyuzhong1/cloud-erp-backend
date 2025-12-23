package com.erp.model.dmp.entity;

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
 * 第三方城市字典表
 * </p>
 *
 * @author jack
 * @since 2025-12-17
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dmp_third_city")
public class DmpThirdCityEntity extends BaseEntity<DmpThirdCityEntity> {

    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 名称
    */
    @TableField("name")
    private String name;
    /**
    * 国家二字码
    */
    @TableField("country_code")
    private String countryCode;
    /**
    * 上级城市编码
    */
    @TableField("parent_code")
    private String parentCode;
    /**
    * 城市编码
    */
    @TableField("code")
    private String code;
    /**
    * 城市类型 
    */
    @TableField("type")
    private String type;
    /**
    * 排序
    */
    @TableField("index")
    private Integer index;
    /**
    * 来源平台
    */
    @TableField("source_platform")
    private String sourcePlatform;


    public static final String DISABLED = "disabled";

    public static final String NAME = "name";

    public static final String COUNTRY_CODE = "country_code";

    public static final String PARENT_CODE = "parent_code";

    public static final String CODE = "code";

    public static final String TYPE = "type";

    public static final String INDEX = "index";

    public static final String SOURCE_PLATFORM = "source_platform";

    @Override
    public Serializable pkVal() {
        return null;
    }

}