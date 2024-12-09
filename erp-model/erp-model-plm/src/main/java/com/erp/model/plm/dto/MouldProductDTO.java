package com.erp.model.plm.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 模具 产品请求响应实体
 * </p>
 *
 * @author liaohui
 * @since 2024-12-03
*/
@Data
@NoArgsConstructor
public class MouldProductDTO implements Serializable {




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
        * 模具id
        */
        private String mouldDetailId;

        /**
        * 产品名称
        */
        private String productName;

        /**
        * 图片地址
        */
        private String imagesUrl;


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
    @Getter
    @Setter
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
        * 产品名称
        */
        @NotBlank(message = "产品名称不能为空")
        @Size(max = 255,message = "产品名称最大长度不能超过255位")
        private String productName;

        /**
        * 图片地址
        */
        @NotBlank(message = "图片地址不能为空")
        @Size(max = 255,message = "图片地址最大长度不能超过255位")
        private String imagesUrl;


    }


}