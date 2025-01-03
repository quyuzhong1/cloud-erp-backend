package com.erp.model.sys.dto;

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
 * 分区国家关联表请求响应实体
 * </p>
 *
 * @author lrp
 * @since 2025-01-03
*/
@Data
@NoArgsConstructor
public class CfgCountryPartitionDTO implements Serializable {




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
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 分区id
        */
        private String partitionId;

        /**
        * 分区编码
        */
        private String partitionCode;

        /**
        * 分区名称
        */
        private String partitionName;

        /**
        * 国家二字码
        */
        private String country;

        /**
        * 国家名称
        */
        private String countryName;

        /**
        * 排序字段
        */
        private Integer index;


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
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * 分区id
        */
        @NotBlank(message = "分区id不能为空")
        @Size(max = 19,message = "分区id最大长度不能超过19位")
        private String partitionId;

        /**
        * 分区编码
        */
        @NotBlank(message = "分区编码不能为空")
        @Size(max = 255,message = "分区编码最大长度不能超过255位")
        private String partitionCode;

        /**
        * 分区名称
        */
        @NotBlank(message = "分区名称不能为空")
        @Size(max = 255,message = "分区名称最大长度不能超过255位")
        private String partitionName;

        /**
        * 国家二字码
        */
        @NotBlank(message = "国家二字码不能为空")
        @Size(max = 20,message = "国家二字码最大长度不能超过20位")
        private String country;

        /**
        * 国家名称
        */
        @NotBlank(message = "国家名称不能为空")
        @Size(max = 255,message = "国家名称最大长度不能超过255位")
        private String countryName;

        /**
        * 排序字段
        */
        @NotNull(message = "排序字段不能为空")
        private Integer index;


    }


}