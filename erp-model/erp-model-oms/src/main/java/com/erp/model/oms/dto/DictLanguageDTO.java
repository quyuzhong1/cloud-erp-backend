package com.erp.model.oms.dto;

import com.baomidou.mybatisplus.annotation.TableField;
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
 * ISO 639-1 语言标准请求响应实体
 * </p>
 *
 * @author jack
 * @since 2025-12-01
*/
@Data
@NoArgsConstructor
public class DictLanguageDTO implements Serializable {




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
         * 本地语言名
         */
        private String nameNative;

        /**
        * 英文名称
        */
        private String nameEn;

        /**
        * 中文名称
        */
        private String nameZh;


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
        * 英文名称
        */
        @NotBlank(message = "英文名称不能为空")
        @Size(max = 32,message = "英文名称最大长度不能超过32位")
        private String nameEn;

        /**
        * 中文名称
        */
        @NotBlank(message = "中文名称不能为空")
        @Size(max = 32,message = "中文名称最大长度不能超过32位")
        private String nameZh;


    }


    /**
     * 列表
     */
    @Data
    @NoArgsConstructor
    public static class ListDTO {
        /**
         * id
         */
        private String id;
        /**
         * 本地语言名
         */
        private String nameNative;

        /**
         * 英文名称
         */
        private String nameEn;

        /**
         * 中文名称
         */
        private String nameZh;


    }

    @Data
    @NoArgsConstructor
    public static class SelectDTO {
        /**
         * 关键词
         */
        private String searchKeyword;
    }



}