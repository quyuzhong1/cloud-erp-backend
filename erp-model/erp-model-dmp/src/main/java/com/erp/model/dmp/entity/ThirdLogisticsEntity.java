package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 三方渠道表
 * </p>
 *
 * @author lrp
 * @since 2024-12-11
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("third_logistics")
public class ThirdLogisticsEntity extends BaseEntity<ThirdLogisticsEntity> {

    /**
    * 是否禁用/停用 true 是 false 不是
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * 平台类型：lingxing领星
    */
    @TableField("platform_type")
    private String platformType;
    /**
    * 物流商类型
    */
    @TableField("type")
    private String type;
    /**
    * 物流商id
    */
    @TableField("logistics_supplier_id")
    private String logisticsSupplierId;
    /**
    * 物流商名称
    */
    @TableField("logistics_supplier_name")
    private String logisticsSupplierName;
    /**
    * 物流渠道Id
    */
    @TableField("channel_id")
    private String channelId;
    /**
    * 物流渠道名称
    */
    @TableField("channel_name")
    private String channelName;
    /**
    * 平台更新时间
    */
    @TableField("platform_update_time")
    private LocalDateTime platformUpdateTime;
    /**
    * 平台创建时间
    */
    @TableField("platform_create_time")
    private LocalDateTime platformCreateTime;


    public static final String DISABLED = "disabled";

    public static final String PLATFORM_TYPE = "platform_type";

    public static final String TYPE = "type";

    public static final String LOGISTICS_SUPPLIER_ID = "logistics_supplier_id";

    public static final String LOGISTICS_SUPPLIER_NAME = "logistics_supplier_name";

    public static final String CHANNEL_ID = "channel_id";

    public static final String CHANNEL_NAME = "channel_name";

    public static final String PLATFORM_UPDATE_TIME = "platform_update_time";

    public static final String PLATFORM_CREATE_TIME = "platform_create_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}