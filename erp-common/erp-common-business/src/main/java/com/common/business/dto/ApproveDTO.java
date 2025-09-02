package com.common.business.dto;

import com.common.business.enums.ApprovePlatformEnum;
import com.common.business.enums.ApproveTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
public class ApproveDTO implements Serializable {

    /**
     * 反审核
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DisApproveDTO{

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;
        /**
         * 单据类型
         */
        @NotBlank(message = "单据类型不能为空")
        private String businessKey;
    }

    /**
     * 取消流程
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelProcessDTO{
        /**
         * 单据类型
         */
        @NotBlank(message = "单据类型不能为空")
        private String businessKey;

        /**
         * 主键id
         */
        private String id;

    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EndProcessDTO{

        /**
         * 审核平台，默认erp
         */
        private ApprovePlatformEnum approvePlatformEnum = ApprovePlatformEnum.ERP;

        /**
         * 业务key
         */
        private String businessKey;

        /**
         * 业务id
         */
        private String businessId;

        /**
         * 审批结果
         */
        private ApproveTypeEnum approveStatus;

        /**
         * 最近任务审批人
         */
        private String approveUserId;

        /**
         * 最近任务审批时间
         */
        private LocalDateTime approveTime;
        /**
         *发货日期
         */
        private LocalDate deliveryDate;

        /**
         * 最近任务审批意见
         */
        private String comment;

        /**
         * 流程参数map
         */
        private Map<String,Object> variablesMap;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddCommentDTO{
        /**
         * 审核平台，默认erp
         */
        private ApprovePlatformEnum approvePlatformEnum = ApprovePlatformEnum.ERP;
        /**
         * 单据类型
         */
        @NotBlank(message = "单据类型不能为空")
        private String businessKey;

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 评论信息
         */
        private List<String> comments;
    }
}