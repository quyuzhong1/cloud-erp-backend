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
 * 销售平台物流渠道表
 * </p>
 *
 * @author lrp
 * @since 2023-12-05
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
    * erp服务平台（oms,tms）
    */
    @TableField("service_platform")
    private String servicePlatform;
    /**
    * 渠道状态0正常1.暂停2.已关闭（默认0）
    */
    @TableField("channel_status")
    private Integer channelStatus;
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
    * 是否可跟踪轨迹t是 f否（默认0）
    */
    @TableField("is_track")
    private Boolean isTrack;
    /**
    * 快递时效
    */
    @TableField("aging")
    private String aging;
    /**
    * 发货地国家二字码
    */
    @TableField("origin_country")
    private String originCountry;
    /**
    * 目的地国家二字码
    */
    @TableField("destination_country")
    private String destinationCountry;
    /**
    * 海外仓id
    */
    @TableField("overseas_warehouse_id")
    private String overseasWarehouseId;
    /**
    * 平台仓库code
    */
    @TableField("platform_warehouse_code")
    private String platformWarehouseCode;


    public static final String PLATFORM_CHANNEL_ID = "platform_channel_id";

    public static final String CN_NAME = "cn_name";

    public static final String EN_NAME = "en_name";

    public static final String FIELD_CODE = "code";

    public static final String CHANNEL_STATUS = "channel_status";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String SUPPLIER_CODE = "supplier_code";

    public static final String SHIPMENT_METHOD = "shipment_method";

    public static final String LOGISTICS_PLATFORM = "logistics_platform";

    public static final String SOURCE_DATA = "source_data";

    public static final String IS_TRACK = "is_track";

    public static final String FIELD_AGING = "aging";

    public static final String ORIGIN_COUNTRY = "origin_country";

    public static final String DESTINATION_COUNTRY = "destination_country";

    public static final String OVERSEAS_WAREHOUSE_ID = "overseas_warehouse_id";

    public static final String PLATFORM_WAREHOUSE_CODE = "platform_warehouse_code";

    @Override
    public Serializable pkVal() {
        return null;
    }

}