package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/7 14:21
 */
@Data
@NoArgsConstructor
public class UploadMultipartFileDTO {

    /**
     * 交付的文档id
     */
    @NotBlank(message = "任务交付的文档id 不能为空")
    private String taskDocsId;

    /**
     * 文件
     */
    private List<MultipartFile> files;

    /**
     * 飞书链接
     */
    private List<String> fileUrls;
}
