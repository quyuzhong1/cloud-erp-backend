package com.common.business.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 任务信息实体类
 * @author Cloud
 */
@Data
@NoArgsConstructor
public class DmpSyncTaskDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParamDTO {

        /**
         * 来源单据类型
         */
        private String sourceType;

        private String targetPlatform;

        /**
         * 目标平台名称
         */
        @NotBlank(message = "目标平台名称不能为空")
        private String targetPlatformName;

        /**
         * 来源系统
         */
        @NotBlank(message = "来源系统不能为空")
        private String sourcePlatformName;

        /**
         * MQ消息主题
         */
        private String mqTopic;

        /**
         * MQ消息TAG  推送第三方平台命名方式
         */
        private String mqTag;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OneDTO extends ParamDTO{

        /**
         * 来源单据id
         */
        @NotBlank(message = "来源单据id不能为空")
        private String sourceId;

        public OneDTO(String sourceType,String sourceId,String targetPlatformName,String sourcePlatformName) {
            this.setSourceType(sourceType);
            this.setTargetPlatformName(targetPlatformName);
            this.setSourcePlatformName(sourcePlatformName);
            this.sourceId = sourceId;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListDTO extends ParamDTO{

        /**
         * 来源单据id
         */
        @NotEmpty(message = "来源单据id不能为空")
        private List<String> sourceIdList;

        public ListDTO(List<String> sourceIdList,String targetPlatformName,String sourcePlatformName) {
            this.setTargetPlatformName(targetPlatformName);
            this.setSourcePlatformName(sourcePlatformName);
            this.sourceIdList = sourceIdList;
        }

        public ListDTO(String sourceType,List<String> sourceIdList,String targetPlatformName,String sourcePlatformName) {
            this.setSourceType(sourceType);
            this.setTargetPlatformName(targetPlatformName);
            this.setSourcePlatformName(sourcePlatformName);
            this.sourceIdList = sourceIdList;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddDTO {
        /**
         * 目标平台名称
         */
        @NotBlank(message = "目标平台名称不能为空")
        private String targetPlatformName;

        /**
         * MQ消息主题
         */
        @NotBlank(message = "MQ消息主题不能为空")
        private String mqTopic;

        /**
         * MQ消息TAG  推送第三方平台命名方式
         */
        @NotBlank(message = "MQ消息TAG不能为空")
        private String mqTag;

        /**
         * MQ消息内容
         */
        @NotBlank(message = "MQ消息内容不能为空")
        private String mqData;

        /**
         * 来源系统
         */
        @NotBlank(message = "来源系统不能为空")
        private String sourcePlatformName;

        /**
         * 来源单据类型
         */
        @NotBlank(message = "来源单据类型不能为空")
        private String sourceType;

        /**
         * 来源单据id
         */
        @NotBlank(message = "来源单据id不能为空")
        private String sourceId;

        /**
         * 来源单据编号
         */
        @NotBlank(message = "来源单据编号不能为空")
        private String sourceCode;

        /**
         * 同步操作
         */
        @NotBlank(message = "同步操作不能为空")
        private String syncOperate;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @NoArgsConstructor
    public static class ListCodeDTO extends ParamDTO{

        /**
         * 来源单据code
         */
        @NotEmpty(message = "来源单据Code不能为空")
        private List<String> sourceCodeList;

        public ListCodeDTO(List<String> sourceCodeList,String targetPlatformName,String sourcePlatformName) {
            this.setTargetPlatformName(targetPlatformName);
            this.setSourcePlatformName(sourcePlatformName);
            this.sourceCodeList = sourceCodeList;
        }

        public ListCodeDTO(String sourceType,List<String> sourceCodeList,String targetPlatformName,String sourcePlatformName) {
            this.setSourceType(sourceType);
            this.setTargetPlatformName(targetPlatformName);
            this.setSourcePlatformName(sourcePlatformName);
            this.sourceCodeList = sourceCodeList;
        }
    }


}
