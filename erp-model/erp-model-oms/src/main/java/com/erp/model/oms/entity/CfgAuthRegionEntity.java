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
 * 授权区域配置
 * </p>
 *
 * @author Jim
 * @since 2025-04-21
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("cfg_auth_region")
public class CfgAuthRegionEntity extends BaseEntity<CfgAuthRegionEntity> {

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
    /**
     * 区域代码（如 us-east-1）
     */
    @TableField("region")
    private String region;
    /**
     * 区域名称（如 北美/欧洲）
     */
    @TableField("name")
    private String name;
    /**
     * OMS平台代号
     */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
     * 批量授权URL
     */
    @TableField("batch_seller_central_url")
    private String batchSellerCentralUrl;
    /**
     * 正式环境端点URL
     */
    @TableField("endpoints")
    private String endpoints;
    /**
     * 沙箱环境端点URL
     */
    @TableField("sandbox_endpoints")
    private String sandboxEndpoints;
    /**
     * 排序
     */
    @TableField("index")
    private Integer index;


    public static final String REMARK = "remark";

    public static final String REGION = "region";

    public static final String NAME = "name";

    public static final String DICT_PLATFROM = "dict_platfrom";

    public static final String BATCH_SELLER_CENTRAL_URL = "batch_seller_central_url";

    public static final String ENDPOINTS = "endpoints";

    public static final String SANDBOX_ENDPOINTS = "sandbox_endpoints";

    @Override
    public Serializable pkVal() {
        return null;
    }

}