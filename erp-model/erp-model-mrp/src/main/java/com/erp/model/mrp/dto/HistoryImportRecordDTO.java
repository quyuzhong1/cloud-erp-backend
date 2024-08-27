package com.erp.model.mrp.dto;

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
 * 历史导入记录请求响应实体
 * </p>
 *
 * @author will
 * @since 2024-08-27
*/
@Data
@NoArgsConstructor
public class HistoryImportRecordDTO implements Serializable {




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
        * 名称
        */
        private String name;

        /**
        * 类型
        */
        private String type;

        /**
        * 模块，SourceTypeEnum枚举
        */
        private String module;

        /**
        * fastdfs文件url
        */
        private String fileUrl;


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
        * 名称
        */
        @NotBlank(message = "名称不能为空")
        @Size(max = 64,message = "名称最大长度不能超过64位")
        private String name;

        /**
        * 类型
        */
        @NotBlank(message = "类型不能为空")
        @Size(max = 32,message = "类型最大长度不能超过32位")
        private String type;

        /**
        * 模块，SourceTypeEnum枚举
        */
        @NotBlank(message = "模块，SourceTypeEnum枚举不能为空")
        @Size(max = 64,message = "模块，SourceTypeEnum枚举最大长度不能超过64位")
        private String module;

        /**
        * fastdfs文件url
        */
        @NotBlank(message = "fastdfs文件url不能为空")
        @Size(max = 255,message = "fastdfs文件url最大长度不能超过255位")
        private String fileUrl;


    }


}