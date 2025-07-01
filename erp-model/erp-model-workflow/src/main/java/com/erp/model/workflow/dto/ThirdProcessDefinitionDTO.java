package com.erp.model.workflow.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 三方审批定义请求响应实体
 * </p>
 *
 * @author hcg
 * @since 2025-05-12
*/
@Data
@NoArgsConstructor
public class ThirdProcessDefinitionDTO implements Serializable {




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
        private String approvalCode;

        /**
        * 启用状态
        */
        private Boolean enableStatus;

        /**
        * 单据名称
        */
        private String name;

        /**
        * 归属平台
        */
        private String sourcePlatform;

        /**
        * 表单json
        */
        private String formJson;

        /**
        * 审批组
        */
        private String dictApprovalGroup;

        /**
        * 审批定义类型：发起/拉取
        */
        private String type;
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
        * 单据编码
        */
        @NotBlank(message = "单据编码不能为空")
        @Size(max = 50,message = "单据编码最大长度不能超过30位")
        private String approvalCode;

        /**
        * 单据名称
        */
        @NotBlank(message = "单据名称不能为空")
        @Size(max = 50,message = "单据名称最大长度不能超过30位")
        private String name;


        /**
        * 审批组
        */
        @NotBlank(message = "审批组不能为空")
        @Size(max = 30,message = "审批组最大长度不能超过30位")
        private String dictApprovalGroup;

        /**
        * 审批定义类型：发起/拉取
        */
        @NotBlank(message = "审批定义类型：发起/拉取不能为空")
        @Size(max = 30,message = "审批定义类型：发起/拉取最大长度不能超过30位")
        private String type;
    }

    @Data
    @NoArgsConstructor
    public static class DropDownDTO {
        /**
         * 单据编码
         */
        private String code;

        /**
         * 单据名称
         */
        private String name;
    }

    /**
     * 分页列表查询参数
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
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

    /**
     * 分页列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 单据编码
         */
        private String approvalCode;

        /**
         * 主键id
         */
        private String  id;

        /**
         * 单据名称
         */
        private String name;

        /**
         * 审批组
         */
        private String dictApprovalGroup;

        /**
         * 审批组名
         */
        private String approvalGroupName;

        /**
         * 状态
         */
        private Boolean enableStatus;

        /**
         * 状态名称
         */
        private String enableStatusName;

        /**
         * 创建时间
         */
        private String createTime;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 更新时间
         */
        private String updateTime;

        /**
         * 更新人
         */
        private String updateUserName;

        /**
         * 定义类型 pull or push
         */
        private String type;

        /**
         * 定义类型 pull or push
         */
        private String typeName;
    }
}