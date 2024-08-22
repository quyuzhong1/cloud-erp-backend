package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
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

    /**
     * 金蝶编码
     */
    @TableField("kingdee_code")
    private String kingdeeCode;

    @TableField("code")
    private String code;
    /**
     * 二字码
     */
    @TableField("code_two")
    private String codeTwo;
    /**
     * 英文编码
     */
    @TableField("code_en")
    private String codeEn;
    /**
     * 葡萄牙语编码
     */
    @TableField("code_pt")
    private String codePt;

    @TableField(exist = false)
    private String countryName;


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

    public DictCityEntity(String provinceName, String code, String parentId, int level, String type, int index, String provinceCode) {
        this.name = provinceName;
        this.countryCode = code;
        this.parentId = parentId;
        this.level = level;
        this.type = type;
        this.index = index;
        this.code = provinceCode;
    }

    @Override
    public Serializable pkVal() {
        return null;
    }

}
