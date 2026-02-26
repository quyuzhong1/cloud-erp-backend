package com.common.business.dto;

import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.enums.ApprovePlatformEnum;
import com.common.business.enums.ApproveTypeEnum;
import com.common.core.anno.StateEnumValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Data
@NoArgsConstructor
public class ApproveDTO implements Serializable {

    /**
     * 审核
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApproveOneDTO{
        /**
         * 单据类型
         */
        @NotBlank(message = "单据类型不能为空")
        private String businessKey;

        /**
         * 主键id集合
         */
        @NotEmpty(message = "审核id不能为空")
        private String id;

        /**
         * 类型（pass、审核通过，reject、审核不通过）
         */
        @NotBlank(message = "审核类型不能为空")
        @StateEnumValue(strValues = {"pass","reject","reject_appoint","revoke","cancel"}, message = "审核类型有误")
        private String type;

        /**
         * 意见
         */
        @Size(max = 255, message = "审核意见最大255个字符")
        private String comment;

        /**
         * 是否需要流程，false则跳过
         */
        private Boolean isNeedProcess;


        /**
         * 是否是pc端访问
         */
        private Boolean pcShow = false;
        /**
         *发货日期
         */
        private LocalDate deliveryDate;

        /**
         * 流程参数map
         */
        private Map<String,Object> variablesMap;

        /**
         * 是否是提审后自动审核
         */
        private Boolean isSubmitAutoApprove = false;
    }

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

        /**
         * ProcessSourcePlatformEnum枚举，默认设置成erp
         */
        private String executeSystem = "erp";


        public CancelProcessDTO(String id) {
            this.id = id;
        }
        public CancelProcessDTO(String id,String executeSystem) {
            this.id = id;
            this.executeSystem = executeSystem;
        }
    }

    /**
     * 取消流程（批量）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchCancelProcessDTO{
        /**
         * 单据类型
         */
        @NotBlank(message = "单据类型不能为空")
        private String businessKey;

        /**
         * 主键ids
         */
        private List<String> ids;

        /**
         * ProcessSourcePlatformEnum枚举，默认设置成erp
         */
        private String executeSystem = "erp";


        public BatchCancelProcessDTO(List<String> ids) {
            this.ids = ids;
        }
        public BatchCancelProcessDTO(List<String> ids,String executeSystem) {
            this.ids = ids;
            this.executeSystem = executeSystem;
        }

        public BatchCancelProcessDTO(CancelProcessDTO dto) {
            this.ids = Collections.singletonList(dto.getId());
            this.executeSystem = dto.getExecuteSystem();
            this.businessKey = dto.getBusinessKey();
        }
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