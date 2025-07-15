package com.erp.model.workflow.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 三方审批生成请求响应实体
 * </p>
 *
 * @author hcg
 * @since 2025-05-23
 */
@Data
@NoArgsConstructor
public class CfgThirdProcessDTO implements Serializable {


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 配置编码
         */
        private String code;

        /**
         * 配置名称
         */
        private String name;

        /**
         * 配置单据
         */
        private String bussinessKey;

        /**
         * 生成/更新配置
         */
        private String operateType;

        /**
         * 流程来源平台
         */
        private String sourcePlatform;

        /**
         * 启用状态
         */
        private Boolean enableStatus;

        /**
         * 启用时间
         */
        private LocalDateTime enableTime;

        /**
         * 第三方审批定义code
         */
        private String thirdProcessDefinitionCode;

        /**
         * 字段映射
         */
        private List<CfgProcessFieldMapDTO.ViewDTO> fieldMapList;
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
         * 配置名称
         */
        @NotBlank(message = "配置名称不能为空")
        @Size(max = 100, message = "配置名称最大长度不能超过100位")
        private String name;

        /**
         * 配置单据
         */
        @NotBlank(message = "配置单据不能为空")
        @Size(max = 30, message = "配置单据最大长度不能超过30位")
        private String bussinessKey;

        /**
         * 生成/更新配置
         */
        @NotBlank(message = "生成/更新配置不能为空")
        @Size(max = 30, message = "生成/更新配置最大长度不能超过30位")
        private String operateType;

        /**
         * 流程来源平台
         */
        @NotBlank(message = "流程来源平台不能为空")
        @Size(max = 30, message = "流程来源平台最大长度不能超过30位")
        private String sourcePlatform;

        /**
         * 启用状态
         */
        @NotNull(message = "启用状态不能为空")
        private Boolean enableStatus;

        /**
         * 启用时间
         */
        private LocalDateTime enableTime;

        /**
         * 第三方审批定义code
         */
        @NotBlank(message = "第三方审批定义code不能为空")
        private String thirdProcessDefinitionCode;

        /**
         * 字段映射
         */
        @NotEmpty(message = "字段映射不能为空")
        @Valid
        private List<CfgProcessFieldMapDTO.AddOrUpdateDTO> fieldMapList;
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
         * 配置名称
         */
        private String name;

        /**
         * 配置单据
         */
        private String bussinessKey;
        private String bussinessKeyName;

        /**
         * 生成/更新配置
         */
        private String operateType;

        /**
         * 流程来源平台
         */
        private String sourcePlatform;
        private String sourcePlatformName;

        /**
         * 启用状态
         */
        private Boolean enableStatus;

        /**
         * 第三方审批定义code
         */
        private String thirdProcessDefinitionName;

        /**
         * 字段映射
         */
        private List<CfgProcessFieldMapDTO.ViewDTO> fieldMapList;
    }

    /**
     * 批量启用/禁用
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
        private Map<String, String> sqlMap;

        /**
         * 主键id
         */
        private List<String> ids;

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
}