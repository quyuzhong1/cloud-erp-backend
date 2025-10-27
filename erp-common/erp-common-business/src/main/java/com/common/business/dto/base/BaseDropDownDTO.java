package com.common.business.dto.base;

import com.common.business.dto.AdvanceQueryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    public static class Tree {
        /**
         * 编码
         */
        private String code;
        private String type;
        /**
         * 值
         */
        private String value;
        private Boolean disabled;
        private List<ChildTree> childTreeList;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChildTree {
        /**
         * 编码
         */
        private String code;
        /**
         * 值
         */
        private String value;
        private Boolean disabled;
    }

    @Data
    @NoArgsConstructor
    public static class DisabledDTO extends CommonDTO {

        /**
         * 启用/禁用
         */
        private Boolean disabled;

        private String type;
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
         * srm 启用/禁用
         */
        private Boolean srmDisabled;
        /**
         * 供应商在协同用户中是否能够使用
         */
        private Boolean supplierDisabled;
        public SrmDisabledDTO(String code, String value, Boolean disabled, Boolean srmDisabled) {
            this.setCode(code);
            this.setValue(value);
            this.disabled = disabled;
            this.srmDisabled = srmDisabled;
            this.supplierDisabled = Objects.isNull(disabled) || Objects.isNull(srmDisabled) || (disabled || srmDisabled);
        }
    }
    @Data
    @NoArgsConstructor
    public static class RemarkDTO extends CommonDTO {
        /**
         * id
         */
        private String id;
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


    @Data
    @NoArgsConstructor
    public static class SupplierDisabledDTO{

        /**
         * 编码
         */
        private String supplierId;
        /**
         * 值
         */
        private String value;
        /**
         * 启用/禁用
         */
        private Boolean disabled;
        public SupplierDisabledDTO(String supplierId, String value, Boolean disabled) {
            this.supplierId = supplierId;
            this.value = value;
            this.disabled = disabled;
        }
    }

    @Data
    @NoArgsConstructor
    public static class SelectDTO {

        /**
         * 关键词
         */
        private String searchKeyword;
        /**
         * 禁用状态
         */
        private Boolean disabled;
        /**
         * 审核状态
         */
        private List<String> approveStatusList;
    }
}
