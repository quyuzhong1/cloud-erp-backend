package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 亚马逊仓储中心配置请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2023-12-25
*/
@Data
@NoArgsConstructor
public class CfgAmzFulfillmentCenterDTO implements Serializable {




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
        * 备注 需要的时候 用到
        */
        private String remark;

        /**
        * 仓储中心代号
        */
        private String code;

        /**
        * 国家代号ID
        */
        private String country;

        /**
        * 禁用状态: f=启用, t=禁用
        */
        private Boolean disabled;


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
        * 备注 需要的时候 用到
        */
        @NotBlank(message = "备注 需要的时候 用到不能为空")
        @Size(max = 255,message = "备注 需要的时候 用到最大长度不能超过255位")
        private String remark;

        /**
        * 国家代号ID
        */
        @NotBlank(message = "国家代号ID不能为空")
        @Size(max = 30,message = "国家代号ID最大长度不能超过30位")
        private String country;

        /**
        * 禁用状态: f=启用, t=禁用
        */
        @NotNull(message = "禁用状态: f=启用, t=禁用不能为空")
        private Boolean disabled;


    }


}