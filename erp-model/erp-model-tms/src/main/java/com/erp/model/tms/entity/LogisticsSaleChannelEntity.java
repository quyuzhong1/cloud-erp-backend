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
 * @since 2023-11-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_sale_channel")
public class LogisticsSaleChannelEntity extends BaseEntity<LogisticsSaleChannelEntity> {

    /**
    * 渠道id(物流平台原始id)
    */
    @TableField("platform_channel_id")
    private String platformChannelId;
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
    * 渠道状态0正常1.暂停2.已关闭（默认0）
    */
    @TableField("channel_status")
    private Integer channelStatus = 0;
    /**
    * 渠道供应商名称
    */
    @TableField("supplier_name")
    private String supplierName;
    /**
    * 渠道供应商编码
    */
    @TableField("supplier_code")
    private String supplierCode;
    /**
    * 发货方式
    */
    @TableField("shipment_method")
    private String shipmentMethod;
    /**
    * 物流平台类型
    */
    @TableField("logistics_platform")
    private String logisticsPlatform;
    /**
    * 获取接口的原始数据
    */
    @TableField("source_data")
    private String sourceData;
    /**
    * 是否可跟踪轨迹0是 1否（默认0）
    */
    @TableField("is_track")
    private Boolean isTrack;
    /**
    * 快递时效
    */
    @TableField("aging")
    private String aging;
    /**
     * 发货地
     */
    @TableField("origin_country")
    private String originCountry;
    /**
     * 目的地
     */
    @TableField("destination_country")
    private String destinationCountry;
    /**
     * 授权表ID
     */
    @TableField("auth_id")
    private String authId;

    /**
     * 是否已同步
     * false 未同步
     */
    @TableField("is_sync")
    private Boolean isSync;

    /**
     * 海外仓id
     */
    @TableField("overseas_warehouse_id")
    private String overseasWarehouseId;



    public static final String PLATFORM_CHANNEL_ID = "platform_channel_id";

    public static final String CN_NAME = "cn_name";

    public static final String EN_NAME = "en_name";

    public static final String CODE = "code";

    public static final String EXPIRE_TIME = "expire_time";

    public static final String CHANNEL_STATUS = "channel_status";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String SUPPLIER_CODE = "supplier_code";

    public static final String SHIPMENT_METHOD = "shipment_method";

    public static final String LOGISTICS_PLATFORM = "logistics_platform";

    public static final String SOURCE_DATA = "source_data";

    public static final String IS_TRACK = "is_track";

    public static final String AGING = "aging";

    @Override
    public Serializable pkVal() {
        return null;
    }

}