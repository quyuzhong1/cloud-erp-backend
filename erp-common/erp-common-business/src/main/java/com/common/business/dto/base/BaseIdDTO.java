package com.common.business.dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * @Classname BaseIdDTO

 * @Date 2022-09-20 11:18
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class BaseIdDTO extends PermissionsDTO  {

    @NotBlank(message = "id不能为空")
    private String id;

    private String name;


    @Data
    @NoArgsConstructor
    public static class CodeDTO {

        private String id;

        private String code;

        private String name;

        private String flagId;

        private Boolean disabled;

    }

    @Data
    @NoArgsConstructor
    public static class ChangeDTO {
        /**
         * 主表id
         */
        @NotBlank(message = "id不能为空")
        private String id;
        /**
         * 明细id
         */
        @NotBlank(message = "明细id不能为空")
        private String detailId;
        /**
         * 源数据id
         */
        private String sourceId;
        /**
         * 目标id
         */
        @NotBlank(message = "变更数据不能为空")
        private String targetId;
    }
}
