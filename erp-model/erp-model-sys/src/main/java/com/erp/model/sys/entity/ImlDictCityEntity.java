package com.erp.model.sys.entity;

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
 * 艾姆勒城市字典表
 * </p>
 *
 * @author lrp
 * @since 2023-11-22
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iml_dict_city")
public class ImlDictCityEntity extends BaseEntity<ImlDictCityEntity> {

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
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 系统字典表id
    */
    @TableField("dict_city_id")
    private String dictCityId;


    public static final String NAME = "name";

    public static final String COUNTRY_CODE = "country_code";

    public static final String PARENT_ID = "parent_id";

    public static final String LEVEL = "level";

    public static final String DISABLED = "disabled";

    public static final String DICT_CITY_ID = "dict_city_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}