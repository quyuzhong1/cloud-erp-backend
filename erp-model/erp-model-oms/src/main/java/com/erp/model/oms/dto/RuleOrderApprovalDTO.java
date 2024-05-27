package com.erp.model.oms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 订单审核规则请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
@Data
@NoArgsConstructor
public class RuleOrderApprovalDTO implements Serializable {


    /**
     * 分页详情
     */
    @Data
    @NoArgsConstructor
    public static class PagingViewDTO {


        private String id;
        /**
         * 名称
         */
        private String name;

        /**
         * 优先级
         */
        private Integer priority;

        /**
         * 禁用状态
         */
        private Boolean disabled;

        /**
         * 备注
         */
        private String remark;

        /**
         * 创建人
         */
        private String createUserName;


        /**
         * 创建时间
         */
        private LocalDateTime createTime;

        /**
         * 修改人
         */
        private String updateUserName;


        /**
         * 修改时间
         */
        private LocalDateTime updateTime;


    }

    /**
     * 分页参数
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
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO extends CommonDTO {

        private String id;


        private List<RuleConditionDTO.ViewDTO> conditionList;
    }


    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        private List<RuleConditionDTO.AddDTO> conditionList;
    }


    /**
     * 修改
     *
     * @author Administrator
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        private List<RuleConditionDTO.UpdateDTO> conditionList;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 名称
         */
        @NotBlank(message = "规则名称不能为空")
        @Size(max = 50, message = "规则名称最大长度不能超过50字符")
        private String name;

        /**
         * 优先级
         */
        @NotNull(message = "优先级不能为空")
        @DecimalMin(value = "0", message = "最小值为1")
        @DecimalMax(value = "10", message = "最小值为10")
        private Integer priority;


        /**
         * 备注
         */
        @Size(max = 255, message = "备注最大长度不能超过255位")
        private String remark;

        /**
         * 分类明细id 来源  http://172.16.100.11:3002/project/110/interface/api/21652
         */
        private List<String> categoryDetailIdList;

        /**
         * 设定操作
         * category 分类
         * flowStatus 流向状态
         */
        @NotNull(message = "设定操作不能为空")
        @Size(min = 1, message = "设定操作不能为空")
        private List<String> operationTypeList;

        /**
         * 流向状态
         * 来源 http://172.16.100.11:3002/project/110/interface/api/13480   type=RuleOrderFlowStatus
         */
        private String flowStatus;

        /**
         * true 禁用  false 启用
         */
        private Boolean disabled;

    }

    /**
     * 规则匹配结果
     */
    @Data
    @NoArgsConstructor
    public static class RuleMatchDTO{

        /**
         * 流转状态
         */
        private String flowStatus;

        /**
         * 分类明细id list
         */
        private  List<String> categoryDetailIdList;

        /**
         * 通过结果
         */
        private Boolean approveSuccess;

        /**
         * 规则名称
         */
        private String ruleName;

    }
}