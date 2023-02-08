package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

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

    @NotBlank(message = "产品id 不能为空")
    private String productId;

    /**
     * 上传类型 0 本地上传  1 飞书 上传 飞书链接
     */
    private Integer uploadType;

    /**
     * 上传文件集合
     */
    @NotEmpty(message = "上传数据不能为空")
    private List<UploadMultipartFileDTO> list;
}
