package com.erp.model.oms.dto;

import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.WorkflowTaskRecordTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
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

        private WorkflowTaskRecordTypeEnum sourceTypeEnum;

        private DictBasicTypeEnum dictBasicTypeEnum;

        private String sourceId;

        private String sourceCode;

        private String traceId;

        //第一个节点的入参
        private Map<String, Object> firstNodeInputData = new HashMap<>();
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