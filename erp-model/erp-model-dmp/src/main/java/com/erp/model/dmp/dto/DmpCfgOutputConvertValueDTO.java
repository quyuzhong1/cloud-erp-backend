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
 * 推送字段映射值请求响应实体
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-20
*/
@Data
@NoArgsConstructor
public class DmpCfgOutputConvertValueDTO implements Serializable {




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
        * dmp_cfg_output_convert_mapping表id
        */
        private String mainId;

        /**
        * 转换后的值
        */
        private String convertAfterValue;

        /**
        * 转换前的值
        */
        private String convertBeforeValue;


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
        * dmp_cfg_output_convert_mapping表id
        */
        @NotBlank(message = "dmp_cfg_output_convert_mapping表id不能为空")
        @Size(max = 255,message = "dmp_cfg_output_convert_mapping表id最大长度不能超过255位")
        private String mainId;

        /**
        * 转换后的值
        */
        @NotBlank(message = "转换后的值不能为空")
        @Size(max = 255,message = "转换后的值最大长度不能超过255位")
        private String convertAfterValue;

        /**
        * 转换前的值
        */
        @NotBlank(message = "转换前的值不能为空")
        @Size(max = 255,message = "转换前的值最大长度不能超过255位")
        private String convertBeforeValue;


    }


}