package com.erp.model.workflow.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * mq消费记录请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-11-04
*/
@Data
@NoArgsConstructor
public class WorkflowMqConsumerRecordDTO implements Serializable {

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class MqDTO  {
        /**
         * 单据数据
         */
        private Map<String, Object> dataJson;

        /**
         * 单据类型
         */
        @NotBlank(message = "单据类型不能为空")
        private String businessKey;

        /**
         * 消费topic
         */
        @NotBlank(message = "消费topic不能为空")
        private String topic;

        /**
         * 消费tag
         */
        @NotBlank(message = "消费tag不能为空")
        private String tag;

        /**
         * 消费者group
         */
        @NotBlank(message = "消费者group不能为空")
        private String consumerGroup;
    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 消费topic
        */
        @NotBlank(message = "消费topic不能为空")
        @Size(max = 255,message = "消费topic最大长度不能超过255位")
        private String topic;

        /**
        * 消费tag
        */
        @NotBlank(message = "消费tag不能为空")
        @Size(max = 255,message = "消费tag最大长度不能超过255位")
        private String tag;

        /**
        * 消费者group
        */
        @NotBlank(message = "消费者group不能为空")
        @Size(max = 255,message = "消费者group最大长度不能超过255位")
        private String consumerGroup;

        /**
        * 原始消息内容
        */
        @NotBlank(message = "原始消息内容不能为空")
        private String dataJson;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;


    }


}