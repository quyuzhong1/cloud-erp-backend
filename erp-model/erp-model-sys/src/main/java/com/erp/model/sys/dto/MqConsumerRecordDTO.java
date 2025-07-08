package com.erp.model.sys.dto;

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
 * @since 2025-05-29
*/
@Data
@NoArgsConstructor
public class MqConsumerRecordDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 消费topic
        */
        private String topic;

        /**
        * 消费tag
        */
        private String tag;

        /**
        * 消费者group
        */
        private String consumerGroup;

        /**
        * 原始消息内容
        */
        private String dataJson;

        /**
        * 备注
        */
        private String remark;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

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

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class MqDTO  {
        /**
         * 表名
         */
        private String table;
        /**
         * 操作类型
         */
        private String operationType;
        /**
         * 数据库
         */
        private String db;
        /**
         * 单据数据
         */
        private Map<String, Object> dataJson;
        /**
         * mq消费记录主表id
         */
        private String mqConsumerRecordId;
        /**
         * 单据类型
         */
        private String businessKey;
        /**
         * 变动字段
         */
        private List<String> diffFields;
    }
}
