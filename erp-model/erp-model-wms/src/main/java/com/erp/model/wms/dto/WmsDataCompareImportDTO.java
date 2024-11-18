package com.erp.model.wms.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * <p>
 * 数据对比导入文件信息请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
*/
@Data
@NoArgsConstructor
public class WmsDataCompareImportDTO implements Serializable {




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
        * 任务id
        */
        private String taskId;

        /**
        * 导入数据文件url地址
        */
        private String fileUrl;

        /**
        * 解析状态：wait=待解析，finish=已解析
        */
        private String parseStatus;

        /**
        * 当前解析偏移量
        */
        private Integer currParseOffset;


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
        * 任务id
        */
        @NotBlank(message = "任务id不能为空")
        @Size(max = 19,message = "任务id最大长度不能超过19位")
        private String taskId;

        /**
        * 导入数据文件url地址
        */
        @NotBlank(message = "导入数据文件url地址不能为空")
        @Size(max = 1024,message = "导入数据文件url地址最大长度不能超过1,024位")
        private String fileUrl;

        /**
        * 解析状态：wait=待解析，finish=已解析
        */
        @NotBlank(message = "解析状态：wait=待解析，finish=已解析不能为空")
        @Size(max = 19,message = "解析状态：wait=待解析，finish=已解析最大长度不能超过19位")
        private String parseStatus;

        /**
        * 当前解析偏移量
        */
        @NotNull(message = "当前解析偏移量不能为空")
        private Integer currParseOffset;


    }


}