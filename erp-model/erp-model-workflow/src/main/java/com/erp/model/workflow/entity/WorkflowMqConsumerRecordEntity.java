package com.erp.model.workflow.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * mq消费记录
 * </p>
 *
 * @author jack
 * @since 2025-11-04
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("mq_consumer_record")
public class WorkflowMqConsumerRecordEntity extends BaseEntity<WorkflowMqConsumerRecordEntity> {

    /**
    * 消费topic
    */
    @TableField("topic")
    private String topic;
    /**
    * 消费tag
    */
    @TableField("tag")
    private String tag;
    /**
    * 消费者group
    */
    @TableField("consumer_group")
    private String consumerGroup;
    /**
    * 原始消息内容
    */
    @TableField("data_json")
    private String dataJson;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;


    public static final String TOPIC = "topic";

    public static final String TAG = "tag";

    public static final String CONSUMER_GROUP = "consumer_group";

    public static final String DATA_JSON = "data_json";

    public static final String REMARK = "remark";

    @Override
    public Serializable pkVal() {
        return null;
    }

}