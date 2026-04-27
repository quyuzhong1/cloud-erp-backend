package com.erp.model.tms.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import com.common.business.dto.base.SuperDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class CfgDeclareRuleDTO implements Serializable {

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
    public static class ListParamDTO {
        @NotBlank(message = "ruleType can not be blank")
        private String ruleType;
    }

    @Data
    @NoArgsConstructor
    public static class ListDTO {
        private String id;
        private String ruleType;
        private String senderType;
        private String senderId;
        private String senderName;
        private String receiverType;
        private String receiverId;
        private String receiverName;
        private Boolean disabled;
        private String detailId;
        private String ruleId;
        private String leftBracket;
        private String field;
        private String compare;
        private String value;
        private String name;
        private String rightBracket;
        private String logic;
        private Integer index;
        private String ruleTypeName;
        private String senderTypeName;
        private String receiverTypeName;
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
        private String ruleType;
        private String senderType;
        private String senderId;
        private String senderName;
        private String receiverType;
        private String receiverId;
        private String receiverName;
        private Boolean disabled;
        private List<CfgDeclareRuleConditionDTO.ListDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class SaveListDTO {
        @NotBlank(message = "ruleType can not be blank")
        private String ruleType;

        private List<CfgDeclareRuleDTO.@Valid SaveDTO> list;
    }

    @Data
    @NoArgsConstructor
    public static class SaveDTO extends CommonDTO {
        private String id;

        @NotEmpty(message = "detailList can not be empty")
        private List<CfgDeclareRuleConditionDTO.@Valid SaveDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        @NotEmpty(message = "detailList can not be empty")
        private List<CfgDeclareRuleConditionDTO.@Valid AddDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {
        @NotBlank(message = "id can not be blank")
        private String id;

        @NotEmpty(message = "detailList can not be empty")
        private List<CfgDeclareRuleConditionDTO.@Valid UpdateDTO> detailList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {
        @NotBlank(message = "ruleType can not be blank")
        @Size(max = 32, message = "ruleType length must be <= 32")
        private String ruleType;

        @NotBlank(message = "senderType can not be blank")
        @Size(max = 100, message = "senderType length must be <= 100")
        private String senderType;

        @NotBlank(message = "senderId can not be blank")
        private String senderId;

        private String senderName;

        @NotBlank(message = "receiverType can not be blank")
        @Size(max = 100, message = "receiverType length must be <= 100")
        private String receiverType;

        @NotBlank(message = "receiverId can not be blank")
        private String receiverId;

        private String receiverName;

        private Boolean disabled;
    }
}
