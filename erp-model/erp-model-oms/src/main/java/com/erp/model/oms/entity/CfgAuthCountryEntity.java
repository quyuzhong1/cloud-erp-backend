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
 * 授权国家配置
 * </p>
 *
 * @author Jim
 * @since 2025-04-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_auth_country")
public class CfgAuthCountryEntity extends BaseEntity<CfgAuthCountryEntity> {

    /**
     * 商城编号 MarketplaceId
     */
    @TableField("marketplace_id")
    private String marketplaceId;
    /**
     * 商城中文名称
     */
    @TableField("name")
    private String name;
    /**
     * 国家/地区代码（ISO标准）
     */
    @TableField("country_code")
    private String countryCode;
    /**
     * 单站点授权卖家平台URL
     */
    @TableField("seller_central_url")
    private String sellerCentralUrl;
    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    /**
     * cfg_auth_region区域编码ID
     */
    @TableField("main_id")
    private String mainId;
    /**
     * 排序
     */
    @TableField("index")
    private Integer index;


    public static final String MARKETPLACE_ID = "marketplace_id";

    public static final String NAME = "name";

    public static final String COUNTRY_CODE = "country_code";

    public static final String SELLER_CENTRAL_URL = "seller_central_url";

    public static final String REMARK = "remark";

    public static final String MAIN_ID = "main_id";

    @Override
    public Serializable pkVal() {
        return null;
    }

}