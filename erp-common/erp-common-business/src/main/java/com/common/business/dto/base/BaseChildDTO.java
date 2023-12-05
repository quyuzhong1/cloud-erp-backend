package com.common.business.dto.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @date 2023/3/16 16:14
 */
@Data
public class BaseChildDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListChildTreeDTO {

        /**
         * id
         */
        private String id;

        /**
         * 名称
         */
        private String name;

        /**
         * 禁用状态
         */
        private Boolean disabled;

        @JsonInclude(value= JsonInclude.Include.NON_NULL)
        private List<ListChildTreeDTO> children;


    }

}
