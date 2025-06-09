package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 物流商授权字段配置表请求响应实体
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
@Data
@NoArgsConstructor
public class CfgLogisticsAuthFieldDTO implements Serializable {


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
         * 物流平台
         */
        private String ogisticsPlatform;

        /**
         * 字段
         */
        private String fieldCode;

        /**
         * 字段名
         */
        private String fieldName;

        /**
         * 物流平台名
         */
        private String ogisticsPlatformName;


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

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        private String id;

        private String fieldCode;

        private String fieldName;


    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 物流平台
         */
        @NotBlank(message = "物流平台不能为空")
        @Size(max = 19, message = "物流平台最大长度不能超过19位")
        private String logisticsPlatform;

        /**
         * 字段
         */
        @NotBlank(message = "字段不能为空")
        @Size(max = 30, message = "字段最大长度不能超过30位")
        private String fieldCode;

        /**
         * 字段名
         */
        @NotBlank(message = "字段名不能为空")
        @Size(max = 50, message = "字段名最大长度不能超过50位")
        private String fieldName;

        /**
         * 物流平台名
         */
        @NotBlank(message = "物流平台名不能为空")
        @Size(max = 50, message = "物流平台名最大长度不能超过50位")
        private String logisticsPlatformName;


    }


}