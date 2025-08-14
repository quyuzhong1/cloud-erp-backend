package com.erp.model.tms.entity;

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
 * 物流-第三方渠道关系明细表
 * </p>
 *
 * @author zdy
 * @since 2025-07-30
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_third_channel_ref_detail")
public class LogisticsThirdChannelRefDetailEntity extends BaseEntity<LogisticsThirdChannelRefDetailEntity> {

    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 店铺id
    */
    @TableField("shop_id")
    private String shopId;
    /**
    * 店铺名称
    */
    @TableField("shop_name")
    private String shopName;
    /**
    * 平台
    */
    @TableField("dict_platform")
    private String dictPlatform;
    /**
    * 手机号码
    */
    @TableField("mobile")
    private String mobile;


    public static final String REMARK = "remark";

    public static final String DISABLED = "disabled";

    public static final String MAIN_ID = "main_id";

    public static final String SHOP_ID = "shop_id";

    public static final String SHOP_NAME = "shop_name";

    public static final String DICT_PLATFORM = "dict_platform";

    public static final String MOBILE = "mobile";

    @Override
    public Serializable pkVal() {
        return null;
    }

}