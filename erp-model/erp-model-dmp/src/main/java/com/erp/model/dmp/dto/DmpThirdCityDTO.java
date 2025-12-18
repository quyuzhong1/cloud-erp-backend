package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 第三方城市字典表请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-12-17
*/
@Data
@NoArgsConstructor
public class DmpThirdCityDTO implements Serializable {




    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 名称
        */
        private String name;

        /**
        * 国家二字码
        */
        private String countryCode;

        /**
        * 上级城市编码
        */
        private String parentCode;

        /**
        * 城市编码
        */
        private String code;

        /**
        * 城市类型 
        */
        private String type;

        /**
        * 排序
        */
        private Integer index;

        /**
        * 来源平台
        */
        private String sourcePlatform;


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
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 255,message = "名称最大长度不能超过255位")
        private String name;

        /**
        * 国家二字码
        */
        @NotBlank(message = "国家二字码不能为空")
        @Size(max = 10,message = "国家二字码最大长度不能超过10位")
        private String countryCode;

        /**
        * 上级城市编码
        */
        @NotBlank(message = "上级城市编码不能为空")
        @Size(max = 32,message = "上级城市编码最大长度不能超过32位")
        private String parentCode;

        /**
        * 城市类型 
        */
        @NotBlank(message = "城市类型 不能为空")
        @Size(max = 255,message = "城市类型 最大长度不能超过255位")
        private String type;

        /**
        * 排序
        */
        @NotNull(message = "排序不能为空")
        private Integer index;

        /**
        * 来源平台
        */
        @NotBlank(message = "来源平台不能为空")
        @Size(max = 19,message = "来源平台最大长度不能超过19位")
        private String sourcePlatform;


    }

    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class SysAddressParamsDTO{

        @NotBlank(message = "来源平台不能为空")
        private String sourcePlatform;

        private List<String> sysIds;
    }
    /**
     *
     */
    @Data
    @NoArgsConstructor
    public static class ThirdAddressMappingDTO{


        /**
         * 系统id
         */
        private String  sysId;

        /**
         * 主键id
         */
        private String  id;

        /**
         * 是否禁用
         */
        private Boolean disabled;

        /**
         * 名称
         */
        private String name;

        /**
         * 国家二字码
         */
        private String countryCode;

        /**
         * 上级城市编码
         */
        private String parentCode;

        /**
         * 城市编码
         */
        private String code;

        /**
         * 城市类型
         */
        private String type;

        /**
         * 排序
         */
        private Integer index;

        /**
         * 来源平台
         */
        private String sourcePlatform;
    }






}