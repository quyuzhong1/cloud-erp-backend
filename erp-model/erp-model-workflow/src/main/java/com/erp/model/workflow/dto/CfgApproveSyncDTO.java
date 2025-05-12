package com.erp.model.workflow.dto;

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

        /**
        * SDC回调地址
        */
        private String webhookUrl;

        /**
        * 审批分组dict_basic表approveGroup
        */
        private String approveGroup;

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

        /**
        * 可见集合
        */
        private String viewer;

        /**
        * 同步平台：feishu=飞书,dd=钉钉,qw=企业微信
        */
        private String syncPlatform;

        /**
        * 飞书审批定义
        */
        private String approvalCode;


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
        * 来源类型
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
        * 审批分组dict_basic表approveGroup
        */
        @NotBlank(message = "审批分组dict_basic表approveGroup不能为空")
        @Size(max = 32,message = "审批分组dict_basic表approveGroup最大长度不能超过32位")
        private String approveGroup;

        /**
        * 启用状态
        */
        @NotNull(message = "启用状态不能为空")
        private Boolean enableStatus;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 200,message = "备注最大长度不能超过200位")
        private String remark;

        /**
        * 审批名称(标题)
        */
        @NotBlank(message = "审批名称(标题)不能为空")
        @Size(max = 50,message = "审批名称(标题)最大长度不能超过50位")
        private String title;

        /**
        * 可见范围dict_basic表viewerType：allUsers=所有用户,specificDepartments=指定部门,specificUsers=指定用户,none=不可见
        */
        @NotBlank(message = "可见范围dict_basic表viewerType：allUsers=所有用户,specificDepartments=指定部门,specificUsers=指定用户,none=不可见不能为空")
        @Size(max = 32,message = "可见范围dict_basic表viewerType：allUsers=所有用户,specificDepartments=指定部门,specificUsers=指定用户,none=不可见最大长度不能超过32位")
        private String viewerType;

        /**
        * 可见集合
        */
        @NotBlank(message = "可见集合不能为空")
        @Size(max = 500,message = "可见集合最大长度不能超过500位")
        private String viewer;

        /**
        * 同步平台：feishu=飞书,dd=钉钉,qw=企业微信
        */
        @NotBlank(message = "同步平台：feishu=飞书,dd=钉钉,qw=企业微信不能为空")
        @Size(max = 32,message = "同步平台：feishu=飞书,dd=钉钉,qw=企业微信最大长度不能超过32位")
        private String syncPlatform;

        /**
        * 飞书审批定义
        */
        @NotBlank(message = "飞书审批定义不能为空")
        @Size(max = 255,message = "飞书审批定义最大长度不能超过255位")
        private String approvalCode;


    }


}