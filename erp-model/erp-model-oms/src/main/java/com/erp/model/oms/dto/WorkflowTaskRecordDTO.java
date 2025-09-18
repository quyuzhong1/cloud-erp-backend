package com.erp.model.oms.dto;

import com.common.business.enums.SourceTypeEnum;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>
 * 任务节点记录表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-09-16
*/
@Data
@NoArgsConstructor
public class WorkflowTaskRecordDTO implements Serializable {




    /**
    * 新增任务
    */
    @Data
    @NoArgsConstructor
    public static class AddTaskDTO {

        private SourceTypeEnum sourceTypeEnum;

        private DictBasicTypeEnum dictBasicTypeEnum;

        private String sourceId;

        private String sourceCode;

        private String traceId;
    }


    /**
    * mq请求体
    */
    @Data
    @NoArgsConstructor
    public static class MqRequestDTO {

        private Map<String,Object> data;

    }




    /**
     * mq响应体
     */
    @Data
    @NoArgsConstructor
    public static class MqResponseDTO {

        private Map<String,Object> data;

        private String errorMsg;
    }

    /**
     * 异常任务报告
     */
    @Data
    @NoArgsConstructor
    public static class TaskErrorReportDTO {

        private String sourceType;

        private String dictBasicId;

        private String dictBasicName;

        private Integer errorCount;
    }


}