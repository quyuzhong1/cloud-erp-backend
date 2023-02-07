package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;

/**
 * @Classname TaskUploadFileDTO
 * @Description TODO
 * @Date 2022-09-23 17:03
 * @Created by yl
 */
@Data
@NoArgsConstructor
public class TaskUploadFileDTO  implements Serializable {



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
