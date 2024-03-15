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
 * 费用管理配置表请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-03-15
*/
@Data
@NoArgsConstructor
public class TmsCfgCostDTO implements Serializable {




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
        * 费用归属（字典dictCostAttribution）
        */
        private String dictCostAttribution;

        /**
        * 费用分类（字典dictCostCategory）
        */
        private String dictCostCategory;

        /**
        * 费用名称
        */
        private String costName;


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
        * 费用归属（字典dictCostAttribution）
        */
        @NotBlank(message = "费用归属（字典dictCostAttribution）不能为空")
        @Size(max = 32,message = "费用归属（字典dictCostAttribution）最大长度不能超过32位")
        private String dictCostAttribution;

        /**
        * 费用分类（字典dictCostCategory）
        */
        @NotBlank(message = "费用分类（字典dictCostCategory）不能为空")
        @Size(max = 32,message = "费用分类（字典dictCostCategory）最大长度不能超过32位")
        private String dictCostCategory;

        /**
        * 费用名称
        */
        @NotBlank(message = "费用名称不能为空")
        @Size(max = 64,message = "费用名称最大长度不能超过64位")
        private String costName;


    }


}