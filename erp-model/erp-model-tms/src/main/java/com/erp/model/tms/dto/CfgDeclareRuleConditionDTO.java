package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.base.SuperDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class CfgDeclareRuleConditionDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TabListDTO {
        private String tabFlag;
        private Integer count;
    }

    @Data
    @NoArgsConstructor
    public static class PagingParamDTO extends SortDTO {
        private List<AdvanceQueryDTO> advanceQueryDTOList;
        private Map<String, String> sqlMap;
    }

    @Data
    @NoArgsConstructor
    public static class ListDTO {
        private String id;
        private String ruleId;
        private String leftBracket;
        private String field;
        private String compare;
        private String value;
        private String name;
        private String rightBracket;
        private String logic;
        private Integer index;
        private String approveStatusName;
        private LocalDateTime createTime;
        private String createUserName;
    }

    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        private List<String> ids;
    }

    @Data
    @NoArgsConstructor
    public static class ViewDTO {
        private String id;
        private String ruleId;
        private String leftBracket;
        private String field;
        private String compare;
        private String value;
        private String name;
        private String rightBracket;
        private String logic;
        private Integer index;
    }

    @Data
    @NoArgsConstructor
    public static class SaveDTO extends CommonDTO {
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {
        @NotBlank(message = "主键id不能为空")
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {
        private String ruleId;

        @NotBlank(message = "左括号不能为空")
        @Size(max = 10, message = "左括号最大长度不能超过10位")
        private String leftBracket;

        @NotBlank(message = "条件的字段不能为空")
        @Size(max = 30, message = "条件的字段最大长度不能超过30位")
        private String field;

        @NotBlank(message = "比较符不能为空")
        @Size(max = 30, message = "比较符最大长度不能超过30位")
        private String compare;

        @NotBlank(message = "值不能为空")
        private String value;

        @NotBlank(message = "值对应名称不能为空")
        private String name;

        @NotBlank(message = "右括号不能为空")
        @Size(max = 10, message = "右括号最大长度不能超过10位")
        private String rightBracket;

        @Size(max = 10, message = "逻辑关系最大长度不能超过10位")
        private String logic;

        private Integer index;
    }
}
