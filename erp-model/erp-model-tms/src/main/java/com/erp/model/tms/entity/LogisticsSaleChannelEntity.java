package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


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
    * 失效时间
    */
    @TableField("expire_time")
    private LocalDateTime expireTime;
    /**
    * 渠道状态0正常1.暂停2.已关闭（默认0）
    */
    @TableField("channel_status")
    private Integer channelStatus;
    /**
    * 是否可跟踪轨迹0是 1否（默认0）
    */
    @TableField("is_track")
    private Boolean isTrack;
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
}