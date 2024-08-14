package com.erp.model.dmp.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.PermissionsDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 中台同步任务表请求响应实体
 * </p>
 *
 * @author Cloud
 * @since 2023-09-06
*/
@Data
@NoArgsConstructor
public class DmpPushTaskDTO implements Serializable {




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
        * 目标平台名称
        */
        private String targetPlatformName;

        /**
        * MQ消息主题
        */
        private String mqTopic;

        /**
        * MQ消息TAG
        */
        private String mqTag;

        /**
        * MQ消息内容
        */
        private String mqData;

        /**
        * 同步状态-0 无需同步 1 待同步 2 同步中，3同步成功，4同步失败
        */
        private String status;

        /**
        * 同步返回信息（错误信息和成功信息）
        */
        private String returnMsg;

        /**
        * 最新同步时间
        */
        private LocalDateTime lastSyncTime;

        /**
        * 来源系统
        */
        private String sourcePlatformName;

        /**
        * 来源单据类型
        */
        private String sourceType;

        /**
        * 来源单据id
        */
        private String sourceId;

        /**
        * 来源单据编号
        */
        private String sourceCode;

        /**
        * 重试次数
        */
        private Integer retryTimes;

        /**
        * 同步操作，同步其他系统的事件
        */
        private String syncOperate;


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
        * 目标平台名称
        */
        @NotBlank(message = "目标平台名称不能为空")
        @Size(max = 64,message = "目标平台名称最大长度不能超过64位")
        private String targetPlatformName;

        /**
        * MQ消息主题
        */
        @NotBlank(message = "MQ消息主题不能为空")
        @Size(max = 64,message = "MQ消息主题最大长度不能超过64位")
        private String mqTopic;

        /**
        * MQ消息TAG
        */
        @NotBlank(message = "MQ消息TAG不能为空")
        @Size(max = 64,message = "MQ消息TAG最大长度不能超过64位")
        private String mqTag;

        /**
        * MQ消息内容
        */
        private String mqData;

        /**
        * 同步状态-0 无需同步 1 待同步 2 同步中，3同步成功，4同步失败
        */
        @NotBlank(message = "同步状态不能为空")
        @Size(max = 16,message = "同步状态最大长度不能超过16位")
        private String status;

        /**
        * 同步返回信息（错误信息和成功信息）
        */
        @NotBlank(message = "同步返回信息（错误信息和成功信息）不能为空")
        private String returnMsg;

        /**
        * 最新同步时间
        */
        private LocalDateTime lastSyncTime;

        /**
        * 来源系统
        */
        @NotBlank(message = "来源系统不能为空")
        @Size(max = 64,message = "来源系统最大长度不能超过64位")
        private String sourcePlatformName;

        /**
        * 来源单据类型
        */
        @NotBlank(message = "来源单据类型不能为空")
        @Size(max = 20,message = "来源单据类型最大长度不能超过20位")
        private String sourceType;

        /**
        * 来源单据id
        */
        @NotBlank(message = "来源单据id不能为空")
        @Size(max = 32,message = "来源单据id最大长度不能超过32位")
        private String sourceId;

        /**
        * 来源单据编号
        */
        @NotBlank(message = "来源单据编号不能为空")
        @Size(max = 64,message = "来源单据编号最大长度不能超过64位")
        private String sourceCode;

        /**
        * 重试次数
        */
        @NotNull(message = "重试次数不能为空")
        private Integer retryTimes;

        /**
        * 同步操作，同步其他系统的事件
        */
        @NotBlank(message = "同步操作，同步其他系统的事件不能为空")
        @Size(max = 16,message = "同步操作，同步其他系统的事件最大长度不能超过16位")
        private String syncOperate;


    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        /**
         * 类型，all全部、0无需同步、同步中、2同步中、3同步成功、4同步失败
         */
        private String tabFlag;

        /**
         * 数量
         */
        private Integer count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParamDTO extends PermissionsDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 来源类型
         */
        private String sourceType;

        /**
         * 来源类型名称
         */
        private String sourceTypeName;

        /**
         * 来源单号
         */
        private String sourceCode;

        /**
         * 同步类型
         */
        private String syncTypeName;

        /**
         * 目标平台名称
         */
        private String targetPlatformName;

        /**
         * 来源系统名称
         */
        private String sourcePlatformName;

        /**
         * 同步状态
         */
        private String status;

        /**
         * 同步状态名称
         */
        private String statusName;

        /**
         * 创建日期
         */
        private LocalDateTime createTime;

        /**
         * 最新推送时间
         */
        private LocalDateTime lastSyncTime;

        /**
         * 推送结果（推送失败原因）
         */
        private String returnMsg;

        /**
         * 同步操作
         */
        private String syncOperate;

        /**
         * 同步操作名称
         */
        private String syncOperateName;

        /**
         * 第三方单号
         */
        private String thirdCode;
    }


}
