package com.common.business.dto.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author Will
 * @version 1.0
 * @date 2023/3/16 16:14
 */
@Data
public class BaseDropDownDTO implements Serializable {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommonBooleanDTO {
        /**
         * 编码
         */
        private Boolean code;

        /**
         * 值
         */
        private String value;
    }


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
    public static class DisabledDTO extends CommonDTO {

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
    @Data
    @NoArgsConstructor
    public static class SrmDisabledDTO extends CommonDTO {

        /**
         * 启用/禁用
         */
        private Boolean disabled;
        /**
         * 启用/禁用
         */
        private Boolean srmDisabled;
        public SrmDisabledDTO(String code, String value, Boolean disabled, Boolean srmDisabled) {
            this.setCode(code);
            this.setValue(value);
            this.disabled = disabled;
            this.srmDisabled = srmDisabled;
        }
    }
    @Data
    @NoArgsConstructor
    public static class RemarkDTO extends CommonDTO {

        /**
         * 备注
         */
        private String remark;

        /**
         * 启用/禁用
         */
        private Boolean disabled;

        public RemarkDTO(String code, String value, String remark, Boolean disabled) {
            this.setCode(code);
            this.setValue(value);
            this.remark = remark;
            this.disabled = disabled;
        }
    }


    @Data
    @NoArgsConstructor
    public static class QcTypeDTO extends CommonDTO {

        /**
         * 是否是内部检验  true 是
         */
        private Boolean isInside;

        public QcTypeDTO(String code, String value, Boolean isInside) {
            this.setCode(code);
            this.setValue(value);
            this.isInside = isInside;
        }
    }

}
