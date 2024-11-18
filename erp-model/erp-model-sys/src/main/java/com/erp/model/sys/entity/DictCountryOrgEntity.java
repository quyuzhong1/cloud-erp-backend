package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 国家-组织（政治经济）关系表
 * </p>
 *
 * @author zdy
 * @since 2024-05-21
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dict_country_org")
public class DictCountryOrgEntity extends BaseEntity<DictCountryOrgEntity> {

    /**
    * 国家二字码ISO 3166-1 Alpha-2 代码
    */
    @TableField("country_id")
    private String countryId;
    /**
    * 组织编码（政治经济组织编码）
    */
    @TableField("org_code")
    private String orgCode;
    /**
    * 组织编码（政治经济组织名称）
    */
    @TableField("org_name")
    private String orgName;

    /**
     * 国家中文名
     */
    @TableField("name_cn")
    private String nameCn;

    /**
     * 国家英文名
     */
    @TableField("name_en")
    private String nameEn;


    public static final String COUNTRY_ID = "country_id";

    public static final String ORG_CODE = "org_code";

    public static final String ORG_NAME = "org_name";

    public static final String NAME_CN = "name_cn";

    public static final String NAME_EN = "name_en";

    @Override
    public Serializable pkVal() {
        return null;
    }

}