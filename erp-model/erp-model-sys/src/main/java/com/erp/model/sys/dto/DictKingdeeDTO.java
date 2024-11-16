package com.erp.model.sys.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * <p>
 * 金蝶字典表请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2024-06-07
*/
@Data
@NoArgsConstructor
public class DictKingdeeDTO implements Serializable {


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ListDTO {
        /**
         * 类型名称
         */
        private String typeName;
        /**
         * 字典值
         */
        private String code;
        /**
         * 字典名
         */
        private String name;
        /**
         * 是否已作废
         */
        private Boolean disabled;
    }

    @Data
    @NoArgsConstructor
    public static class ParamDTO {

        /**
         * 类型名称
         */
        private String typeName;

        /**
         * 字典值
         */
        private String value;
    }


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
        * 金蝶id
        */
        private String kingdeeId;

        /**
        * 类型名称
        */
        private String typeName;

        /**
        * 字典值
        */
        private String value;

        /**
        * 字典名
        */
        private String name;

        /**
        * 是否已作废
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
        * 金蝶id
        */
        private String kingdeeId;

        /**
        * 类型名称
        */
        private String typeName;

        /**
        * 字典值
        */
        private String value;

        /**
        * 字典名
        */
        private String name;

    }


}