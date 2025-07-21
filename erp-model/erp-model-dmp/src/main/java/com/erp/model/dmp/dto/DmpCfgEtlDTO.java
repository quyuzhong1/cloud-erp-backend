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
 * etl配置信息请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-07-21
*/
@Data
@NoArgsConstructor
public class DmpCfgEtlDTO implements Serializable {




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
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 流程编号
        */
        private String processCode;

        /**
        * 流程名称
        */
        private String processName;

        /**
        * 最后成功时间
        */
        private LocalDateTime lastTime;

        /**
        * 下次执行结束时间
        */
        private LocalDateTime nextTime;

        /**
        * 间隔时间长度，单位秒
        */
        private Integer intervalTime;

        /**
        * 覆盖时间，单位秒
        */
        private Integer overrideTime;

        /**
        * 最大重试次数
        */
        private Integer maxRetryCount;

        /**
        * 执行超时时间，单位秒
        */
        private Integer execTimeout;

        /**
        * 延迟时间，单位秒
        */
        private Integer dealyTime;

        /**
        * 最大间隔时间长度单位:秒, 0=按interval_time，-1=按当前时间-延迟时间
        */
        private Integer maxIntervalTime;

        /**
        * 扩展json
        */
        private String extendJson;


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
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * 流程编号
        */
        @NotBlank(message = "流程编号不能为空")
        @Size(max = 50,message = "流程编号最大长度不能超过50位")
        private String processCode;

        /**
        * 流程名称
        */
        @NotBlank(message = "流程名称不能为空")
        @Size(max = 100,message = "流程名称最大长度不能超过100位")
        private String processName;

        /**
        * 最后成功时间
        */
        private LocalDateTime lastTime;

        /**
        * 下次执行结束时间
        */
        private LocalDateTime nextTime;

        /**
        * 间隔时间长度，单位秒
        */
        @NotNull(message = "间隔时间长度，单位秒不能为空")
        private Integer intervalTime;

        /**
        * 覆盖时间，单位秒
        */
        @NotNull(message = "覆盖时间，单位秒不能为空")
        private Integer overrideTime;

        /**
        * 最大重试次数
        */
        @NotNull(message = "最大重试次数不能为空")
        private Integer maxRetryCount;

        /**
        * 执行超时时间，单位秒
        */
        @NotNull(message = "执行超时时间，单位秒不能为空")
        private Integer execTimeout;

        /**
        * 延迟时间，单位秒
        */
        @NotNull(message = "延迟时间，单位秒不能为空")
        private Integer dealyTime;

        /**
        * 最大间隔时间长度单位:秒, 0=按interval_time，-1=按当前时间-延迟时间
        */
        @NotNull(message = "最大间隔时间长度单位:秒, 0=按interval_time，不能为空")
        private Integer maxIntervalTime;

        /**
        * 扩展json
        */
        private String extendJson;


    }


}