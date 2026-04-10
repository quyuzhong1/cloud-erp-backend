package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * B2C销售订单异常表
 * </p>
 *
 * @author lambda
 * @since 2023-12-20
*/
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@TableName("so_b2c_error")
public class SoB2cErrorEntity extends BaseEntity<SoB2cErrorEntity> {

    /**
    * 销售订单id
    */
    @TableField("main_id")
    private String mainId;
    /**
     * 前端显示的异常类型
     * {@link com.erp.model.oms.enums.SoB2cErrorTypeEnum}
    */
    @TableField("type")
    private String type;
    /**
    * 传的json 字符串
    */
    @TableField(value="param_json")
    private String paramJson;
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
    /**
     * 销售订单明细id
     */
    @TableField("detail_id")
    private String detailId;
    /**
     * 系统自动重试次数
     */
    @TableField("retry_count")
    private Integer retryCount = 0;

    /**
     * 错误码
     */
    @TableField("code")
    private String code;

    public static final String MAIN_ID = "main_id";

    public static final String TYPE = "type";

    public static final String PARAM_JSON = "param_json";

    public static final String MESSAGE = "message";

    public static final String RETURN_JSON = "return_json";



}