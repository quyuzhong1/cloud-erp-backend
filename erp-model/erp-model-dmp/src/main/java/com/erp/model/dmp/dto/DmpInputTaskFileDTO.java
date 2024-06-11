package com.erp.model.dmp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * <p>
 * 拉取任务文件存储请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
*/
@Data
@NoArgsConstructor
public class DmpInputTaskFileDTO implements Serializable {




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
        * 拉取任务id
        */
        private String mainId;

        /**
        * 文件url
        */
        private String fileUrl;

        /**
        * 解析状态：wait=待解析，finish=已解析
        */
        private String parseStatus;

        /**
        * 已解析行数
        */
        private Integer currParseCount;

        /**
        * 文件大小
        */
        private Integer fileSize;


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
        * 拉取任务id
        */
        @NotBlank(message = "拉取任务id不能为空")
        @Size(max = 19,message = "拉取任务id最大长度不能超过19位")
        private String mainId;

        /**
        * 文件url
        */
        private String fileUrl;

        /**
        * 解析状态：wait=待解析，finish=已解析
        */
        @NotBlank(message = "解析状态：wait=待解析，finish=已解析不能为空")
        @Size(max = 50,message = "解析状态：wait=待解析，finish=已解析最大长度不能超过50位")
        private String parseStatus;

        /**
        * 已解析行数
        */
        @NotNull(message = "已解析行数不能为空")
        private Integer currParseCount;

        /**
        * 文件大小
        */
        @NotNull(message = "文件大小不能为空")
        private Integer fileSize;


    }


}