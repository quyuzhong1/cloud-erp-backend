package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * b2c发货拦截单
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("so_b2c_delivery_intercept")
public class SoB2cDeliveryInterceptEntity extends BaseEntity<SoB2cDeliveryInterceptEntity> {

    /**
    * 单据编号
    */
    @TableField("code")
    private String code;
    /**
    * 来源id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源单号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 来源类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 处理状态 waitHandle:待处理 handle:已处理 cancel:已取消
    */
    @TableField("handle_status")
    private String handleStatus;
    /**
    * 处理结果 success：拦截成功  failure：拦截失败
    */
    @TableField("handle_result")
    private String handleResult;
    /**
    * 处理备注
    */
    @TableField("handle_remark")
    private String handleRemark;
    /**
    * 销售单号
    */
    @TableField("so_code")
    private String soCode;
    /**
    * 发货单号
    */
    @TableField("so_delivery_code")
    private String soDeliveryCode;
    /**
    * 销售出库单号
    */
    @TableField("so_outstock_code")
    private String soOutstockCode;
    /**
    * 物流渠道id
    */
    @TableField("logistics_channel_id")
    private String logisticsChannelId;
    /**
    * 物流渠道名称
    */
    @TableField("logistics_channel_name")
    private String logisticsChannelName;
    /**
    * 运单号
    */
    @TableField("transport_no")
    private String transportNo;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 处理人id
    */
    @TableField("handle_user_id")
    private String handleUserId;
    /**
    * 处理人名称
    */
    @TableField("handle_user_name")
    private String handleUserName;
    /**
    * 处理时间
    */
    @TableField("handle_time")
    private LocalDateTime handleTime;
    /**
    * 取消状态
    */
    @TableField("cancel_status")
    private String cancelStatus;
    /**
     * 拦截状态
     */
    @TableField("intercept_status")
    private String interceptStatus;
    /**
     * 销售单id
     */
    @TableField("so_id")
    private String soId;
    /**
     * 单据类型：B2B B2C
     */
    @TableField("bill_type")
    private String billType;
    /**
     * 发货单id
     */
    @TableField("delivery_id")
    private String deliveryId;

    

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_TYPE = "source_type";

    public static final String HANDLE_STATUS = "handle_status";

    public static final String HANDLE_RESULT = "handle_result";

    public static final String HANDLE_REMARK = "handle_remark";

    public static final String SO_CODE = "so_code";

    public static final String SO_DELIVERY_CODE = "so_delivery_code";

    public static final String SO_OUTSTOCK_CODE = "so_outstock_code";

    public static final String LOGISTICS_CHANNEL_ID = "logistics_channel_id";

    public static final String LOGISTICS_CHANNEL_NAME = "logistics_channel_name";

    public static final String TRANSPORT_NO = "transport_no";

    

    public static final String HANDLE_USER_ID = "handle_user_id";

    public static final String HANDLE_USER_NAME = "handle_user_name";

    public static final String HANDLE_TIME = "handle_time";

    @Override
    public Serializable pkVal() {
        return null;
    }

}