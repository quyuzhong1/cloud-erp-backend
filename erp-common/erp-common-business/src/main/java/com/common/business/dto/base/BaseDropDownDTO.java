package com.common.business.dto.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/3/16 16:14
 */
@Data
public class BaseDropDownDTO implements Serializable {



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommonDTO {
        /**
         * 编码
         */
        private String code;
        /**
         * 值
         */
        private String value;
    }

    @Data
    @NoArgsConstructor
    public static class DisabledDTO extends CommonDTO{

        /**
         * 启用/禁用
         */
        private Boolean disabled;

        public DisabledDTO(String code, String value, Boolean disabled) {
            this.setCode(code);
            this.setValue(value);
            this.disabled = disabled;
        }
    }

}
