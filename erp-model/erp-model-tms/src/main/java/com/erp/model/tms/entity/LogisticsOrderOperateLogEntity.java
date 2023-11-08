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
 * 物流平台订单操作记录
 * </p>
 *
 * @author zdy
 * @since 2023-11-08
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("logistics_order_operate_log")
public class LogisticsOrderOperateLogEntity extends BaseEntity<LogisticsOrderOperateLogEntity> {

    /**
    * 业务类型（createOrder,confirmOrder,updateOrder）
    */
    @TableField("business_type")
    private String businessType;
    /**
    * 配置Id
    */
    @TableField("auth_id")
    private String authId;
    /**
    * 销售订单Id
    */
    @TableField("so_id")
    private String soId;
    /**
    * 销售订单编码
    */
    @TableField("so_code")
    private String soCode;
    /**
    * 物流平台类型
    */
    @TableField("logistics_platform")
    private String logisticsPlatform;
    /**
    * 请求状态（0请求中 1请求成功 2请求失败）
    */
    @TableField("status")
    private String status;
    /**
    * 操作类型
    */
    @TableField("operation")
    private String operation;
    /**
    * API请求参数
    */
    @TableField("request_param_json")
    private String requestParamJson;
    @TableField("msg")
    private String msg;
    /**
    * API响应参数
    */
    @TableField("response_param_json")
    private String responseParamJson;


    public static final String BUSINESS_TYPE = "business_type";

    public static final String AUTH_ID = "auth_id";

    public static final String SO_ID = "so_id";

    public static final String SO_CODE = "so_code";

    public static final String LOGISTICS_PLATFORM = "logistics_platform";

    public static final String STATUS = "status";

    public static final String OPERATION = "operation";

    public static final String REQUEST_PARAM_JSON = "request_param_json";

    public static final String MSG = "msg";

    public static final String RESPONSE_PARAM_JSON = "response_param_json";

    @Override
    public Serializable pkVal() {
        return null;
    }

}