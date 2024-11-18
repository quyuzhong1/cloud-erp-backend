package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 运费规则分区城市表请求响应实体
 * </p>
 *
 * @author Will
 * @since 2023-11-07
*/
@Data
@NoArgsConstructor
public class ShippingRegionCityDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * 规则id
        */
        private String shippingTemplateRuleId;

        /**
        * 城市
        */
        private String city;

        /**
        * 分区
        */
        private String region;


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
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 19,message = "主表id最大长度不能超过19位")
        private String mainId;

        /**
        * 规则id
        */
        @NotBlank(message = "规则id不能为空")
        @Size(max = 19,message = "规则id最大长度不能超过19位")
        private String shippingTemplateRuleId;

        /**
        * 城市
        */
        @NotBlank(message = "城市不能为空")
        @Size(max = 64,message = "城市最大长度不能超过64位")
        private String city;

        /**
        * 分区
        */
        @NotBlank(message = "分区不能为空")
        @Size(max = 64,message = "分区最大长度不能超过64位")
        private String region;


    }


}