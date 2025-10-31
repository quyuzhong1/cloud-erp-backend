package com.erp.model.dmp.dto;

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
 * DMP飞书审批实例详情记录表请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2025-10-30
*/
@Data
@NoArgsConstructor
public class DmpFeishuInstanceDetailDTO implements Serializable {




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
        * 任务转换ID
        */
        private String convertId;

        /**
        * 店铺ID
        */
        private String nextLevelId;

        /**
        * 任务来源唯一加密代号
        */
        private String uniqueEncrypt;

        /**
        * 任务数据加密代号
        */
        private String dataEncrypt;

        /**
        * 输入任务id
        */
        private String inputTaskId;

        /**
        * 主键ID
        */
        private String mainId;

        /**
        * 审批名称
        */
        private String approvalName;

        /**
        * 开始时间
        */
        private String startTime;

        /**
        * 结束时间
        */
        private String endTime;

        /**
        * 用户ID
        */
        private String userId;

        /**
        * 用户OpenID
        */
        private String openId;

        /**
        * 审批流水号
        */
        private String serialNumber;

        /**
        * 部门ID
        */
        private String departmentId;

        /**
        * 审批状态
        */
        private String status;

        /**
        * 唯一标识UUID
        */
        private String uuid;

        /**
        * 表单内容(JSON文本)
        */
        private String form;

        /**
        * 任务列表(JSON文本)
        */
        private String taskList;

        /**
        * 评论列表(JSON文本)
        */
        private String commentList;

        /**
        * 时间线(JSON文本)
        */
        private String timeline;

        /**
        * 修改后实例编码
        */
        private String modifiedInstanceCode;

        /**
        * 回退实例编码
        */
        private String revertedInstanceCode;

        /**
        * 审批定义编码
        */
        private String approvalCode;

        /**
        * 是否回退
        */
        private Boolean reverted;

        /**
        * 审批实例编码
        */
        private String instanceCode;


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
        * 任务转换ID
        */
        @NotBlank(message = "任务转换ID不能为空")
        @Size(max = 19,message = "任务转换ID最大长度不能超过19位")
        private String convertId;

        /**
        * 店铺ID
        */
        @NotBlank(message = "店铺ID不能为空")
        @Size(max = 19,message = "店铺ID最大长度不能超过19位")
        private String nextLevelId;

        /**
        * 任务来源唯一加密代号
        */
        @NotBlank(message = "任务来源唯一加密代号不能为空")
        private String uniqueEncrypt;

        /**
        * 任务数据加密代号
        */
        @NotBlank(message = "任务数据加密代号不能为空")
        private String dataEncrypt;

        /**
        * 输入任务id
        */
        @NotBlank(message = "输入任务id不能为空")
        @Size(max = 19,message = "输入任务id最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 主键ID
        */
        @NotBlank(message = "主键ID不能为空")
        @Size(max = 19,message = "主键ID最大长度不能超过19位")
        private String mainId;

        /**
        * 审批名称
        */
        @NotBlank(message = "审批名称不能为空")
        @Size(max = 255,message = "审批名称最大长度不能超过255位")
        private String approvalName;

        /**
        * 开始时间
        */
        @NotBlank(message = "开始时间不能为空")
        @Size(max = 64,message = "开始时间最大长度不能超过64位")
        private String startTime;

        /**
        * 结束时间
        */
        @NotBlank(message = "结束时间不能为空")
        @Size(max = 64,message = "结束时间最大长度不能超过64位")
        private String endTime;

        /**
        * 用户ID
        */
        @NotBlank(message = "用户ID不能为空")
        @Size(max = 64,message = "用户ID最大长度不能超过64位")
        private String userId;

        /**
        * 用户OpenID
        */
        @NotBlank(message = "用户OpenID不能为空")
        @Size(max = 64,message = "用户OpenID最大长度不能超过64位")
        private String openId;

        /**
        * 审批流水号
        */
        @NotBlank(message = "审批流水号不能为空")
        @Size(max = 255,message = "审批流水号最大长度不能超过255位")
        private String serialNumber;

        /**
        * 部门ID
        */
        @NotBlank(message = "部门ID不能为空")
        @Size(max = 255,message = "部门ID最大长度不能超过255位")
        private String departmentId;

        /**
        * 审批状态
        */
        @NotBlank(message = "审批状态不能为空")
        @Size(max = 64,message = "审批状态最大长度不能超过64位")
        private String status;

        /**
        * 唯一标识UUID
        */
        @NotBlank(message = "唯一标识UUID不能为空")
        @Size(max = 255,message = "唯一标识UUID最大长度不能超过255位")
        private String uuid;

        /**
        * 表单内容(JSON文本)
        */
        @NotBlank(message = "表单内容(JSON文本)不能为空")
        private String form;

        /**
        * 任务列表(JSON文本)
        */
        @NotBlank(message = "任务列表(JSON文本)不能为空")
        private String taskList;

        /**
        * 评论列表(JSON文本)
        */
        @NotBlank(message = "评论列表(JSON文本)不能为空")
        private String commentList;

        /**
        * 时间线(JSON文本)
        */
        @NotBlank(message = "时间线(JSON文本)不能为空")
        private String timeline;

        /**
        * 修改后实例编码
        */
        @NotBlank(message = "修改后实例编码不能为空")
        @Size(max = 255,message = "修改后实例编码最大长度不能超过255位")
        private String modifiedInstanceCode;

        /**
        * 回退实例编码
        */
        @NotBlank(message = "回退实例编码不能为空")
        @Size(max = 255,message = "回退实例编码最大长度不能超过255位")
        private String revertedInstanceCode;

        /**
        * 审批定义编码
        */
        @NotBlank(message = "审批定义编码不能为空")
        @Size(max = 255,message = "审批定义编码最大长度不能超过255位")
        private String approvalCode;

        /**
        * 是否回退
        */
        @NotNull(message = "是否回退不能为空")
        private Boolean reverted;

        /**
        * 审批实例编码
        */
        @NotBlank(message = "审批实例编码不能为空")
        @Size(max = 255,message = "审批实例编码最大长度不能超过255位")
        private String instanceCode;


    }


}