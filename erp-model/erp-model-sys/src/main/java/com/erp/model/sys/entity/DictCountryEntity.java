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
 * 国家字典表
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("dict_country")
public class DictCountryEntity extends BaseEntity<DictCountryEntity> {

    /**
     * 中文名
     */
    @TableField("name_cn")
    private String nameCn;

    /**
     * 英文名
     */
    @TableField("name_en")
    private String nameEn;

    /**
     * 中文简称
     */
    @TableField("short_name_cn")
    private String shortNameCn;

    /**
     * 英文简称
     */
    @TableField("short_name_en")
    private String shortNameEn;

    /**
     * 国际电话区号
     */
    @TableField("calling_code")
    private String callingCode;

    /**
     * 国家官方语言
     */
    @TableField("languages")
    private String languages;

    /**
     * 子区域code
     */
    @TableField("subregion_code")
    private String subregionCode;

    /**
     * 大区code
     */
    @TableField("region_code")
    private String regionCode;

    /**
     * 国旗图片 URL
     */
    @TableField("flag_url")
    private String flagUrl;

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
     * 官方币种code
     */
    @TableField("currency_code")
    private String currencyCode;

    /**
     * 金蝶编码
     */
    @TableField("kingdee_code")
    private String kingdeeCode;

    /**
     * 亚马逊区域
     */
    @TableField("amazon_area")
    private String amazonArea;

    /**
     * 数据标识
     */
    @TableField("data_flag")
    private String dataFlag;

    /**
     * ISO 3166-1三位字母代码
     */
    @TableField("alpha3")
    private String alpha3;


    public static final String NAME_CN = "name_cn";

    public static final String NAME_EN = "name_en";

    public static final String SHORT_NAME_CN = "short_name_cn";

    public static final String SHORT_NAME_EN = "short_name_en";

    public static final String CALLING_CODE = "calling_code";

    public static final String LANGUAGES = "languages";

    public static final String SUBREGION_CODE = "subregion_code";

    public static final String REGION_CODE = "region_code";

    public static final String FLAG_URL = "flag_url";

    public static final String INDEX = "index";

    public static final String DISABLED = "disabled";

    public static final String CURRENCY_CODE = "currency_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
