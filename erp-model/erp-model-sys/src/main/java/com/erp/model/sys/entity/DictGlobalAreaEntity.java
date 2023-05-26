package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 区域表
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("dict_global_area")
public class DictGlobalAreaEntity extends BaseEntity<DictGlobalAreaEntity> {

    /**
     * 国家所属子区域例如“北欧”、“中东”等
     */
    @TableField("subregion_name")
    private String subregionName;

    /**
     * 国家所属的大洲或地理区域code
     */
    @TableField("region_code")
    private String regionCode;

    /**
     * 国家所属的大洲或地理区域例如“欧洲”、“亚洲”、“南美洲”等
     */
    @TableField("region_name")
    private String regionName;

    /**
     * 序号
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
    private Boolean kingdeeCode;


    public static final String SUBREGION_NAME = "subregion_name";

    public static final String REGION_CODE = "region_code";

    public static final String REGION_NAME = "region_name";

    public static final String INDEX = "index";

    public static final String DISABLED = "disabled";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
