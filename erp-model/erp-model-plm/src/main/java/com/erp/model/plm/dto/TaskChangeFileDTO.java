package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname 变更文档

 * @Date 2022-10-14 11:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskChangeFileDTO implements Serializable {


    @NotBlank(message = "任务id 不能为空")
    private String taskId;

    @NotBlank(message = "产品id 不能为空")
    private String productId;

    /**
     * 上传类型 0 本地上传  1 飞书 上传 飞书链接
     */
    private Integer uploadType;

    /**
     * 文件
     */
    private MultipartFile file;

    /**
     * 文件url
     */
    private String fileUrl;


    /**
     * 已交付完成的id
     */
    @NotBlank(message = "已交付完成的id 不能为空")
    private String finishDocsId;


}



