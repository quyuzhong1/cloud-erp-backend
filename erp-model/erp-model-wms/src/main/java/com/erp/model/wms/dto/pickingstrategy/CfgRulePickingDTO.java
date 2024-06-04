package com.erp.model.wms.dto.pickingstrategy;

import com.common.business.annotation.Dict;
import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CfgRulePickingDTO {
    @Getter
    @Setter
    public static class PagingView {
        private String description;
        private String name;
        private Integer priority;
        private String id;
        private Boolean disabled;
        private String updateUserName;
        private LocalDateTime updateTime;
    }
    @Getter
    @Setter
    public static class PagingParam extends SortDTO {
        /**
         * 页面高级查询
         */
        private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
         * sqlMap 默认key default
         */
        private Map<String,String> sqlMap;
    }
    @Getter
    @Setter
    public static class Add {
        @NotBlank(message = "名字不能为空")
        private String name;
        @NotNull(message = "状态不能为空")
        @Positive(message = "只能输入大于0的整数")
        private Integer priority;
        @NotNull(message = "状态不能为空")
        private Boolean disabled;
        private String description;
        @Valid
        @Size(min = 1, message = "至少存在一条仓位分配规则")
        private List<CfgRuleActionDTO.Add> actions;
        @Valid
        @Size(min = 1, message = "至少存在一条规则条件")
        private List<CfgRuleConditionDTO.Add> conditionList;
    }

    @Getter
    @Setter
    public static class Update {
        @NotBlank(message = "id不能为空")
        private String id;
        @NotBlank(message = "名字不能为空")
        private String name;
        @NotNull(message = "状态不能为空")
        @Positive(message = "只能输入大于0的整数")
        private Integer priority;
        @NotNull(message = "状态不能为空")
        private Boolean disabled;
        private String description;
        @Valid
        @Size(min = 1, message = "至少存在一条仓位分配规则")
        private List<CfgRuleActionDTO.Update> actions;
        @Valid
        @Size(min = 1, message = "至少存在一条规则条件")
        private List<CfgRuleConditionDTO.Update> conditionList;
    }

    @Getter
    @Setter
    public static class View {

        @Dict
        private List<CfgRuleActionDTO.View> actions;
        @Dict
        private List<CfgRuleConditionDTO.View> conditionList;
        private String description;
        private String name;
        private Integer priority;
        private String id;
        private Boolean disabled;
    }
}
