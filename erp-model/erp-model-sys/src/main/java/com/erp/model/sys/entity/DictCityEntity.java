package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("dict_city")
public class DictCityEntity extends BaseEntity<DictCityEntity> {

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
     * 时区
     */
    @TableField("timezone")
    private String timezone;

    /**
     * 邮编
     */
    @TableField("zip_code")
    private String zipCode;

    /**
     * 经度
     */
    @TableField("latitude")
    private BigDecimal latitude;

    /**
     * 维度
     */
    @TableField("longitude")
    private BigDecimal longitude;

    /**
     * 上级城市id
     */
    @TableField("parent_id")
    private String parentId;

    /**
     * 等级
     */
    @TableField("level")
    private Integer level;

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
     * 是否禁用
     */
    @TableField("disabled")
    private Boolean disabled;


    public static final String NAME = "name";

    public static final String COUNTRY_CODE = "country_code";

    public static final String TIMEZONE = "timezone";

    public static final String ZIP_CODE = "zip_code";

    public static final String LATITUDE = "latitude";

    public static final String LONGITUDE = "longitude";

    public static final String PARENT_ID = "parent_id";

    public static final String LEVEL = "level";

    public static final String TYPE = "type";

    public static final String INDEX = "index";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
