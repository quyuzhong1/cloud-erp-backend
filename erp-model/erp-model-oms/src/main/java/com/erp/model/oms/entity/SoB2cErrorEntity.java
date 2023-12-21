package com.erp.model.oms.entity;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.FastjsonTypeHandler;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * B2C销售订单异常表
 * </p>
 *
 * @author lambda
 * @since 2023-12-20
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value="so_b2c_error")
public class SoB2cErrorEntity extends BaseEntity<SoB2cErrorEntity>{

    /**
    * 销售订单id
    */
    @TableField("main_id")
    private String mainId;
    /**
     * 异常类型
     *  submitDelivery 提交发货异常
     *  signDelivery 标记发货异常
     *  getLogisticsCode 获取物流单异常
    */
    @TableField("type")
    private String type;
    /**
    * 传的json 字符串
    */
    @TableField(value="param_json")
    private Object paramJson;
    /**
    * 错误信息
    */
    @TableField("message")
    private String message;
    /**
    * 返回的json 字符串
    */
    @TableField(value="return_json")
    private Object returnJson;


    public static final String MAIN_ID = "main_id";

    public static final String TYPE = "type";

    public static final String PARAM_JSON = "param_json";

    public static final String MESSAGE = "message";

    public static final String RETURN_JSON = "return_json";

    @Override
    public Serializable pkVal() {
        return null;
    }

}