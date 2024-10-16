package com.erp.model.oms.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * <p>
 * B2C销售订单异常表
 * </p>
 *
 * @author lambda
 * @since 2023-12-20
*/
@Data
@Accessors(chain = true)
@TableName("so_b2c_error")
public class SoB2cErrorEntity implements Serializable{


    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 创建人id
     */
    @TableField(value = "create_user_id", fill = FieldFill.INSERT)
    private String createUserId;

    /**
     * 创建人名称
     */
    @TableField(value = "create_user_name", fill = FieldFill.INSERT)
    private String createUserName;

    /**
     * 创建时间
     */
    @TableField(value = "create_time" , fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改人id
     */
    @TableField(value = "update_user_id", fill = FieldFill.INSERT_UPDATE)
    private String updateUserId;

    /**
     * 修改人名称
     */
    @TableField(value = "update_user_name", fill = FieldFill.INSERT_UPDATE)
    private String updateUserName;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 乐观锁版本号
     */
    @Version
    private Integer version;

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


    public static final String MAIN_ID = "main_id";

    public static final String TYPE = "type";

    public static final String PARAM_JSON = "param_json";

    public static final String MESSAGE = "message";

    public static final String RETURN_JSON = "return_json";



}