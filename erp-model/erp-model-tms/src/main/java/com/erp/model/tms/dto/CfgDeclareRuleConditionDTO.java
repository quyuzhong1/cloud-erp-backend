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
        @NotBlank(message = "id can not be blank")
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {
        private String ruleId;

        @NotBlank(message = "leftBracket can not be blank")
        @Size(max = 10, message = "leftBracket length must be <= 10")
        private String leftBracket;

        @NotBlank(message = "field can not be blank")
        @Size(max = 30, message = "field length must be <= 30")
        private String field;

        @NotBlank(message = "compare can not be blank")
        @Size(max = 30, message = "compare length must be <= 30")
        private String compare;

        @NotBlank(message = "value can not be blank")
        private String value;

        @NotBlank(message = "name can not be blank")
        private String name;

        @NotBlank(message = "rightBracket can not be blank")
        @Size(max = 10, message = "rightBracket length must be <= 10")
        private String rightBracket;

        @Size(max = 10, message = "logic length must be <= 10")
        private String logic;

        private Integer index;
    }
}
