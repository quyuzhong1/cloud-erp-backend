package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * @Classname 变更文档
 * @Description TODO
 * @Date 2022-10-14 11:48
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskChangeFileDTO  implements Serializable {

    @NotBlank(message = "任务id 不能为空")
    private String taskId;

    @NotBlank(message = "任务交付的文档id 不能为空")
    private String taskDocsId;

    @NotBlank(message = "完成文档id 不能为空")
    private String finishDocsId;


    private MultipartFile file;
}
