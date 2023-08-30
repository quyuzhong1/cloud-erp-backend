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
 * 
 * </p>
 *
 * @author Lambda
 * @since 2023-08-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("dict_amazon_area_country")
public class DictAmazonAreaCountryEntity extends BaseEntity<DictAmazonAreaCountryEntity> {

    /**
    * 区域code
    */
    @TableField("region_code")
    private String regionCode;
    /**
    * 区域名称
    */
    @TableField("region_name")
    private String regionName;
    /**
    * 国家code
    */
    @TableField("country_code")
    private String countryCode;
    /**
    * 国家名称
    */
    @TableField("country_name")
    private String countryName;


    public static final String REGION_CODE = "region_code";

    public static final String REGION_NAME = "region_name";

    public static final String COUNTRY_CODE = "country_code";

    public static final String COUNTRY_NAME = "country_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}