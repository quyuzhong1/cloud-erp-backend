package com.erp.model.scm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 供应商工厂地信息请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-07-21
*/
@Data
@NoArgsConstructor
public class SupplierPlantAddrDTO implements Serializable {




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
        * 供应商id
        */
        private String supplierId;

        /**
        * 国家
        */
        private String country;

        /**
        * 省份
        */
        private String region;

        /**
        * 城市
        */
        private String city;


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
        * 国家
        */
        @NotBlank(message = "国家不能为空")
        @Size(max = 100,message = "国家最大长度不能超过100位")
        private String country;

        /**
        * 省份
        */
        @Size(max = 50,message = "省份最大长度不能超过50位")
        private String region;

        /**
        * 城市
        */
        @Size(max = 50,message = "城市最大长度不能超过50位")
        private String city;


    }


}