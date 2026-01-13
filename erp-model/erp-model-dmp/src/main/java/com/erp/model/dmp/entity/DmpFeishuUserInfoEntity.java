package com.erp.model.dmp.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * DMP飞书用户信息
 * </p>
 *
 * @author jack
 * @since 2026-01-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("dmp_feishu_user_info")
public class DmpFeishuUserInfoEntity extends BaseEntity<DmpFeishuUserInfoEntity> {

    /**
    * 任务转换ID
    */
    @TableField("convert_id")
    private String convertId;
    /**
    * 店铺ID
    */
    @TableField("next_level_id")
    private String nextLevelId;
    /**
    * 任务来源唯一加密代号
    */
    @TableField("unique_encrypt")
    private String uniqueEncrypt;
    /**
    * 任务数据加密代号
    */
    @TableField("data_encrypt")
    private String dataEncrypt;
    /**
    * 输入任务id
    */
    @TableField("input_task_id")
    private String inputTaskId;
    /**
    * 	事件 ID
    */
    @TableField("event_id")
    private String eventId;
    /**
    * 事件类型
    */
    @TableField("event_type")
    private String eventType;
    /**
    * 用户的 open_id
    */
    @TableField("open_id")
    private String openId;
    /**
    * 用户的 union_id
    */
    @TableField("union_id")
    private String unionId;
    /**
    * 用户的 user_id
    */
    @TableField("user_id")
    private String userId;
    /**
    * 用户名
    */
    @TableField("name")
    private String name;
    /**
    * 手机号
    */
    @TableField("mobile")
    private String mobile;
    /**
    * 性别 0=未知 1 = 男 2=女 3=其他
    */
    @TableField("gender")
    private Integer gender;
    /**
    * 邮箱
    */
    @TableField("email")
    private String email;
    /**
    * 是否为离职状态 true=是 false=否
    */
    @TableField("is_resigned")
    private Boolean isResigned;
    /**
    * 原始json
    */
    @TableField("data_json")
    private String dataJson;


    public static final String CONVERT_ID = "convert_id";

    public static final String NEXT_LEVEL_ID = "next_level_id";

    public static final String UNIQUE_ENCRYPT = "unique_encrypt";

    public static final String DATA_ENCRYPT = "data_encrypt";

    public static final String INPUT_TASK_ID = "input_task_id";

    public static final String EVENT_ID = "event_id";

    public static final String EVENT_TYPE = "event_type";

    public static final String OPEN_ID = "open_id";

    public static final String UNION_ID = "union_id";

    public static final String USER_ID = "user_id";

    public static final String NAME = "name";

    public static final String MOBILE = "mobile";

    public static final String GENDER = "gender";

    public static final String EMAIL = "email";

    public static final String IS_RESIGNED = "is_resigned";

    public static final String DATA_JSON = "data_json";

    @Override
    public Serializable pkVal() {
        return null;
    }

}