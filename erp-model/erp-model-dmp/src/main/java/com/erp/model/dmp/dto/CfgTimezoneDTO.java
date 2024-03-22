package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 国家对应的时区配置请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2024-03-13
*/
@Data
@NoArgsConstructor
public class CfgTimezoneDTO implements Serializable {




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
        * 备注
        */
        private String remark;

        /**
        * 国家代号
        */
        private String country;

        /**
        * 时区
        */
        private String timeZone;

        /**
        * 与UTC相差小时数
        */
        private Integer utcDiffHour;


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
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 国家代号
        */
        @NotBlank(message = "国家代号不能为空")
        @Size(max = 19,message = "国家代号最大长度不能超过19位")
        private String country;

        /**
        * 时区
        */
        @NotBlank(message = "时区不能为空")
        @Size(max = 19,message = "时区最大长度不能超过19位")
        private String timeZone;

        /**
        * 与UTC相差小时数
        */
        @NotNull(message = "与UTC相差小时数不能为空")
        private Integer utcDiffHour;


    }


}