package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * <p>
 * 第三方系统配置请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-05-06
*/
@Data
@NoArgsConstructor
public class SysRefererConfigDTO implements Serializable {




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
        * 第三方系统
        */
        private String referer;

        /**
        * 秘钥
        */
        private String secretKey;


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
        * 第三方系统
        */
        private String referer;

        /**
        * 秘钥
        */
        private String secretKey;


    }


}