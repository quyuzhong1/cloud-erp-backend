package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * kol类型表请求响应实体
 * </p>
 *
 * @author will
 * @since 2025-12-01
*/
@Data
@NoArgsConstructor
public class CfgKolOptionDTO implements Serializable {




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
        private String name;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 备注
        */
        private String remark;

        /**
        * 类型
        */
        private String type;


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
        private String name;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 备注
        */
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 类型
        */
        @NotBlank(message = "类型不能为空")
        @Size(max = 64,message = "类型最大长度不能超过64位")
        private String type;


    }


}