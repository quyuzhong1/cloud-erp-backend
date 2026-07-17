package com.erp.model.plm.dto;

import com.common.business.dto.AdvanceQueryDTO;
import com.common.business.dto.base.SortDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 产品违禁词库请求响应实体
 */
@Data
@NoArgsConstructor
public class CfgProductForbiddenWordDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class AddDTO implements Serializable {

        /**
         * 违禁词集合
         */
        @NotEmpty(message = "违禁词不能为空")
        private List<String> forbiddenWords;
    }

    @Data
    @NoArgsConstructor
    public static class UpdateDTO implements Serializable {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

        /**
         * 版本号
         */
        private Integer version;

        /**
         * 违禁词
         */
        @NotBlank(message = "违禁词不能为空")
        @Size(max = 200, message = "违禁词最大长度不能超过200位")
        private String forbiddenWord;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
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
         * 勾选的id集合
         */
        private List<String> ids;

        /**
         * 违禁词
         */
        private String forbiddenWord;

        /**
         * 状态：false=启用，true=禁用
         */
        private Boolean disabled;

        /**
         * 创建人
         */
        private String createUserName;

        /**
         * 创建开始时间
         */
        private LocalDateTime createStartTime;

        /**
         * 创建结束时间
         */
        private LocalDateTime createEndTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListDTO implements Serializable {

        private String id;

        private String forbiddenWord;

        private Boolean disabled;

        private String disabledName;

        private String createUserId;

        private String createUserName;

        private LocalDateTime createTime;

        private String updateUserId;

        private String updateUserName;

        private LocalDateTime updateTime;

        private Integer version;
    }
}
