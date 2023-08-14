package com.erp.model.sys.dto;

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
 * @author Luo_WG
 * @since 2023-08-14
*/
@Data
@NoArgsConstructor
public class PdaVersionDTO implements Serializable {




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
        * pda版本
        */
        private String pdaVersion;

        /**
        * 升级内容描述
        */
        private String remark;

        /**
        * 是否强制更新
        */
        private Boolean force;

        /**
        * 升级包地址
        */
        private String url;


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
        * pda版本
        */
        @NotBlank(message = "pda版本不能为空")
        @Size(max = 255,message = "pda版本最大长度不能超过255位")
        private String pdaVersion;

        /**
        * 升级内容描述
        */
        @NotBlank(message = "升级内容描述不能为空")
        private String remark;

        /**
        * 是否强制更新
        */
        @NotNull(message = "是否强制更新不能为空")
        private Boolean force;

        /**
        * 升级包地址
        */
        @NotBlank(message = "升级包地址不能为空")
        private String url;


    }


}