package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname TaskUploadFileDTO
 * @Description TODO
 * @Date 2022-09-23 17:03
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskUploadFileDTO  implements Serializable {

    @NotBlank(message = "任务id 不能为空")
    private String taskId;

    @NotBlank(message = "任务交付的文档id 不能为空")
    private String taskDocsId;


    private MultipartFile file;
}
