package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
     * 交付的文档id
     */
    @NotBlank(message = "任务交付的文档id 不能为空")
    private String taskDocsId;

    /**
     * 产品id
     */
    @NotBlank(message = "产品id 不能为空")
    private String productId;

    /**
     * 上传类型 0 本地上传  1 飞书 上传 飞书链接
     */
    @NotNull(message = "上传类型不能为空")
    @StateEnumValue(intValues = {0, 1}, message = "上传类型只能是0或者1")
    private Integer uploadType;


    /**
     * 文件
     */
    private List<UploadMultipartFileDTO> files;


}
