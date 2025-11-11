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
 * 差异策略配置基础信息请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2025-11-11
*/
@Data
@NoArgsConstructor
public class CfgDiffStrategyDTO implements Serializable {




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
        * 配置编码
        */
        private String code;

        /**
        * 配置名称
        */
        private String name;

        /**
        * 单据类型
        */
        private String billType;

        /**
        * 执行状态
        */
        private Boolean status;


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
        * 配置名称
        */
        @NotBlank(message = "配置名称不能为空")
        @Size(max = 255,message = "配置名称最大长度不能超过255位")
        private String name;

        /**
        * 单据类型
        */
        @NotBlank(message = "单据类型不能为空")
        @Size(max = 50,message = "单据类型最大长度不能超过50位")
        private String billType;

        /**
        * 执行状态
        */
        @NotNull(message = "执行状态不能为空")
        private Boolean status;


    }


}