package com.common.business.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @CreateTime: 2023-06-29  14:31
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DmpSyncMqDTO extends DmpSyncTaskIdDTO  implements Serializable {

    /**
     * dmp同步任务mq消息内容实体
     */
    private String mqData;


    public DmpSyncMqDTO(String id, String mqData) {
        super(id);
        this.mqData = mqData;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParamDTO {

        /**
         * 任务id
         */
        private String dmpSyncTaskId;

        /**
         * 同步状态
         */
        private String syncStatus;

        /**
         * 响应消息
         */
        private String responseMsg;
    }

}