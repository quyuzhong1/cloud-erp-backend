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
 * 本地消息表请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-08-21
*/
@Data
@NoArgsConstructor
public class DmpPushMessageDTO implements Serializable {




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
        * 本地消息id
        */
        private String messageId;

        /**
        * 目标系统
        */
        private String targetPlatform;

        /**
        * 来源类型
        */
        private String sourceType;

        /**
        * 来源id
        */
        private String sourceId;

        /**
        * 来源编号
        */
        private String sourceCode;

        /**
        * 操作类型
        */
        private String syncOperate;

        /**
        * 推送数据
        */
        private String pushData;

        /**
        * 消息创建时间
        */
        private LocalDateTime messageCreateTime;

        /**
        * 消息更新时间
        */
        private LocalDateTime messageUpdateTime;

        /**
        * 输入任务id
        */
        private String inputTaskId;

        /**
        * 转换id
        */
        private String convertId;

        /**
        * 下一层级id
        */
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;


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
        * 本地消息id
        */
        @NotBlank(message = "本地消息id不能为空")
        @Size(max = 19,message = "本地消息id最大长度不能超过19位")
        private String messageId;

        /**
        * 目标系统
        */
        @NotBlank(message = "目标系统不能为空")
        @Size(max = 31,message = "目标系统最大长度不能超过31位")
        private String targetPlatform;

        /**
        * 来源类型
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 31,message = "来源类型最大长度不能超过31位")
        private String sourceType;

        /**
        * 来源id
        */
        @NotBlank(message = "来源id不能为空")
        @Size(max = 19,message = "来源id最大长度不能超过19位")
        private String sourceId;

        /**
        * 来源编号
        */
        @NotBlank(message = "来源编号不能为空")
        @Size(max = 63,message = "来源编号最大长度不能超过63位")
        private String sourceCode;

        /**
        * 操作类型
        */
        @NotBlank(message = "操作类型不能为空")
        @Size(max = 63,message = "操作类型最大长度不能超过63位")
        private String syncOperate;

        /**
        * 推送数据
        */
        private String pushData;

        /**
        * 消息创建时间
        */
        private LocalDateTime messageCreateTime;

        /**
        * 消息更新时间
        */
        private LocalDateTime messageUpdateTime;

        /**
        * 输入任务id
        */
        @NotBlank(message = "输入任务id不能为空")
        @Size(max = 19,message = "输入任务id最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 转换id
        */
        @NotBlank(message = "转换id不能为空")
        @Size(max = 19,message = "转换id最大长度不能超过19位")
        private String convertId;

        /**
        * 下一层级id
        */
        @NotBlank(message = "下一层级id不能为空")
        @Size(max = 19,message = "下一层级id最大长度不能超过19位")
        private String nextLevelId;

        /**
        * 唯一字段md5值
        */
        private String uniqueEncrypt;

        /**
        * 数据字段md5值
        */
        private String dataEncrypt;


    }


}