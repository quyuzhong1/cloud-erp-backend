package com.erp.model.workflow.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.erp.model.workflow.entity.CfgApproveSyncEntity;
import com.erp.model.workflow.enums.FSApprovalStatusEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * ERP审批同步配置请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-05-12
*/
@Data
@NoArgsConstructor
public class CfgApproveSyncDTO implements Serializable {




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
        * 单据编码
        */
        private String code;

        /**
        * 来源类型
        */
        private String businessType;
        private String businessTypeName;

        /**
        * SDC回调地址
        */
        private String webhookUrl;

        /**
        * 审批分组dict_basic表approveGroup
        */
        private String approveGroup;
        private String approveGroupName;

        /**
        * 启用状态
        */
        private Boolean enableStatus;

        /**
        * 备注
        */
        private String remark;

        /**
        * 审批名称(标题)
        */
        private String title;

        /**
        * 可见范围dict_basic表viewerType：allUsers=所有用户,specificDepartments=指定部门,specificUsers=指定用户,none=不可见
        */
        private String viewerType;
        private String viewerTypeName;

        /**
        * 可见集合
        */
        private String viewer;
        private List<String> viewerList;

        /**
        * 同步平台：feishu=飞书,dd=钉钉,qw=企业微信
        */
        private String syncPlatform;

        private List<String> syncPlatformList;

        /**
         * 推送信息
         */
        private List<CfgApproveSyncFieldMapDTO.NoticeFieldMapDTO> pushMsgList;
        /**
         * 通知配置
         */
        private CfgApproveNoticeDTO.NoticeSettingDTO approve;

        private CfgApproveNoticeDTO.NoticeSettingDTO approveResult;

        private CfgApproveNoticeDTO.NoticeSettingDTO cc;

        private CfgApproveNoticeDTO.NoticeSettingDTO timeoutWarning;

        private CfgApproveNoticeDTO.NoticeSettingDTO recall;
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
        * 来源类型 /workflow/work/menu/drop/down
        */
        @NotBlank(message = "来源类型不能为空")
        @Size(max = 32,message = "来源类型最大长度不能超过32位")
        private String businessType;

        /**
        * SDC回调地址
        */
        @NotBlank(message = "SDC回调地址不能为空")
        @Size(max = 500,message = "SDC回调地址最大长度不能超过500位")
        private String webhookUrl;

        /**
        * 审批分组 /workflow/dict/basic/drop/down?type=approveGroup
        */
        @NotBlank(message = "审批分组不能为空")
        @Size(max = 32,message = "审批分组最大长度不能超过32位")
        private String approveGroup;

        /**
        * 启用状态
        */
        @NotNull(message = "启用状态不能为空")
        private Boolean enableStatus;

        /**
        * 备注
        */
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
        * 审批名称(标题)
        */
        @NotBlank(message = "审批名称(标题)不能为空")
        @Size(max = 50,message = "审批名称(标题)最大长度不能超过50位")
        private String title;

        /**
        * 可见范围  /workflow/common/enumDropDown?type=CfgApproveSyncViewerType
        */
        @NotBlank(message = "可见范围不能为空")
        @Size(max = 32,message = "可见范围最大长度不能超过32位")
        private String viewerType;

        /**
        * 可见集合
        */
        private String viewer;

        private List<String> viewerList;

        /**
        * 同步平台 /workflow/common/enumDropDown?type=CfgApproveSyncSyncPlatform
        */
        private String syncPlatform;
        @NotEmpty(message = "推送方式不能为空")
        private List<String> syncPlatformList;

        /**
        * 飞书审批定义
        */
        private String approvalCode;
        /**
         * 推送信息
         */
        @NotEmpty
        private List<CfgApproveSyncFieldMapDTO.@Valid NoticeFieldMapDTO> pushMsgList;
        /**
         * 通知配置
         */
//        private List<CfgApproveNoticeDTO.NoticeSettingDTO> noticeSettingList;
        @NotNull(message = "审核通知配置不能为空")
        private CfgApproveNoticeDTO.NoticeSettingDTO approve;

        private CfgApproveNoticeDTO.NoticeSettingDTO approveResult;

        private CfgApproveNoticeDTO.NoticeSettingDTO cc;

        private CfgApproveNoticeDTO.NoticeSettingDTO timeoutWarning;

        private CfgApproveNoticeDTO.NoticeSettingDTO recall;
    }

    @Data
    @NoArgsConstructor
    public static class BaseDTO {

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
    public static class ListDTO extends BaseDTO{

        /**
         * 主键id
         */
        private String  id;

        /**
         * 单据编码
         */
        private String code;

        /**
         * 来源类型
         */
        private String businessType;
        private String businessTypeName;

        /**
         * SDC回调地址
         */
        private String webhookUrl;

        /**
         * 审批分组dict_basic表approveGroup
         */
        private String approveGroup;
        private String approveGroupName;

        /**
         * 启用状态
         */
        private Boolean enableStatus;
        private String enableStatusName;

        /**
         * 备注
         */
        private String remark;

        /**
         * 审批名称(标题)
         */
        private String title;

        /**
         * 可见范围dict_basic表viewerType：allUsers=所有用户,specificDepartments=指定部门,specificUsers=指定用户,none=不可见
         */
        private String viewerType;
        private String viewerTypeName;

        /**
         * 可见集合
         */
        private String viewer;

        /**
         * 同步平台：feishu=飞书,dd=钉钉,qw=企业微信
         */
        private String syncPlatform;
        private String syncPlatformName;
    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class EnableStatusDTO {

        @NotEmpty(message = "ids不能为空")
        private List<String> ids;

        @NotNull(message = "启用状态不能为空")
        private Boolean enableStatus;

    }


    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class SyncFsProcessToMqDTO {
        //审批同步定义
        private CfgApproveSyncEntity cfgApproveSyncEntity;
        //流程管理id
        private String processManagementId;
        //流程名称
        private String businessName;
        //单据编码
        private String businessCode;
        //实例id
        private String instanceId;
        //任务id
        private String curTaskId;
        //流程操作人（创建人、审批人）
        private String operator;
        //流程类型
        private String businessKey;
        //erp的操作动作（发起流程，审批通过，审批不通过，撤销）
        private String approveType;
        //飞书的流程状态
        private FSApprovalStatusEnum fSApprovalStatusEnum;
        /**
         * 流程参数map
         */
        private Map<String,Object> variablesMap;

    }

}