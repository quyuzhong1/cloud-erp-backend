package com.erp.model.sys.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 脱敏词典请求/响应实体
 *
 * @author cloud-erp
 */
@Data
@NoArgsConstructor
public class CfgMaskWordDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Data
    @NoArgsConstructor
    public static class AddDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 词类型：0=黑名单，1=白名单
         */
        @NotNull(message = "词类型不能为空")
        private Integer wordType;

        /**
         * 分类标签，例 phone / idcard / internal_code
         */
        private String category;

        @NotBlank(message = "词内容不能为空")
        private String word;

        private Boolean disabled = Boolean.FALSE;

        private Integer sort = 0;

        private String remark;
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    public static class UpdateDTO extends AddDTO {
        private static final long serialVersionUID = 1L;

        @NotBlank(message = "id不能为空")
        private String id;

        @NotNull(message = "version不能为空")
        private Integer version;
    }

    @Data
    @NoArgsConstructor
    public static class SearchParamDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer wordType;
        private String category;
        private String word;
        private Boolean disabled;
    }

    @Data
    @NoArgsConstructor
    public static class ListDTO implements Serializable {
        private static final long serialVersionUID = 1L;

        private String id;
        private Integer wordType;
        private String category;
        private String word;
        private Boolean disabled;
        private Integer sort;
        private String remark;
        private Integer version;
        private LocalDateTime createTime;
        private String createUserName;
        private LocalDateTime updateTime;
        private String updateUserName;
    }
}
