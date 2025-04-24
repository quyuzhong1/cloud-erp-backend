package com.erp.model.sys.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * PDA用户跳过版本升级记录表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2023-09-12
*/
@Data
@NoArgsConstructor
public class PdaUserSkipVersionDTO implements Serializable {




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
        * 用户id
        */
        private String userId;

        /**
        * 用户名称
        */
        private String userName;

        /**
        * 版本id
        */
        private String versionId;


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
        * 用户id
        */
        @NotBlank(message = "用户id不能为空")
        @Size(max = 19,message = "用户id最大长度不能超过19位")
        private String userId;

        /**
        * 用户名称
        */
        @NotBlank(message = "用户名称不能为空")
        @Size(max = 50,message = "用户名称最大长度不能超过50位")
        private String userName;

        /**
        * 版本id
        */
        @NotBlank(message = "版本id不能为空")
        @Size(max = 19,message = "版本id最大长度不能超过19位")
        private String versionId;


    }


}