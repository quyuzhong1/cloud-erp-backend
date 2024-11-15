package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 国家-组织（政治经济）关系表请求响应实体
 * </p>
 *
 * @author zdy
 * @since 2024-05-21
*/
@Data
@NoArgsConstructor
public class DictCountryOrgDTO implements Serializable {




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
        * 国家二字码ISO 3166-1 Alpha-2 代码
        */
        private String countryId;

        /**
        * 组织编码（政治经济组织编码）
        */
        private String orgCode;

        /**
        * 组织编码（政治经济组织名称）
        */
        private String orgName;

        /**
         * 国家中文名
         */
        private String nameCn;

        /**
         * 国家英文名
         */
        private String nameEn;
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
        * 国家二字码ISO 3166-1 Alpha-2 代码
        */
        @NotBlank(message = "国家二字码ISO 3166不能为空")
        @Size(max = 10,message = "国家二字码ISO 3166最大长度不能超过10位")
        private String countryId;

        /**
        * 组织编码（政治经济组织编码）
        */
        @NotBlank(message = "组织编码（政治经济组织编码）不能为空")
        @Size(max = 64,message = "组织编码（政治经济组织编码）最大长度不能超过64位")
        private String orgCode;

        /**
        * 组织编码（政治经济组织名称）
        */
        @NotBlank(message = "组织编码（政治经济组织名称）不能为空")
        @Size(max = 255,message = "组织编码（政治经济组织名称）最大长度不能超过255位")
        private String orgName;

        /**
         * 国家中文名
         */
        @NotBlank(message = "国家中文名不能为空")
        @Size(max = 255,message = "国家中文名最大长度不能超过255位")
        private String nameCn;

        /**
         * 国家英文名
         */
        @NotBlank(message = "国家英文名不能为空")
        @Size(max = 255,message = "国家英文名最大长度不能超过255位")
        private String nameEn;
    }


}