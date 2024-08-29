package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 第三方城市字典表请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2023-11-23
*/
@Data
@NoArgsConstructor
public class DictThirdCityDTO implements Serializable {




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
        * 名称
        */
        private String regionName;

        /**
        * 国家二字码
        */
        private String countryCode;

        /**
        * 上级城市id
        */
        private String parentRegionId;

        /**
        * 区域等级
        */
        private Integer regionLevel;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 系统字典表id
        */
        private String dictCityId;

        /**
        * 城市id
        */
        private String regionId;


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
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 255,message = "名称最大长度不能超过255位")
        private String regionName;

        /**
        * 国家二字码
        */
        @NotBlank(message = "国家二字码不能为空")
        @Size(max = 2,message = "国家二字码最大长度不能超过2位")
        private String countryCode;

        /**
        * 上级城市id
        */
        @NotBlank(message = "上级城市id不能为空")
        @Size(max = 19,message = "上级城市id最大长度不能超过19位")
        private String parentRegionId;

        /**
        * 区域等级
        */
        @NotNull(message = "区域等级不能为空")
        private Integer regionLevel;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * 系统字典表id
        */
        @NotBlank(message = "系统字典表id不能为空")
        @Size(max = 19,message = "系统字典表id最大长度不能超过19位")
        private String dictCityId;

        /**
        * 城市id
        */
        @NotBlank(message = "城市id不能为空")
        @Size(max = 19,message = "城市id最大长度不能超过19位")
        private String regionId;


    }


}