package com.erp.model.plm.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Digits;

/**
 * <p>
 * 任务文档历史表请求响应实体
 * </p>
 *
 * @author Lambda
 * @since 2023-06-09
*/
@Data
@NoArgsConstructor
public class TaskDocHistoryDTO implements Serializable {




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
        * 完成交付物的表id 对应task_finish_docs表
        */
        private String finishDocId;
        /**
        * 文件名
        */
        private String fileName;
        /**
        * 文件地址
        */
        private String fileUrl;
        /**
        * 交付文档表id  task_delivery_docs表id
        */
        private String requireDocId;
        /**
        * 文件类型
        */
        private String fileType;
        /**
        * 文件大小
        */
        private BigDecimal fileSize;
        /**
        * 文件后缀
        */
        private String fileSuffix;
        /**
        * 产品id
        */
        private String productId;
        /**
        * 上传类型 0 本地  1 链接
        */
        private Integer uploadType;

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
        * 完成交付物的表id 对应task_finish_docs表
        */
        @NotBlank(message = "完成交付物的表id 对应task_finish_docs表不能为空")
        @Size(max = 19,message = "完成交付物的表id 对应task_finish_docs表最大长度不能超过19位")
        private String finishDocId;
        /**
        * 文件名
        */
        @NotBlank(message = "文件名不能为空")
        @Size(max = 50,message = "文件名最大长度不能超过50位")
        private String fileName;
        /**
        * 文件地址
        */
        @NotBlank(message = "文件地址不能为空")
        @Size(max = 100,message = "文件地址最大长度不能超过100位")
        private String fileUrl;
        /**
        * 交付文档表id  task_delivery_docs表id
        */
        @NotBlank(message = "交付文档表id  task_delivery_docs表id不能为空")
        @Size(max = 19,message = "交付文档表id  task_delivery_docs表id最大长度不能超过19位")
        private String requireDocId;
        /**
        * 文件类型
        */
        @NotBlank(message = "文件类型不能为空")
        @Size(max = 50,message = "文件类型最大长度不能超过50位")
        private String fileType;
        /**
        * 文件大小
        */
        @NotNull(message = "文件大小不能为空")
        @Digits(integer = 8, fraction = 2, message = "文件大小整数位不能超过8位，小数位不能超过2位")
        private BigDecimal fileSize;
        /**
        * 文件后缀
        */
        @NotBlank(message = "文件后缀不能为空")
        @Size(max = 10,message = "文件后缀最大长度不能超过10位")
        private String fileSuffix;
        /**
        * 产品id
        */
        @NotBlank(message = "产品id不能为空")
        @Size(max = 19,message = "产品id最大长度不能超过19位")
        private String productId;
        /**
        * 上传类型 0 本地  1 链接
        */
        @NotNull(message = "上传类型 0 本地  1 链接不能为空")
        private Integer uploadType;

    }


}