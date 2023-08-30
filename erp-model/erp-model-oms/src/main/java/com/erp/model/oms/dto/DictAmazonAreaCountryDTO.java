package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-08-30
*/
@Data
@NoArgsConstructor
public class DictAmazonAreaCountryDTO implements Serializable {




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
        * 区域code
        */
        private String regionCode;

        /**
        * 区域名称
        */
        private String regionName;

        /**
        * 国家code
        */
        private String countryCode;

        /**
        * 国家名称
        */
        private String countryName;


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
        * 区域code
        */
        @NotBlank(message = "区域code不能为空")
        @Size(max = 10,message = "区域code最大长度不能超过10位")
        private String regionCode;

        /**
        * 区域名称
        */
        @NotBlank(message = "区域名称不能为空")
        @Size(max = 20,message = "区域名称最大长度不能超过20位")
        private String regionName;

        /**
        * 国家code
        */
        @NotBlank(message = "国家code不能为空")
        @Size(max = 10,message = "国家code最大长度不能超过10位")
        private String countryCode;

        /**
        * 国家名称
        */
        @NotBlank(message = "国家名称不能为空")
        @Size(max = 20,message = "国家名称最大长度不能超过20位")
        private String countryName;


    }


}