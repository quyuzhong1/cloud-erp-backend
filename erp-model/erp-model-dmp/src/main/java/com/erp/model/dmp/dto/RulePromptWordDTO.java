package com.erp.model.dmp.dto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 汉化管理规则表请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2025-01-17
*/
@Data
@NoArgsConstructor
public class RulePromptWordDTO implements Serializable {
    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class UpdateStatusDTO  {

        /**
         * 表 ids
         */
        @NotEmpty(message = "ids不能为空")
        private List<String> ids;


        /**
         * 状态
         */
        @NotNull(message = "状态不能为空")
        private Boolean disabled;
    }
    /**
     * 列表
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
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * id
         */
        private String id;

        /**
         * 优先级
         */
        private Integer index;
        /**
         * 名称
         */
        private String name;

        /**
         * 规则描述
         */
        private String desc;

        /**
         * 禁用状态false 未禁用
         */
        private Boolean disabled;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createTime;
        /**
         * 修改人名称
         */
        private String updateUserName;

        /**
         * 修改时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updateTime;
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
        * 名称
        */
        private String name;

        /**
        * 禁用状态false 未禁用
        */
        private Boolean disabled;

        /**
        * 规则描述
        */
        private String desc;

        /**
        * 提示
        */
        private String tips;

        /**
        * 解决方案
        */
        private String solution;

        /**
        * 优先级
        */
        private Integer index;

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
        @NotBlank(message = "名称不能为空")
        @Size(max = 50,message = "名称最大长度不能超过50位")
        private String name;

        /**
        * 禁用状态false 未禁用
        */
        @NotNull(message = "禁用状态false 未禁用不能为空")
        private Boolean disabled;

        /**
        * 规则描述
        */
        private String desc;

        /**
        * 提示
        */
        @NotBlank(message = "提示不能为空")
        @Size(max = 255,message = "提示最大长度不能超过255位")
        private String tips;

        /**
        * 解决方案
        */
        private String solution;

        /**
        * 优先级
        */
        @NotNull(message = "优先级不能为空")
        private Integer index;


    }


}