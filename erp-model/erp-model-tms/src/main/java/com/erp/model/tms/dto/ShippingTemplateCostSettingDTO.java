package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 运费模板其他费用选值表请求响应实体
 * </p>
 *
 * @author Will
 * @since 2023-11-03
*/
@Data
@NoArgsConstructor
public class ShippingTemplateCostSettingDTO implements Serializable {




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
        * 其他费用id
        */
        private String otherCostId;

        /**
        * 编码
        */
        private String code;

        /**
        * 名称
        */
        private String name;

        /**
         * 计算方式
         */
        private String calculationMethod;


        public ViewDTO (String code,String name,String calculationMethod) {
            this.code = code;
            this.name = name;
            this.calculationMethod = calculationMethod;
        }
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
         * 编码
         */
        @Size(max = 32,message = "编码最大长度不能超过32位")
        private String code;

        /**
         * 其他费用id
         */
        @NotBlank(message = "其他费用id不能为空")
        private String otherCostId;

        /**
         * 计算方式
         */
        private String calculationMethod;
    }


}