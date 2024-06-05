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
 * 处理mongo业务数据任务请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-05-07
*/
@Data
@NoArgsConstructor
public class DmpMongoHandleTaskDTO implements Serializable {




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
        * 业务处理类型
        */
        private String handleType;

        /**
        * 业务处理类型名称
        */
        private String handleTypeName;

        /**
        * 任务每次处理的数量
        */
        private Integer handleCount;

        /**
        * 最后处理的mongo主键ID
        */
        private LocalDateTime lastId;

        /**
        * 下次处理执行时间
        */
        private LocalDateTime nextTime;

        /**
        * 执行时间间隔，单位描述
        */
        private Integer intervalTime;


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
        * 业务处理类型
        */
        @NotBlank(message = "业务处理类型不能为空")
        @Size(max = 100,message = "业务处理类型最大长度不能超过100位")
        private String handleType;

        /**
        * 业务处理类型名称
        */
        @NotBlank(message = "业务处理类型名称不能为空")
        @Size(max = 100,message = "业务处理类型名称最大长度不能超过100位")
        private String handleTypeName;

        /**
        * 任务每次处理的数量
        */
        @NotNull(message = "任务每次处理的数量不能为空")
        private Integer handleCount;

        /**
        * 最后处理的mongo主键ID
        */
        @NotNull(message = "最后处理的mongo主键ID不能为空")
        private LocalDateTime lastId;

        /**
        * 下次处理执行时间
        */
        @NotNull(message = "下次处理执行时间不能为空")
        private LocalDateTime nextTime;

        /**
        * 执行时间间隔，单位描述
        */
        @NotNull(message = "执行时间间隔，单位描述不能为空")
        private Integer intervalTime;


    }


}