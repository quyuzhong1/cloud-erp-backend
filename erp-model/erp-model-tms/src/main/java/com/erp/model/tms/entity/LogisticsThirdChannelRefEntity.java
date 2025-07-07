package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 物流-第三方渠道关系表
 * </p>
 *
 * @author zdy
 * @since 2025-05-29
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_third_channel_ref")
public class LogisticsThirdChannelRefEntity extends BaseEntity<LogisticsThirdChannelRefEntity> {

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
    * 是否推送电话
    */
    @TableField("is_push_mobile")
    private Boolean isPushMobile;
    /**
    * 物流渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 渠道名称
    */
    @TableField("logistics_channel_name")
    private String logisticsChannelName;
    /**
    * 渠道代码
    */
    @TableField("logistics_channel_code")
    private String logisticsChannelCode;
    /**
    * 第三方渠道编码
    */
    @TableField("third_supplier_code")
    private String thirdSupplierCode;
    /**
    * 第三方物流商名称
    */
    @TableField("third_supplier_name")
    private String thirdSupplierName;

    /**
     * 平台类型 TRACK123
     */
    @TableField("platform_type")
    private String platformType;

    /**
     * 手机号
     */
    @TableField("mobile")
    private String mobile;


    public static final String REMARK = "remark";

    public static final String DISABLED = "disabled";

    public static final String IS_PUSH_MOBILE = "is_push_mobile";

    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String LOGISTICS_CHANNEL_NAME = "logistics_channel_name";

    public static final String LOGISTICS_CHANNEL_CODE = "logistics_channel_code";

    public static final String THIRD_SUPPLIER_CODE = "third_supplier_code";

    public static final String THIRD_SUPPLIER_NAME = "third_supplier_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}