package com.erp.model.wms.dto.pickingstrategy;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CfgRulePickingDTO {
    @Getter
    @Setter
    public static class PagingView {
    }
    @Getter
    @Setter
    public static class PagingParam {
    }
    @Getter
    @Setter
    public static class Add {
    }
    @Getter
    @Setter
    public static class View {

        private List<CfgRuleActionDTO> actions;
        private List<CfgRuleConditionDTO> conditionList;
        private String description;
        private String name;
        private Integer priority;
        private String id;
        private String disabled;
    }
}
