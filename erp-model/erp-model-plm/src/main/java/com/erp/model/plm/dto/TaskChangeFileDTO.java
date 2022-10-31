package com.erp.model.plm.dto;

import com.erp.common.annotation.StateEnumValue;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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

    /**
     * 上传类型 0 本地上传  1 飞书 上传 飞书链接
     */
    @NotNull(message = "上传类型不能为空")
    @StateEnumValue(intValues = {0, 1}, message = "上传类型只能是0或者1")
    private Integer uploadType;


    /**
     * 飞书的链接
     */
    private String fileUrl;


    private MultipartFile file;
}
