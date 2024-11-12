package com.erp.model.dmp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * <p>
 * 拉取任务请求响应实体
 * </p>
 */
@Data
@NoArgsConstructor
public class DmpInoutDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class CreateInputDTO extends CommonDTO {

        /**
         * dmp_cfg_input_detail明细扩展参数
         */
        private String detailExtendJson;

        /**
         * 数据开始时间:默认当天零点
         */
        private LocalDateTime startTime;
        /**
         * 数据结束时间:默认当前
         */
        private LocalDateTime endTime;

        public LocalDateTime checkAndGetStartTime() {
            if (null == this.startTime){
                return LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
            }
            return startTime;
        }

        public LocalDateTime checkAndGetEndTime() {
            if (null == this.endTime){
                return LocalDateTime.now();
            }
            return endTime;
        }
    }


    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 系统代号：PlatformDictEnum代号
         * dmp_basic_system中Code
         */
        @NotBlank(message = "systemCode不能为空")
        private String systemCode;

        /**
         * 业务类型：BusinessTypeEnum业务类型
         * 对应dmp_cfg_input的billType
         */
        @NotBlank(message = "billType不能为空")
        private String billType;

        /**
         * 推送下一层级id
         * 销售平台=shop_info店铺ID
         * 第三方仓平台=overseas_provider授权ID
         */
        @NotNull(message = "nextLevelIdList不能为空")
        private List<String> nextLevelIdList;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListDTO {

        /**
         * 系统Code
         */
        private String systemCode;

        /**
         * 任务ID
         */
        private String cfgInputId;

        /**
         * 任务明细ID
         */
        private String detailId;

        /**
         * 下一级ID
         */
        private String nextLevelId;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LastOneDTO {

        /**
         * 系统Code
         */
        private String systemCode;

        /**
         * 任务ID
         */
        private String cfgInputId;

        /**
         * 下一级ID
         */
        private String nextLevelId;

        /**
         * 状态对应
         * SyncStatusEnum
         */
        private String status;

        /**
         * 状态对应名称
         * SyncStatusEnum
         */
        private String statusName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }

}