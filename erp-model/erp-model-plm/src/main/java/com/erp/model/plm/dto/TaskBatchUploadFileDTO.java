package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname TaskBatchUploadFileDTO

 * @Date 2022-11-11 15:34
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskBatchUploadFileDTO  implements Serializable {

    /**
     * 任务id
     */
    @NotBlank(message = "任务id 不能为空")
    private String taskId;

    /**
     * 产品id
     */
    @NotBlank(message = "产品id 不能为空")
    private String productId;


    @NotNull(message = "上传文件不能为空")
    private List<TaskUploadFileDTO>  uploadFileList;

}
