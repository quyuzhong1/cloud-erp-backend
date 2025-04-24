package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 物流平台服务表
 * </p>
 *
 * @author Lambda
 * @since 2024-03-04
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_service_platform")
public class LogisticsServicePlatformEntity extends BaseEntity<LogisticsServicePlatformEntity> {

    /**
    * 服务名称
     * 对应速卖通接口 aliexpress.logistics.redefining.listlogisticsservice 里面的 display_name值
    */
    @TableField("service_name")
    private String serviceName;

    /**
     * 服务类型
     * 对应速卖通接口 aliexpress.logistics.redefining.listlogisticsservice 里面的 service_name 值
     */
    @TableField("logistics_type")
    private String logisticsType;
    /**
    * 物流平台
    */
    @TableField("logistics_platform")
    private String logisticsPlatform;


    public static final String SERVICE_NAME = "service_name";

    public static final String LOGISTICS_PLATFORM = "logistics_platform";

    @Override
    public Serializable pkVal() {
        return null;
    }

}