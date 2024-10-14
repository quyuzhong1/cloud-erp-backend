package com.erp.model.mrp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

/**
 * <p>
 * 平台映射表请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-29
*/
@Data
@NoArgsConstructor
public class CfgPlatformMappingDTO implements Serializable {

    @Data
    @NoArgsConstructor
    public static class SelectDTO {

        /**
         * 关键词
         */
        private String searchKeyword;

        /**
         * 类型，amazonPlatform 亚马逊平台， overseasPlatform海 外平台，internalPlatform 国内平台，b2bPlatform B2B平台
         */
        private String type;
    }


    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * 主表id
         */
        private String id ;

        /**
         * 平台编码
         */
        private String platform;

        /**
         * 平台名称
         */
        private String platformName;

        /**
         * 类型
         */
        private String type;

        /**
         * 禁用
         */
        private Boolean disabled;
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
        * 平台
        */
        private String platform;

        /**
        * 归属平台
        */
        private String type;

        /**
        * 是否禁用
        */
        private Boolean disabled;

        /**
        * 生效时间
        */
        private LocalDate effectiveDate;


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



    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO {

        /**
         * 平台集合
         */
        @NotEmpty(message = "平台集合不能为空")
        private List<String> platformList;

        /**
        * 归属平台
        */
        @NotBlank(message = "归属平台不能为空")
        @Size(max = 32,message = "归属平台最大长度不能超过32位")
        private String type;

        /**
        * 是否禁用
        */
        @NotNull(message = "是否禁用不能为空")
        private Boolean disabled;

        /**
        * 定时生效时间
        */
        @NotNull(message = "定时生效时间不能为空")
        private LocalDate effectiveDate;

        /**
         * 备货模式
         */
        @NotBlank(message = "备货模式不能为空")
        private String stockingMode;
    }


}