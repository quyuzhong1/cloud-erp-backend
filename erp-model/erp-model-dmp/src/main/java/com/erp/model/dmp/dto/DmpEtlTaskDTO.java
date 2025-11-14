package com.erp.model.dmp.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * etl任务请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-07-21
*/
@Data
@NoArgsConstructor
public class DmpEtlTaskDTO implements Serializable {




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
        * 拉取数据配置id
        */
        private String cfgEtlId;

        /**
        * 执行条件的开始时间
        */
        private LocalDateTime startTime;

        /**
        * 执行条件的结束时间
        */
        private LocalDateTime endTime;

        /**
        * 状态：init=待执行，finish=完成，error=异常
        */
        private String status;

        /**
        * 异常原因
        */
        private String errorMessage;

        /**
        * 错误次数
        */
        private Integer errorCount;

        /**
        * 执行超时时间，单位秒
        */
        private Integer execTimeout;

        /**
        * 扩展字段
        */
        private String extendJson;

        /**
        * 下次执行任务时间
        */
        private LocalDateTime nextExecTime;

        /**
        * 实例id
        */
        private String etlInstanceId;


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
        * 拉取数据配置id
        */
        @NotBlank(message = "拉取数据配置id不能为空")
        @Size(max = 50,message = "拉取数据配置id最大长度不能超过50位")
        private String cfgEtlId;

        /**
        * 执行条件的开始时间
        */
        private LocalDateTime startTime;

        /**
        * 执行条件的结束时间
        */
        private LocalDateTime endTime;

        /**
        * 状态：init=待执行，finish=完成，error=异常
        */
        @NotBlank(message = "状态：init=待执行，finish=完成，error=异常不能为空")
        @Size(max = 50,message = "状态：init=待执行，finish=完成，error=异常最大长度不能超过50位")
        private String status;

        /**
        * 异常原因
        */
        private String errorMessage;

        /**
        * 错误次数
        */
        @NotNull(message = "错误次数不能为空")
        private Integer errorCount;

        /**
        * 执行超时时间，单位秒
        */
        private Integer execTimeout;

        /**
        * 扩展字段
        */
        private String extendJson;

        /**
        * 下次执行任务时间
        */
        private LocalDateTime nextExecTime;

        /**
        * 实例id
        */
        @NotBlank(message = "实例id不能为空")
        @Size(max = 50,message = "实例id最大长度不能超过50位")
        private String etlInstanceId;


    }


}