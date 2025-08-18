package com.common.business.dto.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author Will
 * @version 1.0
 * @date 2023/8/3 12:17
 */
@Data
public class BaseDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QtyDTO implements Serializable {

        /**
         * id
         */
        private String id;

        /**
         * 数量
         */
        private Integer qty;

    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportDTO implements Serializable {
        /**
         * 文件URL
         */
        private String fileUrl;
        /**
         * 任务id[后端使用]
         */
        private String taskId;
        /**
         * 处理方式 add 新增 update 更新  addOrUpdate 新增或更新
         * ImportTypeEnum
         */
        private String importType;
        /**
         * 数据总条数
         */
        private Integer importCount;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImportResultDTO extends ImportDTO {
        /**
         * 失败文件URL
         */
        private String errorUrl;
        /**
         * 数据总条数
         */
        private Integer count;
        /**
         * 任务状态
         * FileTaskStatusEnum
         */
        private String status;
        /**
         * 异常描述
         */
        private String remark;
        /**
         * 开始时间
         */
        private LocalDateTime startTime;
        /**
         * 结束时间
         */
        private LocalDateTime finishTime;
    }
}
