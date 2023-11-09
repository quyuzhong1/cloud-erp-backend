package com.erp.model.tms.dto;

import java.time.LocalDateTime;

import com.common.business.validator.AddGroup;
import com.common.core.anno.StateEnumValue;
import com.erp.model.tms.enums.LogisticsSupplierTypeEnums;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 物理商表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
@Data
@NoArgsConstructor
public class LogisticsSupplierDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class TabListDTO {

        /**
         * 类型
         */
        private String type;

        /**
         * 类型名
         */
        private String typeName;

        /**
         * 数量
         */
        private Integer count;

    }


    /**
     * 详情
     */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 供应商id
         */
        private String supplierId;

        /**
         * 名称
         */
        private String supplierName;

        /**
         * 类型
         */
        private String type;

        /**
         * 是否禁用 true 禁用
         */
        private Boolean disabled;

        /**
         * 授权状态
         */
        private String authStatus;

        /**
         * 授权时间
         */
        private LocalDateTime authTime;


    }

    /**
     * 新增
     */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 供应商id
         */
        @NotBlank(message = "供应商id不能为空")
        private String supplierId;


        /**
         * 类型
         */
        @NotNull(message = "类型不能为空")
        private LogisticsSupplierTypeEnums type;


    }


}