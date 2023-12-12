package com.erp.model.oms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 条件字典表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-08-30
*/
@Data
@NoArgsConstructor
public class DictRuleConditionDTO implements Serializable {




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
        * 对应唯一
        */
        private String key;

        /**
        * 名称
        */
        private String value;

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
        * 对应唯一
        */
        @NotBlank(message = "对应唯一不能为空")
        @Size(max = 50,message = "对应唯一最大长度不能超过50位")
        private String key;

        /**
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 50,message = "名称最大长度不能超过50位")
        private String value;

        /**
        * 备注
        */
        @NotBlank(message = "备注不能为空")
        @Size(max = 255,message = "备注最大长度不能超过255位")
        private String remark;

        /**
        * 类型
        */
        @NotBlank(message = "类型不能为空")
        @Size(max = 30,message = "类型最大长度不能超过30位")
        private String type;


    }


}