package com.erp.model.tms.entity;

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
 * 销售平台物流渠道表
 * </p>
 *
 * @author zdy
 * @since 2023-11-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_sale_channel")
public class LogisticsSaleChannelEntity extends BaseEntity<LogisticsSaleChannelEntity> {

    /**
    * 渠道id(物流平台原始id)
    */
    @TableField("channel_id")
    private String channelId;
    /**
    * 渠道名称(默认中文)
    */
    @TableField("cn_name")
    private String cnName;
    /**
    * 渠道名称(英文)
    */
    @TableField("en_name")
    private String enName;
    /**
    * 渠道编码
    */
    @TableField("code")
    private String code;
    /**
    * 时效
    */
    @TableField("effective_time")
    private LocalDateTime effectiveTime;
    /**
    * 渠道状态0正常1.暂停2.已关闭（默认0）
    */
    @TableField("channel_status")
    private Integer channelStatus;
    /**
    * 是否可跟踪轨迹0是 1否（默认0）
    */
    @TableField("track_status")
    private Integer trackStatus;
    /**
    * 渠道供应商名称
    */
    @TableField("provider_name")
    private String providerName;
    /**
    * 渠道供应商编码
    */
    @TableField("provider_code")
    private String providerCode;
    /**
    * 发货方式
    */
    @TableField("shipment_method")
    private String shipmentMethod;
    /**
    * 物流平台类型
    */
    @TableField("sales_platform")
    private String salesPlatform;
    /**
    * 获取接口的原始数据
    */
    @TableField("source_data")
    private String sourceData;


    public static final String CHANNEL_ID = "channel_id";

    public static final String CN_NAME = "cn_name";

    public static final String EN_NAME = "en_name";

    public static final String CODE = "code";

    public static final String EFFECTIVE_TIME = "effective_time";

    public static final String CHANNEL_STATUS = "channel_status";

    public static final String TRACK_STATUS = "track_status";

    public static final String PROVIDER_NAME = "provider_name";

    public static final String PROVIDER_CODE = "provider_code";

    public static final String SHIPMENT_METHOD = "shipment_method";

    public static final String SALES_PLATFORM = "sales_platform";

    public static final String SOURCE_DATA = "source_data";

    @Override
    public Serializable pkVal() {
        return null;
    }

}