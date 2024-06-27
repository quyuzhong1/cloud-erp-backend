package com.erp.model.dmp.dto;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 外部系统接口明细补偿请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-27
*/
@Data
@NoArgsConstructor
public class DmpCfgInputCompensateDTO implements Serializable {




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
        * 输入信息id
        */
        private String mainId;

        /**
        * 拉取历史类型：day=一天之前,week=一周之前,month=一月之前
        */
        private String type;

        /**
        * 最后成功时间
        */
        private LocalDateTime lastTime;

        /**
        * 下次执行结束时间
        */
        private LocalDateTime nextTime;

        /**
        * 间隔时间长度单位秒
        */
        private Integer intervalTime;

        /**
        * 覆盖时间单位秒
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
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 扩展json
        */
        private String extendJson;

        /**
        * 延迟时间，单位秒
        */
        private Integer dealyTime;


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
        * 输入信息id
        */
        @NotBlank(message = "输入信息id不能为空")
        @Size(max = 50,message = "输入信息id最大长度不能超过50位")
        private String mainId;

        /**
        * 拉取历史类型：day=一天之前,week=一周之前,month=一月之前
        */
        @NotBlank(message = "拉取历史类型：day=一天之前,week=一周之前,month=一月之前不能为空")
        @Size(max = 50,message = "拉取历史类型：day=一天之前,week=一周之前,month=一月之前最大长度不能超过50位")
        private String type;

        /**
        * 最后成功时间
        */
        private LocalDateTime lastTime;

        /**
        * 下次执行结束时间
        */
        private LocalDateTime nextTime;

        /**
        * 间隔时间长度单位秒
        */
        @NotNull(message = "间隔时间长度单位秒不能为空")
        private Integer intervalTime;

        /**
        * 覆盖时间单位秒
        */
        @NotNull(message = "覆盖时间单位秒不能为空")
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
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * 扩展json
        */
        private String extendJson;

        /**
        * 延迟时间，单位秒
        */
        @NotNull(message = "延迟时间，单位秒不能为空")
        private Integer dealyTime;


    }


}