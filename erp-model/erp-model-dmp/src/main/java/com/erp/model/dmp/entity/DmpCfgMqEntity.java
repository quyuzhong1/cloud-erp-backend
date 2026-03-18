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
 * 输入输出mq信息
 * </p>
 *
 * @author shukai
 * @since 2026-03-17
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("dmp_cfg_mq")
public class DmpCfgMqEntity extends BaseEntity<DmpCfgMqEntity> {

    /**
    * 系统id
    */
    @TableField("system_id")
    private String systemId;
    /**
    * 输入输出类型：input=输入，output=输出
    */
    @TableField("type")
    private String type;
    /**
    * mq类型:rocketmq=rocketmq,kafka=kafka
    */
    @TableField("mq_type")
    private String mqType;
    /**
    * 主机
    */
    @TableField("host")
    private String host;
    /**
    * 端口
    */
    @TableField("port")
    private Integer port;
    /**
    * 用户名
    */
    @TableField("user_name")
    private String userName;
    /**
    * 密码
    */
    @TableField("pass_word")
    private String passWord;
    /**
    * 主题
    */
    @TableField("topic")
    private String topic;
    /**
    * tag信息
    */
    @TableField("tag")
    private String tag;
    /**
    * 是否禁用
    */
    @TableField("disabled")
    private Boolean disabled;
    /**
    * mq分组
    */
    @TableField("mq_group")
    private String mqGroup;


    public static final String SYSTEM_ID = "system_id";

    public static final String TYPE = "type";

    public static final String MQ_TYPE = "mq_type";

    public static final String HOST = "host";

    public static final String PORT = "port";

    public static final String USER_NAME = "user_name";

    public static final String PASS_WORD = "pass_word";

    public static final String TOPIC = "topic";

    public static final String TAG = "tag";

    public static final String DISABLED = "disabled";

    public static final String MQ_GROUP = "mq_group";

    @Override
    public Serializable pkVal() {
        return null;
    }

}