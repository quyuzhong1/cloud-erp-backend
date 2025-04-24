package com.erp.model.plm.dto;

import com.common.core.anno.StateEnumValue;
import com.erp.model.plm.enums.LabelLevelEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 基础标签表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-09-13
*/
@Data
@NoArgsConstructor
public class BasicLabelDTO implements Serializable {




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
        * 标签名称
        */
        private String name;

        /**
        * 类型
        */
        private String type;

        /**
        * 颜色
        */
        private String color;

        /**
        * 标签级别 private 私有，company 公司
        */
        private String level;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {
        private String id;
    }

    @Data
    @NoArgsConstructor
    public static class SearchDTO{
        /**
         * 名称搜索
         */
        private String searchKeyword;

        /**
         * 标签级别 private 私有，company 公司
         */
        private String level;

        private String createUserId;
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

    /**
     * 修改
     */
    @Data
    @NoArgsConstructor
    public static class DeleteDTO{

        /**
         * 主键id
         */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO implements Serializable{

        /**
        * 标签名称
        */
        @NotBlank(message = "标签名称不能为空")
        @Size(max = 50,message = "标签名称最大长度不能超过50位")
        private String name;
        /**
        * 颜色
        */
        @NotBlank(message = "颜色不能为空")
        @Size(max = 50,message = "颜色最大长度不能超过50位")
        private String color;

        /**
        * 标签级别 private 私有，company 公司
        */
        @NotBlank(message = "标签级别 private 私有，company 公司不能为空")
        @Size(max = 20,message = "标签级别 private 私有，company 公司最大长度不能超过20位")
        @StateEnumValue(clazz = LabelLevelEnum.class, message = "标签级别错误")
        private String level;


    }


}