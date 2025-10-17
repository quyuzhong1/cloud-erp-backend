package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 组包计划明细
 * </p>
 *
 * @author zdy
 * @since 2025-10-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("package_plan_detail")
public class PackagePlanDetailEntity extends BaseEntity<PackagePlanDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * 销售订单code
    */
    @TableField("so_code")
    private String soCode;
    /**
    * 销售订单id
    */
    @TableField("so_id")
    private String soId;
    /**
    * 物流渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 物流渠道名
    */
    @TableField("logistics_channel_name")
    private String logisticsChannelName;
    /**
    * 物流跟踪号
    */
    @TableField("track_no")
    private String trackNo;
    /**
    * 运输单号
    */
    @TableField("transport_no")
    private String transportNo;

    /**
     * 小包条码
     * 多个以逗号分隔
     */
    @TableField("barcode")
    private String barcode;
    /**
     * 平台订单号
     */
    @TableField("platform_code")
    private String platformCode;

    public static final String MAIN_ID = "main_id";

    public static final String SO_CODE = "so_code";

    public static final String SO_ID = "so_id";

    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String LOGISTICS_CHANNEL_NAME = "logistics_channel_name";

    public static final String TRACK_NO = "track_no";

    public static final String TRANSPORT_NO = "transport_no";

    @Override
    public Serializable pkVal() {
        return null;
    }

}