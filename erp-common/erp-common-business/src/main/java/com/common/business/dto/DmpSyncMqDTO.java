package com.common.business.dto;

import com.common.business.enums.SourceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @CreateTime: 2023-06-29  14:31
 * @Author: zhangchunlin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DmpSyncMqDTO extends DmpSyncTaskIdDTO implements Serializable {

    /**
     * dmp同步任务mq消息内容实体
     */
    private String mqData;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParamDTO {

        /**
         * 任务id
         */
        private String dmpSyncTaskId;

        /**
         * 版本
         */
        private Integer version;

        /**
         * 同步状态
         */
        private String syncStatus;

        /**
         * 响应消息
         */
        private String responseMsg;

        public ParamDTO (String dmpSyncTaskId,String syncStatus,String responseMsg) {
            this.dmpSyncTaskId = dmpSyncTaskId;
            this.syncStatus = syncStatus;
            this.responseMsg = responseMsg;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncParamDTO {

        /**
         * 来源集合
         */
        @NotEmpty(message = "来源集合不能为空")
        private List<SyncParamDetailDTO> sourceDetailList;

        /**
         * 来源类型
         */
        @NotNull(message = "来源类型不能为空")
        private SourceTypeEnum sourceType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncParamDetailDTO {

        /**
         * 来源id
         */
        @NotBlank(message = "来源id不能为空")
        private String sourceId;

        /**
         * 同步操作
         */
        @NotBlank(message = "同步操作不能为空")
        private String syncOperate;
    }
}