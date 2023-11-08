package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 物流授权表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
*/
@Data
@NoArgsConstructor
public class LogisticsAuthDTO implements Serializable {




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
        * 物流平台
        */
        private String logisticsPlatform;

        /**
        * 物流商id
        */
        private String mainId;

        /**
        * name
        */
        private String name;

        /**
        * 账号
        */
        private String account;

        /**
        * 密码
        */
        private String password;


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
        * 物流平台
        */
        @NotBlank(message = "物流平台不能为空")
        @Size(max = 30,message = "物流平台最大长度不能超过30位")
        private String logisticsPlatform;

        /**
        * 物流商id
        */
        @NotBlank(message = "物流商id不能为空")
        @Size(max = 19,message = "物流商id最大长度不能超过19位")
        private String mainId;

        /**
        * name
        */
        @NotBlank(message = "name不能为空")
        @Size(max = 30,message = "name最大长度不能超过30位")
        private String name;

        /**
        * 账号
        */
        @NotBlank(message = "账号不能为空")
        @Size(max = 255,message = "账号最大长度不能超过255位")
        private String account;

        /**
        * 密码
        */
        @NotBlank(message = "密码不能为空")
        @Size(max = 50,message = "密码最大长度不能超过50位")
        private String password;


    }


}