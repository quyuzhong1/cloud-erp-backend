package com.erp.model.tms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 多选下拉存储表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-24
*/
@Data
@NoArgsConstructor
public class MultipleOptionDTO implements Serializable {




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
        * 主表id , type字段的表id
        */
        private String mainId;

        /**
        * 单据类型
        */
        private String type;

        /**
        * 下拉值Id
        */
        private String refId;


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
        * 主表id , type字段的表id
        */
        @NotBlank(message = "主表id , type字段的表id不能为空")
        @Size(max = 19,message = "主表id , type字段的表id最大长度不能超过19位")
        private String mainId;

        /**
        * 单据类型
        */
        @NotBlank(message = "单据类型不能为空")
        @Size(max = 64,message = "单据类型最大长度不能超过64位")
        private String type;

        /**
        * 多选的下拉值Id
        */
        @NotBlank(message = "下拉值Id不能为空")
        @Size(max = 19,message = "下拉值Id最大长度不能超过19位")
        private List<String> refIdList;


    }


}