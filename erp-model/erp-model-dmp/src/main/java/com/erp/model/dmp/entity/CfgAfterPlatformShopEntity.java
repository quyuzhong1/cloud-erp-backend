package com.erp.model.dmp.entity;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;
import org.apache.ibatis.type.JdbcType;


/**
 * <p>
 * 
 * </p>
 *
 * @author wtr
 * @since 2026-03-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("cfg_after_platform_shop")
public class CfgAfterPlatformShopEntity extends BaseEntity<CfgAfterPlatformShopEntity> {

    /**
    * 平台编码
    */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
    * 平台名称
    */
    @TableField("platform_name")
    private String platformName;
    /**
    * 店铺id json
    */
    @TableField(value = "shop_json", jdbcType = JdbcType.OTHER)
    private JSONObject shopJson;
    /**
    * 售后人员id json
    */
    @TableField(value = "cs_agent_json", jdbcType = JdbcType.OTHER)
    private JSONObject csAgentJson;


    public static final String PLATFORM_CODE = "platform_code";

    public static final String PLATFORM_NAME = "platform_name";

    public static final String SHOP_JSON = "shop_json";

    public static final String CS_AGENT_JSON = "cs_agent_json";

    public static final String SORT = "sort";

    @Override
    public Serializable pkVal() {
        return null;
    }

}