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
 * 推送字段映射表请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-15
*/
@Data
@NoArgsConstructor
public class DmpCfgOutputConvertMappingDTO implements Serializable {




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
        * dmp_cfg_output表id
        */
        private String mainId;

        /**
        * 原始字段
        */
        private String originalKey;

        /**
        * 转换后字段
        */
        private String convertKey;

        /**
        * 是否禁用
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
        * dmp_cfg_output表id
        */
        @NotBlank(message = "dmp_cfg_output表id不能为空")
        @Size(max = 19,message = "dmp_cfg_output表id最大长度不能超过19位")
        private String mainId;

        /**
        * 原始字段
        */
        @NotBlank(message = "原始字段不能为空")
        @Size(max = 64,message = "原始字段最大长度不能超过64位")
        private String originalKey;

        /**
        * 转换后字段
        */
        @NotBlank(message = "转换后字段不能为空")
        @Size(max = 64,message = "转换后字段最大长度不能超过64位")
        private String convertKey;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;


    }


}