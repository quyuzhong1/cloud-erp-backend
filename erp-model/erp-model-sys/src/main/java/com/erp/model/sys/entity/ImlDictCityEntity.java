package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
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
 * @since 2023-11-23
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iml_dict_city")
public class ImlDictCityEntity extends BaseEntity<ImlDictCityEntity> {

    /**
    * 名称
    */
    @TableField("region_name")
    private String regionName;
    /**
    * 国家二字码
    */
    @TableField("country_code")
    private String countryCode;
    /**
    * 上级城市id
    */
    @TableField("parent_region_id")
    private String parentRegionId;
    /**
    * 区域等级
    */
    @TableField("region_level")
    private Integer regionLevel;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 系统字典表id
    */
    @TableField(value = "dict_city_id",updateStrategy = FieldStrategy.IGNORED)
    private String dictCityId;
    /**
    * 城市id
    */
    @TableField("region_id")
    private String regionId;


    public static final String REGION_NAME = "region_name";

    public static final String COUNTRY_CODE = "country_code";

    public static final String PARENT_REGION_ID = "parent_region_id";

    public static final String REGION_LEVEL = "region_level";

    public static final String DISABLED = "disabled";

    public static final String DICT_CITY_ID = "dict_city_id";

    public static final String REGION_ID = "region_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}