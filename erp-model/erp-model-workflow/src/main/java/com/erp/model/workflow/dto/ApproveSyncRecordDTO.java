package com.erp.model.workflow.dto;

import java.time.LocalDateTime;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * ERP审批同步-通知配置请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-05-12
*/
@Data
@NoArgsConstructor
public class ApproveSyncRecordDTO implements Serializable {


    /**
     * 状态统计
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {

        /**
         * 类型
         */
        private String tabFlag;
        /**
         * 类型
         */
        private String tabFlagName;

        /**
         * 数量
         */
        private Integer count;

    }


    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO extends BaseDTO {
        /**
         * 主表id
         */
        private String cfgApproveSyncId;

        /**
         * 通知类型：messagePush=消息通知,approvalPush=审批推送
         */
        private String noticeType;
        private String noticeTypeName;

        /**
         * 单据名称
         */
        private String businessType;
        private String businessTypeName;

        /**
         * 单据单号
         */
        private String businessCode;

        /**
         * 提醒方式
         */
        private String noticeMethod;
        private String noticeMethodName;

        /**
         * 接收人id
         */
        private String receiverId;

        /**
         * 接收人
         */
        private String receiverName;

        /**
         * 发送时间
         */
        private LocalDateTime sendTime;

        /**
         * 通知标题
         */
        private String title;

        /**
         * 状态：success=推送成功, failed=推送失败
         */
        private String status;
        private String statusName;

        /**
         * 失败原因
         */
        private String errorReason;

        /**
         * 通知节点
         */
        private String noticeNode;
        private String noticeNodeName;

    }

    @Data
    @NoArgsConstructor
    public static class BaseDTO {

        /**
         *
         */
        private String id;
        /**
         * 创建人id
         */
        private String createUserId;

        /**
         * 创建人名称
         */
        private String createUserName;

        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人id
         */
        private String updateUserId;

        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 更新时间
         */
        private LocalDateTime updateTime;
    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {

        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;

        /**
         * 主键id
         */
        private List<String> ids;

    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    public static class externalInstanceParamDTO {

        /**
         * process_management 主表id
         */
        private List<String> ids;


    }

}