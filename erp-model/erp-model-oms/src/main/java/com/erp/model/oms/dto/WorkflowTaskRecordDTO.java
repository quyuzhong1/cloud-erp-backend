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
    *
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
    *
    */
    @Data
    @NoArgsConstructor
    public static class MqRequestDTO {

        private Map<String,Object> data;

    }




    /**
     * mq
     */
    @Data
    @NoArgsConstructor
    public static class MqResponseDTO {

        private Map<String,Object> data;

        private String errorMsg;
    }


}