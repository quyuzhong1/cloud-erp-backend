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
 * 平台文件转存FastDFS关系记录表请求响应实体
 * </p>
 *
 * @author Jim
 * @since 2025-10-30
*/
@Data
@NoArgsConstructor
public class DmpRefPlatformFileDTO implements Serializable {




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
        * 输入任务id
        */
        private String inputTaskId;

        /**
        * 任务转换ID
        */
        private String convertId;

        /**
        * 店铺ID
        */
        private String nextLevelId;

        /**
        * 任务来源唯一加密代号
        */
        private String uniqueEncrypt;

        /**
        * 任务数据加密代号
        */
        private String dataEncrypt;

        /**
        * 来源平台编码
        */
        private String sourceSystem;

        /**
        * 来源文件URL
        */
        private String sourceUrl;

        /**
        * FastDFS文件URL
        */
        private String fileUrl;

        /**
        * 文件类型（json/png/jpeg等）
        */
        private String fileType;

        /**
        * 业务类型
        */
        private String billTopic;

        /**
        * 业务ID
        */
        private String billId;

        /**
        * 文件唯一标识
        */
        private String fileKey;

        /**
        * 文件名称
        */
        private String fileName;


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
        * 输入任务id
        */
        @NotBlank(message = "输入任务id不能为空")
        @Size(max = 19,message = "输入任务id最大长度不能超过19位")
        private String inputTaskId;

        /**
        * 任务转换ID
        */
        @NotBlank(message = "任务转换ID不能为空")
        @Size(max = 19,message = "任务转换ID最大长度不能超过19位")
        private String convertId;

        /**
        * 店铺ID
        */
        @NotBlank(message = "店铺ID不能为空")
        @Size(max = 19,message = "店铺ID最大长度不能超过19位")
        private String nextLevelId;

        /**
        * 任务来源唯一加密代号
        */
        @NotBlank(message = "任务来源唯一加密代号不能为空")
        private String uniqueEncrypt;

        /**
        * 任务数据加密代号
        */
        @NotBlank(message = "任务数据加密代号不能为空")
        private String dataEncrypt;

        /**
        * 来源平台编码
        */
        @NotBlank(message = "来源平台编码不能为空")
        @Size(max = 192,message = "来源平台编码最大长度不能超过192位")
        private String sourceSystem;

        /**
        * 来源文件URL
        */
        @NotBlank(message = "来源文件URL不能为空")
        private String sourceUrl;

        /**
        * FastDFS文件URL
        */
        @NotBlank(message = "FastDFS文件URL不能为空")
        @Size(max = 765,message = "FastDFS文件URL最大长度不能超过765位")
        private String fileUrl;

        /**
        * 文件类型（json/png/jpeg等）
        */
        @NotBlank(message = "文件类型（json/png/jpeg等）不能为空")
        @Size(max = 64,message = "文件类型（json/png/jpeg等）最大长度不能超过64位")
        private String fileType;

        /**
        * 业务类型
        */
        @NotBlank(message = "业务类型不能为空")
        @Size(max = 64,message = "业务类型最大长度不能超过64位")
        private String billTopic;

        /**
        * 业务ID
        */
        @NotBlank(message = "业务ID不能为空")
        @Size(max = 765,message = "业务ID最大长度不能超过765位")
        private String billId;

        /**
        * 文件唯一标识
        */
        @NotBlank(message = "文件唯一标识不能为空")
        @Size(max = 765,message = "文件唯一标识最大长度不能超过765位")
        private String fileKey;

        /**
        * 文件名称
        */
        @NotBlank(message = "文件名称不能为空")
        @Size(max = 765,message = "文件名称最大长度不能超过765位")
        private String fileName;


    }


}