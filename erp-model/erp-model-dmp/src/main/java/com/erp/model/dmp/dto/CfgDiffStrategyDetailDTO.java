package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 差异策略配置明细请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-11-11
*/
@Data
@NoArgsConstructor
public class CfgDiffStrategyDetailDTO implements Serializable {




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
        * 主表id
        */
        private String mainId;

        /**
        * 差异标签
        */
        private String diffTag;

        /**
        * 建议处理方式
        */
        private String suggestType;

        /**
        * 条件sql
        */
        private String conditionSql;

        /**
        * 条件描述
        */
        private String conditionDesc;


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
        * 主表id
        */
        @NotBlank(message = "主表id不能为空")
        @Size(max = 50,message = "主表id最大长度不能超过50位")
        private String mainId;

        /**
        * 差异标签
        */
        @NotBlank(message = "差异标签不能为空")
        @Size(max = 50,message = "差异标签最大长度不能超过50位")
        private String diffTag;

        /**
        * 建议处理方式
        */
        @NotBlank(message = "建议处理方式不能为空")
        @Size(max = 255,message = "建议处理方式最大长度不能超过255位")
        private String suggestType;

        /**
        * 条件sql
        */
        @NotBlank(message = "条件sql不能为空")
        private String conditionSql;

        /**
        * 条件描述
        */
        @NotBlank(message = "条件描述不能为空")
        private String conditionDesc;


    }


}