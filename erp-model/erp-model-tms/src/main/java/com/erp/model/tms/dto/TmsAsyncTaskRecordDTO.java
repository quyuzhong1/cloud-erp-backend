package com.erp.model.tms.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.Map;

import com.common.business.dto.base.SuperDTO;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 异步任务记录请求响应实体
 * </p>
 *
 * @author jack
 * @since 2026-01-28
*/
@Data
@NoArgsConstructor
public class TmsAsyncTaskRecordDTO implements Serializable {

    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型
         */
        private String tabFlag;
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;

    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 勾选的id集合
         */
        private List<String> ids;

    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingDetailParamDTO extends SortDTO {

        /**
         * 勾选的id
         */
        @NotBlank(message = "id不能为空")
        private String id;

    }


    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String  id;
        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;

        /**
         * 任务id
         */
        private String code;

        /**
         * 执行系统
         */
        private String sysModule;
        private String sysModuleName;

        /**
         * 单据名称
         */
        private String businessType;
        private String businessTypeName;

        /**
         * 开始时间
         */
        private LocalDateTime startTime;

        /**
         * 结束时间
         */
        private LocalDateTime endTime;

        /**
         * 任务超时时间 单位：秒
         */
        private Integer execTimeout;

        /**
         * 重试次数
         */
        private Integer retryTimes;

        /**
         * 状态：pending=待执行,ing=进行中, finish=已完成, failed=失败 AsyncTaskRecordStatusEnum
         */
        private String status;
        private String statusName;

        /**
         * 错误信息
         */
        private String errorData;

        /**
         * 执行数量
         */
        private Integer detailCount;

        /**
         * 错误数量
         */
        private Integer errorCount;

        /**
         * 执行类型：auto=自动, manual=手动 AsyncTaskRecordExecTypeEnum
         */
        private String execType;
        private String execTypeName;

    }

//     /**
//     * 状态统计
//     */
//     @Data
//     @NoArgsConstructor
//     @AllArgsConstructor
//     public static class TaskDTO {
//
//         /**
//         *
//         */
//         private List<String> ids;
//
//         /**
//          *核算日期
//          */
//         private String reportDate;
//         /**
//          * 主任务id
//          */
//         private String taskId;
//         /**
//          * 单据类型
//          */
//         private String businessType;
//         /**
//          *物流标签类型
//          */
//         private String type;
//     }


    /**
     * 明细分页列表
     */
    @Data
    @NoArgsConstructor
    public static class DetailListDTO {
        /**
         * 主键id
         */
        private String id;
        /**
         * 明细id
         */
        private String detailId;
        /**
         * 单据id
         */
        private String businessId;
        /**
         * 业务单号
         */
        private String businessCode;
        /**
         * 错误原因
         */
        private String errorData;
    }


    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO {
        /**
         * 主键id
         */
        @NotBlank(message = "id不能为空")
        private String id;

        /**
         * 开始时间
         */
        @NotNull(message = "开始时间不能为空")
        private Long startTime;

    }

    /**
     * 手动创建任务
     */
    @Data
    @NoArgsConstructor
    public static class PushParamsDTO {

        /**
         * ids
         */
        private List<String> ids;
        /**
         *核算日期
         */
        private String reportDate;
        /**
         * 主任务id
         */
        private String taskId;
        /**
         * 单据类型
         */
        private String businessType;
        /**
         *物流标签类型
         */
        private String type;

        /**
         *开始日期
         */
        private LocalDate startDate;
        /**
         *结束日期
         */
        private LocalDate endDate;

        /**
         *开始日期
         */
        private LocalDateTime startTime;
        /**
         *结束日期
         */
        private LocalDateTime endTime;

        /**
         * 游标分页：上一批最后一条记录的 id（首次传空字符串）
         * 仅在服务内部循环中使用，不随 MQ 消息体传递
         */
        private String lastId;

        /**
         * 游标分页：每批查询条数
         */
        private Integer batchSize;

    }

    /**
     * 内部类：批次处理结果
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BatchProcessResult {

        private int successCount;

        private int failedCount;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {

        /**
         * 任务id
         */
        @NotBlank(message = "任务id不能为空")
        @Size(max = 32, message = "任务id最大长度不能超过32位")
        private String code;

        /**
         * 执行系统
         */
        @NotBlank(message = "执行系统不能为空")
        @Size(max = 32, message = "执行系统最大长度不能超过32位")
        private String sysModule;

        /**
         * 单据名称
         */
        @NotBlank(message = "单据名称不能为空")
        @Size(max = 50, message = "单据名称最大长度不能超过50位")
        private String businessType;

        /**
         * 开始时间
         */
        @NotNull(message = "开始时间不能为空")
        private LocalDateTime startTime;

        /**
         * 结束时间
         */
        @NotNull(message = "结束时间不能为空")
        private LocalDateTime endTime;

        /**
         * 任务超时时间 单位：秒
         */
        private Integer execTimeout;

        /**
         * 重试次数
         */
        private Integer retryTimes;

        /**
         * 状态：pending=待执行,ing=进行中, finish=已完成, failed=失败
         */
        @NotBlank(message = "状态不能为空")
        private String status;

        /**
         * json
         */
        private String dataJson;

        /**
         * 错误信息
         */
        @Size(max = 500, message = "错误信息最大长度不能超过500位")
        private String errorData;

        /**
         * 执行数量
         */
        private Integer detailCount;

        /**
         * 错误数量
         */
        private Integer errorCount;

        /**
         * 执行类型：auto=自动, manual=手动
         */
        private String execType;
    }


    /**
     * 任务执行结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskResult {
        private String name;
        private boolean success;
        private String errorMsg;
        private long duration;

        public boolean isSuccess() {
            return success;
        }
    }


}