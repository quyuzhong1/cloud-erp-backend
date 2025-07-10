package com.erp.model.workflow.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 三方生成查询请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-05-27
*/
@Data
@NoArgsConstructor
public class ApproveTaskInfoDTO implements Serializable {


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

    }

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
         * 主键id【可排序】
         */
        private String id;

        /**
         * 流程来源【可排序】
         */
        private String sourcePlatform;

        /**
         * 流程来源名称,同流程配置枚举,CfgProcessRuleTypeEnum
         */
        private String sourcePlatformName;

        /**
         * 类型,ApproveTaskTypeEnum【可排序】
         */
        private String type;
        /**
         * 类型名称
         */
        private String typeName;

        /**
         * 第三方单据（飞书实例name）【可排序】
         */
        private String thirdDefinniationName;

        /**
         * 第三方单号（飞书实例id）【可排序】
         */
        private String thirdInstanceId;

        /**
         * 第三方单据实际单号
         */
        private String serialNumber;

        /**
         * 第三方审批定义【可排序】
         */
        private String thirdApprovalCode;

        /**
         * 数大臣单据名称，采购订单等【可排序】
         */
        private String bussinessKey;

        /**
         * 数大臣单据名称，采购订单等
         */
        private String bussinessKeyName;

        /**
         * 数大臣单号，单据号【可排序】
         */
        private String bussinessCode;

        /**
         * 发生时间【可排序】
         */
        private LocalDateTime happenTime;

        /**
         * 执行状态，ApproveTaskStatusEnum枚举【可排序】
         */
        private String status;

        /**
         * 执行状态名称
         */
        private String statusName;

        /**
         * 失败原因【可排序】
         */
        private String reason;
    }

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
        * 流程来源
        */
        private String sourcePlatform;
        /**
         * 流程来源名称
         */
        private String sourcePlatformName;

        /**
        * 类型
        */
        private String type;
        /**
         * 类型名称
         */
        private String typeName;

        /**
        * 第三方单据（飞书实例name）
        */
        private String thirdDefinniationName;

        /**
        * 第三方单号（飞书实例id）
        */
        private String thirdInstanceId;

        /**
        * 第三方审批定义
        */
        private String thirdApprovalCode;

        /**
        * 数大臣单据名称，采购订单等
        */
        private String bussinessKey;
        /**
         * 数大臣单据名称，采购订单等
         */
        private String bussinessKeyName;

        /**
        * 数大臣单号，单据号
        */
        private String bussinessCode;

        /**
         * 执行状态，ApproveTaskStatusEnum枚举
         */
        private String status;

        /**
         * 执行状态名称
         */
        private String statusName;
        /**
         * 发生时间
         */
        private LocalDateTime happenTime;

        /**
         * 明细
         */
        private List<ApproveTaskDetailDTO.ViewDTO> detailList;
    }

    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDetailDTO {
        /**
         * 实体名称
         */
        private String entityName;

        /**
         * 明细字段映射数据
         */
        private List<ApproveTaskDetailDTO.ViewDTO> detailList;
    }


    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {

        /**
         * 三方查询明细
         */
        @NotEmpty(message = "三方查询明细数据不能为空")
        private List<ApproveTaskDetailDTO.AddDTO> detailList;
    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO  {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;
        /**
         * 三方查询明细
         */
        @NotEmpty(message = "三方查询明细数据不能为空")
        @Valid
        private List<ApproveTaskDetailDTO.UpdateDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 流程来源
        */
        @NotBlank(message = "流程来源不能为空")
        @Size(max = 32,message = "流程来源最大长度不能超过32位")
        private String sourcePlatform;

        /**
        * 类型
        */
        @NotBlank(message = "类型不能为空")
        @Size(max = 32,message = "类型最大长度不能超过32位")
        private String type;

        /**
        * 第三方单据（飞书实例name）
        */
        @NotBlank(message = "第三方单据（飞书实例name）不能为空")
        @Size(max = 64,message = "第三方单据（飞书实例name）最大长度不能超过64位")
        private String thirdDefinniationName;

        /**
        * 第三方单号（飞书实例id）
        */
        @NotBlank(message = "第三方单号（飞书实例id）不能为空")
        @Size(max = 64,message = "第三方单号（飞书实例id）最大长度不能超过64位")
        private String thirdInstanceId;

        /**
        * 第三方审批定义
        */
        @NotBlank(message = "第三方审批定义不能为空")
        @Size(max = 64,message = "第三方审批定义最大长度不能超过64位")
        private String thirdApprovalCode;

        /**
        * 数大臣单据名称，采购订单等
        */
        @NotBlank(message = "数大臣单据名称，采购订单等不能为空")
        @Size(max = 32,message = "数大臣单据名称，采购订单等最大长度不能超过32位")
        private String bussinessKey;

        /**
        * 数大臣单号，单据号
        */
        @NotBlank(message = "数大臣单号，单据号不能为空")
        @Size(max = 64,message = "数大臣单号，单据号最大长度不能超过64位")
        private String bussinessCode;

        /**
         * 数大臣单据id，单据id
         */
        @NotBlank(message = "数大臣单据id，单据id不能为空")
        @Size(max = 19,message = "数大臣单据id，单据id最大长度不能超过19位")
        private String bussinessId;

        /**
         * 发生时间
         */
        private LocalDateTime happenTime;

        /**
         * 执行状态，ApproveTaskStatusEnum枚举
         */
        private String status;

        /**
         * 失败原因
         */
        private String reason;
    }
    @Data
    @NoArgsConstructor
    public static class AfreshGenerateTO {
        /**
         * 主键ids
         */
        @NotEmpty(message = "主键ids不能为空")
        private List<String> ids;


    }


}